import { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Search, Lock, ArrowRight, Eye, EyeOff, CheckCircle } from 'lucide-react';
import authService from '../../services/authService';
import { useToast } from '../../components/Toast';
import { Input, Button } from '../../components/ui';
import './ResetPassword.css';

function ResetPassword() {
    const location = useLocation();
    const navigate = useNavigate();
    const toast = useToast();
    const email = location.state?.email;
    const otp = location.state?.otp;

    const [formData, setFormData] = useState({ password: '', confirmPassword: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (!email || !otp) {
            navigate('/forgot-password', { replace: true });
        }
    }, [email, otp, navigate]);

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

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    };

    const validate = () => {
        const newErrors = {};
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
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;

        setIsLoading(true);
        try {
            const result = await authService.resetPassword(email, otp, formData.password);
            if (result.success) {
                toast.success('Your password has been reset. Please sign in.', { title: 'Password Reset', duration: 3000 });
                setTimeout(() => navigate('/login', { replace: true }), 800);
            } else {
                const msg = typeof result.error === 'string' ? result.error : 'Password reset failed.';
                toast.error(msg, { title: 'Error' });
            }
        } catch {
            toast.error('Something went wrong. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!email || !otp) return null;

    return (
        <div className="reset-page">
            <div className="reset-card glass">
                <div className="form-header">
                    <div className="logo">
                        <Search className="logo-icon" size={28} />
                        <span className="logo-text">UniLost</span>
                    </div>
                    <div className="reset-icon-wrapper">
                        <CheckCircle size={32} />
                    </div>
                    <h2>Set New Password</h2>
                    <p>Create a new password for <strong>{email}</strong></p>
                </div>

                <form onSubmit={handleSubmit} className="reset-form" noValidate>
                    <div className="form-group">
                        <Input
                            label="New Password"
                            icon={Lock}
                            type={showPassword ? 'text' : 'password'}
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="••••••••"
                            error={errors.password}
                            autoComplete="new-password"
                            autoFocus
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
                        {passwordStrength && !errors.password && (
                            <div className="password-strength">
                                <div className="strength-bar">
                                    <div className={`strength-fill strength-${passwordStrength.level}`}></div>
                                </div>
                                <span className={`strength-label strength-${passwordStrength.level}`}>{passwordStrength.label}</span>
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <Input
                            label="Confirm New Password"
                            icon={Lock}
                            type={showConfirm ? 'text' : 'password'}
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            placeholder="••••••••"
                            error={errors.confirmPassword}
                            autoComplete="new-password"
                            rightAction={
                                <button
                                    type="button"
                                    onClick={() => setShowConfirm(v => !v)}
                                    tabIndex={-1}
                                    aria-label={showConfirm ? 'Hide password' : 'Show password'}
                                >
                                    {showConfirm ? <EyeOff size={16} /> : <Eye size={16} />}
                                </button>
                            }
                        />
                    </div>

                    <Button
                        type="submit"
                        variant="primary"
                        size="lg"
                        fullWidth
                        loading={isLoading}
                        iconRight={!isLoading ? ArrowRight : undefined}
                    >
                        {isLoading ? 'Resetting...' : 'Reset Password'}
                    </Button>
                </form>

                <div className="form-footer">
                    <Link to="/login">Back to Sign In</Link>
                </div>
            </div>
        </div>
    );
}

export default ResetPassword;
