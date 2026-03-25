import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Users, Search, ShieldOff, ShieldCheck, ChevronLeft, ChevronRight, X, AlertTriangle, Loader2, Download, CheckSquare } from 'lucide-react';
import Header from '../../components/Header';
import Dropdown from '../../components/ui/Dropdown';
import adminService from '../../services/adminService';
import './AdminUsers.css';

const roleOptions = ['All', 'STUDENT', 'ADMIN'];
const statusOptions = ['All', 'ACTIVE', 'SUSPENDED'];

const roleLabels = { STUDENT: 'Student', ADMIN: 'Admin' };

const avatarColors = [
    '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e',
    '#f97316', '#eab308', '#22c55e', '#14b8a6',
    '#06b6d4', '#3b82f6',
];

const getAvatarColor = (name) => {
    if (!name) return avatarColors[0];
    let hash = 0;
    for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
    return avatarColors[Math.abs(hash) % avatarColors.length];
};

const AdminUsers = () => {
    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState('All');
    const [statusFilter, setStatusFilter] = useState('All');
    const [users, setUsers] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showBanModal, setShowBanModal] = useState(false);
    const [banTarget, setBanTarget] = useState(null);
    const [banReason, setBanReason] = useState('');
    const [actionLoading, setActionLoading] = useState(false);
    const [exportLoading, setExportLoading] = useState(false);
    const [selectedIds, setSelectedIds] = useState(new Set());
    const [bulkLoading, setBulkLoading] = useState(false);
    const perPage = 10;

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        setError('');
        const params = { page: currentPage, size: perPage };
        if (search) params.keyword = search;
        if (roleFilter !== 'All') params.role = roleFilter;
        if (statusFilter !== 'All') params.accountStatus = statusFilter;

        const result = await adminService.getCampusUsers(params);
        if (result.success) {
            setUsers(result.data.content || []);
            setTotalElements(result.data.totalElements || 0);
        } else {
            setError(result.error);
        }
        setLoading(false);
    }, [currentPage, search, roleFilter, statusFilter]);

    useEffect(() => { fetchUsers(); }, [fetchUsers]);

    const totalPages = Math.ceil(totalElements / perPage);

    const handleBanToggle = (user) => {
        setBanTarget(user);
        setBanReason('');
        setShowBanModal(true);
    };

    const confirmBanToggle = async () => {
        if (!banTarget) return;
        setActionLoading(true);
        const newStatus = banTarget.accountStatus === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED';
        const result = await adminService.updateUserStatus(banTarget.id, newStatus);
        if (result.success) {
            fetchUsers();
        } else {
            setError(result.error);
        }
        setActionLoading(false);
        setShowBanModal(false);
        setBanTarget(null);
    };

    const timeAgo = (dateStr) => {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        const now = new Date();
        const diff = Math.floor((now - date) / 1000);
        if (diff < 60) return 'just now';
        if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
        return `${Math.floor(diff / 86400)}d ago`;
    };

    const handleExport = async () => {
        setExportLoading(true);
        await adminService.exportUsers();
        setExportLoading(false);
    };

    const studentUsers = users.filter(u => u.role === 'STUDENT');

    const toggleSelect = (id) => {
        setSelectedIds(prev => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const toggleSelectAll = () => {
        if (selectedIds.size === studentUsers.length) {
            setSelectedIds(new Set());
        } else {
            setSelectedIds(new Set(studentUsers.map(u => u.id)));
        }
    };

    const handleBulkUserStatus = async (status) => {
        setBulkLoading(true);
        const result = await adminService.bulkUpdateUserStatus([...selectedIds], status);
        if (result.success) {
            setSelectedIds(new Set());
            fetchUsers();
        } else {
            setError(result.error);
        }
        setBulkLoading(false);
    };

    const isBanned = (user) => user.accountStatus === 'SUSPENDED';

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
                            <span className="count-badge">{totalElements} users</span>
                        </div>
                    </div>

                    {/* Filters */}
                    <div className="filter-bar glass">
                        <div className="search-input">
                            <Search size={18} />
                            <input
                                type="text"
                                placeholder="Search by name or email..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter') { setCurrentPage(0); fetchUsers(); }
                                }}
                            />
                        </div>
                        <Dropdown label={roleFilter === 'All' ? 'All Roles' : (roleLabels[roleFilter] || roleFilter)} align="left" width={160}>
                            {({ close }) => roleOptions.map(o => (
                                <Dropdown.Item key={o} className={roleFilter === o ? 'active' : ''} onClick={() => { setRoleFilter(o); setCurrentPage(0); close(); }}>
                                    {o === 'All' ? 'All Roles' : roleLabels[o] || o}
                                </Dropdown.Item>
                            ))}
                        </Dropdown>
                        <Dropdown label={statusFilter === 'All' ? 'All Statuses' : statusFilter} align="left" width={170}>
                            {({ close }) => statusOptions.map(o => (
                                <Dropdown.Item key={o} className={statusFilter === o ? 'active' : ''} onClick={() => { setStatusFilter(o); setCurrentPage(0); close(); }}>
                                    {o === 'All' ? 'All Statuses' : o}
                                </Dropdown.Item>
                            ))}
                        </Dropdown>
                        <button className="btn-export" onClick={handleExport} disabled={exportLoading}>
                            <Download size={16} /> {exportLoading ? 'Exporting...' : 'Export CSV'}
                        </button>
                    </div>

                    {/* Bulk Toolbar */}
                    {selectedIds.size > 0 && (
                        <div className="bulk-toolbar glass">
                            <CheckSquare size={18} />
                            <span>{selectedIds.size} user(s) selected</span>
                            <button className="bulk-btn danger" onClick={() => handleBulkUserStatus('SUSPENDED')} disabled={bulkLoading}>Suspend</button>
                            <button className="bulk-btn" onClick={() => handleBulkUserStatus('ACTIVE')} disabled={bulkLoading}>Reactivate</button>
                            <button className="bulk-btn secondary" onClick={() => setSelectedIds(new Set())}>Clear</button>
                        </div>
                    )}

                    {error && (
                        <div className="error-state">
                            <AlertTriangle size={18} />
                            <span>{error}</span>
                        </div>
                    )}

                    {/* Table */}
                    <div className="admin-table glass">
                        {loading ? (
                            <div className="table-empty">
                                <Loader2 size={32} className="spinner" />
                                <p>Loading users...</p>
                            </div>
                        ) : (
                            <table>
                                <thead>
                                    <tr>
                                        <th className="checkbox-cell">
                                            <input type="checkbox" checked={studentUsers.length > 0 && selectedIds.size === studentUsers.length} onChange={toggleSelectAll} />
                                        </th>
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
                                    {users.map((user, i) => (
                                        <tr key={user.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                            <td className="checkbox-cell">
                                                {user.role === 'STUDENT' ? (
                                                    <input type="checkbox" checked={selectedIds.has(user.id)} onChange={() => toggleSelect(user.id)} />
                                                ) : null}
                                            </td>
                                            <td>
                                                <div className="user-cell">
                                                    <div className="user-avatar" style={{ background: getAvatarColor(user.fullName) }}>{user.fullName?.split(' ').map(n => n[0]).join('').substring(0, 2)}</div>
                                                    <span className="user-name">{user.fullName}</span>
                                                </div>
                                            </td>
                                            <td className="td-email">{user.email}</td>
                                            <td>{user.campus?.name || '-'}</td>
                                            <td>
                                                <span className={`role-chip ${user.role?.toLowerCase()}`}>
                                                    {roleLabels[user.role] || user.role}
                                                </span>
                                            </td>
                                            <td className="td-karma">{user.karmaScore}</td>
                                            <td>
                                                <span className={`status-dot ${isBanned(user) ? 'banned' : 'active'}`}>
                                                    {isBanned(user) ? 'Suspended' : 'Active'}
                                                </span>
                                            </td>
                                            <td className="td-date">{timeAgo(user.createdAt)}</td>
                                            <td className="td-actions">
                                                {user.role === 'STUDENT' && (
                                                    <button
                                                        className={`action-btn ${isBanned(user) ? 'success' : 'danger'}`}
                                                        title={isBanned(user) ? 'Reactivate' : 'Suspend'}
                                                        onClick={() => handleBanToggle(user)}
                                                    >
                                                        {isBanned(user) ? <ShieldCheck size={16} /> : <ShieldOff size={16} />}
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}

                        {!loading && users.length === 0 && (
                            <div className="table-empty">
                                <Users size={40} />
                                <p>No users match your filters</p>
                            </div>
                        )}
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="pagination">
                            <span>Showing {currentPage * perPage + 1}-{Math.min((currentPage + 1) * perPage, totalElements)} of {totalElements}</span>
                            <div className="pagination-btns">
                                <button disabled={currentPage === 0} onClick={() => setCurrentPage(p => p - 1)}>
                                    <ChevronLeft size={16} /> Prev
                                </button>
                                <button disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage(p => p + 1)}>
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
                                    <AlertTriangle size={20} color={isBanned(banTarget) ? '#10b981' : '#ef4444'} />
                                    <h3>{isBanned(banTarget) ? 'Reactivate User' : 'Suspend User'}</h3>
                                    <button className="modal-close" onClick={() => setShowBanModal(false)}>
                                        <X size={18} />
                                    </button>
                                </div>
                                <div className="modal-body">
                                    <div className="ban-user-summary">
                                        <div className="user-avatar lg" style={{ background: getAvatarColor(banTarget.fullName) }}>{banTarget.fullName?.split(' ').map(n => n[0]).join('').substring(0, 2)}</div>
                                        <div>
                                            <strong>{banTarget.fullName}</strong>
                                            <span>{banTarget.email}</span>
                                            <span>{banTarget.campus?.name || '-'} &middot; Karma: {banTarget.karmaScore}</span>
                                        </div>
                                    </div>
                                    <label>Reason</label>
                                    <textarea
                                        value={banReason}
                                        onChange={(e) => setBanReason(e.target.value)}
                                        placeholder={isBanned(banTarget) ? 'Reason for reactivating...' : 'Reason for suspending this user...'}
                                        rows={3}
                                    />
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowBanModal(false)}>Cancel</button>
                                    <button
                                        className={isBanned(banTarget) ? 'btn-success' : 'btn-danger'}
                                        onClick={confirmBanToggle}
                                        disabled={actionLoading}
                                    >
                                        {actionLoading ? 'Processing...' : (isBanned(banTarget) ? 'Confirm Reactivate' : 'Confirm Suspend')}
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
