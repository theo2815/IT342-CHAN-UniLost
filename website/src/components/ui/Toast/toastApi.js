/* Global toast API bridge */
let globalToastRef = null;

export function setGlobalToast(toastApi) {
  globalToastRef = toastApi;
}

export function showSuccess(message, options) { globalToastRef?.success(message, options); }
export function showError(message, options) { globalToastRef?.error(message, options); }
export function showWarning(message, options) { globalToastRef?.warning(message, options); }
export function showInfo(message, options) { globalToastRef?.info(message, options); }
