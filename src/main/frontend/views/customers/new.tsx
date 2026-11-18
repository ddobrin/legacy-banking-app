import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { EmailField } from '@vaadin/react-components/EmailField.js';
import { DatePicker } from '@vaadin/react-components/DatePicker.js';
import { Select } from '@vaadin/react-components/Select.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { ApiError, bankingApi } from '../../api/bankingApi';
import { useNotifications } from '../../components/NotificationContext';

export const config: ViewConfig = {
  title: 'Register New Customer — Legacy Bank',
};

const RISK_OPTIONS = [
  { label: 'Low', value: 'LOW' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'High', value: 'HIGH' },
];

export default function CreateCustomerView() {
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useNotifications();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [cifNumber, setCifNumber] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [addressLine1, setAddressLine1] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [riskRating, setRiskRating] = useState('LOW');

  const [submitting, setSubmitting] = useState(false);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);

    if (!firstName.trim() || !lastName.trim() || !email.trim() || !dateOfBirth.trim()) {
      setErrorBanner('First Name, Last Name, Email Address, and Date of Birth are required.');
      return;
    }

    setSubmitting(true);
    try {
      const created = await bankingApi.createCustomer({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        dateOfBirth: dateOfBirth.trim(),
        ...(cifNumber.trim() ? { cifNumber: cifNumber.trim() } : {}),
        ...(phoneNumber.trim() ? { phoneNumber: phoneNumber.trim() } : {}),
        ...(addressLine1.trim() ? { addressLine1: addressLine1.trim() } : {}),
        ...(city.trim() ? { city: city.trim() } : {}),
        ...(state.trim() ? { state: state.trim() } : {}),
        ...(postalCode.trim() ? { postalCode: postalCode.trim() } : {}),
        ...(countryCode.trim() ? { countryCode: countryCode.trim().toUpperCase() } : {}),
        riskRating,
      });

      notifySuccess('Customer registered successfully');
      navigate(`/customers/${created.customerId}`);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Unable to register customer';
      setErrorBanner(message);
      notifyError(`Error: ${message}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <nav className="breadcrumbs" aria-label="Breadcrumb">
        <Link to="/">Dashboard</Link>
        <span aria-hidden="true">/</span>
        <Link to="/customers">Customer Directory</Link>
        <span aria-hidden="true">/</span>
        <span aria-current="page">Register New Customer</span>
      </nav>

      <div className="page-header">
        <div>
          <h1 className="page-title">Register New Customer</h1>
          <p className="page-subtitle">
            Create a new Customer Information File (CIF) profile in the core banking ledger.
          </p>
        </div>
      </div>

      <section className="md-card" aria-labelledby="register-customer-heading">
        <div className="md-card__header">
          <div className="md-card__title-group">
            <Icon icon="vaadin:user-card" aria-hidden="true" />
            <h2 id="register-customer-heading" className="md-card__title">
              Customer Identity &amp; Contact Information
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
              <TextField
                label="First Name"
                value={firstName}
                onValueChanged={(e) => setFirstName(e.detail.value)}
                required
                maxlength={100}
                errorMessage="First name is required"
              />

              <TextField
                label="Last Name"
                value={lastName}
                onValueChanged={(e) => setLastName(e.detail.value)}
                required
                maxlength={100}
                errorMessage="Last name is required"
              />

              <EmailField
                label="Email Address"
                value={email}
                onValueChanged={(e) => setEmail(e.detail.value)}
                required
                maxlength={150}
                errorMessage="Please enter a valid email address"
              />

              <DatePicker
                label="Date of Birth (YYYY-MM-DD)"
                placeholder="1990-01-01"
                value={dateOfBirth}
                onValueChanged={(e) => setDateOfBirth(e.detail.value)}
                required
                errorMessage="Date of birth is required"
              />

              <TextField
                label="CIF Number"
                placeholder="CIF-2024-001"
                helperText="Optional — leave blank to auto-generate a unique CIF number"
                value={cifNumber}
                onValueChanged={(e) => setCifNumber(e.detail.value)}
                maxlength={20}
              />

              <Select
                label="Risk Rating"
                items={RISK_OPTIONS}
                value={riskRating}
                onValueChanged={(e) => setRiskRating(e.detail.value)}
              />

              <TextField
                label="Phone Number"
                placeholder="+1-555-0100"
                value={phoneNumber}
                onValueChanged={(e) => setPhoneNumber(e.detail.value)}
                maxlength={20}
              />

              <TextField
                label="Address Line 1"
                value={addressLine1}
                onValueChanged={(e) => setAddressLine1(e.detail.value)}
                maxlength={255}
              />

              <TextField
                label="City"
                value={city}
                onValueChanged={(e) => setCity(e.detail.value)}
                maxlength={100}
              />

              <TextField
                label="State / Province"
                value={state}
                onValueChanged={(e) => setState(e.detail.value)}
                maxlength={100}
              />

              <TextField
                label="Postal Code"
                value={postalCode}
                onValueChanged={(e) => setPostalCode(e.detail.value)}
                maxlength={20}
              />

              <TextField
                label="Country Code (ISO-2)"
                placeholder="US"
                value={countryCode}
                onValueChanged={(e) => setCountryCode(e.detail.value)}
                maxlength={2}
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
                {submitting ? 'Registering...' : 'Submit Registration'}
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
