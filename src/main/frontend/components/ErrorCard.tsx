import React from 'react';
import { useNavigate } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { Icon } from '@vaadin/react-components/Icon.js';

interface ErrorCardProps {
  title?: string;
  message: string;
  showDirectoryButton?: boolean;
}

export function ErrorCard({
  title = 'Resource Not Found',
  message,
  showDirectoryButton = true,
}: ErrorCardProps) {
  const navigate = useNavigate();

  return (
    <section className="md-card md-card--error" role="alert" aria-labelledby="error-card-title">
      <div className="md-card__header">
        <div className="md-card__title-group">
          <Icon icon="vaadin:exclamation-circle" className="md-icon-error" aria-hidden="true" />
          <h2 id="error-card-title" className="md-card__title">
            {title}
          </h2>
        </div>
      </div>
      <div className="md-card__body">
        <p className="md-error-text">{message}</p>
        <div className="md-actions-row">
          <Button theme="primary" onClick={() => navigate('/')}>
            <Icon slot="prefix" icon="vaadin:home" aria-hidden="true" />
            Return to Dashboard
          </Button>
          {showDirectoryButton && (
            <Button theme="secondary" onClick={() => navigate('/customers')}>
              <Icon slot="prefix" icon="vaadin:users" aria-hidden="true" />
              Customer Directory
            </Button>
          )}
        </div>
      </div>
    </section>
  );
}
