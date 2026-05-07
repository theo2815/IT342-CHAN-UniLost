import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Package, Search, Eye, Trash2, ChevronLeft, ChevronRight, X, AlertTriangle, Loader2, Flag, EyeOff, RotateCcw, Download, CheckSquare } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import Dropdown from '../../../shared/components/ui/Dropdown';
import adminService from '../adminService';
import './AdminItems.css';

const statusOptions = ['All', 'ACTIVE', 'CLAIMED', 'HANDED_OVER', 'EXPIRED', 'RETURNED', 'HIDDEN'];
const typeOptions = ['All', 'LOST', 'FOUND'];

const AdminItems = () => {
    const navigate = useNavigate();
    const [search, setSearch] = useState('');
    const [typeFilter, setTypeFilter] = useState('All');
    const [statusFilter, setStatusFilter] = useState('All');
    const [items, setItems] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showRemoveModal, setShowRemoveModal] = useState(false);
    const [removeTarget, setRemoveTarget] = useState(null);
    const [removeReason, setRemoveReason] = useState('');
    const [actionLoading, setActionLoading] = useState(false);
    const [exportLoading, setExportLoading] = useState(false);
    const [selectedIds, setSelectedIds] = useState(new Set());
    const [bulkLoading, setBulkLoading] = useState(false);
    const perPage = 10;

    const fetchItems = useCallback(async () => {
        setLoading(true);
        setError('');
        const params = { page: currentPage, size: perPage };
        if (search) params.keyword = search;
        if (typeFilter !== 'All') params.type = typeFilter;
        if (statusFilter !== 'All') params.status = statusFilter;

        const result = await adminService.getCampusItems(params);
        if (result.success) {
            setItems(result.data.content || []);
            setTotalElements(result.data.totalElements || 0);
        } else {
            setError(result.error);
        }
        setLoading(false);
    }, [currentPage, search, typeFilter, statusFilter]);

    useEffect(() => { fetchItems(); }, [fetchItems]);

    const totalPages = Math.ceil(totalElements / perPage);

    const handleRemove = (item) => {
        setRemoveTarget(item);
        setRemoveReason('');
        setShowRemoveModal(true);
    };

    const confirmRemove = async () => {
        if (!removeTarget) return;
        setActionLoading(true);
        const result = await adminService.forceDeleteItem(removeTarget.id);
        if (result.success) {
            fetchItems();
        } else {
            setError(result.error);
        }
        setActionLoading(false);
        setShowRemoveModal(false);
        setRemoveTarget(null);
    };

    const handleStatusChange = async (itemId, newStatus) => {
        setActionLoading(true);
        const result = await adminService.updateItemStatus(itemId, newStatus);
        if (result.success) {
            fetchItems();
        } else {
            setError(result.error);
        }
        setActionLoading(false);
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

    const handleSearchSubmit = () => {
        setCurrentPage(0);
        fetchItems();
    };

    const handleExport = async () => {
        setExportLoading(true);
        await adminService.exportItems();
        setExportLoading(false);
    };

    const toggleSelect = (id) => {
        setSelectedIds(prev => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const toggleSelectAll = () => {
        if (selectedIds.size === items.length) {
            setSelectedIds(new Set());
        } else {
            setSelectedIds(new Set(items.map(i => i.id)));
        }
    };

    const handleBulkStatus = async (status) => {
        setBulkLoading(true);
        const result = await adminService.bulkUpdateItemStatus([...selectedIds], status);
        if (result.success) {
            setSelectedIds(new Set());
            fetchItems();
        } else {
            setError(result.error);
        }
        setBulkLoading(false);
    };

    const handleBulkDelete = async () => {
        if (!window.confirm(`Delete ${selectedIds.size} selected item(s)? This cannot be undone.`)) return;
        setBulkLoading(true);
        const result = await adminService.bulkDeleteItems([...selectedIds]);
        if (result.success) {
            setSelectedIds(new Set());
            fetchItems();
        } else {
            setError(result.error);
        }
        setBulkLoading(false);
    };

    return (
        <div className="admin-items-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Items</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Package size={24} />
                            <h1>Item Management</h1>
                            <span className="count-badge">{totalElements} items</span>
                        </div>
                    </div>

                    {/* Filters */}
                    <div className="filter-bar glass">
                        <div className="search-input">
                            <Search size={18} />
                            <input
                                type="text"
                                placeholder="Search by title or description..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleSearchSubmit()}
                            />
                        </div>
                        <Dropdown label={typeFilter === 'All' ? 'All Types' : typeFilter} align="left" width={160}>
                            {({ close }) => typeOptions.map(o => (
                                <Dropdown.Item key={o} className={typeFilter === o ? 'active' : ''} onClick={() => { setTypeFilter(o); setCurrentPage(0); close(); }}>
                                    {o === 'All' ? 'All Types' : o}
                                </Dropdown.Item>
                            ))}
                        </Dropdown>
                        <Dropdown label={statusFilter === 'All' ? 'All Statuses' : statusFilter.replace(/_/g, ' ')} align="left" width={200}>
                            {({ close }) => statusOptions.map(o => (
                                <Dropdown.Item key={o} className={statusFilter === o ? 'active' : ''} onClick={() => { setStatusFilter(o); setCurrentPage(0); close(); }}>
                                    {o === 'All' ? 'All Statuses' : o.replace(/_/g, ' ')}
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
                            <span>{selectedIds.size} item(s) selected</span>
                            <button className="bulk-btn" onClick={() => handleBulkStatus('HIDDEN')} disabled={bulkLoading}>Hide</button>
                            <button className="bulk-btn" onClick={() => handleBulkStatus('ACTIVE')} disabled={bulkLoading}>Unhide</button>
                            <button className="bulk-btn danger" onClick={handleBulkDelete} disabled={bulkLoading}>Delete</button>
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
                                <p>Loading items...</p>
                            </div>
                        ) : (
                            <table>
                                <thead>
                                    <tr>
                                        <th className="checkbox-cell">
                                            <input type="checkbox" checked={items.length > 0 && selectedIds.size === items.length} onChange={toggleSelectAll} />
                                        </th>
                                        <th>Item</th>
                                        <th>Status</th>
                                        <th>Posted By</th>
                                        <th>Flags</th>
                                        <th>Date</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {items.map((item, i) => (
                                        <tr key={item.id} style={{ animationDelay: `${i * 0.03}s` }} className="clickable-row" onClick={() => navigate(`/items/${item.id}`)}>
                                            <td className="checkbox-cell" onClick={(e) => e.stopPropagation()}>
                                                <input type="checkbox" checked={selectedIds.has(item.id)} onChange={() => toggleSelect(item.id)} />
                                            </td>
                                            <td>
                                                <div className="item-cell">
                                                    <img
                                                        src={item.imageUrls?.[0] || '/placeholder.png'}
                                                        alt=""
                                                        className="table-thumb"
                                                    />
                                                    <div className="item-cell-info">
                                                        <span className="item-cell-title">{item.title}</span>
                                                        <span className={`type-badge-sm ${item.type?.toLowerCase()}`}>{item.type}</span>
                                                    </div>
                                                </div>
                                            </td>
                                            <td><span className={`status-badge ${item.status?.toLowerCase().replace(/_/g, '-')}`}>{item.status?.replace(/_/g, ' ')}</span></td>
                                            <td className="td-poster">
                                                <span>{item.reporter?.fullName || '-'}</span>
                                            </td>
                                            <td>
                                                {item.flagCount > 0 ? (
                                                    <span className="flag-count"><Flag size={14} /> {item.flagCount}</span>
                                                ) : '-'}
                                            </td>
                                            <td className="td-date">{timeAgo(item.createdAt)}</td>
                                            <td className="td-actions" onClick={(e) => e.stopPropagation()}>
                                                <button className="action-btn" title="View" onClick={() => navigate(`/items/${item.id}`)}>
                                                    <Eye size={16} />
                                                </button>
                                                {item.status === 'ACTIVE' && (
                                                    <button className="action-btn warning" title="Hide" onClick={() => handleStatusChange(item.id, 'HIDDEN')} disabled={actionLoading}>
                                                        <EyeOff size={16} />
                                                    </button>
                                                )}
                                                {item.status === 'HIDDEN' && (
                                                    <button className="action-btn success" title="Unhide" onClick={() => handleStatusChange(item.id, 'ACTIVE')} disabled={actionLoading}>
                                                        <RotateCcw size={16} />
                                                    </button>
                                                )}
                                                <button className="action-btn danger" title="Remove" onClick={() => handleRemove(item)}>
                                                    <Trash2 size={16} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}

                        {!loading && items.length === 0 && (
                            <div className="table-empty">
                                <Package size={40} />
                                <p>No items match your filters</p>
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
                                        <img src={removeTarget.imageUrls?.[0] || '/placeholder.png'} alt="" />
                                        <div>
                                            <strong>{removeTarget.title}</strong>
                                            <span>Posted by {removeTarget.reporter?.fullName || '-'}</span>
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
                                    <button className="btn-danger" onClick={confirmRemove} disabled={actionLoading}>
                                        {actionLoading ? 'Removing...' : 'Confirm Remove'}
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

export default AdminItems;
