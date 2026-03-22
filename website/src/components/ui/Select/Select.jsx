import { forwardRef, useId } from 'react';
import { ChevronDown, AlertCircle } from 'lucide-react';
import './Select.css';

const Select = forwardRef(({
  label,
  error,
  helper,
  icon: Icon,
  size = 'md',
  required = false,
  disabled = false,
  className = '',
  id,
  children,
  ...props
}, ref) => {
  const generatedId = useId();
  const selectId = id || generatedId;

  return (
    <div className={`ui-select-wrapper ${className}`}>
      {label && (
        <label
          className={`ui-select__label ${required ? 'ui-select__label--required' : ''}`}
          htmlFor={selectId}
        >
          {label}
        </label>
      )}

      <div
        className={[
          'ui-select-group',
          `ui-select-group--${size}`,
          error && 'ui-select-group--error',
          disabled && 'ui-select-group--disabled',
        ].filter(Boolean).join(' ')}
      >
        {Icon && (
          <span className="ui-select__icon" aria-hidden="true">
            <Icon size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
          </span>
        )}

        <select
          ref={ref}
          id={selectId}
          className="ui-select__field"
          disabled={disabled}
          required={required}
          aria-invalid={!!error}
          aria-describedby={error ? `${selectId}-error` : helper ? `${selectId}-helper` : undefined}
          {...props}
        >
          {children}
        </select>

        <span className="ui-select__chevron" aria-hidden="true">
          <ChevronDown size={16} />
        </span>
      </div>

      {error && (
        <span className="ui-select__error" id={`${selectId}-error`} role="alert">
          <AlertCircle size={12} />
          {error}
        </span>
      )}

      {helper && !error && (
        <span className="ui-select__helper" id={`${selectId}-helper`}>
          {helper}
        </span>
      )}
    </div>
  );
});

Select.displayName = 'Select';

export default Select;
