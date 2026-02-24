import { useState, useEffect } from 'react';
import Header from '../../components/Header';
import authService from '../../services/authService';
import './Profile.css';

function Profile() {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const currentUser = authService.getCurrentUser();
        if (currentUser) {
            setUser(currentUser);
        }
    }, []);

    if (!user) {
        return <div className="loading">Loading profile...</div>;
    }

    const getInitials = () => {
        return `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase();
    };

    const roleBadge = () => {
        const role = user.role || 'STUDENT';
        const labels = {
            'STUDENT': 'Student',
            'ADMIN': 'Campus Admin',
            'SUPER_ADMIN': 'Super Admin'
        };
        return labels[role] || 'Student';
    };

    return (
        <div className="profile-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="profile-header glass">
                        <div className="profile-cover"></div>
                        <div className="profile-info-container">
                            <div className="avatar-wrapper">
                                <div className="avatar">
                                    {getInitials()}
                                </div>
                                <div className="verification-badge" title="Verified Student">
                                    <span className="badge-icon">✓</span>
                                </div>
                            </div>

                            <div className="user-details">
                                <h1>{user.firstName} {user.lastName}</h1>
                                <p className="school-name">{user.school?.name || 'University not detected'}</p>
                                <p className="join-date">Member since {new Date().getFullYear()}</p>
                            </div>

                            <div className="profile-actions">
                                <button className="btn-secondary">Edit Profile</button>
                            </div>
                        </div>
                    </div>

                    <div className="profile-grid">
                        {/* Sidebar / Info */}
                        <div className="profile-sidebar glass">
                            <h3>Student Info</h3>
                            <div className="info-row">
                                <span className="label">Email</span>
                                <span className="value">{user.email}</span>
                            </div>
                            <div className="info-row">
                                <span className="label">Student ID</span>
                                <span className="value">{user.studentIdNumber || 'N/A'}</span>
                            </div>
                            <div className="info-row">
                                <span className="label">School</span>
                                <span className="value">{user.school?.name || 'Not Enrolled'}</span>
                            </div>
                            <div className="info-row">
                                <span className="label">Role</span>
                                <span className="status-tag verified">{roleBadge()}</span>
                            </div>
                            <div className="info-row">
                                <span className="label">Karma Score</span>
                                <span className="value">{user.karmaScore ?? 0} pts</span>
                            </div>
                        </div>

                        {/* Main Content / Items */}
                        <div className="profile-content glass">
                            <div className="tabs">
                                <button className="tab active">My Reports</button>
                                <button className="tab">Claims</button>
                                <button className="tab">Recovered</button>
                            </div>

                            <div className="empty-state">
                                <span className="empty-icon">🔍</span>
                                <h3>No reports yet</h3>
                                <p>Report a lost item or post a found item to help your campus community!</p>
                                <button className="btn-primary">Report Item</button>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default Profile;
