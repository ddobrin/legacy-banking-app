import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { NumberField } from '@vaadin/react-components/NumberField.js';
import { Select } from '@vaadin/react-components/Select.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { ApiError, bankingApi } from '../../api/bankingApi';
import { useNotifications } from '../../components/NotificationContext';

export const config: ViewConfig = {
  title: 'New Financial Transaction — Legacy Bank',
};

const OPERATION_OPTIONS = [
  { label: 'Deposit', value: 'DEPOSIT' },
  { label: 'Withdraw', value: 'WITHDRAWAL' },
  { label: 'Transfer', value: 'TRANSFER' },
];

export default function CreateTransactionView() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotifications();

  const [transactionType, setTransactionType] = useState<'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER'>(
    'DEPOSIT',
  );
  const [accountId, setAccountId] = useState(searchParams.get('accountId') ?? '');
  const [targetAccountId, setTargetAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);

    const parsedAccountId = Number(accountId.trim());
    if (!accountId.trim() || !Number.isInteger(parsedAccountId) || parsedAccountId <= 0) {
      setErrorBanner('Please enter a valid numeric Source Account ID.');
      return;
    }

    let parsedTargetId: number | undefined;
    if (transactionType === 'TRANSFER') {
      parsedTargetId = Number(targetAccountId.trim());
      if (!targetAccountId.trim() || !Number.isInteger(parsedTargetId) || parsedTargetId <= 0) {
        setErrorBanner('Please enter a valid numeric Destination Account ID for transfers.');
        return;
      }
    }

    const parsedAmount = Number(amount);
    if (!amount.trim() || Number.isNaN(parsedAmount) || parsedAmount <= 0) {
      setErrorBanner('Transaction amount must be greater than zero.');
      return;
    }

    setSubmitting(true);
    try {
      await bankingApi.createTransaction({
        accountId: parsedAccountId,
        transactionType,
        amount: parsedAmount,
        ...(description.trim() ? { description: description.trim() } : {}),
        ...(parsedTargetId !== undefined ? { targetAccountId: parsedTargetId } : {}),
      });
      notifySuccess('Transaction completed');
      navigate(`/accounts/${parsedAccountId}`);
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Transaction failed';
      setErrorBanner(msg);
      notifyError(`Error: ${msg}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <nav className="breadcrumbs" aria-label="Breadcrumb">
        <Link to="/">Dashboard</Link>
        <span aria-hidden="true">/</span>
        <span aria-current="page">New Financial Transaction</span>
      </nav>

      <div className="page-header">
        <div>
          <h1 className="page-title">New Financial Transaction</h1>
          <p className="page-subtitle">
            Execute deposits, withdrawals, or atomic account-to-account transfers.
          </p>
        </div>
      </div>

      <section className="md-card" aria-labelledby="new-transaction-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:exchange" aria-hidden="true" />
            <h2 id="new-transaction-heading" className="md-card__title">
              Transaction Details
            </h2>
          </div>
        </div>
        <div className="md-card__body">
          {errorBanner && (
            <div className="md-inline-alert md-inline-alert--error" role="alert">
              <Icon icon="vaadin:exclamation-circle" aria-hidden="true" />
              <span>{errorBanner}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div className="form-grid">
              <Select
                label="Operation"
                items={OPERATION_OPTIONS}
                value={transactionType}
                onValueChanged={(e) =>
                  setTransactionType(e.detail.value as 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER')
                }
              />

              <TextField
                label="Source Account ID"
                value={accountId}
                onValueChanged={(e) => setAccountId(e.detail.value)}
                required
                placeholder="e.g. 1"
              />

              {transactionType === 'TRANSFER' && (
                <TextField
                  label="Destination Account ID"
                  value={targetAccountId}
                  onValueChanged={(e) => setTargetAccountId(e.detail.value)}
                  required
                  placeholder="e.g. 2"
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
                disabled={submitting}
                onClick={(e) => {
                  void handleSubmit(e);
                }}
              >
                <Icon slot="prefix" icon="vaadin:check" aria-hidden="true" />
                {submitting ? 'Processing...' : 'Execute Transaction'}
              </Button>
              <Button
                theme="tertiary"
                onClick={() => navigate('/')}
                disabled={submitting}
              >
                Cancel
              </Button>
            </div>
          </form>
        </div>
      </section>
    </div>
  );
}
