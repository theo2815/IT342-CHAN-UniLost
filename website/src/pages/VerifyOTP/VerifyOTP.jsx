import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Search, ArrowLeft, ShieldCheck } from 'lucide-react';
import authService from '../../services/authService';
import { useToast } from '../../components/Toast';
import './VerifyOTP.css';

function VerifyOTP() {
    const location = useLocation();
    const navigate = useNavigate();
    const toast = useToast();
    const email = location.state?.email;

    const [otp, setOtp] = useState(['', '', '', '', '', '']);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const [resendCooldown, setResendCooldown] = useState(0);
    const inputRefs = useRef([]);

    // Redirect if no email in state
    useEffect(() => {
        if (!email) {
            navigate('/forgot-password', { replace: true });
        }
    }, [email, navigate]);

    // Cooldown timer for resend
    useEffect(() => {
        if (resendCooldown <= 0) return;
        const timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
        return () => clearTimeout(timer);
    }, [resendCooldown]);

    const handleChange = (index, value) => {
        // Only allow digits
        if (value && !/^\d$/.test(value)) return;

        const newOtp = [...otp];
        newOtp[index] = value;
        setOtp(newOtp);
        setError('');

        // Auto-focus next input
        if (value && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !otp[index] && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
        if (!pastedData) return;

        const newOtp = [...otp];
        for (let i = 0; i < pastedData.length; i++) {
            newOtp[i] = pastedData[i];
        }
        setOtp(newOtp);
        setError('');

        const focusIndex = Math.min(pastedData.length, 5);
        inputRefs.current[focusIndex]?.focus();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const code = otp.join('');
        if (code.length !== 6) {
            setError('Please enter the complete 6-digit code');
            return;
        }

        setIsLoading(true);
        setError('');
        try {
            const result = await authService.verifyOtp(email, code);
            if (result.success) {
                toast.success('Code verified!', { title: 'Verified', duration: 2000 });
                navigate('/reset-password', { state: { email, otp: code } });
            } else {
                const msg = typeof result.error === 'string' ? result.error : 'Invalid code.';
                setError(msg);
                toast.error(msg, { title: 'Verification Failed' });
            }
        } catch {
            const msg = 'Something went wrong. Please try again.';
            setError(msg);
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    };

    const handleResend = async () => {
        if (resendCooldown > 0) return;
        setResendCooldown(60);
        try {
            const result = await authService.forgotPassword(email);
            if (result.success) {
                toast.success('A new code has been sent to your email.', { title: 'Code Resent' });
                setOtp(['', '', '', '', '', '']);
                inputRefs.current[0]?.focus();
            } else {
                toast.error(result.error || 'Failed to resend code.');
            }
        } catch {
            toast.error('Failed to resend code.');
        }
    };

    if (!email) return null;

    return (
        <div className="otp-page">
            <div className="otp-card glass">
                <div className="form-header">
                    <div className="logo">
                        <Search className="logo-icon" size={28} />
                        <span className="logo-text">UniLost</span>
                    </div>
                    <div className="otp-icon-wrapper">
                        <ShieldCheck size={32} />
                    </div>
                    <h2>Enter Verification Code</h2>
                    <p>We sent a 6-digit code to <strong>{email}</strong></p>
                </div>

                <form onSubmit={handleSubmit} className="otp-form">
                    <div className="otp-inputs" onPaste={handlePaste}>
                        {otp.map((digit, index) => (
                            <input
                                key={index}
                                ref={(el) => (inputRefs.current[index] = el)}
                                type="text"
                                inputMode="numeric"
                                maxLength={1}
                                value={digit}
                                onChange={(e) => handleChange(index, e.target.value)}
                                onKeyDown={(e) => handleKeyDown(index, e)}
                                className={`otp-input ${error ? 'otp-error' : ''}`}
                                autoFocus={index === 0}
                            />
                        ))}
                    </div>
                    {error && <span className="field-error">{error}</span>}

                    <button type="submit" className="btn-primary" disabled={isLoading}>
                        {isLoading ? (
                            <span className="btn-loading">
                                <span className="spinner"></span>
                                Verifying...
                            </span>
                        ) : (
                            <>Verify Code</>
                        )}
                    </button>
                </form>

                <div className="resend-section">
                    <p>Didn't receive the code?</p>
                    <button
                        className="resend-btn"
                        onClick={handleResend}
                        disabled={resendCooldown > 0}
                    >
                        {resendCooldown > 0 ? `Resend in ${resendCooldown}s` : 'Resend Code'}
                    </button>
                </div>

                <div className="form-footer">
                    <Link to="/forgot-password" className="back-link"><ArrowLeft size={16} /> Change email</Link>
                </div>
            </div>
        </div>
    );
}

export default VerifyOTP;
