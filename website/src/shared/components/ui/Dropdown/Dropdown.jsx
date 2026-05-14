import { useState, useRef, useEffect, cloneElement } from 'react';
import { ChevronDown } from 'lucide-react';
import './Dropdown.css';

/**
 * Reusable Dropdown component.
 *
 * @param {ReactElement} trigger     - Custom trigger element; receives `onClick` and `ref`.
 *                                     If omitted, a default button trigger is rendered.
 * @param {string}       label       - Text for the default trigger button.
 * @param {ReactNode}    children    - Dropdown menu content (use Dropdown.Item, .Divider, .Header).
 * @param {string}       align       - 'left' | 'right' (default 'right').
 * @param {number}       width       - Menu width in px (default 240).
 * @param {string}       className   - Extra class on the wrapper.
 */
function Dropdown({ trigger, label, children, align = 'right', width = 240, className = '' }) {
  const [isOpen, setIsOpen] = useState(false);
  const wrapperRef = useRef(null);

  // Close on click outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Close on Escape key
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') setIsOpen(false);
    };
    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      return () => document.removeEventListener('keydown', handleEscape);
    }
  }, [isOpen]);

  const toggle = () => setIsOpen((prev) => !prev);
  const close = () => setIsOpen(false);

  // Render trigger — supports render-prop: trigger={(isOpen) => <button>...</button>}
  const triggerElement = trigger
    ? typeof trigger === 'function'
      ? trigger(isOpen)
      : cloneElement(trigger, { onClick: toggle })
    : (
      <button className="ui-dropdown-trigger" onClick={toggle}>
        <span>{label || 'Menu'}</span>
        <ChevronDown size={16} className={`ui-dropdown-chevron ${isOpen ? 'open' : ''}`} />
      </button>
    );

  // Wrap render-prop triggers with an onClick handler
  const finalTrigger = typeof trigger === 'function'
    ? cloneElement(triggerElement, { onClick: toggle })
    : triggerElement;

  return (
    <div className={`ui-dropdown ${className}`} ref={wrapperRef}>
      {finalTrigger}

      {isOpen && (
        <div
          className={`ui-dropdown-menu glass ${align === 'left' ? 'align-left' : 'align-right'}`}
          style={{ width }}
          role="menu"
        >
          {typeof children === 'function' ? children({ close }) : children}
        </div>
      )}
    </div>
  );
}

/* ── Sub-components ── */

function DropdownItem({ children, onClick, variant, className = '', ...rest }) {
  return (
    <button
      className={`ui-dropdown-item ${variant === 'danger' ? 'danger' : ''} ${className}`}
      onClick={onClick}
      role="menuitem"
      {...rest}
    >
      {children}
    </button>
  );
}

function DropdownDivider() {
  return <div className="ui-dropdown-divider" role="separator" />;
}

function DropdownHeader({ children, className = '' }) {
  return <div className={`ui-dropdown-header ${className}`}>{children}</div>;
}

/* Attach sub-components */
Dropdown.Item = DropdownItem;
Dropdown.Divider = DropdownDivider;
Dropdown.Header = DropdownHeader;

export default Dropdown;
