import { useState } from 'react';
import { Star, Building2, Edit3, UserPlus, X, Check, ToggleLeft, ToggleRight } from 'lucide-react';
import Header from '../../components/Header';
import { mockSchools, mockCampusStats } from '../../mockData/adminData';
import './SuperAdminPanel.css';

const SuperAdminPanel = () => {
    const [schools, setSchools] = useState(mockSchools);
    const [showEditModal, setShowEditModal] = useState(false);
    const [editTarget, setEditTarget] = useState(null);
    const [editForm, setEditForm] = useState({ name: '', shortName: '', emailDomain: '' });

    // Create admin form
    const [adminForm, setAdminForm] = useState({ firstName: '', lastName: '', email: '', schoolId: '', password: '' });
    const [adminCreated, setAdminCreated] = useState(false);

    const handleEdit = (school) => {
        setEditTarget(school);
        setEditForm({ name: school.name, shortName: school.shortName, emailDomain: school.emailDomain });
        setShowEditModal(true);
    };

    const saveEdit = () => {
        if (editTarget) {
            setSchools(prev => prev.map(s => s.id === editTarget.id ? { ...s, ...editForm } : s));
        }
        setShowEditModal(false);
    };

    const toggleActive = (id) => {
        setSchools(prev => prev.map(s => s.id === id ? { ...s, isActive: !s.isActive } : s));
    };

    const handleCreateAdmin = (e) => {
        e.preventDefault();
        setAdminCreated(true);
        setTimeout(() => {
            setAdminCreated(false);
            setAdminForm({ firstName: '', lastName: '', email: '', schoolId: '', password: '' });
        }, 3000);
    };

    return (
        <div className="superadmin-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <div className="page-header-top">
                            <Star size={28} />
                            <h1>Super Admin Panel</h1>
                            <span className="role-badge super">Super Admin</span>
                        </div>
                        <p>Cross-campus management and system administration</p>
                    </div>

                    {/* Cross-Campus Statistics */}
                    <h2 className="section-title">Cross-Campus Statistics</h2>
                    <div className="campus-grid">
                        {mockCampusStats.map((campus, i) => (
                            <div className="campus-card glass" key={campus.id} style={{ animationDelay: `${i * 0.05}s` }}>
                                <div className="campus-header">
                                    <Building2 size={18} />
                                    <div>
                                        <h3>{campus.shortName}</h3>
                                        <span className="campus-fullname">{campus.name}</span>
                                    </div>
                                </div>
                                <div className="campus-stats">
                                    <div className="campus-stat">
                                        <span className="cs-value">{campus.studentCount}</span>
                                        <span className="cs-label">Users</span>
                                    </div>
                                    <div className="campus-stat">
                                        <span className="cs-value">{campus.itemCount}</span>
                                        <span className="cs-label">Items</span>
                                    </div>
                                    <div className="campus-stat">
                                        <span className="cs-value">{campus.recoveryRate}%</span>
                                        <span className="cs-label">Recovery</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* School Management Table */}
                    <h2 className="section-title">School Management</h2>
                    <div className="admin-table glass">
                        <table>
                            <thead>
                                <tr>
                                    <th>School Name</th>
                                    <th>Short Name</th>
                                    <th>Email Domain</th>
                                    <th>Students</th>
                                    <th>Items</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {schools.map((school, i) => (
                                    <tr key={school.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                        <td className="td-school-name">{school.name}</td>
                                        <td><strong>{school.shortName}</strong></td>
                                        <td className="td-domain">@{school.emailDomain}</td>
                                        <td>{school.studentCount}</td>
                                        <td>{school.itemCount}</td>
                                        <td>
                                            <button
                                                className={`toggle-btn ${school.isActive ? 'active' : 'inactive'}`}
                                                onClick={() => toggleActive(school.id)}
                                            >
                                                {school.isActive ? <ToggleRight size={20} /> : <ToggleLeft size={20} />}
                                                {school.isActive ? 'Active' : 'Inactive'}
                                            </button>
                                        </td>
                                        <td>
                                            <button className="action-btn" title="Edit" onClick={() => handleEdit(school)}>
                                                <Edit3 size={16} />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Create Admin Account */}
                    <h2 className="section-title">Create Admin Account</h2>
                    <div className="create-admin-card glass">
                        {adminCreated ? (
                            <div className="admin-success">
                                <div className="success-icon">
                                    <Check size={28} />
                                </div>
                                <h3>Admin Account Created!</h3>
                                <p>The new admin will receive login credentials at their email.</p>
                            </div>
                        ) : (
                            <form onSubmit={handleCreateAdmin}>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label>First Name</label>
                                        <input
                                            type="text"
                                            value={adminForm.firstName}
                                            onChange={(e) => setAdminForm(f => ({ ...f, firstName: e.target.value }))}
                                            placeholder="First name"
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Last Name</label>
                                        <input
                                            type="text"
                                            value={adminForm.lastName}
                                            onChange={(e) => setAdminForm(f => ({ ...f, lastName: e.target.value }))}
                                            placeholder="Last name"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label>Email</label>
                                    <input
                                        type="email"
                                        value={adminForm.email}
                                        onChange={(e) => setAdminForm(f => ({ ...f, email: e.target.value }))}
                                        placeholder="admin@school.edu"
                                        required
                                    />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label>Assign to School</label>
                                        <select
                                            value={adminForm.schoolId}
                                            onChange={(e) => setAdminForm(f => ({ ...f, schoolId: e.target.value }))}
                                            required
                                        >
                                            <option value="">Select school...</option>
                                            {mockSchools.map(s => (
                                                <option key={s.id} value={s.id}>{s.shortName} - {s.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label>Temporary Password</label>
                                        <input
                                            type="text"
                                            value={adminForm.password}
                                            onChange={(e) => setAdminForm(f => ({ ...f, password: e.target.value }))}
                                            placeholder="Temporary password"
                                            required
                                        />
                                    </div>
                                </div>
                                <button type="submit" className="btn-primary">
                                    <UserPlus size={18} />
                                    Create Admin Account
                                </button>
                            </form>
                        )}
                    </div>

                    {/* Edit School Modal */}
                    {showEditModal && editTarget && (
                        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                            <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                                <div className="modal-header">
                                    <Edit3 size={20} color="var(--color-primary)" />
                                    <h3>Edit School</h3>
                                    <button className="modal-close" onClick={() => setShowEditModal(false)}>
                                        <X size={18} />
                                    </button>
                                </div>
                                <div className="modal-body">
                                    <div className="form-group">
                                        <label>School Name</label>
                                        <input
                                            type="text"
                                            value={editForm.name}
                                            onChange={(e) => setEditForm(f => ({ ...f, name: e.target.value }))}
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Short Name</label>
                                        <input
                                            type="text"
                                            value={editForm.shortName}
                                            onChange={(e) => setEditForm(f => ({ ...f, shortName: e.target.value }))}
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Email Domain</label>
                                        <input
                                            type="text"
                                            value={editForm.emailDomain}
                                            onChange={(e) => setEditForm(f => ({ ...f, emailDomain: e.target.value }))}
                                        />
                                    </div>
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowEditModal(false)}>Cancel</button>
                                    <button className="btn-primary" onClick={saveEdit}>Save Changes</button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SuperAdminPanel;
