import { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, GraduationCap, Mail, Lock, User, ArrowRight, AlertTriangle, CheckSquare, Square, CheckCircle, ShieldCheck, MapPinned, Eye, EyeOff } from 'lucide-react';
import authService from '../../services/authService';
import campusService from '../../services/campusService';
import { useToast } from '../../components/Toast';
import './Register.css';

function Register() {
    const navigate = useNavigate();
    const toast = useToast();
    const [campuses, setCampuses] = useState([]);
    const [detectedCampus, setDetectedCampus] = useState(null);
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        confirmPassword: '',
        agreeToTerms: false
    });

    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState({});
    const [apiError, setApiError] = useState('');

    // Password strength calculation
    const passwordStrength = useMemo(() => {
        const pw = formData.password;
        if (!pw) return null;
        let score = 0;
        if (pw.length >= 6) score++;
        if (pw.length >= 10) score++;
        if (/[A-Z]/.test(pw)) score++;
        if (/[0-9]/.test(pw)) score++;
        if (/[^A-Za-z0-9]/.test(pw)) score++;
        if (score <= 1) return { label: 'Weak', level: 'weak' };
        if (score <= 3) return { label: 'Medium', level: 'medium' };
        return { label: 'Strong', level: 'strong' };
    }, [formData.password]);

    // Fetch campuses on mount for domain validation display
    useEffect(() => {
        const fetchCampuses = async () => {
            try {
                const result = await campusService.getAllCampuses();
                if (result.success) {
                    setCampuses(result.data);
                }
            } catch (err) {
                console.error("Failed to fetch campuses", err);
            }
        };
        fetchCampuses();
    }, []);

    // Auto-detect campus from email domain
    useEffect(() => {
        const email = formData.email;
        if (email && email.includes('@')) {
            const domain = email.split('@')[1]?.toLowerCase();
            if (domain) {
                const matched = campuses.find(c =>
                    c.domainWhitelist && c.domainWhitelist.toLowerCase() === domain
                );
                setDetectedCampus(matched || null);
            } else {
                setDetectedCampus(null);
            }
        } else {
            setDetectedCampus(null);
        }
    }, [formData.email, campuses]);

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
        if (!formData.fullName.trim()) newErrors.fullName = 'Full name is required';

        if (!formData.email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Please enter a valid email address';
        } else if (!detectedCampus) {
            newErrors.email = 'Use your university email (e.g., name@cit.edu)';
        }

        if (!formData.password) {
            newErrors.password = 'Password is required';
        } else if (formData.password.length < 6) {
            newErrors.password = 'Password must be at least 6 characters';
        }

        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        if (!formData.agreeToTerms) newErrors.agreeToTerms = 'You must agree to the terms';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsLoading(true);
        setApiError('');

        try {
            const result = await authService.register(formData);
            if (result.success) {
                toast.success('Your account has been created. Please sign in.', { title: 'Registration Successful', duration: 3000 });
                setTimeout(() => navigate('/login'), 800);
            } else {
                const msg = typeof result.error === 'string' ? result.error : 'Registration failed.';
                setApiError(msg);
                toast.error(msg, { title: 'Registration Failed' });
            }
        } catch (err) {
            const msg = 'Something went wrong. Please try again.';
            setApiError(msg);
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="register-page">
            <div className="register-container">
                {/* Left Side - Form */}
                <div className="register-form-section">
                    <div className="form-wrapper glass">
                        <div className="form-header">
                            <div className="logo">
                                <Search className="logo-icon" size={28} />
                                <span className="logo-text">UniLost</span>
                            </div>
                            <h2>Create Account</h2>
                            <p>Join the Cebu City campus lost & found network.</p>
                        </div>

                        {apiError && (
                            <div className="error-alert">
                                <AlertTriangle size={18} /> {apiError}
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="register-form" noValidate>
                            <div className="form-group full-width">
                                <label htmlFor="fullName">Full Name <span className="required">*</span></label>
                                <div className="input-group">
                                    <User className="input-icon" size={18} />
                                    <input
                                        type="text"
                                        id="fullName"
                                        name="fullName"
                                        value={formData.fullName}
                                        onChange={handleChange}
                                        placeholder="Jane Doe"
                                        className={errors.fullName ? 'input-error' : ''}
                                        autoComplete="name"
                                    />
                                </div>
                                {errors.fullName && <span className="field-error">{errors.fullName}</span>}
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="email">University Email <span className="required">*</span></label>
                                <div className="input-group">
                                    <Mail className="input-icon" size={18} />
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        placeholder="yourname@cit.edu"
                                        className={errors.email ? 'input-error' : ''}
                                        autoComplete="email"
                                    />
                                </div>
                                {detectedCampus && (
                                    <span className="school-detected">
                                        <CheckCircle size={14} /> {detectedCampus.name}
                                    </span>
                                )}
                                {errors.email && <span className="field-error">{errors.email}</span>}
                                {!detectedCampus && formData.email.includes('@') && !errors.email && (
                                    <span className="email-hint">
                                        Supported: cit.edu, usc.edu.ph, usjr.edu.ph, uc.edu.ph, up.edu.ph, swu.edu.ph, cnu.edu.ph, ctu.edu.ph
                                    </span>
                                )}
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="password">Password <span className="required">*</span></label>
                                <div className="input-group">
                                    <Lock className="input-icon" size={18} />
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        id="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        placeholder="••••••"
                                        className={errors.password ? 'input-error' : ''}
                                        autoComplete="new-password"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle"
                                        onClick={() => setShowPassword(v => !v)}
                                        tabIndex={-1}
                                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                                    >
                                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                                    </button>
                                </div>
                                {errors.password && <span className="field-error">{errors.password}</span>}
                                {passwordStrength && !errors.password && (
                                    <div className="password-strength">
                                        <div className="strength-bar">
                                            <div className={`strength-fill strength-${passwordStrength.level}`}></div>
                                        </div>
                                        <span className={`strength-label strength-${passwordStrength.level}`}>{passwordStrength.label}</span>
                                    </div>
                                )}
                            </div>

                            <div className="form-group full-width">
                                <label htmlFor="confirmPassword">Confirm Password <span className="required">*</span></label>
                                <div className="input-group">
                                    <CheckCircle className="input-icon" size={18} />
                                    <input
                                        type={showConfirm ? 'text' : 'password'}
                                        id="confirmPassword"
                                        name="confirmPassword"
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        placeholder="••••••"
                                        className={errors.confirmPassword ? 'input-error' : ''}
                                        autoComplete="new-password"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle"
                                        onClick={() => setShowConfirm(v => !v)}
                                        tabIndex={-1}
                                        aria-label={showConfirm ? 'Hide password' : 'Show password'}
                                    >
                                        {showConfirm ? <EyeOff size={16} /> : <Eye size={16} />}
                                    </button>
                                </div>
                                {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
                            </div>

                            <div className="form-group checkbox-group">
                                <label className="checkbox-container">
                                    <input
                                        type="checkbox"
                                        name="agreeToTerms"
                                        checked={formData.agreeToTerms}
                                        onChange={handleChange}
                                    />
                                    <div className="checkbox-custom">
                                        {formData.agreeToTerms ? <CheckSquare size={18} /> : <Square size={18} />}
                                    </div>
                                    <span>I agree to the <a href="#">Terms</a> & <a href="#">Privacy Policy</a></span>
                                </label>
                                {errors.agreeToTerms && <span className="field-error">{errors.agreeToTerms}</span>}
                            </div>

                            <button type="submit" className="btn-primary" disabled={isLoading}>
                                {isLoading ? (
                                    <span className="btn-loading">
                                        <span className="spinner"></span>
                                        Creating Account...
                                    </span>
                                ) : (
                                    <>Join UniLost <ArrowRight size={18} /></>
                                )}
                            </button>
                        </form>

                        <div className="form-footer">
                            <p>Already a member? <Link to="/login">Sign In</Link></p>
                        </div>
                    </div>
                </div>

                {/* Right Side - Branding */}
                <div className="register-branding">
                    <div className="branding-content">
                        <h1>Never Lose<br />Track Again.</h1>
                        <p>Connect with students across <strong>8 Cebu City universities</strong> to recover lost items safely.</p>

                        <div className="benefits-list">
                            <div className="benefit-item">
                                <div className="benefit-icon">
                                    <ShieldCheck size={24} />
                                </div>
                                <span>Verified University Emails</span>
                            </div>
                            <div className="benefit-item">
                                <div className="benefit-icon">
                                    <MapPinned size={24} />
                                </div>
                                <span>Cross-Campus Search</span>
                            </div>
                            <div className="benefit-item">
                                <div className="benefit-icon">
                                    <GraduationCap size={24} />
                                </div>
                                <span>Safe Campus Handover</span>
                            </div>
                        </div>
                    </div>
                    {/* Decorative Elements */}
                    <div className="decorative-circle circle-3"></div>
                    <div className="decorative-circle circle-4"></div>
                </div>
            </div>
        </div>
    );
}

export default Register;
