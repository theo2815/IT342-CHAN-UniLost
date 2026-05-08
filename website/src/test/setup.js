// Test setup: ensure each test starts with a clean localStorage / sessionStorage.
import { afterEach } from 'vitest';

afterEach(() => {
  if (typeof window !== 'undefined') {
    window.localStorage?.clear();
    window.sessionStorage?.clear();
  }
});
