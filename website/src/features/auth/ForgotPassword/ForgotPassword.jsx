import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, ArrowRight, ArrowLeft } from 'lucide-react';
import authService from '../authService';
import { useToast } from '../../../shared/hooks/useToast';
import { Input, Button } from '../../../shared/components/ui';
import './ForgotPassword.css';

function ForgotPassword() {
    const [email, setEmail] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const toast = useToast();

    const validateEmail = () => {
        if (!email.trim()) {
            setError('Email is required');
            return false;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            setError('Please enter a valid email address');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!validateEmail()) return;

        setIsLoading(true);
        try {
            const result = await authService.forgotPassword(email.trim().toLowerCase());
            if (result.success) {
                toast.success('Verification code sent to your email.', { title: 'Code Sent', duration: 3000 });
                navigate('/verify-otp', { state: { email: email.trim().toLowerCase() } });
            } else {
                const msg = typeof result.error === 'string' ? result.error : 'Failed to send code.';
                setError(msg);
                toast.error(msg, { title: 'Error' });
            }
        } catch {
            const msg = 'Something went wrong. Please try again.';
            setError(msg);
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="forgot-page">
            <div className="forgot-card glass">
                <div className="form-header">
                    <div className="logo">
                        <img src="/unilost-logo.png" alt="UniLost" className="logo-icon" width="72" height="72" />
                        <span className="logo-text">UniLost</span>
                    </div>
                    <h2>Forgot Password</h2>
                    <p>Enter your university email and we'll send you a verification code.</p>
                </div>

                <form onSubmit={handleSubmit} className="forgot-form" noValidate>
                    <div className="form-group">
                        <Input
                            label="University Email"
                            icon={Mail}
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => { setEmail(e.target.value); setError(''); }}
                            placeholder="student@cit.edu"
                            error={error}
                            autoComplete="email"
                            autoFocus
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
                        {isLoading ? 'Sending Code...' : 'Send Verification Code'}
                    </Button>
                </form>

                <div className="form-footer">
                    <Link to="/login" className="back-link"><ArrowLeft size={16} /> Back to Sign In</Link>
                </div>
            </div>
        </div>
    );
}

export default ForgotPassword;
