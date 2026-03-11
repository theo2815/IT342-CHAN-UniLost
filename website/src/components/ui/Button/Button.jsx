import { forwardRef } from 'react';
import './Button.css';

const Button = forwardRef(({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  icon: Icon,
  iconRight: IconRight,
  fullWidth = false,
  type = 'button',
  className = '',
  ...props
}, ref) => {
  const isIconOnly = !children && (Icon || IconRight);

  const classes = [
    'ui-btn',
    `ui-btn--${variant}`,
    `ui-btn--${size}`,
    loading && 'ui-btn--loading',
    disabled && 'ui-btn--disabled',
    isIconOnly && 'ui-btn--icon-only',
    fullWidth && 'ui-btn--full',
    className,
  ].filter(Boolean).join(' ');

  return (
    <button
      ref={ref}
      type={type}
      className={classes}
      disabled={disabled || loading}
      aria-busy={loading}
      aria-disabled={disabled || loading}
      {...props}
    >
      {loading && <span className="ui-btn__spinner" aria-hidden="true" />}
      {Icon && <Icon size={size === 'sm' ? 14 : size === 'lg' ? 20 : 16} aria-hidden="true" />}
      {children}
      {IconRight && <IconRight size={size === 'sm' ? 14 : size === 'lg' ? 20 : 16} aria-hidden="true" />}
    </button>
  );
});

Button.displayName = 'Button';

export default Button;
