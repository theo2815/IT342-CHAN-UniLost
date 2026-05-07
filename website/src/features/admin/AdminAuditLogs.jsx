import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { ScrollText, ChevronLeft, ChevronRight, Loader2, AlertTriangle } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import Dropdown from '../../../shared/components/ui/Dropdown';
import adminService from '../adminService';
import './AdminAuditLogs.css';

const actionOptions = ['All', 'UPDATE_ITEM_STATUS', 'DELETE_ITEM', 'UPDATE_USER_STATUS', 'FORCE_COMPLETE_HANDOVER', 'BULK_UPDATE_ITEMS', 'BULK_DELETE_ITEMS', 'BULK_UPDATE_USERS', 'EXPORT_DATA'];
const targetTypeOptions = ['All', 'ITEM', 'USER', 'CLAIM'];

const AdminAuditLogs = () => {
    const [actionFilter, setActionFilter] = useState('All');
    const [targetTypeFilter, setTargetTypeFilter] = useState('All');
    const [logs, setLogs] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const perPage = 20;

    const fetchLogs = useCallback(async () => {
        setLoading(true);
        setError('');
        const params = { page: currentPage, size: perPage };
        if (actionFilter !== 'All') params.action = actionFilter;
        if (targetTypeFilter !== 'All') params.targetType = targetTypeFilter;

        const result = await adminService.getAuditLogs(params);
        if (result.success) {
            setLogs(result.data.content || []);
            setTotalElements(result.data.totalElements || 0);
        } else {
            setError(result.error);
        }
        setLoading(false);
    }, [currentPage, actionFilter, targetTypeFilter]);

    useEffect(() => { fetchLogs(); }, [fetchLogs]);

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

    const formatAction = (action) => {
        return action?.replace(/_/g, ' ') || '-';
    };

    const actionClass = (action) => {
        return action?.toLowerCase().replace(/_/g, '-') || '';
    };

    return (
        <div className="admin-audit-page">
            <Header />
            <div className="main-content">
                <div className="content-wrapper">
                    <div className="breadcrumb">
                        <Link to="/admin">Admin</Link>
                        <span>/</span>
                        <span>Audit Logs</span>
                    </div>

                    <div className="page-header">
                        <div className="page-header-top">
                            <ScrollText size={24} />
                            <h1>Audit Logs</h1>
                            <span className="count-badge">{totalElements} entries</span>
                        </div>
                    </div>

                    {/* Filters */}
                    <div className="filter-bar glass">
                        <Dropdown label={actionFilter === 'All' ? 'All Actions' : actionFilter.replace(/_/g, ' ')} align="left" width={220}>
                            {({ close }) => actionOptions.map(o => (
                                <Dropdown.Item key={o} className={actionFilter === o ? 'active' : ''} onClick={() => { setActionFilter(o); setCurrentPage(0); close(); }}>
                                    {o === 'All' ? 'All Actions' : o.replace(/_/g, ' ')}
                                </Dropdown.Item>
                            ))}
                        </Dropdown>
                        <Dropdown label={targetTypeFilter === 'All' ? 'All Targets' : targetTypeFilter} align="left" width={160}>
                            {({ close }) => targetTypeOptions.map(o => (
                                <Dropdown.Item key={o} className={targetTypeFilter === o ? 'active' : ''} onClick={() => { setTargetTypeFilter(o); setCurrentPage(0); close(); }}>
                                    {o === 'All' ? 'All Targets' : o}
                                </Dropdown.Item>
                            ))}
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
                                <p>Loading audit logs...</p>
                            </div>
                        ) : (
                            <table>
                                <thead>
                                    <tr>
                                        <th>Action</th>
                                        <th>Target</th>
                                        <th>Admin</th>
                                        <th>Details</th>
                                        <th>Time</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {logs.map((log, i) => (
                                        <tr key={log.id} style={{ animationDelay: `${i * 0.03}s` }}>
                                            <td>
                                                <span className={`action-badge ${actionClass(log.action)}`}>
                                                    {formatAction(log.action)}
                                                </span>
                                            </td>
                                            <td>
                                                <div className="td-target">
                                                    <span className="target-badge">{log.targetType}</span>
                                                    {log.targetId && (
                                                        <span className="td-target-id">{log.targetId.substring(0, 12)}...</span>
                                                    )}
                                                </div>
                                            </td>
                                            <td className="td-admin">{log.adminEmail}</td>
                                            <td className="td-details" title={log.details}>{log.details}</td>
                                            <td className="td-date">{timeAgo(log.createdAt)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}

                        {!loading && logs.length === 0 && (
                            <div className="table-empty">
                                <ScrollText size={40} />
                                <p>No audit logs found</p>
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

export default AdminAuditLogs;
