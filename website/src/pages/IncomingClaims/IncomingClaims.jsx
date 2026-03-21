import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, AlertTriangle, Check, X, Lock, MessageSquare, User, ChevronLeft, ChevronRight } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import claimService from '../../services/claimService';
import itemService from '../../services/itemService';
import { timeAgo } from '../../utils/timeAgo';
import './IncomingClaims.css';

function IncomingClaims() {
    const { itemId } = useParams();
    const navigate = useNavigate();

    const [item, setItem] = useState(null);
    const [claims, setClaims] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionLoading, setActionLoading] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchClaims = async (targetPage) => {
        const claimsResult = await claimService.getClaimsForItem(itemId, targetPage, 10);
        if (claimsResult.success) {
            setClaims(claimsResult.data);
            setTotalPages(claimsResult.totalPages || 0);
        } else {
            setError(claimsResult.error);
        }
    };

    useEffect(() => {
        const controller = new AbortController();
        const fetchData = async () => {
            setLoading(true);
            setError('');

            const itemResult = await itemService.getItemById(itemId);
            if (controller.signal.aborted) return;
            if (itemResult.success) {
                setItem(itemResult.data);
            } else {
                setError(itemResult.error);
                setLoading(false);
                return;
            }

            await fetchClaims(page);
            if (controller.signal.aborted) return;

            setLoading(false);
        };
        fetchData();
        return () => controller.abort();
    }, [itemId, page]);

    const hasAccepted = claims.some((c) => c.status === 'ACCEPTED' || c.status === 'HANDED_OVER' || c.status === 'COMPLETED');

    if (loading) {
        return (
            <div className="incoming-claims-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state"><p>Loading...</p></div>
                    </div>
                </main>
            </div>
        );
    }

    if (!item) {
        return (
            <div className="incoming-claims-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <h2>{error || 'Item Not Found'}</h2>
                            <button className="back-btn" onClick={() => navigate('/profile')}>
                                <ArrowLeft size={18} /> Back to Profile
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const getInitials = (name) => {
        if (!name) return '??';
        const parts = name.split(' ');
        return parts.map((p) => p.charAt(0)).join('').toUpperCase();
    };

    const handleApprove = async (claimId) => {
        if (actionLoading) return;
        setActionLoading(claimId);
        const result = await claimService.acceptClaim(claimId);
        if (result.success) {
            await fetchClaims(page);
        } else {
            setError(result.error);
        }
        setActionLoading(null);
    };

    const handleReject = async (claimId) => {
        if (actionLoading) return;
        setActionLoading(claimId);
        const result = await claimService.rejectClaim(claimId);
        if (result.success) {
            await fetchClaims(page);
        } else {
            setError(result.error);
        }
        setActionLoading(null);
    };

    const imageUrl = item.imageUrls?.[0] || 'https://picsum.photos/seed/placeholder/400/300';

    return (
        <div className="incoming-claims-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <button className="back-link" onClick={() => navigate('/profile')}>
                        <ArrowLeft size={18} /> Back to Profile
                    </button>

                    {/* Item Summary */}
                    <div className="ic-item-summary glass">
                        <img src={imageUrl} alt={item.title} className="ic-item-thumb" />
                        <div className="ic-item-info">
                            <div className="ic-item-badges">
                                <StatusBadge type={item.type} />
                                <StatusBadge status={item.status} />
                            </div>
                            <h2>{item.title}</h2>
                            <p>{claims.length} claim{claims.length !== 1 ? 's' : ''} submitted</p>
                        </div>
                    </div>

                    {/* Warning */}
                    {error && (
                        <div className="ic-warning">
                            <AlertTriangle size={16} />
                            <span>{error}</span>
                        </div>
                    )}
                    {!hasAccepted && claims.length > 0 && !error && (
                        <div className="ic-warning">
                            <AlertTriangle size={16} />
                            <span>Only one claim can be approved per item. Review carefully before approving.</span>
                        </div>
                    )}

                    {/* Claims */}
                    {claims.length > 0 ? (
                        <div className="ic-claims-list">
                            {claims.map((claim, index) => (
                                <div
                                    key={claim.id}
                                    className="ic-claim-card glass"
                                    style={{ animationDelay: `${index * 0.05}s` }}
                                >
                                    {/* Claimant header */}
                                    <div className="ic-claim-header">
                                        <div className="ic-claimant">
                                            <div className="ic-avatar">{getInitials(claim.claimantName)}</div>
                                            <div>
                                                <span className="ic-claimant-name">{claim.claimantName}</span>
                                                <span className="ic-claimant-school">{claim.claimantSchool}</span>
                                            </div>
                                        </div>
                                        <div className="ic-claim-status">
                                            <StatusBadge status={claim.status} />
                                            <span className="ic-claim-time">{timeAgo(claim.createdAt)}</span>
                                        </div>
                                    </div>

                                    {/* Secret detail comparison (FOUND items only) */}
                                    {item.type === 'FOUND' && claim.providedAnswer && (
                                        <div className="ic-secret-comparison">
                                            <div className="ic-secret-col">
                                                <div className="ic-secret-label">
                                                    <Lock size={12} /> Your Secret Detail
                                                </div>
                                                <div className="ic-secret-value yours">
                                                    {claim.secretDetailQuestion || 'Not set'}
                                                </div>
                                            </div>
                                            <div className="ic-secret-col">
                                                <div className="ic-secret-label">
                                                    <User size={12} /> Their Answer
                                                </div>
                                                <div className="ic-secret-value theirs">
                                                    {claim.providedAnswer}
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    {/* Message */}
                                    <div className="ic-claim-message">
                                        <MessageSquare size={14} />
                                        <p>{claim.message}</p>
                                    </div>

                                    {/* Actions (PENDING only) */}
                                    {claim.status === 'PENDING' && !hasAccepted && (
                                        <div className="ic-claim-actions">
                                            <button
                                                className="ic-approve-btn"
                                                disabled={actionLoading === claim.id}
                                                onClick={() => handleApprove(claim.id)}
                                            >
                                                <Check size={16} /> {actionLoading === claim.id ? 'Processing...' : 'Approve'}
                                            </button>
                                            <button
                                                className="ic-reject-btn"
                                                disabled={actionLoading === claim.id}
                                                onClick={() => handleReject(claim.id)}
                                            >
                                                <X size={16} /> {actionLoading === claim.id ? 'Processing...' : 'Reject'}
                                            </button>
                                        </div>
                                    )}

                                    {/* Chat link for accepted claims */}
                                    {(claim.status === 'ACCEPTED' || claim.status === 'COMPLETED' || claim.status === 'HANDED_OVER') && claim.chatId && (
                                        <div className="ic-claim-actions">
                                            <button
                                                className="ic-chat-btn"
                                                onClick={() => navigate(`/messages?chatId=${claim.chatId}`)}
                                            >
                                                <MessageSquare size={16} /> Open Chat
                                            </button>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <EmptyState
                            title="No claims yet"
                            message="No one has submitted a claim on this item yet."
                            actionLabel="Back to Profile"
                            onAction={() => navigate('/profile')}
                        />
                    )}

                    {totalPages > 1 && (
                        <div className="ic-pagination">
                            <button
                                className="ic-page-btn"
                                disabled={page === 0}
                                onClick={() => setPage((p) => p - 1)}
                            >
                                <ChevronLeft size={16} /> Previous
                            </button>
                            <span className="ic-page-info">
                                Page {page + 1} of {totalPages}
                            </span>
                            <button
                                className="ic-page-btn"
                                disabled={page >= totalPages - 1}
                                onClick={() => setPage((p) => p + 1)}
                            >
                                Next <ChevronRight size={16} />
                            </button>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default IncomingClaims;
