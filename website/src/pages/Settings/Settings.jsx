import { useState, useRef } from 'react';
import { User, Lock, Bell, Palette, Camera, Save, X, Edit2, Eye, EyeOff } from 'lucide-react';
import authService from '../../services/authService';
import userService from '../../services/userService';
import Header from '../../components/Header';
import { useToast } from '../../components/Toast';
import { useTheme } from '../../context/ThemeContext';
import './Settings.css';

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
const PASSWORD_REGEX = /^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=]).{8,}$/;

function getPasswordStrength(password) {
    if (!password) return { level: '', label: '' };
    let score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[!@#$%^&*()_+\-=]/.test(password)) score++;
    if (score <= 2) return { level: 'weak', label: 'Weak' };
    if (score <= 3) return { level: 'medium', label: 'Medium' };
    return { level: 'strong', label: 'Strong' };
}

function Settings() {
    const { theme, setTheme } = useTheme();
    const toast = useToast();
    const [activeTab, setActiveTab] = useState('profile');
    const fileInputRef = useRef(null);

    // ── Profile State ──────────────────────────────────────
    const [isEditing, setIsEditing] = useState(false);
    const [saving, setSaving] = useState(false);
    const [pendingFile, setPendingFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState('');
    const [user, setUser] = useState(() => {
        const currentUser = authService.getCurrentUser() || {};
        return {
            id: currentUser.id || '',
            fullName: currentUser.fullName || '',
            email: currentUser.email || '',
            campus: currentUser.campus?.name || 'Unknown School',
            karmaScore: currentUser.karmaScore || 0,
            role: currentUser.role || 'STUDENT',
            profilePictureUrl: currentUser.profilePictureUrl || '',
        };
    });
    const [originalUser, setOriginalUser] = useState(user);

    // ── Password State ─────────────────────────────────────
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [passwordSaving, setPasswordSaving] = useState(false);

    // ── Notifications State ────────────────────────────────
    const [notificationsEnabled, setNotificationsEnabled] = useState(() => {
        const stored = localStorage.getItem('notificationsEnabled');
        return stored === null ? true : stored === 'true';
    });

    // ── Profile Handlers ───────────────────────────────────
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUser(prev => ({ ...prev, [name]: value }));
    };

    const handleEdit = () => setIsEditing(true);

    const handleCancel = () => {
        setUser(originalUser);
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        setPendingFile(null);
        setPreviewUrl('');
        setIsEditing(false);
    };

    const handleSave = async () => {
        setSaving(true);
        let updatedPictureUrl = user.profilePictureUrl;

        // Upload pending profile picture first
        if (pendingFile) {
            const picResult = await userService.uploadProfilePicture(user.id, pendingFile);
            if (picResult.success) {
                updatedPictureUrl = picResult.data.profilePictureUrl;
            } else {
                toast.error(picResult.error || 'Failed to upload photo');
                setSaving(false);
                return;
            }
        }

        // Save name
        const result = await userService.updateProfile(user.id, {
            fullName: user.fullName,
        });
        if (result.success) {
            const savedUser = { ...user, profilePictureUrl: updatedPictureUrl };
            const storedUser = authService.getCurrentUser();
            if (storedUser) {
                storedUser.fullName = user.fullName;
                storedUser.profilePictureUrl = updatedPictureUrl;
                localStorage.setItem('user', JSON.stringify(storedUser));
            }
            setUser(savedUser);
            setOriginalUser(savedUser);
            if (previewUrl) URL.revokeObjectURL(previewUrl);
            setPendingFile(null);
            setPreviewUrl('');
            setIsEditing(false);
            toast.success('Profile updated successfully!');
            window.dispatchEvent(new Event('userProfileUpdated'));
        } else {
            toast.error(result.error || 'Failed to update profile');
        }
        setSaving(false);
    };

    // ── Profile Picture Handlers ───────────────────────────
    const handlePhotoClick = () => fileInputRef.current?.click();

    const handleFileSelect = (e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        e.target.value = '';

        if (!ALLOWED_TYPES.includes(file.type)) {
            toast.error('Invalid file type. Please use JPEG, PNG, GIF, or WebP.');
            return;
        }
        if (file.size > MAX_FILE_SIZE) {
            toast.error('File too large. Maximum size is 5 MB.');
            return;
        }

        // Store file for upload on Save; show local preview only
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        setPendingFile(file);
        setPreviewUrl(URL.createObjectURL(file));
    };

    // ── Password Handlers ──────────────────────────────────
    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPasswordForm(prev => ({ ...prev, [name]: value }));
    };

    const passwordErrors = (() => {
        const errors = [];
        const { currentPassword, newPassword, confirmPassword } = passwordForm;
        if (!currentPassword) errors.push('Current password is required');
        if (newPassword && newPassword.length < 8) errors.push('New password must be at least 8 characters');
        if (newPassword && !PASSWORD_REGEX.test(newPassword)) errors.push('Must include uppercase, number, and special character');
        if (confirmPassword && newPassword !== confirmPassword) errors.push('Passwords do not match');
        return errors;
    })();

    const isPasswordFormValid = passwordForm.currentPassword &&
        passwordForm.newPassword &&
        passwordForm.confirmPassword &&
        PASSWORD_REGEX.test(passwordForm.newPassword) &&
        passwordForm.newPassword === passwordForm.confirmPassword;

    const handleChangePassword = async () => {
        if (!isPasswordFormValid) return;
        setPasswordSaving(true);

        const result = await userService.changePassword(user.id, {
            currentPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword,
        });

        if (result.success) {
            toast.success('Password changed successfully!');
            setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
        } else {
            toast.error(result.error || 'Failed to change password');
        }
        setPasswordSaving(false);
    };

    // ── Notifications Handler ──────────────────────────────
    const handleNotificationToggle = () => {
        const newValue = !notificationsEnabled;
        setNotificationsEnabled(newValue);
        localStorage.setItem('notificationsEnabled', String(newValue));
        toast.success(newValue ? 'Notifications enabled' : 'Notifications disabled');
    };

    const strength = getPasswordStrength(passwordForm.newPassword);

    // Tab Navigation Items
    const navItems = [
        { id: 'profile', label: 'Edit Profile', icon: <User size={20} /> },
        { id: 'password', label: 'Change Password', icon: <Lock size={20} /> },
        { id: 'notifications', label: 'Notifications', icon: <Bell size={20} /> },
        { id: 'theme', label: 'Theme', icon: <Palette size={20} /> },
    ];

    return (
        <div className="settings-page">
            <Header />
            <div className="settings-container">
                {/* Sidebar Navigation */}
            <div className="settings-sidebar">
                <nav className="sidebar-nav">
                    {navItems.map(item => (
                        <button
                            key={item.id}
                            className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
                            onClick={() => setActiveTab(item.id)}
                        >
                            {item.icon}
                            <span>{item.label}</span>
                        </button>
                    ))}
                </nav>
            </div>

            {/* Main Content Area */}
            <div className="settings-content">
                <header className="settings-header">
                    <h1 className="settings-title">
                        {navItems.find(item => item.id === activeTab)?.label}
                    </h1>
                    <p className="settings-subtitle">Manage your account settings and preferences.</p>
                </header>

                {/* ═══════════ PROFILE TAB ═══════════ */}
                {activeTab === 'profile' && (
                    <div className="settings-form">
                        
                        {/* Profile Picture Section */}
                        <div className="profile-avatar-section">
                            <div className="avatar-large" onClick={isEditing ? handlePhotoClick : undefined} style={isEditing ? { cursor: 'pointer' } : {}}>
                                {(previewUrl || user.profilePictureUrl) ? (
                                    <img src={previewUrl || user.profilePictureUrl} alt={user.fullName} className="avatar-image" />
                                ) : (
                                    user.fullName?.split(' ').map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase()
                                )}
                                {isEditing && (
                                    <div className="avatar-overlay">
                                        <Camera size={20} />
                                    </div>
                                )}
                            </div>
                            <div className="avatar-actions">
                                {isEditing && (
                                    <button className="btn-secondary" onClick={handlePhotoClick}>
                                        <Camera size={16} style={{marginRight: '8px'}}/>
                                        Change Photo
                                    </button>
                                )}
                            </div>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/jpeg,image/png,image/gif,image/webp"
                                hidden
                                onChange={handleFileSelect}
                            />
                        </div>

                        {/* Account Details Section */}
                        <section className="form-section">
                            <h2 className="section-title">Account Details</h2>
                            <div className="form-grid">
                                <div className="settings-form-group full-width">
                                    <label>Full Name</label>
                                    <input
                                        type="text"
                                        name="fullName"
                                        className="settings-input"
                                        value={user.fullName}
                                        onChange={handleInputChange}
                                        disabled={!isEditing}
                                    />
                                </div>
                                <div className="settings-form-group full-width">
                                    <label>School (Read-only)</label>
                                    <input
                                        type="text"
                                        className="settings-input"
                                        value={user.campus}
                                        disabled
                                    />
                                </div>
                            </div>
                        </section>

                        {/* Private Details Section */}
                        <section className="form-section">
                            <h2 className="section-title">Private Details</h2>
                            <div className="form-grid">
                                <div className="settings-form-group full-width">
                                    <label>Email Address (Read-only)</label>
                                    <input
                                        type="email"
                                        className="settings-input"
                                        value={user.email}
                                        disabled
                                    />
                                </div>
                                <div className="settings-form-group">
                                    <label>Role</label>
                                    <input
                                        type="text"
                                        className="settings-input"
                                        value={user.role}
                                        disabled
                                    />
                                </div>
                                <div className="settings-form-group">
                                    <label>Karma Score</label>
                                    <input
                                        type="text"
                                        className="settings-input"
                                        value={user.karmaScore}
                                        disabled
                                    />
                                </div>
                            </div>
                        </section>

                        {/* Actions */}
                        <div className="form-actions">
                            {!isEditing ? (
                                <button className="btn-primary" onClick={handleEdit}>
                                    <Edit2 size={16} style={{marginRight: '8px', display: 'inline'}}/>
                                    Edit Profile
                                </button>
                            ) : (
                                <>
                                    <button className="btn-danger" onClick={handleCancel}>
                                        <X size={16} style={{marginRight: '8px', display: 'inline'}}/>
                                        Cancel
                                    </button>
                                    <button className="btn-primary" onClick={handleSave} disabled={saving}>
                                        <Save size={16} style={{marginRight: '8px', display: 'inline'}}/>
                                        {saving ? 'Saving...' : 'Save Changes'}
                                    </button>
                                </>
                            )}
                        </div>

                    </div>
                )}

                {/* ═══════════ PASSWORD TAB ═══════════ */}
                {activeTab === 'password' && (
                    <div className="settings-form">
                        <section className="form-section">
                            <h2 className="section-title">Change Your Password</h2>
                            <p className="settings-subtitle" style={{marginBottom: '1.5rem'}}>
                                For security, please enter your current password before setting a new one.
                            </p>

                            <div className="password-form">
                                <div className="settings-form-group">
                                    <label>Current Password</label>
                                    <div className="password-input-wrapper">
                                        <input
                                            type={showCurrentPassword ? 'text' : 'password'}
                                            name="currentPassword"
                                            className="settings-input"
                                            value={passwordForm.currentPassword}
                                            onChange={handlePasswordChange}
                                            placeholder="Enter current password"
                                        />
                                        <button
                                            type="button"
                                            className="password-toggle-btn"
                                            onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                                        >
                                            {showCurrentPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                                        </button>
                                    </div>
                                </div>

                                <div className="settings-form-group">
                                    <label>New Password</label>
                                    <div className="password-input-wrapper">
                                        <input
                                            type={showNewPassword ? 'text' : 'password'}
                                            name="newPassword"
                                            className="settings-input"
                                            value={passwordForm.newPassword}
                                            onChange={handlePasswordChange}
                                            placeholder="Enter new password"
                                        />
                                        <button
                                            type="button"
                                            className="password-toggle-btn"
                                            onClick={() => setShowNewPassword(!showNewPassword)}
                                        >
                                            {showNewPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                                        </button>
                                    </div>
                                    {passwordForm.newPassword && (
                                        <div className="password-strength">
                                            <div className="strength-bar">
                                                <div className={`strength-fill ${strength.level}`}></div>
                                            </div>
                                            <span className={`strength-label ${strength.level}`}>{strength.label}</span>
                                        </div>
                                    )}
                                </div>

                                <div className="settings-form-group">
                                    <label>Confirm New Password</label>
                                    <div className="password-input-wrapper">
                                        <input
                                            type={showConfirmPassword ? 'text' : 'password'}
                                            name="confirmPassword"
                                            className="settings-input"
                                            value={passwordForm.confirmPassword}
                                            onChange={handlePasswordChange}
                                            placeholder="Confirm new password"
                                        />
                                        <button
                                            type="button"
                                            className="password-toggle-btn"
                                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                        >
                                            {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                                        </button>
                                    </div>
                                    {passwordForm.confirmPassword && passwordForm.newPassword !== passwordForm.confirmPassword && (
                                        <p className="field-error">Passwords do not match</p>
                                    )}
                                </div>
                            </div>

                            {passwordErrors.length > 0 && passwordForm.newPassword && (
                                <ul className="password-requirements">
                                    {passwordErrors.map((err, i) => (
                                        <li key={i}>{err}</li>
                                    ))}
                                </ul>
                            )}

                            <div className="form-actions">
                                <button
                                    className="btn-primary"
                                    onClick={handleChangePassword}
                                    disabled={!isPasswordFormValid || passwordSaving}
                                >
                                    <Lock size={16} style={{marginRight: '8px', display: 'inline'}}/>
                                    {passwordSaving ? 'Changing...' : 'Change Password'}
                                </button>
                            </div>
                        </section>
                    </div>
                )}

                {/* ═══════════ NOTIFICATIONS TAB ═══════════ */}
                {activeTab === 'notifications' && (
                    <div className="settings-form">
                        <section className="form-section">
                            <h2 className="section-title">Notification Preferences</h2>
                            <p className="settings-subtitle" style={{marginBottom: '1.5rem'}}>
                                Control how you receive notifications in the app.
                            </p>

                            <div className="notification-setting-row">
                                <div className="notification-setting-info">
                                    <Bell size={20} />
                                    <div>
                                        <p className="notification-setting-label">In-App Notifications</p>
                                        <p className="notification-setting-desc">
                                            {notificationsEnabled
                                                ? 'You will see badge counts and popup alerts for new notifications.'
                                                : 'Notification badges and popup alerts are hidden. You can still view notifications manually.'}
                                        </p>
                                    </div>
                                </div>
                                <button
                                    className={`toggle-switch ${notificationsEnabled ? 'active' : ''}`}
                                    onClick={handleNotificationToggle}
                                    role="switch"
                                    aria-checked={notificationsEnabled}
                                >
                                    <span className="toggle-knob"></span>
                                </button>
                            </div>
                        </section>
                    </div>
                )}

                {/* ═══════════ THEME TAB ═══════════ */}
                {activeTab === 'theme' && (
                    <div className="settings-form">
                        <section className="form-section">
                            <h2 className="section-title">Theme Preferences</h2>
                            <p className="settings-subtitle" style={{marginBottom: '1.5rem'}}>
                                Choose how UniLost looks to you. Select a single theme that will apply across all pages.
                            </p>
                            
                            <div className="theme-selection-grid">
                                <button
                                    className={`theme-option ${theme === 'light' ? 'active' : ''}`}
                                    onClick={() => setTheme('light')}
                                >
                                    <div className="theme-preview light">
                                        <div className="preview-header"></div>
                                        <div className="preview-body">
                                            <div className="preview-line"></div>
                                            <div className="preview-line short"></div>
                                        </div>
                                    </div>
                                    <span className="theme-label">Light Mode</span>
                                    {theme === 'light' && <div className="theme-check">✓</div>}
                                </button>

                                <button
                                    className={`theme-option ${theme === 'dark' ? 'active' : ''}`}
                                    onClick={() => setTheme('dark')}
                                >
                                    <div className="theme-preview dark">
                                        <div className="preview-header"></div>
                                        <div className="preview-body">
                                            <div className="preview-line"></div>
                                            <div className="preview-line short"></div>
                                        </div>
                                    </div>
                                    <span className="theme-label">Dark Mode</span>
                                    {theme === 'dark' && <div className="theme-check">✓</div>}
                                </button>
                            </div>
                        </section>
                    </div>
                )}
            </div>
            </div>
        </div>
    );
}

export default Settings;
