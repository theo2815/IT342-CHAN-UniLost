import './Badge.css';

function Badge({
  children,
  variant = 'default',
  size = 'md',
  solid = false,
  outline = false,
  dot = false,
  icon: Icon,
  className = '',
}) {
  const classes = [
    'ui-badge',
    `ui-badge--${variant}`,
    `ui-badge--${size}`,
    solid && 'ui-badge--solid',
    outline && 'ui-badge--outline',
    className,
  ].filter(Boolean).join(' ');

  return (
    <span className={classes}>
      {dot && <span className="ui-badge__dot" aria-hidden="true" />}
      {Icon && <Icon size={size === 'sm' ? 10 : size === 'lg' ? 14 : 12} aria-hidden="true" />}
      {children}
    </span>
  );
}

export default Badge;
