import { useEffect } from 'react';
import { AlertTriangle, X } from 'lucide-react';
import './ConfirmDialog.css';

function ConfirmDialog({
    isOpen,
    onClose,
    onConfirm,
    title = 'Confirm Action',
    message = 'Are you sure you want to proceed?',
    confirmLabel = 'Confirm',
    cancelLabel = 'Cancel',
    variant = 'danger' // 'danger' | 'warning' | 'success'
}) {
    // Close on Escape key
    useEffect(() => {
        const handleEscape = (e) => {
            if (e.key === 'Escape' && isOpen) {
                onClose();
            }
        };
        document.addEventListener('keydown', handleEscape);
        return () => document.removeEventListener('keydown', handleEscape);
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    const handleBackdropClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    return (
        <div className="confirm-dialog-backdrop" onClick={handleBackdropClick}>
            <div className="confirm-dialog">
                {/* Header */}
                <div className="confirm-dialog-header">
                    <div className={`confirm-dialog-icon ${variant}`}>
                        <AlertTriangle size={24} />
                    </div>
                    <button className="confirm-dialog-close" onClick={onClose}>
                        <X size={18} />
                    </button>
                </div>

                {/* Content */}
                <div className="confirm-dialog-content">
                    <h3 className="confirm-dialog-title">{title}</h3>
                    <div className="confirm-dialog-message">
                        {typeof message === 'string' ? <p>{message}</p> : message}
                    </div>
                </div>

                {/* Actions */}
                <div className="confirm-dialog-actions">
                    <button className="confirm-dialog-btn cancel" onClick={onClose}>
                        {cancelLabel}
                    </button>
                    <button
                        className={`confirm-dialog-btn confirm ${variant}`}
                        onClick={() => {
                            onConfirm();
                            onClose();
                        }}
                    >
                        {confirmLabel}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ConfirmDialog;
