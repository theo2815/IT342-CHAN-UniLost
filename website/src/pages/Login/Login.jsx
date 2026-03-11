import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Mail, Lock, CheckSquare, Square, AlertTriangle, ArrowRight, ShieldCheck, MapPin, Eye, EyeOff } from 'lucide-react';
import authService from '../../services/authService';
import { useToast } from '../../components/Toast';
import { Input, Button, Alert } from '../../components/ui';
import './Login.css';

function Login() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false
  });
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const navigate = useNavigate();
  const toast = useToast();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    if (apiError) setApiError('');
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    setApiError('');

    try {
      const result = await authService.login(formData.email, formData.password);
      if (result.success) {
        toast.success('Welcome back! Redirecting...', { title: 'Login Successful', duration: 2000 });
        setTimeout(() => navigate('/dashboard'), 600);
      } else {
        const msg = typeof result.error === 'string' ? result.error : 'Login failed. Please check your credentials.';
        setApiError(msg);
        toast.error(msg, { title: 'Login Failed' });
      }
    } catch (err) {
      const msg = 'An unexpected error occurred. Please try again later.';
      setApiError(msg);
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container">
        {/* Left Side - Form */}
        <div className="login-form-section">
          <div className="form-wrapper glass">
            <div className="form-header">
              <div className="logo">
                <Search className="logo-icon" size={28} />
                <span className="logo-text">UniLost</span>
              </div>
              <h2>Welcome Back</h2>
              <p>Your campus lost & found network.</p>
            </div>

            {apiError && (
              <Alert type="error" icon={AlertTriangle}>
                {apiError}
              </Alert>
            )}

            <form onSubmit={handleSubmit} className="login-form" noValidate>
              <div className="form-group">
                <Input
                  label="University Email"
                  icon={Mail}
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="student@cit.edu"
                  error={errors.email}
                  autoComplete="email"
                />
              </div>

              <div className="form-group">
                <Input
                  label="Password"
                  icon={Lock}
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  error={errors.password}
                  autoComplete="current-password"
                  rightAction={
                    <button
                      type="button"
                      onClick={() => setShowPassword(v => !v)}
                      tabIndex={-1}
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  }
                />
              </div>

              <div className="form-options">
                <label className="checkbox-container">
                  <input
                    type="checkbox"
                    name="rememberMe"
                    checked={formData.rememberMe}
                    onChange={handleChange}
                  />
                  <div className="checkbox-custom">
                    {formData.rememberMe ? <CheckSquare size={18} /> : <Square size={18} />}
                  </div>
                  <span>Remember me</span>
                </label>
                <Link to="/forgot-password">Forgot Password?</Link>
              </div>

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                loading={isLoading}
                iconRight={!isLoading ? ArrowRight : undefined}
              >
                {isLoading ? 'Signing in...' : 'Sign In'}
              </Button>
            </form>

            <div className="form-footer">
              <p>New to UniLost? <Link to="/register">Create Account</Link></p>
            </div>
          </div>
        </div>

        {/* Right Side - Branding */}
        <div className="login-branding">
          <div className="branding-content">
            <div className="branding-text">
              <h1>Find What's Lost.<br /><span className="highlight">Across Campus.</span></h1>
              <p>A centralized lost & found platform for students across all major Cebu City universities.</p>
            </div>

            <div className="branding-features">
              <div className="feature-item">
                <div className="feature-icon-wrapper">
                  <ShieldCheck size={24} />
                </div>
                <div className="feature-text">
                  <h3>Verified Students</h3>
                  <p>University email authentication keeps it secure.</p>
                </div>
              </div>
              <div className="feature-item">
                <div className="feature-icon-wrapper">
                  <MapPin size={24} />
                </div>
                <div className="feature-text">
                  <h3>Multi-Campus</h3>
                  <p>CIT-U, USC, USJ-R, UC, and more — all connected.</p>
                </div>
              </div>
            </div>
          </div>
          <div className="decorative-circle circle-1"></div>
          <div className="decorative-circle circle-2"></div>
        </div>
      </div>
    </div>
  );
}

export default Login;
