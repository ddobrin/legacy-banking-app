import React from 'react';
import { useLocation } from 'react-router';
import { ErrorCard } from '../components/ErrorCard';

export default function NotFoundView() {
  const location = useLocation();
  return (
    <ErrorCard
      title="Resource Not Found"
      message={`No banking page matches "${location.pathname}". Use the navigation menu or return to the dashboard.`}
    />
  );
}
