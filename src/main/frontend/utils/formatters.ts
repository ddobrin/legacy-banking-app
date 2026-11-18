const currencyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const dateTimeFormatter = new Intl.DateTimeFormat('en-US', {
  year: 'numeric',
  month: 'short',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

export function formatCurrency(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '$0.00';
  }
  const numeric = typeof value === 'number' ? value : Number(value);
  if (Number.isNaN(numeric)) {
    return String(value);
  }
  return currencyFormatter.format(numeric);
}

export function formatInterestRate(rate: number | null | undefined): string {
  if (rate === null || rate === undefined) {
    return '0.00%';
  }
  return `${(rate * 100).toFixed(2)}%`;
}

export function formatDate(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }
  return value;
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return dateTimeFormatter.format(parsed);
}

export function formatAddress(parts: {
  addressLine1?: string | null;
  city?: string | null;
  state?: string | null;
  postalCode?: string | null;
  countryCode?: string | null;
}): string {
  const tokens = [
    parts.addressLine1,
    parts.city,
    [parts.state, parts.postalCode].filter(Boolean).join(' '),
    parts.countryCode,
  ].filter((item) => item && item.trim().length > 0);
  return tokens.length > 0 ? tokens.join(', ') : '—';
}
