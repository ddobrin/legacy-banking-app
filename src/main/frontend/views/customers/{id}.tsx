import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { ConfirmDialog } from '@vaadin/react-components/ConfirmDialog.js';
import { Dialog } from '@vaadin/react-components/Dialog.js';
import { EmailField } from '@vaadin/react-components/EmailField.js';
import { DatePicker } from '@vaadin/react-components/DatePicker.js';
import { Grid } from '@vaadin/react-components/Grid.js';
import { GridColumn } from '@vaadin/react-components/GridColumn.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { NumberField } from '@vaadin/react-components/NumberField.js';
import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import { Select } from '@vaadin/react-components/Select.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { ApiError, bankingApi } from '../../api/bankingApi';
import type { CustomerDto, ProductDto } from '../../types/banking';
import { ErrorCard } from '../../components/ErrorCard';
import { StatusBadge } from '../../components/StatusBadge';
import { useNotifications } from '../../components/NotificationContext';
import { formatAddress, formatCurrency, formatDate, formatDateTime } from '../../utils/formatters';

export const config: ViewConfig = {
  title: 'Customer Record — Legacy Bank',
};

const DEFAULT_PRODUCTS = [
  { label: 'Standard Checking (CHK-STD)', value: 'CHK-STD' },
  { label: 'High Yield Savings (SAV-HYS)', value: 'SAV-HYS' },
];

const KYC_OPTIONS = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Verified', value: 'VERIFIED' },
  { label: 'Rejected', value: 'REJECTED' },
];

const RISK_OPTIONS = [
  { label: 'Low', value: 'LOW' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'High', value: 'HIGH' },
];

export default function CustomerDetailView() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useNotifications();

  const [customer, setCustomer] = useState<CustomerDto | null>(null);
  const [products, setProducts] = useState<ProductDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Inline create account state
  const [productCode, setProductCode] = useState('CHK-STD');
  const [openingDeposit, setOpeningDeposit] = useState('0');
  const [creatingAccount, setCreatingAccount] = useState(false);

  // Delete confirmation dialog
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  // Edit customer dialog
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [savingEdit, setSavingEdit] = useState(false);
  const [editForm, setEditForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    dateOfBirth: '',
    phoneNumber: '',
    addressLine1: '',
    city: '',
    state: '',
    postalCode: '',
    countryCode: '',
    kycStatus: 'PENDING',
    riskRating: 'LOW',
  });

  const loadCustomer = useCallback(async () => {
    if (!id) {
      setErrorMessage('Customer identifier is missing.');
      setLoading(false);
      return;
    }
    setLoading(true);
    setErrorMessage(null);
    try {
      const [custData, prodRes] = await Promise.all([
        bankingApi.getCustomer(id),
        bankingApi.getProducts().catch(() => [] as ProductDto[]),
      ]);
      setCustomer(custData);
      setProducts(prodRes);
      setEditForm({
        firstName: custData.firstName ?? '',
        lastName: custData.lastName ?? '',
        email: custData.email ?? '',
        dateOfBirth: custData.dateOfBirth ?? '',
        phoneNumber: custData.phoneNumber ?? '',
        addressLine1: custData.addressLine1 ?? '',
        city: custData.city ?? '',
        state: custData.state ?? '',
        postalCode: custData.postalCode ?? '',
        countryCode: custData.countryCode ?? '',
        kycStatus: custData.kycStatus ?? 'PENDING',
        riskRating: custData.riskRating ?? 'LOW',
      });
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Customer not found';
      setErrorMessage(msg);
      setCustomer(null);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void loadCustomer();
  }, [loadCustomer]);

  const productItems =
    products.length > 0
      ? products
          .filter((p) => p.active)
          .map((p) => ({ label: `${p.name} (${p.productCode})`, value: p.productCode }))
      : DEFAULT_PRODUCTS;

  const handleCreateAccount = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customer) return;

    const numericBalance = Number(openingDeposit || '0');
    if (Number.isNaN(numericBalance) || numericBalance < 0) {
      notifyError('Error: Opening deposit cannot be negative');
      return;
    }

    setCreatingAccount(true);
    try {
      await bankingApi.createAccount({
        customerId: customer.customerId,
        productCode,
        balance: numericBalance,
      });
      notifySuccess('Account created successfully');
      setOpeningDeposit('0');
      await loadCustomer();
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Failed to create account';
      notifyError(`Error: ${msg}`);
    } finally {
      setCreatingAccount(false);
    }
  };

  const handleDeleteCustomer = async () => {
    if (!customer) return;
    setDeleting(true);
    try {
      await bankingApi.deleteCustomer(customer.customerId);
      notifySuccess('Customer deleted');
      setDeleteDialogOpen(false);
      navigate('/');
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Unable to delete customer';
      notifyError(`Error: ${msg}`);
      setDeleteDialogOpen(false);
    } finally {
      setDeleting(false);
    }
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customer) return;
    setSavingEdit(true);
    try {
      await bankingApi.updateCustomer(customer.customerId, {
        firstName: editForm.firstName.trim(),
        lastName: editForm.lastName.trim(),
        email: editForm.email.trim(),
        dateOfBirth: editForm.dateOfBirth.trim(),
        phoneNumber: editForm.phoneNumber.trim() || undefined,
        addressLine1: editForm.addressLine1.trim() || undefined,
        city: editForm.city.trim() || undefined,
        state: editForm.state.trim() || undefined,
        postalCode: editForm.postalCode.trim() || undefined,
        countryCode: editForm.countryCode.trim().toUpperCase() || undefined,
        kycStatus: editForm.kycStatus,
        riskRating: editForm.riskRating,
      });
      notifySuccess('Customer profile updated');
      setEditDialogOpen(false);
      await loadCustomer();
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Unable to update customer';
      notifyError(`Error: ${msg}`);
    } finally {
      setSavingEdit(false);
    }
  };

  if (loading) {
    return (
      <section className="md-card" aria-busy="true" aria-label="Loading customer record">
        <div className="md-card__body">
          <ProgressBar indeterminate aria-label="Loading customer profile" />
          <p>Loading customer profile...</p>
        </div>
      </section>
    );
  }

  if (errorMessage || !customer) {
    return <ErrorCard title="Resource Not Found" message={errorMessage ?? 'Customer not found'} />;
  }

  const initials = `${customer.firstName?.charAt(0) ?? ''}${customer.lastName?.charAt(0) ?? ''}`.toUpperCase();
  const accounts = customer.accounts ?? [];

  return (
    <div>
      <nav className="breadcrumbs" aria-label="Breadcrumb">
        <Link to="/">Dashboard</Link>
        <span aria-hidden="true">/</span>
        <Link to="/customers">Customer Directory</Link>
        <span aria-hidden="true">/</span>
        <span aria-current="page">
          {customer.firstName} {customer.lastName}
        </span>
      </nav>

      <div className="page-header">
        <div className="md-card__title-group">
          <div className="customer-avatar" aria-hidden="true">
            {initials || 'C'}
          </div>
          <div>
            <h1 className="page-title">
              Customer Record: {customer.firstName} {customer.lastName}
            </h1>
            <p className="page-subtitle">
              CIF <strong className="tabular-num">{customer.cifNumber}</strong> &bull; Customer ID{' '}
              <strong className="tabular-num">#{customer.customerId}</strong>
            </p>
          </div>
        </div>

        <div className="md-actions-row" style={{ marginTop: 0 }}>
          <Button theme="secondary" onClick={() => setEditDialogOpen(true)}>
            <Icon slot="prefix" icon="vaadin:edit" aria-hidden="true" />
            Edit Profile
          </Button>
          <Button
            theme="error secondary"
            onClick={() => setDeleteDialogOpen(true)}
            disabled={deleting}
          >
            <Icon slot="prefix" icon="vaadin:trash" aria-hidden="true" />
            Delete Record
          </Button>
        </div>
      </div>

      <section className="md-card" aria-labelledby="profile-details-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:user" aria-hidden="true" />
            <h2 id="profile-details-heading" className="md-card__title">
              CIF Profile Overview
            </h2>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <StatusBadge value={`KYC: ${customer.kycStatus ?? 'PENDING'}`} variant="info" />
            <StatusBadge value={customer.riskRating ?? 'LOW'} />
          </div>
        </div>
        <div className="md-card__body">
          <div className="detail-grid">
            <div>
              <div className="detail-item__label">Customer ID</div>
              <div className="detail-item__value tabular-num">{customer.customerId}</div>
            </div>
            <div>
              <div className="detail-item__label">CIF Number</div>
              <div className="detail-item__value tabular-num">{customer.cifNumber}</div>
            </div>
            <div>
              <div className="detail-item__label">Email</div>
              <div className="detail-item__value">{customer.email}</div>
            </div>
            <div>
              <div className="detail-item__label">Date of Birth</div>
              <div className="detail-item__value tabular-num">{formatDate(customer.dateOfBirth)}</div>
            </div>
            <div>
              <div className="detail-item__label">KYC Status</div>
              <div className="detail-item__value">
                <StatusBadge value={customer.kycStatus ?? 'PENDING'} />
              </div>
            </div>
            <div>
              <div className="detail-item__label">Risk Rating</div>
              <div className="detail-item__value">
                <StatusBadge value={customer.riskRating ?? 'LOW'} />
              </div>
            </div>
            <div>
              <div className="detail-item__label">Phone Number</div>
              <div className="detail-item__value">{customer.phoneNumber || '—'}</div>
            </div>
            <div>
              <div className="detail-item__label">Address</div>
              <div className="detail-item__value">{formatAddress(customer)}</div>
            </div>
            <div>
              <div className="detail-item__label">Created At</div>
              <div className="detail-item__value">{formatDateTime(customer.createdAt)}</div>
            </div>
          </div>
        </div>
      </section>

      <section className="md-card" aria-labelledby="customer-accounts-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:wallet" aria-hidden="true" />
            <h2 id="customer-accounts-heading" className="md-card__title">
              Accounts ({accounts.length})
            </h2>
          </div>
        </div>
        <div className="md-card__body">
          {accounts.length === 0 ? (
            <p>No accounts found for this customer.</p>
          ) : (
            <Grid items={accounts} allRowsVisible theme="row-stripes">
              <GridColumn
                header="Account Number"
                autoWidth
                renderer={({ item }) => (
                  <Link to={`/accounts/${item.accountId}`} className="entity-link tabular-num">
                    {item.accountNumber}
                  </Link>
                )}
              />
              <GridColumn
                header="Product"
                autoWidth
                renderer={({ item }) => <StatusBadge value={item.productCode} />}
              />
              <GridColumn
                header="Balance"
                autoWidth
                renderer={({ item }) => (
                  <strong className="tabular-num">{formatCurrency(item.balance)}</strong>
                )}
              />
              <GridColumn
                header="Status"
                autoWidth
                renderer={({ item }) => <StatusBadge value={item.status} />}
              />
              <GridColumn
                header="Action"
                autoWidth
                renderer={({ item }) => (
                  <Button
                    theme="small secondary"
                    onClick={() => navigate(`/accounts/${item.accountId}`)}
                    aria-label={`View account ${item.accountNumber}`}
                  >
                    View
                  </Button>
                )}
              />
            </Grid>
          )}
        </div>
      </section>

      <section className="md-card" aria-labelledby="open-inline-account-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:plus-circle" aria-hidden="true" />
            <h2 id="open-inline-account-heading" className="md-card__title">
              Open New Account
            </h2>
          </div>
        </div>
        <div className="md-card__body">
          <form onSubmit={handleCreateAccount}>
            <div className="form-grid">
              <Select
                label="Product Type"
                items={productItems}
                value={productCode}
                onValueChanged={(e) => setProductCode(e.detail.value)}
              />
              <NumberField
                label="Opening Deposit"
                value={openingDeposit}
                min={0}
                step={0.01}
                onValueChanged={(e) => setOpeningDeposit(e.detail.value)}
                helperText="Initial deposit amount in USD"
              >
                <span slot="prefix">$</span>
              </NumberField>
            </div>
            <div className="md-actions-row">
              <Button
                theme="primary"
                disabled={creatingAccount}
                onClick={(e) => {
                  void handleCreateAccount(e);
                }}
              >
                <Icon slot="prefix" icon="vaadin:check" aria-hidden="true" />
                {creatingAccount ? 'Processing...' : 'Initialize Account'}
              </Button>
            </div>
          </form>
        </div>
      </section>

      <ConfirmDialog
        header="Delete Customer Record"
        cancelButtonVisible
        confirmText={deleting ? 'Deleting...' : 'Delete Record'}
        confirmTheme="error primary"
        opened={deleteDialogOpen}
        onOpenedChanged={(e) => setDeleteDialogOpen(e.detail.value)}
        onConfirm={() => {
          void handleDeleteCustomer();
        }}
      >
        Are you sure you want to delete this customer ({customer.firstName} {customer.lastName})?
        Customers with existing accounts cannot be deleted until accounts are removed.
      </ConfirmDialog>

      <Dialog
        headerTitle={`Edit Customer: ${customer.firstName} ${customer.lastName}`}
        opened={editDialogOpen}
        onOpenedChanged={(e) => setEditDialogOpen(e.detail.value)}
      >
        <form id="edit-customer-form" onSubmit={handleSaveEdit}>
          <div className="form-grid" style={{ minWidth: 'min(520px, 80vw)' }}>
            <TextField
              label="First Name"
              value={editForm.firstName}
              required
              onValueChanged={(e) => setEditForm((f) => ({ ...f, firstName: e.detail.value }))}
            />
            <TextField
              label="Last Name"
              value={editForm.lastName}
              required
              onValueChanged={(e) => setEditForm((f) => ({ ...f, lastName: e.detail.value }))}
            />
            <EmailField
              label="Email Address"
              value={editForm.email}
              required
              onValueChanged={(e) => setEditForm((f) => ({ ...f, email: e.detail.value }))}
            />
            <DatePicker
              label="Date of Birth"
              value={editForm.dateOfBirth}
              required
              onValueChanged={(e) => setEditForm((f) => ({ ...f, dateOfBirth: e.detail.value }))}
            />
            <Select
              label="KYC Status"
              items={KYC_OPTIONS}
              value={editForm.kycStatus}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, kycStatus: e.detail.value }))}
            />
            <Select
              label="Risk Rating"
              items={RISK_OPTIONS}
              value={editForm.riskRating}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, riskRating: e.detail.value }))}
            />
            <TextField
              label="Phone Number"
              value={editForm.phoneNumber}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, phoneNumber: e.detail.value }))}
            />
            <TextField
              label="Address Line 1"
              value={editForm.addressLine1}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, addressLine1: e.detail.value }))}
            />
            <TextField
              label="City"
              value={editForm.city}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, city: e.detail.value }))}
            />
            <TextField
              label="State"
              value={editForm.state}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, state: e.detail.value }))}
            />
            <TextField
              label="Postal Code"
              value={editForm.postalCode}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, postalCode: e.detail.value }))}
            />
            <TextField
              label="Country Code"
              maxlength={2}
              value={editForm.countryCode}
              onValueChanged={(e) => setEditForm((f) => ({ ...f, countryCode: e.detail.value }))}
            />
          </div>
          <div className="md-actions-row">
            <Button
              theme="primary"
              disabled={savingEdit}
              onClick={(e) => {
                void handleSaveEdit(e);
              }}
            >
              {savingEdit ? 'Saving...' : 'Save Changes'}
            </Button>
            <Button
              theme="tertiary"
              onClick={() => setEditDialogOpen(false)}
              disabled={savingEdit}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Dialog>
    </div>
  );
}
