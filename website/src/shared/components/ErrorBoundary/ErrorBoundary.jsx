import { Component } from 'react';
import { Link } from 'react-router-dom';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Application render error:', error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '24px', background: '#f8fafc' }}>
          <div style={{ width: '100%', maxWidth: '520px', background: '#ffffff', borderRadius: '16px', padding: '32px', boxShadow: '0 10px 30px rgba(15, 23, 42, 0.08)', textAlign: 'center' }}>
            <h1 style={{ margin: '0 0 12px', fontSize: '1.75rem', color: '#0f172a' }}>Something went wrong</h1>
            <p style={{ margin: '0 0 24px', color: '#475569', lineHeight: 1.6 }}>
              The page crashed unexpectedly. You can reload the app or go back to the home page.
            </p>
            <div style={{ display: 'flex', gap: '12px', justifyContent: 'center', flexWrap: 'wrap' }}>
              <button
                type="button"
                onClick={this.handleReload}
                style={{ border: 'none', borderRadius: '10px', padding: '12px 18px', background: '#2563eb', color: '#ffffff', fontWeight: 600, cursor: 'pointer' }}
              >
                Reload App
              </button>
              <Link
                to="/"
                style={{ borderRadius: '10px', padding: '12px 18px', background: '#e2e8f0', color: '#0f172a', fontWeight: 600, textDecoration: 'none' }}
              >
                Go Home
              </Link>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
