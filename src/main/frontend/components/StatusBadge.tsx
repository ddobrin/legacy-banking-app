import React from 'react';

interface StatusBadgeProps {
  value?: string | null;
  variant?: 'auto' | 'success' | 'warning' | 'error' | 'info' | 'neutral';
}

function resolveVariant(value?: string | null): 'success' | 'warning' | 'error' | 'info' | 'neutral' {
  const normalized = (value ?? '').trim().toUpperCase();
  switch (normalized) {
    case 'ACTIVE':
    case 'OPEN':
    case 'COMPLETED':
    case 'VERIFIED':
    case 'LOW':
    case 'DEPOSIT':
      return 'success';
    case 'PENDING':
    case 'MEDIUM':
    case 'FROZEN':
    case 'TRANSFER':
      return 'warning';
    case 'CLOSED':
    case 'HIGH':
    case 'WITHDRAWAL':
    case 'WITHDRAW':
    case 'REJECTED':
      return 'error';
    case 'CHK-STD':
    case 'SAV-HYS':
    case 'CHECKING':
    case 'SAVINGS':
      return 'info';
    default:
      return 'neutral';
  }
}

export function StatusBadge({ value, variant = 'auto' }: StatusBadgeProps) {
  const resolved = variant === 'auto' ? resolveVariant(value) : variant;
  return (
    <span className={`md-badge md-badge--${resolved}`}>
      {value || '—'}
    </span>
  );
}
