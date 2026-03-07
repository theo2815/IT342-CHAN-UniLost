import { useState, useEffect, useCallback, createContext, useContext } from 'react';
import { CheckCircle, AlertTriangle, Info, X } from 'lucide-react';
import './Toast.css';

const ToastContext = createContext(null);

export function useToast() {
    const context = useContext(ToastContext);
    if (!context) throw new Error('useToast must be used within a ToastProvider');
    return context;
}

function ToastItem({ toast, onRemove }) {
    useEffect(() => {
        const timer = setTimeout(() => onRemove(toast.id), toast.duration || 4000);
        return () => clearTimeout(timer);
    }, [toast.id, toast.duration, onRemove]);

    const icons = {
        success: <CheckCircle size={20} />,
        error: <AlertTriangle size={20} />,
        info: <Info size={20} />,
    };

    return (
        <div className={`toast-item toast-${toast.type}`} role="alert">
            <div className="toast-icon">{icons[toast.type] || icons.info}</div>
            <div className="toast-body">
                {toast.title && <strong className="toast-title">{toast.title}</strong>}
                <span className="toast-message">{toast.message}</span>
            </div>
            <button className="toast-close" onClick={() => onRemove(toast.id)} aria-label="Close">
                <X size={16} />
            </button>
            <div className="toast-progress" style={{ animationDuration: `${toast.duration || 4000}ms` }} />
        </div>
    );
}

let toastIdCounter = 0;

export function ToastProvider({ children }) {
    const [toasts, setToasts] = useState([]);

    const removeToast = useCallback((id) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    }, []);

    const addToast = useCallback((message, type = 'info', options = {}) => {
        const id = ++toastIdCounter;
        setToasts(prev => [...prev, { id, message, type, ...options }]);
        return id;
    }, []);

    const toast = {
        success: (message, options) => addToast(message, 'success', options),
        error: (message, options) => addToast(message, 'error', { duration: 5000, ...options }),
        info: (message, options) => addToast(message, 'info', options),
    };

    return (
        <ToastContext.Provider value={toast}>
            {children}
            <div className="toast-container" aria-live="polite">
                {toasts.map(t => (
                    <ToastItem key={t.id} toast={t} onRemove={removeToast} />
                ))}
            </div>
        </ToastContext.Provider>
    );
}
