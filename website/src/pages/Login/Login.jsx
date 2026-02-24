import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Mail, Lock, CheckSquare, Square, AlertTriangle, ArrowRight, ShieldCheck, MapPin } from 'lucide-react';
import authService from '../../services/authService';
import './Login.css';

function Login() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const result = await authService.login(formData.email, formData.password);
      if (result.success) {
        navigate('/dashboard');
      } else {
        setError(result.error || 'Login failed. Please check your credentials.');
      }
    } catch (err) {
      setError('An unexpected error occurred. Please try again later.');
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
                <Search className="logo-icon" size={32} />
                <span className="logo-text">UniLost</span>
              </div>
              <h2>Welcome Back</h2>
              <p>Your campus lost & found network.</p>
            </div>

            {error && (
              <div className="error-alert">
                <AlertTriangle className="error-icon" size={20} />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="login-form">
              <div className="form-group">
                <label htmlFor="email">University Email</label>
                <div className="input-group">
                  <Mail className="input-icon" size={20} />
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="student@cit.edu"
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="password">Password</label>
                <div className="input-group">
                  <Lock className="input-icon" size={20} />
                  <input
                    type="password"
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="••••••••"
                    required
                  />
                </div>
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

              <button type="submit" className="btn-primary" disabled={isLoading}>
                {isLoading ? (
                  <span className="spinner"></span>
                ) : (
                  <>
                    Sign In <ArrowRight size={20} />
                  </>
                )}
              </button>
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
          {/* Decorative Elements */}
          <div className="decorative-circle circle-1"></div>
          <div className="decorative-circle circle-2"></div>
        </div>
      </div>
    </div>
  );
}

export default Login;
