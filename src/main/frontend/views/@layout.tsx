import React, { useEffect, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router';
import { AppLayout } from '@vaadin/react-components/AppLayout.js';
import { DrawerToggle } from '@vaadin/react-components/DrawerToggle.js';
import { SideNav } from '@vaadin/react-components/SideNav.js';
import { SideNavItem } from '@vaadin/react-components/SideNavItem.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import '@vaadin/icons';
import '../styles/material-theme.css';
import { NotificationProvider, useNotifications } from '../components/NotificationContext';

function LayoutShell() {
  const location = useLocation();
  const navigate = useNavigate();
  const { notifyError } = useNotifications();
  const [customerQuery, setCustomerQuery] = useState('');
  const [accountQuery, setAccountQuery] = useState('');

  useEffect(() => {
    document.title = 'Legacy Bank — Corporate CIF Portal';
  }, [location.pathname]);

  const handleCustomerLookup = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = customerQuery.trim();
    if (!trimmed) {
      notifyError('Please enter a Customer ID or Name to search.');
      return;
    }
    navigate(`/customers/${encodeURIComponent(trimmed)}`);
    setCustomerQuery('');
  };

  const handleAccountLookup = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = accountQuery.trim();
    if (!trimmed) {
      notifyError('Please enter an Account ID to search.');
      return;
    }
    navigate(`/accounts/${encodeURIComponent(trimmed)}`);
    setAccountQuery('');
  };

  return (
    <>
      <a href="#main-content" className="skip-link">
        Skip to main content
      </a>

      <AppLayout primarySection="drawer" className="bank-app-layout">
        <div slot="navbar" className="navbar-container">
          <div className="navbar-brand">
            <DrawerToggle
              className="navbar-drawer-toggle"
              aria-label="Toggle navigation menu"
            />
            <Link to="/" className="navbar-title-link" aria-label="Legacy Bank Corporate Intranet Home">
              <Icon icon="vaadin:institution" aria-hidden="true" />
              <span>
                <span className="navbar-title-text">Legacy Bank Corporate Intranet</span>
                <span className="navbar-subtitle">Core Banking &amp; Customer Information File (CIF)</span>
              </span>
            </Link>
          </div>

          <div className="navbar-search-group">
            <form
              className="navbar-search-form"
              role="search"
              aria-label="Quick Customer Lookup"
              onSubmit={handleCustomerLookup}
            >
              <label htmlFor="nav-customer-search" className="navbar-search-label">
                Customer Search
              </label>
              <input
                id="nav-customer-search"
                type="text"
                className="navbar-search-input"
                placeholder="ID or Name"
                value={customerQuery}
                onChange={(e) => setCustomerQuery(e.target.value)}
                aria-label="Customer ID or Name"
              />
              <button type="submit" className="navbar-search-btn">
                Go
              </button>
            </form>

            <form
              className="navbar-search-form"
              role="search"
              aria-label="Quick Account Lookup"
              onSubmit={handleAccountLookup}
            >
              <label htmlFor="nav-account-search" className="navbar-search-label">
                Account Search
              </label>
              <input
                id="nav-account-search"
                type="text"
                className="navbar-search-input"
                placeholder="Account ID"
                value={accountQuery}
                onChange={(e) => setAccountQuery(e.target.value)}
                aria-label="Account ID"
              />
              <button type="submit" className="navbar-search-btn">
                Go
              </button>
            </form>
          </div>
        </div>

        <div slot="drawer" className="drawer-wrapper">
          <div className="drawer-section-label" id="nav-heading">
            Navigation
          </div>
          <nav className="drawer-nav" aria-labelledby="nav-heading">
            <SideNav
              location={location.pathname}
              onNavigate={({ path }) => {
                if (path) {
                  navigate(path);
                }
              }}
            >
              <SideNavItem path="/">
                <Icon slot="prefix" icon="vaadin:dashboard" aria-hidden="true" />
                Dashboard
              </SideNavItem>
              <SideNavItem path="/customers">
                <Icon slot="prefix" icon="vaadin:users" aria-hidden="true" />
                Customer List
              </SideNavItem>
              <SideNavItem path="/customers/new">
                <Icon slot="prefix" icon="vaadin:user-card" aria-hidden="true" />
                New Customer
              </SideNavItem>
              <SideNavItem path="/accounts/new">
                <Icon slot="prefix" icon="vaadin:wallet" aria-hidden="true" />
                New Account
              </SideNavItem>
              <SideNavItem path="/transactions/new">
                <Icon slot="prefix" icon="vaadin:exchange" aria-hidden="true" />
                New Transaction
              </SideNavItem>
            </SideNav>
          </nav>

          <footer className="drawer-footer">
            <div>
              <strong>Legacy Bank CIF System v2.0</strong>
            </div>
            <div>Internal Use Only</div>
          </footer>
        </div>

        <main id="main-content" className="main-content" tabIndex={-1}>
          <Outlet />
        </main>
      </AppLayout>
    </>
  );
}

export default function MainLayout() {
  return (
    <NotificationProvider>
      <LayoutShell />
    </NotificationProvider>
  );
}
