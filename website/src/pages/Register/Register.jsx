import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Sparkles, GraduationCap, Mail, Lock, User, MapPin, Phone, Building, ArrowRight, AlertTriangle, CheckSquare, Square, CheckCircle, Leaf } from 'lucide-react';
import authService from '../../services/authService';
import schoolService from '../../services/schoolService';
import './Register.css';

function Register() {
    const navigate = useNavigate();
    const [schools, setSchools] = useState([]);

    const [formData, setFormData] = useState({
        schoolId: '', // Default school set below
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        studentIdNumber: '',
        address: '',
        phoneNumber: '',
        agreeToTerms: false
    });

    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState({});
    const [apiError, setApiError] = useState('');

    // Fetch schools on mount
    useEffect(() => {
        const fetchSchools = async () => {
            try {
                const result = await schoolService.getAllSchools();
                if (result.success) {
                    setSchools(result.data);
                    // Auto-select CIT-U if found, purely as a helper
                    const cit = result.data.find(s => s.name.includes("Cebu Institute of Technology"));
                    if (cit) {
                        setFormData(prev => ({ ...prev, schoolId: cit.id }));
                    }
                }
            } catch (err) {
                console.error("Failed to fetch schools", err);
            }
        };
        fetchSchools();
    }, []);

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
        if (!formData.schoolId) newErrors.schoolId = 'Please select your university';
        if (!formData.firstName.trim()) newErrors.firstName = 'First name is required';
        if (!formData.lastName.trim()) newErrors.lastName = 'Last name is required';

        if (!formData.email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!formData.email.endsWith('.edu')) {
            // Optional: Enforce .edu email for "Verified Student" feel
            // newErrors.email = 'Please use your school (.edu) email address';
        }

        if (!formData.password) newErrors.password = 'Password is required';
        else if (formData.password.length < 6) newErrors.password = 'Min. 6 characters';

        if (formData.password !== formData.confirmPassword) newErrors.confirmPassword = 'Passwords do not match';
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
                navigate('/login');
            } else {
                setApiError(result.error || 'Registration failed.');
            }
        } catch (err) {
            setApiError('Something went wrong. Please try again.');
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
                                <Sparkles className="logo-icon" size={28} />
                                <span className="logo-text">HulamPay</span>
                            </div>
                            <h2>Create Account</h2>
                            <p>Join your campus marketplace today.</p>
                        </div>

                        {apiError && (
                            <div className="error-alert">
                                <AlertTriangle size={18} /> {apiError}
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="register-form">
                            {/* School Selection */}
                            <div className="form-group full-width">
                                <label>Select University <span className="required">*</span></label>
                                <div className="input-group">
                                    <Building className="input-icon" size={18} />
                                    <select
                                        name="schoolId"
                                        value={formData.schoolId}
                                        onChange={handleChange}
                                        className={`select-input ${errors.schoolId ? 'error' : ''}`}
                                    >
                                        <option value="">-- Choose your school --</option>
                                        {schools.map(school => (
                                            <option key={school.id} value={school.id}>{school.name}</option>
                                        ))}
                                    </select>
                                </div>
                                {errors.schoolId && <span className="error-msg">{errors.schoolId}</span>}
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label>First Name</label>
                                    <div className="input-group">
                                        <User className="input-icon" size={18} />
                                        <input
                                            type="text"
                                            name="firstName"
                                            value={formData.firstName}
                                            onChange={handleChange}
                                            placeholder="Jane"
                                            className={errors.firstName ? 'error' : ''}
                                        />
                                    </div>
                                    {errors.firstName && <span className="error-msg">{errors.firstName}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Last Name</label>
                                    <div className="input-group">
                                        <User className="input-icon" size={18} />
                                        <input
                                            type="text"
                                            name="lastName"
                                            value={formData.lastName}
                                            onChange={handleChange}
                                            placeholder="Doe"
                                            className={errors.lastName ? 'error' : ''}
                                        />
                                    </div>
                                    {errors.lastName && <span className="error-msg">{errors.lastName}</span>}
                                </div>
                            </div>

                            <div className="form-group">
                                <label>School Email</label>
                                <div className="input-group">
                                    <Mail className="input-icon" size={18} />
                                    <input
                                        type="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        placeholder="student@cit.edu"
                                        className={errors.email ? 'error' : ''}
                                    />
                                </div>
                                {errors.email && <span className="error-msg">{errors.email}</span>}
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label>Password</label>
                                    <div className="input-group">
                                        <Lock className="input-icon" size={18} />
                                        <input
                                            type="password"
                                            name="password"
                                            value={formData.password}
                                            onChange={handleChange}
                                            placeholder="••••••"
                                            className={errors.password ? 'error' : ''}
                                        />
                                    </div>
                                    {errors.password && <span className="error-msg">{errors.password}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Confirm</label>
                                    <div className="input-group">
                                        <CheckCircle className="input-icon" size={18} />
                                        <input
                                            type="password"
                                            name="confirmPassword"
                                            value={formData.confirmPassword}
                                            onChange={handleChange}
                                            placeholder="••••••"
                                            className={errors.confirmPassword ? 'error' : ''}
                                        />
                                    </div>
                                    {errors.confirmPassword && <span className="error-msg">{errors.confirmPassword}</span>}
                                </div>
                            </div>

                            {/* Optional Fields Toggle or Section */}
                            <div className="optional-section">
                                <p className="section-label">Student Verification (Optional)</p>
                                <div className="form-row">
                                    <div className="form-group">
                                        <div className="input-group">
                                            <GraduationCap className="input-icon" size={18} />
                                            <input
                                                type="text"
                                                name="studentIdNumber"
                                                value={formData.studentIdNumber}
                                                onChange={handleChange}
                                                placeholder="Student ID No."
                                            />
                                        </div>
                                    </div>
                                    <div className="form-group">
                                        <div className="input-group">
                                            <Phone className="input-icon" size={18} />
                                            <input
                                                type="tel"
                                                name="phoneNumber"
                                                value={formData.phoneNumber}
                                                onChange={handleChange}
                                                placeholder="Phone Number"
                                            />
                                        </div>
                                    </div>
                                </div>
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
                                {errors.agreeToTerms && <span className="error-msg">{errors.agreeToTerms}</span>}
                            </div>

                            <button type="submit" className="btn-primary" disabled={isLoading}>
                                {isLoading ? 'Creating Account...' : (
                                    <>Join HulamPay <ArrowRight size={18} /></>
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
                        <h1>The Student<br />Economy.</h1>
                        <p>Connect with peers from <strong>Cebu Institute of Technology - University</strong> and beyond.</p>

                        <div className="benefits-list">
                            <div className="benefit-item">
                                <div className="benefit-icon">
                                    <GraduationCap size={24} />
                                </div>
                                <span>Campus-Exclusive Deals</span>
                            </div>
                            <div className="benefit-item">
                                <div className="benefit-icon">
                                    <User size={24} />
                                </div>
                                <span>Meet on Campus</span>
                            </div>
                            <div className="benefit-item">
                                <div className="benefit-icon">
                                    <Leaf size={24} />
                                </div>
                                <span>Sustainable Living</span>
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
