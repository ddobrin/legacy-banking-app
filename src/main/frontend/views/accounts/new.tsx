import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { ComboBox } from '@vaadin/react-components/ComboBox.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { NumberField } from '@vaadin/react-components/NumberField.js';
import { Select } from '@vaadin/react-components/Select.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { ApiError, bankingApi } from '../../api/bankingApi';
import type { CustomerDto, ProductDto } from '../../types/banking';
import { useNotifications } from '../../components/NotificationContext';

export const config: ViewConfig = {
  title: 'Open New Account — Legacy Bank',
};

const DEFAULT_PRODUCTS = [
  { label: 'Standard Checking (CHK-STD)', value: 'CHK-STD' },
  { label: 'High Yield Savings (SAV-HYS)', value: 'SAV-HYS' },
];

export default function CreateAccountView() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotifications();

  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [products, setProducts] = useState<ProductDto[]>([]);

  const [customerId, setCustomerId] = useState(searchParams.get('customerId') ?? '');
  const [productCode, setProductCode] = useState('CHK-STD');
  const [balance, setBalance] = useState('0');
  const [overdraftLimit, setOverdraftLimit] = useState('0');
  const [submitting, setSubmitting] = useState(false);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  useEffect(() => {
    Promise.allSettled([bankingApi.getCustomers(), bankingApi.getProducts()]).then(
      ([custRes, prodRes]) => {
        if (custRes.status === 'fulfilled') {
          setCustomers(custRes.value);
        }
        if (prodRes.status === 'fulfilled') {
          setProducts(prodRes.value);
        }
      },
    );
  }, []);

  const customerItems = customers.map((c) => ({
    label: `${c.lastName}, ${c.firstName} (ID: ${c.customerId} — ${c.cifNumber})`,
    value: String(c.customerId),
  }));

  const productItems =
    products.length > 0
      ? products
          .filter((p) => p.active)
          .map((p) => ({ label: `${p.name} (${p.productCode})`, value: p.productCode }))
      : DEFAULT_PRODUCTS;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);

    const parsedCustomerId = Number(customerId.trim());
    if (!customerId.trim() || !Number.isInteger(parsedCustomerId) || parsedCustomerId <= 0) {
      setErrorBanner('Please enter or select a valid numeric Customer ID.');
      return;
    }

    const parsedBalance = Number(balance || '0');
    if (Number.isNaN(parsedBalance) || parsedBalance < 0) {
      setErrorBanner('Opening deposit cannot be negative.');
      return;
    }

    const parsedOverdraft = Number(overdraftLimit || '0');
    if (Number.isNaN(parsedOverdraft) || parsedOverdraft < 0) {
      setErrorBanner('Overdraft limit cannot be negative.');
      return;
    }

    setSubmitting(true);
    try {
      const created = await bankingApi.createAccount({
        customerId: parsedCustomerId,
        productCode,
        balance: parsedBalance,
        overdraftLimit: parsedOverdraft,
      });
      notifySuccess('New account opened');
      navigate(`/accounts/${created.accountId}`);
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Unable to open account';
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
        <span aria-current="page">Open New Account</span>
      </nav>

      <div className="page-header">
        <div>
          <h1 className="page-title">Open New Account</h1>
          <p className="page-subtitle">
            Provision a checking or high-yield savings account for an existing CIF customer.
          </p>
        </div>
      </div>

      <section className="md-card" aria-labelledby="open-account-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:wallet" aria-hidden="true" />
            <h2 id="open-account-heading" className="md-card__title">
              Account Configuration
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
              {customerItems.length > 0 && (
                <ComboBox
                  label="Select Customer"
                  items={customerItems}
                  itemLabelPath="label"
                  itemValuePath="value"
                  value={customerId}
                  onValueChanged={(e) => setCustomerId(e.detail.value)}
                  helperText="Choose a customer from the directory or enter Customer ID directly"
                />
              )}

              <TextField
                label="Customer ID"
                value={customerId}
                onValueChanged={(e) => setCustomerId(e.detail.value)}
                required
                placeholder="e.g. 1"
                helperText="Numeric primary key of the owning customer"
              />

              <Select
                label="Product Type"
                items={productItems}
                value={productCode}
                onValueChanged={(e) => setProductCode(e.detail.value)}
              />

              <NumberField
                label="Opening Deposit"
                value={balance}
                min={0}
                step={0.01}
                onValueChanged={(e) => setBalance(e.detail.value)}
                helperText="Initial deposit in USD"
              >
                <span slot="prefix">$</span>
              </NumberField>

              <NumberField
                label="Overdraft Limit"
                value={overdraftLimit}
                min={0}
                step={0.01}
                onValueChanged={(e) => setOverdraftLimit(e.detail.value)}
                helperText="Optional overdraft protection limit"
              >
                <span slot="prefix">$</span>
              </NumberField>
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
                {submitting ? 'Opening...' : 'Initialize Account'}
              </Button>
              <Button
                theme="tertiary"
                onClick={() => navigate('/customers')}
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
