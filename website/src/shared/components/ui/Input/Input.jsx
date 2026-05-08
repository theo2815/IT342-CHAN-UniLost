import { forwardRef, useId } from 'react';
import { AlertCircle } from 'lucide-react';
import './Input.css';

const Input = forwardRef(({
  label,
  error,
  helper,
  icon: Icon,
  iconRight: IconRight,
  rightAction,
  size = 'md',
  required = false,
  disabled = false,
  textarea = false,
  maxLength,
  value,
  className = '',
  id,
  ...props
}, ref) => {
  const generatedId = useId();
  const inputId = id || generatedId;
  const Tag = textarea ? 'textarea' : 'input';
  const charCount = maxLength && typeof value === 'string' ? value.length : null;

  return (
    <div className={`ui-input-wrapper ${className}`}>
      {label && (
        <label
          className={`ui-input__label ${required ? 'ui-input__label--required' : ''}`}
          htmlFor={inputId}
        >
          {label}
        </label>
      )}

      <div
        className={[
          'ui-input-group',
          `ui-input-group--${size}`,
          error && 'ui-input-group--error',
          disabled && 'ui-input-group--disabled',
          textarea && 'ui-input-group--textarea',
        ].filter(Boolean).join(' ')}
      >
        {Icon && (
          <span className="ui-input__icon" aria-hidden="true">
            <Icon size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
          </span>
        )}

        <Tag
          ref={ref}
          id={inputId}
          className={`ui-input__field ${textarea ? 'ui-input__field--textarea' : ''}`}
          disabled={disabled}
          required={required}
          maxLength={maxLength}
          value={value}
          aria-invalid={!!error}
          aria-describedby={error ? `${inputId}-error` : helper ? `${inputId}-helper` : undefined}
          {...props}
        />

        {IconRight && (
          <span className="ui-input__icon" aria-hidden="true">
            <IconRight size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
          </span>
        )}

        {rightAction && (
          <span className="ui-input__action">
            {rightAction}
          </span>
        )}
      </div>

      {charCount !== null && (
        <span className={`ui-input__char-count ${charCount > maxLength ? 'ui-input__char-count--over' : ''}`}>
          {charCount}/{maxLength}
        </span>
      )}

      {error && (
        <span className="ui-input__error" id={`${inputId}-error`} role="alert">
          <AlertCircle size={12} />
          {error}
        </span>
      )}

      {helper && !error && (
        <span className="ui-input__helper" id={`${inputId}-helper`}>
          {helper}
        </span>
      )}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
