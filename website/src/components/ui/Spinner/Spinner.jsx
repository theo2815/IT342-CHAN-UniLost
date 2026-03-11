import './Spinner.css';

function Spinner({ size = 'md', label, fullPage = false, className = '' }) {
  const spinner = (
    <span
      className={`ui-spinner ui-spinner--${size} ${className}`}
      role="status"
      aria-label={label || 'Loading'}
    >
      <span className="sr-only">{label || 'Loading...'}</span>
    </span>
  );

  if (fullPage) {
    return (
      <div className="ui-spinner-overlay">
        {spinner}
        {label && <span className="ui-spinner-overlay__label">{label}</span>}
      </div>
    );
  }

  return spinner;
}

export default Spinner;
