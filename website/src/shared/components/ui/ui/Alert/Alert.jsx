import { useState, useEffect, useCallback } from 'react';
import { CheckCircle, AlertTriangle, AlertCircle, Info, X } from 'lucide-react';
import './Alert.css';

const iconMap = {
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
};

function Alert({
  type = 'info',
  title,
  children,
  dismissible = false,
  autoDismiss = 0,
  onDismiss,
  icon: CustomIcon,
  showIcon = true,
  className = '',
}) {
  const [visible, setVisible] = useState(true);
  const [exiting, setExiting] = useState(false);

  const dismiss = useCallback(() => {
    setExiting(true);
    setTimeout(() => {
      setVisible(false);
      onDismiss?.();
    }, 200);
  }, [onDismiss]);

  useEffect(() => {
    if (autoDismiss > 0) {
      const timer = setTimeout(dismiss, autoDismiss);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, dismiss]);

  if (!visible) return null;

  const IconComponent = CustomIcon || iconMap[type];

  return (
    <div
      className={`ui-alert ui-alert--${type} ${exiting ? 'ui-alert--exiting' : ''} ${className}`}
      role="alert"
      aria-live="polite"
    >
      {showIcon && IconComponent && (
        <span className="ui-alert__icon" aria-hidden="true">
          <IconComponent size={20} />
        </span>
      )}

      <div className="ui-alert__content">
        {title && <div className="ui-alert__title">{title}</div>}
        <div className="ui-alert__message">{children}</div>
      </div>

      {dismissible && (
        <button
          className="ui-alert__dismiss"
          onClick={dismiss}
          aria-label="Dismiss alert"
        >
          <X size={16} />
        </button>
      )}
    </div>
  );
}

export default Alert;
