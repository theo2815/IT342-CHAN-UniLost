import { useEffect } from 'react';
import { useToast } from '../components/ui/Toast/toastContext';
import { setGlobalToast } from '../components/ui/Toast/toastApi';

/**
 * Hook that initializes the global toast API.
 * Call this once at the app root so showSuccess/showError/etc. work globally.
 */
export function useGlobalToast() {
  const toast = useToast();

  useEffect(() => {
    setGlobalToast(toast);
    return () => setGlobalToast(null);
  }, [toast]);

  return toast;
}

// Re-export useToast for convenience
export { useToast } from '../components/ui/Toast/toastContext';
