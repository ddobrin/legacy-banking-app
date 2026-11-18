import React, { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { ConfirmDialog } from '@vaadin/react-components/ConfirmDialog.js';
import { Grid } from '@vaadin/react-components/Grid.js';
import { GridColumn } from '@vaadin/react-components/GridColumn.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { NumberField } from '@vaadin/react-components/NumberField.js';
import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import { Select } from '@vaadin/react-components/Select.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { ApiError, bankingApi } from '../../api/bankingApi';
import type { AccountDto, TransactionDto } from '../../types/banking';
import { ErrorCard } from '../../components/ErrorCard';
import { StatusBadge } from '../../components/StatusBadge';
import { useNotifications } from '../../components/NotificationContext';
import { formatCurrency, formatDateTime } from '../../utils/formatters';

export const config: ViewConfig = {
  title: 'Account Overview — Legacy Bank',
};

const OPERATION_OPTIONS = [
  { label: 'Deposit', value: 'DEPOSIT' },
  { label: 'Withdraw', value: 'WITHDRAWAL' },
  { label: 'Transfer', value: 'TRANSFER' },
];

export default function AccountDetailView() {
  const { id } = useParams();
  const { notifySuccess, notifyError } = useNotifications();

  const [account, setAccount] = useState<AccountDto | null>(null);
  const [transactions, setTransactions] = useState<TransactionDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Inline transaction form
  const [transactionType, setTransactionType] = useState<'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER'>(
    'DEPOSIT',
  );
  const [targetAccountId, setTargetAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [submittingTxn, setSubmittingTxn] = useState(false);
  const [txnError, setTxnError] = useState<string | null>(null);

  // Status update confirmation
  const [confirmAction, setConfirmAction] = useState<'CLOSED' | 'FROZEN' | 'ACTIVE' | null>(null);
  const [updatingStatus, setUpdatingStatus] = useState(false);

  const loadAccountAndTransactions = useCallback(async () => {
    if (!id) {
      setErrorMessage('Account ID is missing.');
      setLoading(false);
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      const accountData = await bankingApi.getAccount(id);
      setAccount(accountData);
      const txnsData = await bankingApi
        .getTransactionsByAccount(accountData.accountId)
        .catch(() => [] as TransactionDto[]);
      setTransactions(txnsData);
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Account not found';
      setErrorMessage(msg);
      setAccount(null);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void loadAccountAndTransactions();
  }, [loadAccountAndTransactions]);

  const handleTransactionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!account) return;
    setTxnError(null);

    const parsedAmount = Number(amount);
    if (!amount.trim() || Number.isNaN(parsedAmount) || parsedAmount <= 0) {
      setTxnError('Transaction amount must be greater than zero.');
      return;
    }

    let parsedTargetId: number | undefined;
    if (transactionType === 'TRANSFER') {
      parsedTargetId = Number(targetAccountId.trim());
      if (!targetAccountId.trim() || !Number.isInteger(parsedTargetId) || parsedTargetId <= 0) {
        setTxnError('Please enter a valid numeric Destination Account ID for transfers.');
        return;
      }
    }

    setSubmittingTxn(true);
    try {
      await bankingApi.createTransaction({
        accountId: account.accountId,
        transactionType,
        amount: parsedAmount,
        ...(description.trim() ? { description: description.trim() } : {}),
        ...(parsedTargetId !== undefined ? { targetAccountId: parsedTargetId } : {}),
      });
      notifySuccess('Transaction completed');
      setAmount('');
      setDescription('');
      setTargetAccountId('');
      await loadAccountAndTransactions();
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Transaction failed';
      setTxnError(msg);
      notifyError(`Error: ${msg}`);
    } finally {
      setSubmittingTxn(false);
    }
  };

  const handleStatusChange = async () => {
    if (!account || !confirmAction) return;
    setUpdatingStatus(true);
    try {
      await bankingApi.updateAccountStatus(account.accountId, { status: confirmAction });
      notifySuccess(
        confirmAction === 'CLOSED'
          ? 'Account deactivated'
          : `Account status updated to ${confirmAction}`,
      );
      setConfirmAction(null);
      await loadAccountAndTransactions();
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Unable to update account status';
      notifyError(`Error: ${msg}`);
      setConfirmAction(null);
    } finally {
      setUpdatingStatus(false);
    }
  };

  if (loading) {
    return (
      <section className="md-card" aria-busy="true" aria-label="Loading account overview">
        <div className="md-card__body">
          <ProgressBar indeterminate aria-label="Loading account overview" />
          <p>Loading account overview...</p>
        </div>
      </section>
    );
  }

  if (errorMessage || !account) {
    return <ErrorCard title="Resource Not Found" message={errorMessage ?? 'Account not found'} />;
  }

  const normalizedStatus = (account.status ?? '').toUpperCase();
  const isOpen = normalizedStatus === 'ACTIVE' || normalizedStatus === 'OPEN';

  return (
    <div>
      <nav className="breadcrumbs" aria-label="Breadcrumb">
        <Link to="/">Dashboard</Link>
        <span aria-hidden="true">/</span>
        <Link to={`/customers/${account.customerId}`}>Customer #{account.customerId}</Link>
        <span aria-hidden="true">/</span>
        <span aria-current="page">Account #{account.accountId}</span>
      </nav>

      <div className="page-header">
        <div>
          <h1 className="page-title">Account Overview: #{account.accountId}</h1>
          <p className="page-subtitle">
            Account Number <strong className="tabular-num">{account.accountNumber}</strong> &bull;
            Product <strong>{account.productCode}</strong>
          </p>
        </div>

        <div className="md-actions-row" style={{ marginTop: 0 }}>
          {isOpen && (
            <>
              <Button
                theme="secondary"
                onClick={() => setConfirmAction('FROZEN')}
                disabled={updatingStatus}
              >
                Freeze Account
              </Button>
              <Button
                theme="error primary"
                onClick={() => setConfirmAction('CLOSED')}
                disabled={updatingStatus}
              >
                <Icon slot="prefix" icon="vaadin:ban" aria-hidden="true" />
                Deactivate Account
              </Button>
            </>
          )}
          {normalizedStatus === 'FROZEN' && (
            <>
              <Button
                theme="primary"
                onClick={() => setConfirmAction('ACTIVE')}
                disabled={updatingStatus}
              >
                Reactivate Account
              </Button>
              <Button
                theme="error secondary"
                onClick={() => setConfirmAction('CLOSED')}
                disabled={updatingStatus}
              >
                Deactivate Account
              </Button>
            </>
          )}
        </div>
      </div>

      <section className="md-card" aria-labelledby="account-overview-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:wallet" aria-hidden="true" />
            <h2 id="account-overview-heading" className="md-card__title">
              Account Summary
            </h2>
          </div>
          <StatusBadge value={account.status} />
        </div>
        <div className="md-card__body">
          <div className="detail-grid">
            <div>
              <div className="detail-item__label">Account Number</div>
              <div className="detail-item__value tabular-num">{account.accountNumber}</div>
            </div>
            <div>
              <div className="detail-item__label">Product Code</div>
              <div className="detail-item__value">
                <StatusBadge value={account.productCode} />
              </div>
            </div>
            <div>
              <div className="detail-item__label">Status</div>
              <div className="detail-item__value">
                <StatusBadge value={account.status} />
              </div>
            </div>
            <div>
              <div className="detail-item__label">Current Balance</div>
              <div
                className="detail-item__value tabular-num"
                style={{ fontSize: '1.375rem', fontWeight: 700, color: 'var(--md-primary)' }}
              >
                {formatCurrency(account.balance)}
              </div>
            </div>
            <div>
              <div className="detail-item__label">Primary Owner ID</div>
              <div className="detail-item__value">
                <Link to={`/customers/${account.customerId}`} className="entity-link tabular-num">
                  Customer #{account.customerId}
                </Link>
              </div>
            </div>
            <div>
              <div className="detail-item__label">Overdraft Limit</div>
              <div className="detail-item__value tabular-num">
                {formatCurrency(account.overdraftLimit ?? 0)}
              </div>
            </div>
            <div>
              <div className="detail-item__label">Opened At</div>
              <div className="detail-item__value">{formatDateTime(account.openedAt)}</div>
            </div>
            {account.closedAt && (
              <div>
                <div className="detail-item__label">Closed At</div>
                <div className="detail-item__value">{formatDateTime(account.closedAt)}</div>
              </div>
            )}
          </div>
        </div>
      </section>

      <section className="md-card" aria-labelledby="inline-transaction-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:exchange" aria-hidden="true" />
            <h2 id="inline-transaction-heading" className="md-card__title">
              Record New Transaction
            </h2>
          </div>
        </div>
        <div className="md-card__body">
          {!isOpen && (
            <div className="md-inline-alert md-inline-alert--warning" role="status">
              <Icon icon="vaadin:warning" aria-hidden="true" />
              <span>
                This account is currently <strong>{account.status}</strong>. Transactions on non-active
                accounts will be rejected by the banking ledger.
              </span>
            </div>
          )}

          {txnError && (
            <div className="md-inline-alert md-inline-alert--error" role="alert">
              <Icon icon="vaadin:exclamation-circle" aria-hidden="true" />
              <span>{txnError}</span>
            </div>
          )}

          <form onSubmit={handleTransactionSubmit} noValidate>
            <div className="form-grid">
              <Select
                label="Operation"
                items={OPERATION_OPTIONS}
                value={transactionType}
                onValueChanged={(e) =>
                  setTransactionType(e.detail.value as 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER')
                }
              />

              {transactionType === 'TRANSFER' && (
                <TextField
                  label="Destination Account ID"
                  value={targetAccountId}
                  onValueChanged={(e) => setTargetAccountId(e.detail.value)}
                  required
                  placeholder="Target Account ID"
                />
              )}

              <NumberField
                label="Transaction Amount"
                value={amount}
                min={0.01}
                step={0.01}
                required
                onValueChanged={(e) => setAmount(e.detail.value)}
              >
                <span slot="prefix">$</span>
              </NumberField>

              <TextField
                label="Description"
                value={description}
                onValueChanged={(e) => setDescription(e.detail.value)}
                placeholder="Optional memo or reference"
                maxlength={1000}
              />
            </div>

            <div className="md-actions-row">
              <Button
                theme="primary"
                disabled={submittingTxn}
                onClick={(e) => {
                  void handleTransactionSubmit(e);
                }}
              >
                <Icon slot="prefix" icon="vaadin:check" aria-hidden="true" />
                {submittingTxn ? 'Processing...' : 'Execute Transaction'}
              </Button>
            </div>
          </form>
        </div>
      </section>

      <section className="md-card" aria-labelledby="recent-activity-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:records" aria-hidden="true" />
            <h2 id="recent-activity-heading" className="md-card__title">
              Recent Activity ({transactions.length})
            </h2>
          </div>
        </div>
        <div className="md-card__body">
          {transactions.length === 0 ? (
            <p>No transactions posted for this account yet.</p>
          ) : (
            <Grid items={transactions} allRowsVisible theme="row-stripes">
              <GridColumn
                header="Transaction Type"
                autoWidth
                renderer={({ item }) => <StatusBadge value={item.transactionType} />}
              />
              <GridColumn
                header="Amount"
                autoWidth
                renderer={({ item }) => (
                  <strong className="tabular-num">{formatCurrency(item.amount)}</strong>
                )}
              />
              <GridColumn
                header="Balance After"
                autoWidth
                renderer={({ item }) => (
                  <span className="tabular-num">
                    {item.balanceAfter !== null && item.balanceAfter !== undefined
                      ? formatCurrency(item.balanceAfter)
                      : '—'}
                  </span>
                )}
              />
              <GridColumn
                header="Description"
                autoWidth
                renderer={({ item }) => <span>{item.description || '—'}</span>}
              />
              <GridColumn
                header="Post Date"
                autoWidth
                renderer={({ item }) => (
                  <span className="tabular-num">{formatDateTime(item.transactionDate)}</span>
                )}
              />
            </Grid>
          )}
        </div>
      </section>

      <ConfirmDialog
        header={
          confirmAction === 'CLOSED'
            ? 'Deactivate Account'
            : confirmAction === 'FROZEN'
              ? 'Freeze Account'
              : 'Reactivate Account'
        }
        cancelButtonVisible
        confirmText={
          updatingStatus
            ? 'Updating...'
            : confirmAction === 'CLOSED'
              ? 'Deactivate Account'
              : 'Confirm'
        }
        confirmTheme={confirmAction === 'CLOSED' ? 'error primary' : 'primary'}
        opened={confirmAction !== null}
        onOpenedChanged={(e) => {
          if (!e.detail.value) setConfirmAction(null);
        }}
        onConfirm={() => {
          void handleStatusChange();
        }}
      >
        {confirmAction === 'CLOSED'
          ? 'Are you sure you want to deactivate this account? Closed accounts cannot process further transactions.'
          : confirmAction === 'FROZEN'
            ? 'Are you sure you want to freeze this account?'
            : 'Are you sure you want to reactivate this account?'}
      </ConfirmDialog>
    </div>
  );
}
