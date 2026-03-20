import { useState } from 'react';
import { User, Lock, Bell, Palette, Camera, Save, X, Edit2 } from 'lucide-react';
import authService from '../../services/authService';
import userService from '../../services/userService';
import Header from '../../components/Header';
import { useTheme } from '../../context/ThemeContext';
import './Settings.css';

function Settings() {
    const { theme, setTheme } = useTheme();
    const [activeTab, setActiveTab] = useState('profile');
    const [isEditing, setIsEditing] = useState(false);
    const [saving, setSaving] = useState(false);
    const [saveMessage, setSaveMessage] = useState('');
    const [user, setUser] = useState(() => {
        const currentUser = authService.getCurrentUser() || {};
        return {
            id: currentUser.id || '',
            fullName: currentUser.fullName || '',
            email: currentUser.email || '',
            campus: currentUser.campus?.name || 'Unknown School',
            karmaScore: currentUser.karmaScore || 0,
            role: currentUser.role || 'STUDENT',
        };
    });

    const [originalUser, setOriginalUser] = useState(user);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUser(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleEdit = () => {
        setIsEditing(true);
    };

    const handleCancel = () => {
        setUser(originalUser);
        setIsEditing(false);
    };

    const handleSave = async () => {
        setSaving(true);
        setSaveMessage('');
        const result = await userService.updateProfile(user.id, {
            fullName: user.fullName,
        });
        if (result.success) {
            const storedUser = authService.getCurrentUser();
            if (storedUser) {
                storedUser.fullName = user.fullName;
                localStorage.setItem('user', JSON.stringify(storedUser));
            }
            setOriginalUser(user);
            setIsEditing(false);
            setSaveMessage('Profile updated successfully!');
        } else {
            setSaveMessage(result.error);
        }
        setSaving(false);
    };

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

                {activeTab === 'profile' && (
                    <div className="settings-form">
                        
                        {/* Profile Picture Section */}
                        <div className="profile-avatar-section">
                            <div className="avatar-large">
                                {user.fullName?.split(' ').map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase()}
                            </div>
                            <div className="avatar-actions">
                                {isEditing && (
                                    <button className="btn-secondary">
                                        <Camera size={16} style={{marginRight: '8px'}}/>
                                        Change Photo
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* Account Details Section */}
                        <section className="form-section">
                            <h2 className="section-title">Account Details</h2>
                            {saveMessage && (
                                <p style={{color: saveMessage.includes('success') ? 'var(--color-success)' : 'var(--color-danger)', marginBottom: '1rem'}}>
                                    {saveMessage}
                                </p>
                            )}
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

                {activeTab !== 'profile' && activeTab !== 'theme' && (
                    <div className="form-section">
                        <p style={{color: 'var(--color-text-secondary)'}}>
                            This section is under development.
                        </p>
                    </div>
                )}
            </div>
            </div>
        </div>
    );
}

export default Settings;
