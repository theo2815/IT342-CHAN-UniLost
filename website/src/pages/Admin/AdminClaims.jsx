import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Gavel, Eye, ChevronLeft, ChevronRight, Loader2, AlertTriangle } from 'lucide-react';
import Header from '../../components/Header';
import adminService from '../../services/adminService';
import './AdminClaims.css';

const tabs = [
    { key: 'ALL', label: 'All', param: null },
    { key: 'PENDING', label: 'Pending', param: 'PENDING' },
    { key: 'ACCEPTED', label: 'Accepted', param: 'ACCEPTED' },
    { key: 'COMPLETED', label: 'Completed', param: 'COMPLETED' },
    { key: 'REJECTED', label: 'Rejected', param: 'REJECTED' },
    { key: 'CANCELLED', label: 'Cancelled', param: 'CANCELLED' },
];

const AdminClaims = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('ALL');
    const [claims, setClaims] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const perPage = 10;

    const fetchClaims = useCallback(async () => {
        setLoading(true);
        setError('');
        const params = { page: currentPage, size: perPage };
        const tab = tabs.find(t => t.key === activeTab);
        if (tab?.param) params.status = tab.param;

        const result = await adminService.getCampusClaims(params);
        if (result.success) {
            setClaims(result.data.content || []);
            setTotalElements(result.data.totalElements || 0);
        } else {
            setError(result.error);
        }
        setLoading(false);
    }, [currentPage, activeTab]);

    useEffect(() => { fetchClaims(); }, [fetchClaims]);

    const totalPages = Math.ceil(totalElements / perPage);

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
                            <span className="count-badge">{totalElements} claims</span>
                        </div>
                    </div>

                    {/* Tabs */}
                    <div className="tabs">
                        {tabs.map(tab => (
                            <button
                                key={tab.key}
                                className={`tab ${activeTab === tab.key ? 'active' : ''}`}
                                onClick={() => { setActiveTab(tab.key); setCurrentPage(0); }}
                            >
                                {tab.label}
                            </button>
                        ))}
                    </div>

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
                                <p>Loading claims...</p>
                            </div>
                        ) : (
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
                                    {claims.map((claim, i) => (
                                        <tr key={claim.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                            <td>
                                                <div className="item-cell">
                                                    <img src={claim.itemImageUrl || '/placeholder.png'} alt="" className="table-thumb" />
                                                    <div className="item-cell-info">
                                                        <span className="item-cell-title">{claim.itemTitle || '-'}</span>
                                                        <span className={`type-badge-sm ${claim.itemType?.toLowerCase()}`}>{claim.itemType}</span>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="person-cell">
                                                    <span className="person-name">{claim.claimantName || '-'}</span>
                                                    <span className="person-email">{claim.claimantSchool || '-'}</span>
                                                </div>
                                            </td>
                                            <td className="td-poster-name">{claim.finderName || '-'}</td>
                                            <td>
                                                <span className={`status-badge ${claim.status?.toLowerCase().replace(/_/g, '-')}`}>
                                                    {claim.status?.replace(/_/g, ' ')}
                                                </span>
                                            </td>
                                            <td className="td-date">{timeAgo(claim.createdAt)}</td>
                                            <td className="td-actions">
                                                <button className="action-btn" title="View Detail" onClick={() => navigate(`/claims/${claim.id}`)}>
                                                    <Eye size={16} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}

                        {!loading && claims.length === 0 && (
                            <div className="table-empty">
                                <Gavel size={40} />
                                <p>No claims match this filter</p>
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
                </div>
            </div>
        </div>
    );
};

export default AdminClaims;
