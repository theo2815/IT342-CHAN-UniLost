import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Gavel, Eye, ChevronLeft, ChevronRight, X, AlertTriangle } from 'lucide-react';
import Header from '../../components/Header';
import { mockAdminClaims, timeAgo } from '../../mockData/adminData';
import './AdminClaims.css';

const tabs = [
    { key: 'ALL', label: 'All' },
    { key: 'PENDING', label: 'Pending' },
    { key: 'APPROVED', label: 'Approved' },
    { key: 'REJECTED', label: 'Rejected' },
    { key: 'HANDED_OVER', label: 'Handed Over' },
];

const AdminClaims = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('ALL');
    const [claims, setClaims] = useState(mockAdminClaims);
    const [showOverrideModal, setShowOverrideModal] = useState(false);
    const [overrideTarget, setOverrideTarget] = useState(null);
    const [overrideReason, setOverrideReason] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const perPage = 10;

    const filtered = activeTab === 'ALL' ? claims : claims.filter(c => c.status === activeTab);
    const totalPages = Math.ceil(filtered.length / perPage);
    const paginated = filtered.slice((currentPage - 1) * perPage, currentPage * perPage);

    const getTabCount = (key) => {
        if (key === 'ALL') return claims.length;
        return claims.filter(c => c.status === key).length;
    };

    const handleOverride = (claim) => {
        setOverrideTarget(claim);
        setOverrideReason('');
        setShowOverrideModal(true);
    };

    const confirmOverride = () => {
        if (overrideTarget) {
            setClaims(prev => prev.map(c => c.id === overrideTarget.id ? { ...c, status: 'HANDED_OVER' } : c));
        }
        setShowOverrideModal(false);
        setOverrideTarget(null);
    };

    return (
        <div className="admin-claims-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Claims</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Gavel size={24} />
                            <h1>Claims Management</h1>
                            <span className="count-badge">{filtered.length} claims</span>
                        </div>
                    </div>

                    {/* Tabs */}
                    <div className="tabs">
                        {tabs.map(tab => (
                            <button
                                key={tab.key}
                                className={`tab ${activeTab === tab.key ? 'active' : ''}`}
                                onClick={() => { setActiveTab(tab.key); setCurrentPage(1); }}
                            >
                                {tab.label}
                                <span className="tab-count">{getTabCount(tab.key)}</span>
                            </button>
                        ))}
                    </div>

                    {/* Table */}
                    <div className="admin-table glass">
                        <table>
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Claimant</th>
                                    <th>Poster</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {paginated.map((claim, i) => (
                                    <tr key={claim.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                        <td>
                                            <div className="item-cell">
                                                <img src={claim.itemImageUrl} alt="" className="table-thumb" />
                                                <div className="item-cell-info">
                                                    <span className="item-cell-title">{claim.itemTitle}</span>
                                                    <span className={`type-badge-sm ${claim.itemType.toLowerCase()}`}>{claim.itemType}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div className="person-cell">
                                                <span className="person-name">{claim.claimantName}</span>
                                                <span className="person-email">{claim.claimantEmail}</span>
                                            </div>
                                        </td>
                                        <td className="td-poster-name">{claim.posterName}</td>
                                        <td>
                                            <span className={`status-badge ${claim.status.toLowerCase().replace('_', '-')}`}>
                                                {claim.status.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td className="td-date">{timeAgo(claim.createdAt)}</td>
                                        <td className="td-actions">
                                            <button className="action-btn" title="View Detail" onClick={() => navigate(`/claims/${claim.id}`)}>
                                                <Eye size={16} />
                                            </button>
                                            {claim.status === 'APPROVED' && (
                                                <button className="action-btn warning" title="Override Handover" onClick={() => handleOverride(claim)}>
                                                    <Gavel size={16} />
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                        {paginated.length === 0 && (
                            <div className="table-empty">
                                <Gavel size={40} />
                                <p>No claims match this filter</p>
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

                    {/* Override Modal */}
                    {showOverrideModal && overrideTarget && (
                        <div className="modal-overlay" onClick={() => setShowOverrideModal(false)}>
                            <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                                <div className="modal-header">
                                    <AlertTriangle size={20} color="#f59e0b" />
                                    <h3>Override Handover</h3>
                                    <button className="modal-close" onClick={() => setShowOverrideModal(false)}>
                                        <X size={18} />
                                    </button>
                                </div>
                                <div className="modal-body">
                                    <div className="override-summary">
                                        <img src={overrideTarget.itemImageUrl} alt="" />
                                        <div>
                                            <strong>{overrideTarget.itemTitle}</strong>
                                            <span>Claimant: {overrideTarget.claimantName}</span>
                                            <span>Poster: {overrideTarget.posterName}</span>
                                        </div>
                                    </div>
                                    <p className="override-warning">
                                        This will force-complete the handover without both parties confirming. Use only when necessary.
                                    </p>
                                    <label>Reason for override</label>
                                    <textarea
                                        value={overrideReason}
                                        onChange={(e) => setOverrideReason(e.target.value)}
                                        placeholder="Enter the reason for overriding this handover..."
                                        rows={3}
                                    />
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowOverrideModal(false)}>Cancel</button>
                                    <button className="btn-warning" onClick={confirmOverride}>Force Complete Handover</button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminClaims;
