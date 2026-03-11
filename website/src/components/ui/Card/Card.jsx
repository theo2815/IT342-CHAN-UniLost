import { forwardRef } from 'react';
import './Card.css';

const Card = forwardRef(({
  children,
  hoverable = false,
  padded = false,
  compact = false,
  elevated = false,
  glass = false,
  fullWidth = false,
  className = '',
  onClick,
  ...props
}, ref) => {
  const classes = [
    'ui-card',
    hoverable && 'ui-card--hoverable',
    padded && 'ui-card--padded',
    compact && 'ui-card--compact',
    elevated && 'ui-card--elevated',
    glass && 'ui-card--glass',
    fullWidth && 'ui-card--full',
    className,
  ].filter(Boolean).join(' ');

  return (
    <div ref={ref} className={classes} onClick={onClick} {...props}>
      {children}
    </div>
  );
});

Card.displayName = 'Card';

function CardHeader({ title, children, className = '' }) {
  return (
    <div className={`ui-card__header ${className}`}>
      {title && <h3 className="ui-card__header-title">{title}</h3>}
      {children}
    </div>
  );
}

function CardBody({ children, className = '' }) {
  return <div className={`ui-card__body ${className}`}>{children}</div>;
}

function CardFooter({ children, className = '' }) {
  return <div className={`ui-card__footer ${className}`}>{children}</div>;
}

function CardImage({ src, alt, className = '' }) {
  return <img className={`ui-card__image ${className}`} src={src} alt={alt} loading="lazy" />;
}

Card.Header = CardHeader;
Card.Body = CardBody;
Card.Footer = CardFooter;
Card.Image = CardImage;

export default Card;
