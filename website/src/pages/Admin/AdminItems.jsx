import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Package, Search, Eye, Trash2, ChevronLeft, ChevronRight, X, AlertTriangle } from 'lucide-react';
import Header from '../../components/Header';
import { mockAdminItems, timeAgo } from '../../mockData/adminData';
import './AdminItems.css';

const statusOptions = ['All', 'ACTIVE', 'CLAIMED', 'HANDED_OVER', 'EXPIRED', 'CANCELLED'];
const typeOptions = ['All', 'LOST', 'FOUND'];
const schoolOptions = ['All', 'CIT-U', 'USC', 'UP Cebu', 'USJ-R', 'UC', 'SWU', 'CNU', 'CTU'];

const AdminItems = () => {
    const navigate = useNavigate();
    const [search, setSearch] = useState('');
    const [typeFilter, setTypeFilter] = useState('All');
    const [statusFilter, setStatusFilter] = useState('All');
    const [schoolFilter, setSchoolFilter] = useState('All');
    const [selectedIds, setSelectedIds] = useState([]);
    const [showRemoveModal, setShowRemoveModal] = useState(false);
    const [removeTarget, setRemoveTarget] = useState(null);
    const [removeReason, setRemoveReason] = useState('');
    const [items, setItems] = useState(mockAdminItems);
    const [currentPage, setCurrentPage] = useState(1);
    const perPage = 10;

    const filtered = items.filter(item => {
        if (search && !item.title.toLowerCase().includes(search.toLowerCase()) && !item.postedBy.email.toLowerCase().includes(search.toLowerCase())) return false;
        if (typeFilter !== 'All' && item.type !== typeFilter) return false;
        if (statusFilter !== 'All' && item.status !== statusFilter) return false;
        if (schoolFilter !== 'All' && item.school.shortName !== schoolFilter) return false;
        return true;
    });

    const totalPages = Math.ceil(filtered.length / perPage);
    const paginated = filtered.slice((currentPage - 1) * perPage, currentPage * perPage);

    const toggleSelect = (id) => {
        setSelectedIds(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
    };

    const toggleSelectAll = () => {
        if (selectedIds.length === paginated.length) {
            setSelectedIds([]);
        } else {
            setSelectedIds(paginated.map(i => i.id));
        }
    };

    const handleRemove = (item) => {
        setRemoveTarget(item);
        setRemoveReason('');
        setShowRemoveModal(true);
    };

    const confirmRemove = () => {
        if (removeTarget) {
            setItems(prev => prev.filter(i => i.id !== removeTarget.id));
            setSelectedIds(prev => prev.filter(id => id !== removeTarget.id));
        }
        setShowRemoveModal(false);
        setRemoveTarget(null);
    };

    const handleBulkRemove = () => {
        setItems(prev => prev.filter(i => !selectedIds.includes(i.id)));
        setSelectedIds([]);
    };

    return (
        <div className="admin-items-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    {/* Breadcrumb */}
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Items</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Package size={24} />
                            <h1>Item Management</h1>
                            <span className="count-badge">{filtered.length} items</span>
                        </div>
                    </div>

                    {/* Filters */}
                    <div className="filter-bar glass">
                        <div className="search-input">
                            <Search size={18} />
                            <input
                                type="text"
                                placeholder="Search by title or email..."
                                value={search}
                                onChange={(e) => { setSearch(e.target.value); setCurrentPage(1); }}
                            />
                        </div>
                        <select value={typeFilter} onChange={(e) => { setTypeFilter(e.target.value); setCurrentPage(1); }}>
                            {typeOptions.map(o => <option key={o} value={o}>{o === 'All' ? 'All Types' : o}</option>)}
                        </select>
                        <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}>
                            {statusOptions.map(o => <option key={o} value={o}>{o === 'All' ? 'All Statuses' : o.replace('_', ' ')}</option>)}
                        </select>
                        <select value={schoolFilter} onChange={(e) => { setSchoolFilter(e.target.value); setCurrentPage(1); }}>
                            {schoolOptions.map(o => <option key={o} value={o}>{o === 'All' ? 'All Schools' : o}</option>)}
                        </select>
                    </div>

                    {/* Bulk Action Bar */}
                    {selectedIds.length > 0 && (
                        <div className="bulk-bar">
                            <span>{selectedIds.length} item{selectedIds.length > 1 ? 's' : ''} selected</span>
                            <button className="btn-danger-sm" onClick={handleBulkRemove}>
                                <Trash2 size={14} /> Remove Selected
                            </button>
                        </div>
                    )}

                    {/* Table */}
                    <div className="admin-table glass">
                        <table>
                            <thead>
                                <tr>
                                    <th><input type="checkbox" checked={selectedIds.length === paginated.length && paginated.length > 0} onChange={toggleSelectAll} /></th>
                                    <th>Image</th>
                                    <th>Title</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>School</th>
                                    <th>Posted By</th>
                                    <th>Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {paginated.map((item, i) => (
                                    <tr key={item.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                        <td><input type="checkbox" checked={selectedIds.includes(item.id)} onChange={() => toggleSelect(item.id)} /></td>
                                        <td>
                                            <img src={item.imageUrl} alt="" className="table-thumb" />
                                        </td>
                                        <td className="td-title">{item.title}</td>
                                        <td><span className={`type-badge ${item.type.toLowerCase()}`}>{item.type}</span></td>
                                        <td><span className={`status-badge ${item.status.toLowerCase().replace('_', '-')}`}>{item.status.replace('_', ' ')}</span></td>
                                        <td>{item.school.shortName}</td>
                                        <td className="td-poster">
                                            <span>{item.postedBy.firstName} {item.postedBy.lastName}</span>
                                        </td>
                                        <td className="td-date">{timeAgo(item.createdAt)}</td>
                                        <td className="td-actions">
                                            <button className="action-btn" title="View" onClick={() => navigate(`/items/${item.id}`)}>
                                                <Eye size={16} />
                                            </button>
                                            <button className="action-btn danger" title="Remove" onClick={() => handleRemove(item)}>
                                                <Trash2 size={16} />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                        {paginated.length === 0 && (
                            <div className="table-empty">
                                <Package size={40} />
                                <p>No items match your filters</p>
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

                    {/* Remove Modal */}
                    {showRemoveModal && removeTarget && (
                        <div className="modal-overlay" onClick={() => setShowRemoveModal(false)}>
                            <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                                <div className="modal-header">
                                    <AlertTriangle size={20} color="#ef4444" />
                                    <h3>Remove Item</h3>
                                    <button className="modal-close" onClick={() => setShowRemoveModal(false)}>
                                        <X size={18} />
                                    </button>
                                </div>
                                <div className="modal-body">
                                    <div className="remove-item-summary">
                                        <img src={removeTarget.imageUrl} alt="" />
                                        <div>
                                            <strong>{removeTarget.title}</strong>
                                            <span>Posted by {removeTarget.postedBy.firstName} {removeTarget.postedBy.lastName}</span>
                                        </div>
                                    </div>
                                    <label>Reason for removal</label>
                                    <textarea
                                        value={removeReason}
                                        onChange={(e) => setRemoveReason(e.target.value)}
                                        placeholder="Enter the reason for removing this item..."
                                        rows={3}
                                    />
                                </div>
                                <div className="modal-footer">
                                    <button className="btn-secondary" onClick={() => setShowRemoveModal(false)}>Cancel</button>
                                    <button className="btn-danger" onClick={confirmRemove}>Confirm Remove</button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminItems;
