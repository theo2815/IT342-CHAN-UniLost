import { useState, useEffect } from 'react';
import { Building2, Edit3, Plus, X, Loader } from 'lucide-react';
import { Link } from 'react-router-dom';
import Header from '../../../shared/layout/Header/Header';
import adminService from '../adminService';
import campusService from '../../../shared/services/campusService';
import api from '../../../shared/services/api';
import './CampusManagement.css';

const CampusManagement = () => {
    const [campusStats, setCampusStats] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [showEditModal, setShowEditModal] = useState(false);
    const [editTarget, setEditTarget] = useState(null);
    const [editForm, setEditForm] = useState({ name: '', domainWhitelist: '' });
    const [editLoading, setEditLoading] = useState(false);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [createForm, setCreateForm] = useState({
        name: '', universityCode: '', campusName: '', shortLabel: '', address: '', domainWhitelist: '',
    });
    const [createLoading, setCreateLoading] = useState(false);

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        setLoading(true);
        setError('');
        const result = await adminService.getCrossCampusStats();
        if (result.success) {
            setCampusStats(result.data);
        } else {
            setError(result.error);
        }
        setLoading(false);
    };

    const handleEdit = (campus) => {
        setEditTarget(campus);
        setEditForm({ name: campus.name, domainWhitelist: campus.domainWhitelist });
        setShowEditModal(true);
    };

    const saveEdit = async () => {
        if (!editTarget) return;
        setEditLoading(true);
        try {
            await api.put(`/campuses/${editTarget.id}`, {
                name: editForm.name,
                domainWhitelist: editForm.domainWhitelist,
            });
            setShowEditModal(false);
            fetchStats();
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to update campus');
        }
        setEditLoading(false);
    };

    const openCreateModal = () => {
        setCreateForm({ name: '', universityCode: '', campusName: '', shortLabel: '', address: '', domainWhitelist: '' });
        setShowCreateModal(true);
    };

    const saveCreate = async () => {
        if (!createForm.name || !createForm.domainWhitelist) {
            setError('School name and email domain are required');
            return;
        }
        setCreateLoading(true);
        const result = await campusService.createCampus(createForm);
        if (result.success) {
            setShowCreateModal(false);
            fetchStats();
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Failed to create school');
        }
        setCreateLoading(false);
    };

    return (
        <div className="campus-mgmt-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Campuses</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Building2 size={28} />
                            <h1>Campus Management</h1>
                            <button className="btn-add-school" onClick={openCreateModal}>
                                <Plus size={16} /> Add School
                            </button>
                        </div>
                        <p>Cross-campus statistics and school settings</p>
                    </div>

                    {error && (
                        <div className="sa-error-banner">{error}</div>
                    )}

                    {loading ? (
                        <div className="sa-loading">
                            <Loader size={24} className="spin" />
                            <span>Loading campus data...</span>
                        </div>
                    ) : (
                        <>
                            {/* Cross-Campus Statistics */}
                            <h2 className="section-title">Campus Overview</h2>
                            <div className="campus-grid">
                                {campusStats.map((campus, i) => (
                                    <div className="campus-card glass" key={campus.id} style={{ animationDelay: `${i * 0.05}s` }}>
                                        <div className="campus-header">
                                            <Building2 size={18} />
                                            <div>
                                                <h3>{campus.id}</h3>
                                                <span className="campus-fullname">{campus.name}</span>
                                            </div>
                                        </div>
                                        <div className="campus-stats">
                                            <div className="campus-stat">
                                                <span className="cs-value">{campus.userCount}</span>
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
                            <h2 className="section-title">School Settings</h2>
                            <div className="admin-table glass">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>School Name</th>
                                            <th>Campus ID</th>
                                            <th>Email Domain</th>
                                            <th>Users</th>
                                            <th>Items</th>
                                            <th>Recovery</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {campusStats.map((campus, i) => (
                                            <tr key={campus.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                                <td className="td-school-name">{campus.name}</td>
                                                <td><strong>{campus.id}</strong></td>
                                                <td className="td-domain">@{campus.domainWhitelist}</td>
                                                <td>{campus.userCount}</td>
                                                <td>{campus.itemCount}</td>
                                                <td>{campus.recoveryRate}%</td>
                                                <td>
                                                    <button className="action-btn" title="Edit" onClick={() => handleEdit(campus)}>
                                                        <Edit3 size={16} />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}

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
                                        <label>Email Domain</label>
                                        <input
                                            type="text"
                                            value={editForm.domainWhitelist}
                                            onChange={(e) => setEditForm(f => ({ ...f, domainWhitelist: e.target.value }))}
                                        />
                                    </div>
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowEditModal(false)}>Cancel</button>
                                    <button className="btn-primary" onClick={saveEdit} disabled={editLoading}>
                                        {editLoading ? 'Saving...' : 'Save Changes'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Create School Modal */}
                    {showCreateModal && (
                        <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                            <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                                <div className="modal-header">
                                    <Plus size={20} color="var(--color-primary)" />
                                    <h3>Add New School</h3>
                                    <button className="modal-close" onClick={() => setShowCreateModal(false)}>
                                        <X size={18} />
                                    </button>
                                </div>
                                <div className="modal-body">
                                    <div className="form-group">
                                        <label>School Name *</label>
                                        <input
                                            type="text"
                                            placeholder="e.g. University of San Carlos - Talamban Campus"
                                            value={createForm.name}
                                            onChange={(e) => setCreateForm(f => ({ ...f, name: e.target.value }))}
                                        />
                                    </div>
                                    <div className="form-row">
                                        <div className="form-group">
                                            <label>University Code</label>
                                            <input
                                                type="text"
                                                placeholder="e.g. USC"
                                                value={createForm.universityCode}
                                                onChange={(e) => setCreateForm(f => ({ ...f, universityCode: e.target.value }))}
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Campus Name</label>
                                            <input
                                                type="text"
                                                placeholder="e.g. Talamban Campus"
                                                value={createForm.campusName}
                                                onChange={(e) => setCreateForm(f => ({ ...f, campusName: e.target.value }))}
                                            />
                                        </div>
                                    </div>
                                    <div className="form-row">
                                        <div className="form-group">
                                            <label>Short Label</label>
                                            <input
                                                type="text"
                                                placeholder="e.g. USC Talamban"
                                                value={createForm.shortLabel}
                                                onChange={(e) => setCreateForm(f => ({ ...f, shortLabel: e.target.value }))}
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Email Domain *</label>
                                            <input
                                                type="text"
                                                placeholder="e.g. usc.edu.ph"
                                                value={createForm.domainWhitelist}
                                                onChange={(e) => setCreateForm(f => ({ ...f, domainWhitelist: e.target.value }))}
                                            />
                                        </div>
                                    </div>
                                    <div className="form-group">
                                        <label>Address</label>
                                        <input
                                            type="text"
                                            placeholder="e.g. Nasipit, Talamban, Cebu City"
                                            value={createForm.address}
                                            onChange={(e) => setCreateForm(f => ({ ...f, address: e.target.value }))}
                                        />
                                    </div>
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowCreateModal(false)}>Cancel</button>
                                    <button className="btn-primary" onClick={saveCreate} disabled={createLoading}>
                                        {createLoading ? 'Creating...' : 'Create School'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CampusManagement;
