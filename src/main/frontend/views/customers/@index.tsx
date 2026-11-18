import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { Grid } from '@vaadin/react-components/Grid.js';
import { GridColumn } from '@vaadin/react-components/GridColumn.js';
import { GridSortColumn } from '@vaadin/react-components/GridSortColumn.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { ProgressBar } from '@vaadin/react-components/ProgressBar.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { ApiError, bankingApi } from '../../api/bankingApi';
import type { CustomerDto } from '../../types/banking';
import { ErrorCard } from '../../components/ErrorCard';
import { StatusBadge } from '../../components/StatusBadge';

export const config: ViewConfig = {
  title: 'Customer Directory — Legacy Bank',
};

export default function CustomerListView() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [filterText, setFilterText] = useState('');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadCustomers = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await bankingApi.getCustomers();
      setCustomers(data);
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Error loading customers';
      setErrorMessage(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadCustomers();
  }, []);

  const filteredCustomers = useMemo(() => {
    const q = filterText.trim().toLowerCase();
    if (!q) return customers;
    return customers.filter((c) => {
      const fullName = `${c.lastName} ${c.firstName} ${c.firstName} ${c.lastName}`.toLowerCase();
      return (
        fullName.includes(q) ||
        (c.cifNumber ?? '').toLowerCase().includes(q) ||
        (c.email ?? '').toLowerCase().includes(q) ||
        String(c.customerId).includes(q)
      );
    });
  }, [customers, filterText]);

  if (errorMessage) {
    return (
      <ErrorCard
        title="Unable to Load Customer Directory"
        message={errorMessage}
        showDirectoryButton={false}
      />
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Customer Directory</h1>
          <p className="page-subtitle">
            Browse, search, and manage Customer Information File (CIF) records.
          </p>
        </div>
        <Button theme="primary" onClick={() => navigate('/customers/new')}>
          <Icon slot="prefix" icon="vaadin:plus" aria-hidden="true" />
          Register New Customer
        </Button>
      </div>

      <section className="md-card" aria-label="Customer Directory List">
        <div className="md-card__header">
          <TextField
            label="Filter directory"
            placeholder="Search by name, CIF number, email, or ID..."
            value={filterText}
            clearButtonVisible
            onValueChanged={(e) => setFilterText(e.detail.value)}
            style={{ minWidth: '280px', flex: '1', maxWidth: '440px' }}
          >
            <Icon slot="prefix" icon="vaadin:search" aria-hidden="true" />
          </TextField>

          <div className="md-badge md-badge--info">
            {loading ? 'Loading...' : `${filteredCustomers.length} customer(s)`}
          </div>
        </div>

        <div className="md-card__body">
          {loading ? (
            <ProgressBar indeterminate aria-label="Loading customers" />
          ) : filteredCustomers.length === 0 ? (
            <p>
              No customers match your filter.{' '}
              <Link to="/customers/new" className="entity-link">
                Register a new customer
              </Link>
              .
            </p>
          ) : (
            <Grid items={filteredCustomers} allRowsVisible theme="row-stripes">
              <GridSortColumn
                path="lastName"
                header="Name"
                autoWidth
                renderer={({ item }) => (
                  <Link to={`/customers/${item.customerId}`} className="entity-link">
                    {item.lastName}, {item.firstName}
                  </Link>
                )}
              />
              <GridSortColumn
                path="cifNumber"
                header="CIF Number"
                autoWidth
                renderer={({ item }) => <span className="tabular-num">{item.cifNumber}</span>}
              />
              <GridSortColumn path="email" header="Email" autoWidth />
              <GridColumn
                header="KYC Status"
                autoWidth
                renderer={({ item }) => <StatusBadge value={item.kycStatus ?? 'PENDING'} />}
              />
              <GridSortColumn
                path="accountCount"
                header="Accounts"
                autoWidth
                renderer={({ item }) => (
                  <span className="md-badge md-badge--info tabular-num">
                    {item.accountCount ?? 0}
                  </span>
                )}
              />
              <GridColumn
                header="Actions"
                autoWidth
                renderer={({ item }) => (
                  <Button
                    theme="small secondary"
                    onClick={() => navigate(`/customers/${item.customerId}`)}
                    aria-label={`View profile for ${item.firstName} ${item.lastName}`}
                  >
                    View Profile
                  </Button>
                )}
              />
            </Grid>
          )}
        </div>
      </section>
    </div>
  );
}
