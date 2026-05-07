import { AlertCircle } from 'lucide-react';
import './FormError.css';

function FormError({ message, inline = false, className = '' }) {
  if (!message) return null;

  return (
    <div
      className={`ui-form-error ${inline ? 'ui-form-error--inline' : ''} ${className}`}
      role="alert"
    >
      <AlertCircle size={inline ? 14 : 16} className="ui-form-error__icon" aria-hidden="true" />
      <span className="ui-form-error__message">{message}</span>
    </div>
  );
}

export default FormError;
