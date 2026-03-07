import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Users, Search, Eye, ShieldOff, ShieldCheck, ChevronLeft, ChevronRight, X, AlertTriangle } from 'lucide-react';
import Header from '../../components/Header';
import { mockAdminUsers, timeAgo } from '../../mockData/adminData';
import './AdminUsers.css';

const roleOptions = ['All', 'STUDENT', 'FACULTY', 'ADMIN'];
const statusOptions = ['All', 'Active', 'Banned'];

const roleLabels = { STUDENT: 'Student', FACULTY: 'Faculty', ADMIN: 'Admin' };

const AdminUsers = () => {
    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState('All');
    const [statusFilter, setStatusFilter] = useState('All');
    const [users, setUsers] = useState(mockAdminUsers);
    const [showBanModal, setShowBanModal] = useState(false);
    const [banTarget, setBanTarget] = useState(null);
    const [banReason, setBanReason] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const perPage = 10;

    const filtered = users.filter(user => {
        if (search) {
            const q = search.toLowerCase();
            if (!user.fullName.toLowerCase().includes(q) && !user.email.toLowerCase().includes(q)) return false;
        }
        if (roleFilter !== 'All' && user.role !== roleFilter) return false;
        if (statusFilter === 'Active' && user.isBanned) return false;
        if (statusFilter === 'Banned' && !user.isBanned) return false;
        return true;
    });

    const totalPages = Math.ceil(filtered.length / perPage);
    const paginated = filtered.slice((currentPage - 1) * perPage, currentPage * perPage);

    const handleBanToggle = (user) => {
        setBanTarget(user);
        setBanReason('');
        setShowBanModal(true);
    };

    const confirmBanToggle = () => {
        if (banTarget) {
            setUsers(prev => prev.map(u => u.id === banTarget.id ? { ...u, isBanned: !u.isBanned } : u));
        }
        setShowBanModal(false);
        setBanTarget(null);
    };

    return (
        <div className="admin-users-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Users</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Users size={24} />
                            <h1>User Management</h1>
                            <span className="count-badge">{filtered.length} users</span>
                        </div>
                    </div>

                    {/* Filters */}
                    <div className="filter-bar glass">
                        <div className="search-input">
                            <Search size={18} />
                            <input
                                type="text"
                                placeholder="Search by name, email, or student ID..."
                                value={search}
                                onChange={(e) => { setSearch(e.target.value); setCurrentPage(1); }}
                            />
                        </div>
                        <select value={roleFilter} onChange={(e) => { setRoleFilter(e.target.value); setCurrentPage(1); }}>
                            {roleOptions.map(o => <option key={o} value={o}>{o === 'All' ? 'All Roles' : roleLabels[o] || o}</option>)}
                        </select>
                        <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}>
                            {statusOptions.map(o => <option key={o} value={o}>{o === 'All' ? 'All Statuses' : o}</option>)}
                        </select>
                    </div>

                    {/* Table */}
                    <div className="admin-table glass">
                        <table>
                            <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Email</th>
                                    <th>School</th>
                                    <th>Role</th>
                                    <th>Karma</th>
                                    <th>Status</th>
                                    <th>Joined</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {paginated.map((user, i) => (
                                    <tr key={user.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                        <td>
                                            <div className="user-cell">
                                                <div className="user-avatar">{user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2)}</div>
                                                <span className="user-name">{user.fullName}</span>
                                            </div>
                                        </td>
                                        <td className="td-email">{user.email}</td>
                                        <td>{user.campus?.id || '-'}</td>
                                        <td>
                                            <span className={`role-chip ${user.role.toLowerCase().replace('_', '-')}`}>
                                                {roleLabels[user.role]}
                                            </span>
                                        </td>
                                        <td className="td-karma">{user.karmaScore}</td>
                                        <td>
                                            <span className={`status-dot ${user.isBanned ? 'banned' : 'active'}`}>
                                                {user.isBanned ? 'Banned' : 'Active'}
                                            </span>
                                        </td>
                                        <td className="td-date">{timeAgo(user.createdAt)}</td>
                                        <td className="td-actions">
                                            <button className="action-btn" title="View Profile">
                                                <Eye size={16} />
                                            </button>
                                            {user.role === 'STUDENT' && (
                                                <button
                                                    className={`action-btn ${user.isBanned ? 'success' : 'danger'}`}
                                                    title={user.isBanned ? 'Unban' : 'Ban'}
                                                    onClick={() => handleBanToggle(user)}
                                                >
                                                    {user.isBanned ? <ShieldCheck size={16} /> : <ShieldOff size={16} />}
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                        {paginated.length === 0 && (
                            <div className="table-empty">
                                <Users size={40} />
                                <p>No users match your filters</p>
                            </div>
                        )}
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="pagination">
                            <span>Showing {(currentPage - 1) * perPage + 1}-{Math.min(currentPage * perPage, filtered.length)} of {filtered.length}</span>
                            <div className="pagination-btns">
                                <button disabled={currentPage === 1} onClick={() => setCurrentPage(p => p - 1)}>
                                    <ChevronLeft size={16} /> Prev
                                </button>
                                <button disabled={currentPage === totalPages} onClick={() => setCurrentPage(p => p + 1)}>
                                    Next <ChevronRight size={16} />
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Ban/Unban Modal */}
                    {showBanModal && banTarget && (
                        <div className="modal-overlay" onClick={() => setShowBanModal(false)}>
                            <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                                <div className="modal-header">
                                    <AlertTriangle size={20} color={banTarget.isBanned ? '#10b981' : '#ef4444'} />
                                    <h3>{banTarget.isBanned ? 'Unban User' : 'Ban User'}</h3>
                                    <button className="modal-close" onClick={() => setShowBanModal(false)}>
                                        <X size={18} />
                                    </button>
                                </div>
                                <div className="modal-body">
                                    <div className="ban-user-summary">
                                        <div className="user-avatar lg">{banTarget.fullName.split(' ').map(n => n[0]).join('').substring(0, 2)}</div>
                                        <div>
                                            <strong>{banTarget.fullName}</strong>
                                            <span>{banTarget.email}</span>
                                            <span>{banTarget.campus?.id || '-'} &middot; Karma: {banTarget.karmaScore}</span>
                                        </div>
                                    </div>
                                    <label>Reason</label>
                                    <textarea
                                        value={banReason}
                                        onChange={(e) => setBanReason(e.target.value)}
                                        placeholder={banTarget.isBanned ? 'Reason for unbanning...' : 'Reason for banning this user...'}
                                        rows={3}
                                    />
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowBanModal(false)}>Cancel</button>
                                    <button
                                        className={banTarget.isBanned ? 'btn-success' : 'btn-danger'}
                                        onClick={confirmBanToggle}
                                    >
                                        {banTarget.isBanned ? 'Confirm Unban' : 'Confirm Ban'}
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

export default AdminUsers;
