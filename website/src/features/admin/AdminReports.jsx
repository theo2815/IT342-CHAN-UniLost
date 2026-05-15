import { useState, useEffect, useCallback, useMemo, Fragment } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
    Flag,
    Search,
    Eye,
    EyeOff,
    RotateCcw,
    Trash2,
    CheckCircle2,
    XCircle,
    ChevronLeft,
    ChevronRight,
    ChevronDown,
    Loader2,
    AlertTriangle,
    User as UserIcon,
    MessageSquare,
    X,
} from 'lucide-react';
import Header from '../../shared/layout/Header/Header';
import Dropdown from '../../shared/components/ui/Dropdown';
import EmptyState from '../../shared/components/EmptyState/EmptyState';
import ConfirmDialog from '../../shared/components/ConfirmDialog/ConfirmDialog';
import { useToast } from '../../shared/components/ui/Toast/toastContext';
import { timeAgo } from '../../shared/utils/timeAgo';
import campusService from '../../shared/services/campusService';
import adminService from './adminService';
import './AdminReports.css';

const REASON_OPTIONS = ['SPAM', 'INAPPROPRIATE', 'FAKE', 'DUPLICATE'];
const STATUS_OPTIONS = ['ACTIVE', 'CLAIMED', 'PENDING_OWNER_CONFIRMATION', 'RETURNED', 'EXPIRED', 'TURNED_OVER_TO_OFFICE', 'HIDDEN'];
const APPEAL_OPTIONS = ['All', 'PENDING', 'APPROVED', 'REJECTED', 'NONE'];

const formatLabel = (value) => value.charAt(0) + value.slice(1).toLowerCase().replace(/_/g, ' ');
const appealLabel = (value) => {
    if (!value || value === 'NONE') return 'No appeal';
    return formatLabel(value);
};

const AdminReports = () => {
    const navigate = useNavigate();
    const toast = useToast();

    const [items, setItems] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [expandedId, setExpandedId] = useState(null);
    const [actionLoadingId, setActionLoadingId] = useState(null);

    const [search, setSearch] = useState('');
    const [reasonFilter, setReasonFilter] = useState('All');
    const [statusFilter, setStatusFilter] = useState('All');
    const [campusFilter, setCampusFilter] = useState('All');
    const [appealFilter, setAppealFilter] = useState('All');
    const [campuses, setCampuses] = useState([]);

    const [dismissTarget, setDismissTarget] = useState(null);
    const [deleteTarget, setDeleteTarget] = useState(null);
    const [approveTarget, setApproveTarget] = useState(null);

    const [hideTarget, setHideTarget] = useState(null);
    const [hideReason, setHideReason] = useState('');
    const [rejectTarget, setRejectTarget] = useState(null);
    const [rejectNote, setRejectNote] = useState('');

    const perPage = 10;

    const fetchReports = useCallback(async () => {
        setLoading(true);
        setError('');
        const result = await adminService.getFlaggedItems({ page: currentPage, size: perPage });
        if (result.success) {
            setItems(result.data.content || []);
            setTotalElements(result.data.totalElements || 0);
        } else {
            setError(typeof result.error === 'string' ? result.error : 'Failed to load reports');
        }
        setLoading(false);
    }, [currentPage]);

    useEffect(() => { fetchReports(); }, [fetchReports]);

    useEffect(() => {
        const loadCampuses = async () => {
            const result = await campusService.getAllCampuses();
            if (result.success && Array.isArray(result.data)) setCampuses(result.data);
        };
        loadCampuses();
    }, []);

    const filteredItems = useMemo(() => {
        return items.filter((item) => {
            if (search && !(item.title || '').toLowerCase().includes(search.toLowerCase())) return false;
            if (statusFilter !== 'All' && item.status !== statusFilter) return false;
            if (campusFilter !== 'All' && item.campusId !== campusFilter) return false;
            if (reasonFilter !== 'All' && !(item.flagReasons || []).includes(reasonFilter)) return false;
            if (appealFilter !== 'All') {
                const a = item.appealStatus || 'NONE';
                if (a !== appealFilter) return false;
            }
            return true;
        });
    }, [items, search, statusFilter, campusFilter, reasonFilter, appealFilter]);

    const totalPages = Math.ceil(totalElements / perPage);

    const distinctReasons = (item) => Array.from(new Set(item.flagReasons || []));

    const toggleExpand = (id) => setExpandedId((current) => (current === id ? null : id));

    const handleHide = (item) => {
        setHideTarget(item);
        setHideReason('');
    };

    const confirmHide = async () => {
        if (!hideTarget) return;
        setActionLoadingId(hideTarget.id);
        const result = await adminService.updateItemStatus(hideTarget.id, 'HIDDEN', hideReason);
        if (result.success) {
            toast.success('Item hidden from feed');
            await fetchReports();
        } else {
            toast.error(typeof result.error === 'string' ? result.error : 'Failed to hide item');
        }
        setActionLoadingId(null);
        setHideTarget(null);
    };

    const handleUnhide = async (item) => {
        setActionLoadingId(item.id);
        const result = await adminService.updateItemStatus(item.id, 'ACTIVE');
        if (result.success) {
            toast.success('Item restored');
            await fetchReports();
        } else {
            toast.error(typeof result.error === 'string' ? result.error : 'Failed to restore item');
        }
        setActionLoadingId(null);
    };

    const handleDismiss = async () => {
        if (!dismissTarget) return;
        setActionLoadingId(dismissTarget.id);
        const result = await adminService.dismissItemFlags(dismissTarget.id);
        if (result.success) {
            toast.success('Reports dismissed');
            await fetchReports();
        } else {
            toast.error(typeof result.error === 'string' ? result.error : 'Failed to dismiss reports');
        }
        setActionLoadingId(null);
        setDismissTarget(null);
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;
        setActionLoadingId(deleteTarget.id);
        const result = await adminService.forceDeleteItem(deleteTarget.id);
        if (result.success) {
            toast.success('Item removed');
            await fetchReports();
        } else {
            toast.error(typeof result.error === 'string' ? result.error : 'Failed to delete item');
        }
        setActionLoadingId(null);
        setDeleteTarget(null);
    };

    const handleApproveAppeal = async () => {
        if (!approveTarget) return;
        setActionLoadingId(approveTarget.id);
        const result = await adminService.reviewAppeal(approveTarget.id, 'APPROVED');
        if (result.success) {
            toast.success('Appeal approved — item restored');
            await fetchReports();
        } else {
            toast.error(typeof result.error === 'string' ? result.error : 'Failed to approve appeal');
        }
        setActionLoadingId(null);
        setApproveTarget(null);
    };

    const confirmReject = async () => {
        if (!rejectTarget) return;
        setActionLoadingId(rejectTarget.id);
        const result = await adminService.reviewAppeal(rejectTarget.id, 'REJECTED', rejectNote);
        if (result.success) {
            toast.success('Appeal rejected');
            await fetchReports();
        } else {
            toast.error(typeof result.error === 'string' ? result.error : 'Failed to reject appeal');
        }
        setActionLoadingId(null);
        setRejectTarget(null);
        setRejectNote('');
    };

    return (
        <div className="admin-reports-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Reports</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <Flag size={24} />
                            <h1>Reports</h1>
                            <span className="count-badge">{totalElements} open</span>
                        </div>
                        <p className="page-subtitle">Review user reports and owner appeals. Approve, reject, or take down items here.</p>
                    </div>

                    {/* Filters */}
                    <div className="filter-bar glass">
                        <div className="search-input">
                            <Search size={18} />
                            <input
                                type="text"
                                placeholder="Search by title..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                            />
                        </div>
                        <Dropdown label={reasonFilter === 'All' ? 'All Reasons' : formatLabel(reasonFilter)} align="left" width={180}>
                            {({ close }) => (
                                <>
                                    <Dropdown.Item className={reasonFilter === 'All' ? 'active' : ''} onClick={() => { setReasonFilter('All'); close(); }}>
                                        All Reasons
                                    </Dropdown.Item>
                                    {REASON_OPTIONS.map((r) => (
                                        <Dropdown.Item key={r} className={reasonFilter === r ? 'active' : ''} onClick={() => { setReasonFilter(r); close(); }}>
                                            {formatLabel(r)}
                                        </Dropdown.Item>
                                    ))}
                                </>
                            )}
                        </Dropdown>
                        <Dropdown label={statusFilter === 'All' ? 'All Statuses' : formatLabel(statusFilter)} align="left" width={220}>
                            {({ close }) => (
                                <>
                                    <Dropdown.Item className={statusFilter === 'All' ? 'active' : ''} onClick={() => { setStatusFilter('All'); close(); }}>
                                        All Statuses
                                    </Dropdown.Item>
                                    {STATUS_OPTIONS.map((s) => (
                                        <Dropdown.Item key={s} className={statusFilter === s ? 'active' : ''} onClick={() => { setStatusFilter(s); close(); }}>
                                            {formatLabel(s)}
                                        </Dropdown.Item>
                                    ))}
                                </>
                            )}
                        </Dropdown>
                        <Dropdown label={appealFilter === 'All' ? 'All Appeals' : appealLabel(appealFilter)} align="left" width={180}>
                            {({ close }) => (
                                <>
                                    {APPEAL_OPTIONS.map((opt) => (
                                        <Dropdown.Item key={opt} className={appealFilter === opt ? 'active' : ''} onClick={() => { setAppealFilter(opt); close(); }}>
                                            {opt === 'All' ? 'All Appeals' : appealLabel(opt)}
                                        </Dropdown.Item>
                                    ))}
                                </>
                            )}
                        </Dropdown>
                        <Dropdown label={campusFilter === 'All' ? 'All Campuses' : (campuses.find(c => c.id === campusFilter)?.shortLabel || campuses.find(c => c.id === campusFilter)?.name || 'Campus')} align="left" width={220}>
                            {({ close }) => (
                                <>
                                    <Dropdown.Item className={campusFilter === 'All' ? 'active' : ''} onClick={() => { setCampusFilter('All'); close(); }}>
                                        All Campuses
                                    </Dropdown.Item>
                                    {campuses.map((c) => (
                                        <Dropdown.Item key={c.id} className={campusFilter === c.id ? 'active' : ''} onClick={() => { setCampusFilter(c.id); close(); }}>
                                            {c.shortLabel || c.name}
                                        </Dropdown.Item>
                                    ))}
                                </>
                            )}
                        </Dropdown>
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
                                <p>Loading reports...</p>
                            </div>
                        ) : filteredItems.length === 0 ? (
                            <EmptyState
                                icon={<Flag size={40} />}
                                title="No reports right now"
                                message={items.length === 0
                                    ? 'No items have been flagged or appealed. You will see incoming reports here.'
                                    : 'No reports match the current filters.'}
                            />
                        ) : (
                            <table>
                                <thead>
                                    <tr>
                                        <th className="expand-cell" aria-label="Expand"></th>
                                        <th>Item</th>
                                        <th>Status</th>
                                        <th>Reports</th>
                                        <th>Reasons</th>
                                        <th>Appeal</th>
                                        <th>Posted By</th>
                                        <th>Last Update</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredItems.map((item, i) => {
                                        const isExpanded = expandedId === item.id;
                                        const reasons = distinctReasons(item);
                                        const isBusy = actionLoadingId === item.id;
                                        const appealStatus = item.appealStatus || 'NONE';
                                        return (
                                            <Fragment key={item.id}>
                                                <tr
                                                    style={{ animationDelay: `${i * 0.03}s` }}
                                                    className={`clickable-row ${isExpanded ? 'expanded' : ''}`}
                                                    onClick={() => toggleExpand(item.id)}
                                                >
                                                    <td className="expand-cell" onClick={(e) => { e.stopPropagation(); toggleExpand(item.id); }}>
                                                        <button className="expand-btn" aria-label={isExpanded ? 'Collapse' : 'Expand'}>
                                                            <ChevronDown size={16} className={isExpanded ? 'rotated' : ''} />
                                                        </button>
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
                                                    <td>
                                                        <span className={`status-badge ${item.status?.toLowerCase().replace(/_/g, '-')}`}>
                                                            {item.status?.replace(/_/g, ' ')}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <span className="flag-count"><Flag size={14} /> {item.flagCount}</span>
                                                    </td>
                                                    <td>
                                                        <div className="reason-badges">
                                                            {reasons.map((r) => (
                                                                <span key={r} className={`reason-badge ${r.toLowerCase()}`}>{formatLabel(r)}</span>
                                                            ))}
                                                        </div>
                                                    </td>
                                                    <td>
                                                        {appealStatus !== 'NONE' ? (
                                                            <span className={`appeal-badge ${appealStatus.toLowerCase()}`}>{appealLabel(appealStatus)}</span>
                                                        ) : '-'}
                                                    </td>
                                                    <td className="td-poster">
                                                        <span>{item.reporter?.fullName || '-'}</span>
                                                    </td>
                                                    <td className="td-date">{timeAgo(item.updatedAt || item.createdAt)}</td>
                                                    <td className="td-actions" onClick={(e) => e.stopPropagation()}>
                                                        <button className="action-btn info" title="View" onClick={() => navigate(`/items/${item.id}`)}>
                                                            <Eye size={16} />
                                                        </button>
                                                        {item.status === 'ACTIVE' && (
                                                            <button className="action-btn warning" title="Hide from feed" onClick={() => handleHide(item)} disabled={isBusy}>
                                                                <EyeOff size={16} />
                                                            </button>
                                                        )}
                                                        {item.status === 'HIDDEN' && appealStatus !== 'PENDING' && (
                                                            <button className="action-btn success" title="Restore to feed" onClick={() => handleUnhide(item)} disabled={isBusy}>
                                                                <RotateCcw size={16} />
                                                            </button>
                                                        )}
                                                        {appealStatus === 'PENDING' && (
                                                            <>
                                                                <button className="action-btn success" title="Approve appeal" onClick={() => setApproveTarget(item)} disabled={isBusy}>
                                                                    <CheckCircle2 size={16} />
                                                                </button>
                                                                <button className="action-btn danger" title="Reject appeal" onClick={() => { setRejectTarget(item); setRejectNote(''); }} disabled={isBusy}>
                                                                    <XCircle size={16} />
                                                                </button>
                                                            </>
                                                        )}
                                                        {item.flagCount > 0 && (
                                                            <button className="action-btn success" title="Dismiss reports" onClick={() => setDismissTarget(item)} disabled={isBusy}>
                                                                <CheckCircle2 size={16} />
                                                            </button>
                                                        )}
                                                        <button className="action-btn danger" title="Delete item" onClick={() => setDeleteTarget(item)} disabled={isBusy}>
                                                            <Trash2 size={16} />
                                                        </button>
                                                    </td>
                                                </tr>
                                                {isExpanded && (
                                                    <tr className="report-detail-row">
                                                        <td colSpan={9}>
                                                            <div className="report-detail-panel">
                                                                {appealStatus !== 'NONE' && (
                                                                    <div className="appeal-section">
                                                                        <h4 className="report-detail-heading">
                                                                            <MessageSquare size={14} /> Owner appeal
                                                                            <span className={`appeal-badge ${appealStatus.toLowerCase()}`}>{appealLabel(appealStatus)}</span>
                                                                        </h4>
                                                                        {item.appealText ? (
                                                                            <p className="report-detail-text">{item.appealText}</p>
                                                                        ) : (
                                                                            <p className="report-detail-text empty">No appeal text recorded.</p>
                                                                        )}
                                                                        <p className="report-detail-time">
                                                                            Submitted {timeAgo(item.appealedAt)}
                                                                            {item.appealResolvedAt ? ` · Resolved ${timeAgo(item.appealResolvedAt)}` : ''}
                                                                        </p>
                                                                        {item.appealAdminNote && (
                                                                            <p className="report-detail-text appeal-admin-note">
                                                                                <strong>Admin note:</strong> {item.appealAdminNote}
                                                                            </p>
                                                                        )}
                                                                    </div>
                                                                )}
                                                                {item.adminActionReason && (
                                                                    <div className="appeal-section">
                                                                        <h4 className="report-detail-heading">Admin action note</h4>
                                                                        <p className="report-detail-text">{item.adminActionReason}</p>
                                                                    </div>
                                                                )}
                                                                <h4 className="report-detail-heading">Individual reports</h4>
                                                                {Array.isArray(item.flagDetails) && item.flagDetails.length > 0 ? (
                                                                    <ul className="report-detail-list">
                                                                        {item.flagDetails.map((detail, idx) => (
                                                                            <li key={idx} className="report-detail-item">
                                                                                <div className="report-detail-avatar" title="Reporter (admin-only)">
                                                                                    <UserIcon size={16} />
                                                                                </div>
                                                                                <div className="report-detail-body">
                                                                                    <div className="report-detail-meta">
                                                                                        <span className={`reason-badge ${(detail.reason || '').toLowerCase()}`}>
                                                                                            {detail.reason ? formatLabel(detail.reason) : 'Unknown'}
                                                                                        </span>
                                                                                        <span className="report-detail-time">{timeAgo(detail.createdAt)}</span>
                                                                                    </div>
                                                                                    {detail.description ? (
                                                                                        <p className="report-detail-text">{detail.description}</p>
                                                                                    ) : (
                                                                                        <p className="report-detail-text empty">No additional details provided.</p>
                                                                                    )}
                                                                                </div>
                                                                            </li>
                                                                        ))}
                                                                    </ul>
                                                                ) : item.flagCount > 0 ? (
                                                                    <p className="report-detail-empty">
                                                                        These reports were submitted before per-report detail was tracked. Only reason categories are available.
                                                                    </p>
                                                                ) : (
                                                                    <p className="report-detail-empty">No active reports on this item.</p>
                                                                )}
                                                            </div>
                                                        </td>
                                                    </tr>
                                                )}
                                            </Fragment>
                                        );
                                    })}
                                </tbody>
                            </table>
                        )}
                    </div>

                    {totalPages > 1 && (
                        <div className="pagination">
                            <span>Showing {currentPage * perPage + 1}-{Math.min((currentPage + 1) * perPage, totalElements)} of {totalElements}</span>
                            <div className="pagination-btns">
                                <button disabled={currentPage === 0} onClick={() => setCurrentPage((p) => p - 1)}>
                                    <ChevronLeft size={16} /> Prev
                                </button>
                                <button disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage((p) => p + 1)}>
                                    Next <ChevronRight size={16} />
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Hide modal — symmetrical to AdminItems remove modal */}
            {hideTarget && (
                <div className="modal-overlay" onClick={() => actionLoadingId !== hideTarget.id && setHideTarget(null)}>
                    <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <EyeOff size={20} color="#f59e0b" />
                            <h3>Hide Item</h3>
                            <button className="modal-close" onClick={() => actionLoadingId !== hideTarget.id && setHideTarget(null)}>
                                <X size={18} />
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="remove-item-summary">
                                <img src={hideTarget.imageUrls?.[0] || '/placeholder.png'} alt="" />
                                <div>
                                    <strong>{hideTarget.title}</strong>
                                    <span>Posted by {hideTarget.reporter?.fullName || '-'}</span>
                                </div>
                            </div>
                            <label>Reason for hiding (optional)</label>
                            <textarea
                                value={hideReason}
                                onChange={(e) => setHideReason(e.target.value.slice(0, 280))}
                                placeholder="Tell the owner why this item was hidden so they can fix it or appeal."
                                rows={3}
                                maxLength={280}
                            />
                            <div className="flag-description-meta">
                                <span>{hideReason.length}/280</span>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn-secondary" onClick={() => setHideTarget(null)} disabled={actionLoadingId === hideTarget.id}>Cancel</button>
                            <button className="btn-danger" onClick={confirmHide} disabled={actionLoadingId === hideTarget.id}>
                                {actionLoadingId === hideTarget.id ? 'Hiding...' : 'Confirm Hide'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Reject appeal modal */}
            {rejectTarget && (
                <div className="modal-overlay" onClick={() => actionLoadingId !== rejectTarget.id && setRejectTarget(null)}>
                    <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <XCircle size={20} color="#ef4444" />
                            <h3>Reject Appeal</h3>
                            <button className="modal-close" onClick={() => actionLoadingId !== rejectTarget.id && setRejectTarget(null)}>
                                <X size={18} />
                            </button>
                        </div>
                        <div className="modal-body">
                            <p className="reject-summary">The item will stay hidden and the owner will be notified.</p>
                            <label>Note to the owner (optional)</label>
                            <textarea
                                value={rejectNote}
                                onChange={(e) => setRejectNote(e.target.value.slice(0, 280))}
                                placeholder="Explain why the appeal was not approved..."
                                rows={3}
                                maxLength={280}
                            />
                            <div className="flag-description-meta">
                                <span>{rejectNote.length}/280</span>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn-secondary" onClick={() => setRejectTarget(null)} disabled={actionLoadingId === rejectTarget.id}>Cancel</button>
                            <button className="btn-danger" onClick={confirmReject} disabled={actionLoadingId === rejectTarget.id}>
                                {actionLoadingId === rejectTarget.id ? 'Rejecting...' : 'Confirm Reject'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <ConfirmDialog
                isOpen={!!dismissTarget}
                onClose={() => setDismissTarget(null)}
                onConfirm={handleDismiss}
                title="Dismiss reports?"
                message={dismissTarget
                    ? `This will clear all ${dismissTarget.flagCount} report(s) on "${dismissTarget.title}". The item will stay live in the feed.`
                    : ''}
                confirmLabel="Dismiss"
                variant="warning"
            />

            <ConfirmDialog
                isOpen={!!approveTarget}
                onClose={() => setApproveTarget(null)}
                onConfirm={handleApproveAppeal}
                title="Approve appeal?"
                message={approveTarget
                    ? `"${approveTarget.title}" will be restored to the feed and all flags will be cleared.`
                    : ''}
                confirmLabel="Approve"
                variant="success"
            />

            <ConfirmDialog
                isOpen={!!deleteTarget}
                onClose={() => setDeleteTarget(null)}
                onConfirm={handleDelete}
                title="Delete item?"
                message={deleteTarget
                    ? `Remove "${deleteTarget.title}" from the platform? This cannot be undone.`
                    : ''}
                confirmLabel="Delete"
                variant="danger"
            />
        </div>
    );
};

export default AdminReports;
