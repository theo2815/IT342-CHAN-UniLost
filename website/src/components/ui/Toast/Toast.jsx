import { useState, useEffect, useCallback, useRef } from 'react';
import { CheckCircle, AlertTriangle, AlertCircle, Info, X } from 'lucide-react';
import './Toast.css';
import { ToastContext } from './toastContext';

/* ── Individual Toast ── */
function ToastItem({ toast, onRemove }) {
  const [exiting, setExiting] = useState(false);
  const timerRef = useRef(null);

  const handleRemove = useCallback(() => {
    setExiting(true);
    setTimeout(() => onRemove(toast.id), 250);
  }, [toast.id, onRemove]);

  useEffect(() => {
    const duration = toast.duration || 4000;
    timerRef.current = setTimeout(handleRemove, duration);
    return () => clearTimeout(timerRef.current);
  }, [toast.id, toast.duration, handleRemove]);

  const icons = {
    success: <CheckCircle size={20} />,
    error: <AlertCircle size={20} />,
    warning: <AlertTriangle size={20} />,
    info: <Info size={20} />,
  };

  return (
    <div
      className={`ui-toast ui-toast--${toast.type} ${exiting ? 'ui-toast--exiting' : ''}`}
      role="alert"
      aria-live="assertive"
    >
      <span className="ui-toast__icon" aria-hidden="true">
        {icons[toast.type] || icons.info}
      </span>

      <div className="ui-toast__body">
        {toast.title && <strong className="ui-toast__title">{toast.title}</strong>}
        <span className="ui-toast__message">{toast.message}</span>
      </div>

      <button
        className="ui-toast__close"
        onClick={handleRemove}
        aria-label="Close notification"
      >
        <X size={16} />
      </button>

      <div
        className="ui-toast__progress"
        style={{ animationDuration: `${toast.duration || 4000}ms` }}
        aria-hidden="true"
      />
    </div>
  );
}

/* ── Provider ── */
let toastCounter = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  const addToast = useCallback((message, type = 'info', options = {}) => {
    const id = ++toastCounter;
    setToasts(prev => {
      // Keep max 5 toasts visible
      const next = prev.length >= 5 ? prev.slice(1) : prev;
      return [...next, { id, message, type, ...options }];
    });
    return id;
  }, []);

  const toast = {
    success: (message, options) => addToast(message, 'success', options),
    error: (message, options) => addToast(message, 'error', { duration: 5000, ...options }),
    warning: (message, options) => addToast(message, 'warning', options),
    info: (message, options) => addToast(message, 'info', options),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="ui-toast-container" aria-live="polite" aria-label="Notifications">
        {toasts.map(t => (
          <ToastItem key={t.id} toast={t} onRemove={removeToast} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}


