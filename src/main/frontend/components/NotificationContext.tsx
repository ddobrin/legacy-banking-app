import React, { createContext, useCallback, useContext, useState } from 'react';
import { Notification } from '@vaadin/react-components/Notification.js';
import { Button } from '@vaadin/react-components/Button.js';
import { Icon } from '@vaadin/react-components/Icon.js';

export type NotificationKind = 'success' | 'error' | 'info';

interface NotificationState {
  id: number;
  message: string;
  kind: NotificationKind;
  opened: boolean;
}

interface NotificationContextValue {
  notifySuccess: (message: string) => void;
  notifyError: (message: string) => void;
  notifyInfo: (message: string) => void;
}

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const [notice, setNotice] = useState<NotificationState | null>(null);

  const show = useCallback((message: string, kind: NotificationKind) => {
    setNotice({
      id: Date.now(),
      message,
      kind,
      opened: true,
    });
  }, []);

  const notifySuccess = useCallback((message: string) => show(message, 'success'), [show]);
  const notifyError = useCallback((message: string) => show(message, 'error'), [show]);
  const notifyInfo = useCallback((message: string) => show(message, 'info'), [show]);

  const theme =
    notice?.kind === 'error'
      ? 'error'
      : notice?.kind === 'success'
        ? 'success'
        : 'contrast';

  return (
    <NotificationContext.Provider value={{ notifySuccess, notifyError, notifyInfo }}>
      {children}
      <Notification
        key={notice?.id ?? 0}
        opened={Boolean(notice?.opened)}
        duration={notice?.kind === 'error' ? 6000 : 4000}
        position="top-end"
        theme={theme}
        onOpenedChanged={(event) => {
          if (!event.detail.value) {
            setNotice((prev) => (prev ? { ...prev, opened: false } : null));
          }
        }}
      >
        <div
          className="notification-content"
          role={notice?.kind === 'error' ? 'alert' : 'status'}
          aria-live={notice?.kind === 'error' ? 'assertive' : 'polite'}
        >
          <Icon
            icon={
              notice?.kind === 'error'
                ? 'vaadin:exclamation-circle'
                : notice?.kind === 'success'
                  ? 'vaadin:check-circle'
                  : 'vaadin:info-circle'
            }
            aria-hidden="true"
          />
          <span className="notification-message">{notice?.message}</span>
          <Button
            theme="tertiary-inline icon"
            aria-label="Dismiss notification"
            onClick={() => setNotice((prev) => (prev ? { ...prev, opened: false } : null))}
          >
            <Icon icon="vaadin:close-small" aria-hidden="true" />
          </Button>
        </div>
      </Notification>
    </NotificationContext.Provider>
  );
}

export function useNotifications(): NotificationContextValue {
  const ctx = useContext(NotificationContext);
  if (!ctx) {
    throw new Error('useNotifications must be used inside NotificationProvider');
  }
  return ctx;
}
