import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import legacyBankLogo from '../images/legacy-bank.gif';
import { bankingApi } from '../api/bankingApi';
import type { CustomerDto, ProductDto } from '../types/banking';
import { formatInterestRate } from '../utils/formatters';
import { StatusBadge } from '../components/StatusBadge';

export const config: ViewConfig = {
  title: 'Dashboard — Legacy Bank',
};

const LEGACY_PRODUCT_TARGETS = [
  { productType: 'Savings', standardRate: '5.0%', internalTarget: '12% Growth' },
  { productType: 'CD', standardRate: '7.2%', internalTarget: '5% Retention' },
];

export default function DashboardView() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [products, setProducts] = useState<ProductDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    Promise.allSettled([bankingApi.getCustomers(), bankingApi.getProducts()]).then(
      ([custRes, prodRes]) => {
        if (!active) return;
        if (custRes.status === 'fulfilled') {
          setCustomers(custRes.value);
        }
        if (prodRes.status === 'fulfilled') {
          setProducts(prodRes.value);
        }
        setLoading(false);
      },
    );
    return () => {
      active = false;
    };
  }, []);

  const totalAccounts = customers.reduce((sum, c) => sum + (c.accountCount ?? 0), 0);

  return (
    <div>
      <section className="hero-banner" aria-labelledby="welcome-heading">
        <div className="hero-logo-wrap">
          <img
            src={legacyBankLogo}
            alt="Legacy Bank Logo"
            className="hero-logo"
            width={240}
          />
        </div>
        <div className="hero-copy">
          <h1 id="welcome-heading" className="page-title">
            Welcome to the Employee Portal
          </h1>
          <p className="page-subtitle">
            You are logged into the Legacy Bank Core Banking System. Use the navigation drawer,
            quick search bar, or actions below to perform customer, account, and transaction
            operations.
          </p>
          <div className="md-actions-row">
            <Button theme="primary" onClick={() => navigate('/customers')}>
              <Icon slot="prefix" icon="vaadin:users" aria-hidden="true" />
              Customer Directory
            </Button>
            <Button theme="secondary" onClick={() => navigate('/customers/new')}>
              <Icon slot="prefix" icon="vaadin:plus-circle" aria-hidden="true" />
              New Customer
            </Button>
            <Button theme="secondary" onClick={() => navigate('/accounts/new')}>
              <Icon slot="prefix" icon="vaadin:wallet" aria-hidden="true" />
              New Account
            </Button>
            <Button theme="secondary" onClick={() => navigate('/transactions/new')}>
              <Icon slot="prefix" icon="vaadin:exchange" aria-hidden="true" />
              New Transaction
            </Button>
          </div>
        </div>
      </section>

      {loading && <ProgressBar indeterminate aria-label="Loading dashboard summary" />}

      <section className="kpi-grid" aria-label="Core Banking Metrics">
        <div className="kpi-card">
          <div className="kpi-card__label">Registered Customers</div>
          <div className="kpi-card__value">{loading ? '—' : customers.length}</div>
        </div>
        <div className="kpi-card">
          <div className="kpi-card__label">Managed Accounts</div>
          <div className="kpi-card__value">{loading ? '—' : totalAccounts}</div>
        </div>
        <div className="kpi-card">
          <div className="kpi-card__label">Active Banking Products</div>
          <div className="kpi-card__value">
            {loading ? '—' : products.filter((p) => p.active).length}
          </div>
        </div>
      </section>

      <div className="two-col-grid">
        <section className="md-card" aria-labelledby="announcements-heading">
          <div className="md-card__header">
            <div className="md-card__title-group">
              <Icon icon="vaadin:megaphone" aria-hidden="true" />
              <h2 id="announcements-heading" className="md-card__title">
                Internal Announcements
              </h2>
            </div>
          </div>
          <div className="md-card__body">
            <ul className="announcements-list">
              <li>
                <strong>System Maintenance:</strong> The CIF database will be down for maintenance
                this Saturday at midnight.
              </li>
              <li>
                <strong>New 24H ATM:</strong> A new ATM has been successfully installed in the
                downtown branch.
              </li>
              <li>
                <strong>Q1 Performance:</strong> Savings interest rates have been adjusted to 5.0%.
              </li>
            </ul>
          </div>
        </section>

        <section className="md-card" aria-labelledby="targets-heading">
          <div className="md-card__header">
            <div className="md-card__title-group">
              <Icon icon="vaadin:chart" aria-hidden="true" />
              <h2 id="targets-heading" className="md-card__title">
                Product Rates &amp; Internal Targets
              </h2>
            </div>
          </div>
          <div className="md-card__body">
            <div className="md-table-wrap">
              <table className="md-table">
                <thead>
                  <tr>
                    <th scope="col">Product Type</th>
                    <th scope="col">Standard Rate</th>
                    <th scope="col">Internal Target</th>
                  </tr>
                </thead>
                <tbody>
                  {LEGACY_PRODUCT_TARGETS.map((row) => (
                    <tr key={row.productType}>
                      <td>
                        <strong>{row.productType}</strong>
                      </td>
                      <td className="tabular-num">{row.standardRate}</td>
                      <td>
                        <StatusBadge value={row.internalTarget} variant="info" />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </div>

      {products.length > 0 && (
        <section className="md-card" aria-labelledby="catalog-heading">
          <div className="md-card__header">
            <div className="md-card__title-group">
              <Icon icon="vaadin:list" aria-hidden="true" />
              <h2 id="catalog-heading" className="md-card__title">
                Active Core Banking Product Catalog
              </h2>
            </div>
          </div>
          <div className="md-card__body">
            <div className="md-table-wrap">
              <table className="md-table">
                <thead>
                  <tr>
                    <th scope="col">Product Code</th>
                    <th scope="col">Name</th>
                    <th scope="col">Category</th>
                    <th scope="col">Interest Rate</th>
                    <th scope="col">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {products.map((product) => (
                    <tr key={product.productCode}>
                      <td className="tabular-num">
                        <strong>{product.productCode}</strong>
                      </td>
                      <td>{product.name}</td>
                      <td>{product.category}</td>
                      <td className="tabular-num">{formatInterestRate(product.interestRate)}</td>
                      <td>
                        <StatusBadge value={product.active ? 'ACTIVE' : 'CLOSED'} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
