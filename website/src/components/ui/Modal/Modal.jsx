import { useEffect, useCallback, useRef, useId } from 'react';
import { X } from 'lucide-react';
import './Modal.css';

function Modal({
  isOpen,
  onClose,
  title,
  size = 'md',
  children,
  footer,
  closeOnBackdrop = true,
  closeOnEscape = true,
  className = '',
}) {
  const modalRef = useRef(null);
  const previousFocus = useRef(null);
  const titleId = useId();

  // Trap focus & keyboard handling
  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Escape' && closeOnEscape) {
      onClose();
      return;
    }
    // Focus trap
    if (e.key === 'Tab' && modalRef.current) {
      const focusable = modalRef.current.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      if (focusable.length === 0) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    }
  }, [onClose, closeOnEscape]);

  useEffect(() => {
    if (isOpen) {
      previousFocus.current = document.activeElement;
      document.body.style.overflow = 'hidden';
      document.addEventListener('keydown', handleKeyDown);
      // Focus the modal
      setTimeout(() => modalRef.current?.focus(), 50);
    }
    return () => {
      document.body.style.overflow = '';
      document.removeEventListener('keydown', handleKeyDown);
      previousFocus.current?.focus();
    };
  }, [isOpen, handleKeyDown]);

  if (!isOpen) return null;

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget && closeOnBackdrop) {
      onClose();
    }
  };

  return (
    <div
      className="ui-modal-backdrop"
      onClick={handleBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby={title ? titleId : undefined}
    >
      <div
        className={`ui-modal ui-modal--${size} ${className}`}
        ref={modalRef}
        tabIndex={-1}
      >
        {title && (
          <div className="ui-modal__header">
            <h2 className="ui-modal__title" id={titleId}>{title}</h2>
            <button className="ui-modal__close" onClick={onClose} aria-label="Close modal">
              <X size={20} />
            </button>
          </div>
        )}

        <div className="ui-modal__body">
          {children}
        </div>

        {footer && (
          <div className="ui-modal__footer">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}

export default Modal;
