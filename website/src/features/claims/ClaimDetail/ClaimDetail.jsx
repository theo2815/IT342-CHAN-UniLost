import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle, Circle, Clock, Lock, MessageSquare, XCircle } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import StatusBadge from '../../../shared/components/StatusBadge/StatusBadge';
import claimService from '../claimService';
import authService from '../../auth/authService';
import { timeAgo } from '../../../shared/utils/timeAgo';
import './ClaimDetail.css';

function ClaimDetail() {
    const { claimId } = useParams();
    const navigate = useNavigate();
    const currentUser = authService.getCurrentUser();

    const [claim, setClaim] = useState(null);
    const [loading, setLoading] = useState(true);
    const [cancelLoading, setCancelLoading] = useState(false);
    const [error, setError] = useState('');

    const handleGoBack = () => {
        if (window.history.state && window.history.state.idx > 0) {
            navigate(-1);
        } else {
            navigate('/profile?tab=claims');
        }
    };

    useEffect(() => {
        const controller = new AbortController();
        const fetchClaim = async () => {
            setLoading(true);
            const result = await claimService.getClaimById(claimId);
            if (controller.signal.aborted) return;
            if (result.success) {
                setClaim(result.data);
            } else {
                setError(result.error);
            }
            setLoading(false);
        };
        fetchClaim();
        return () => controller.abort();
    }, [claimId]);

    if (loading) {
        return (
            <div className="claim-detail-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state"><p>Loading...</p></div>
                    </div>
                </main>
            </div>
        );
    }

    if (!claim) {
        return (
            <div className="claim-detail-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <h2>{error || 'Claim Not Found'}</h2>
                            <button className="back-btn" onClick={handleGoBack}>
                                <ArrowLeft size={18} /> Back to My Claims
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const isClaimant = currentUser && claim.claimantId === currentUser.id;
    const isPoster = currentUser && claim.finderId === currentUser.id;

    const getInitials = (name) => {
        if (!name) return '??';
        const parts = name.split(' ');
        return parts.map((p) => p.charAt(0)).join('').toUpperCase();
    };

    const handleCancelClaim = async () => {
        if (cancelLoading) return;
        setCancelLoading(true);
        const result = await claimService.cancelClaim(claim.id);
        if (result.success) {
            setClaim(result.data);
        } else {
            setError(result.error);
        }
        setCancelLoading(false);
    };

    const imageUrl = claim.itemImageUrl || 'https://picsum.photos/seed/placeholder/400/300';

    return (
        <div className="claim-detail-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <button className="back-link" onClick={handleGoBack}>
                        <ArrowLeft size={18} /> Back to My Claims
                    </button>

                    {/* Claim Summary Card */}
                    <div className="cd-summary glass">
                        <div className="cd-summary-left">
                            <img src={imageUrl} alt={claim.itemTitle} className="cd-item-thumb" />
                            <div>
                                <span className={`claim-type-badge ${claim.itemType?.toLowerCase()}`}>
                                    {claim.itemType}
                                </span>
                                <h2>{claim.itemTitle}</h2>
                                <p className="cd-claim-date">Claimed {timeAgo(claim.createdAt)}</p>
                            </div>
                        </div>
                        <div className="cd-status-badge">
                            <StatusBadge status={claim.status} />
                        </div>
                    </div>

                    {/* Parties */}
                    <div className="cd-parties">
                        <div className="cd-party glass">
                            <div className="cd-party-avatar poster">{getInitials(claim.finderName)}</div>
                            <div>
                                <span className="cd-party-role">Posted by</span>
                                <span className="cd-party-name">{claim.finderName}</span>
                            </div>
                        </div>
                        <div className="cd-party glass">
                            <div className="cd-party-avatar claimant">{getInitials(claim.claimantName)}</div>
                            <div>
                                <span className="cd-party-role">Claimed by</span>
                                <span className="cd-party-name">{claim.claimantName}{claim.claimantSchool ? ` (${claim.claimantSchool})` : ''}</span>
                            </div>
                        </div>
                    </div>

                    {/* Claim Content */}
                    <div className="cd-content glass">
                        {claim.providedAnswer && (
                            <div className="cd-secret">
                                <div className="cd-section-label">
                                    <Lock size={14} /> Secret Detail Answer
                                </div>
                                <p>{claim.providedAnswer}</p>
                            </div>
                        )}
                        <div className="cd-message">
                            <div className="cd-section-label">
                                <MessageSquare size={14} /> Message
                            </div>
                            <p>{claim.message}</p>
                        </div>
                    </div>

                    {/* Status-specific content */}
                    {claim.status === 'PENDING' && (
                        <div className="cd-status-message pending">
                            <Clock size={20} />
                            <div>
                                <h3>Waiting for Review</h3>
                                <p>The poster has not reviewed your claim yet. You will be notified when they respond.</p>
                            </div>
                        </div>
                    )}

                    {claim.status === 'PENDING' && isClaimant && (
                        <div className="cd-cancel-section">
                            <button
                                className="cd-cancel-btn"
                                disabled={cancelLoading}
                                onClick={handleCancelClaim}
                            >
                                <XCircle size={18} />
                                {cancelLoading ? 'Cancelling...' : 'Cancel Claim'}
                            </button>
                        </div>
                    )}

                    {claim.status === 'REJECTED' && (
                        <div className="cd-status-message rejected">
                            <Circle size={20} />
                            <div>
                                <h3>Claim Rejected</h3>
                                <p>The poster did not approve this claim. You may submit claims on other items.</p>
                            </div>
                        </div>
                    )}

                    {claim.status === 'CANCELLED' && (
                        <div className="cd-status-message rejected">
                            <Circle size={20} />
                            <div>
                                <h3>Claim Cancelled</h3>
                                <p>You cancelled this claim.</p>
                            </div>
                        </div>
                    )}

                    {/* Accepted — direct to chat for handover */}
                    {claim.status === 'ACCEPTED' && (
                        <div className="cd-status-message accepted">
                            <CheckCircle size={20} />
                            <div>
                                <h3>Claim Accepted</h3>
                                <p>Coordinate the handover with the poster via chat. Once the item is returned, both parties will confirm in the chat.</p>
                            </div>
                        </div>
                    )}

                    {(claim.status === 'ACCEPTED' || claim.status === 'COMPLETED' || claim.status === 'HANDED_OVER') && claim.chatId && (
                        <div className="cd-chat-section">
                            <button
                                className="cd-chat-btn"
                                onClick={() => navigate(`/messages?chatId=${claim.chatId}`)}
                            >
                                <MessageSquare size={18} />
                                Open Chat
                            </button>
                        </div>
                    )}

                    {(claim.status === 'COMPLETED' || claim.status === 'HANDED_OVER') && (
                        <div className="cd-status-message completed">
                            <CheckCircle size={20} />
                            <div>
                                <h3>Item Returned</h3>
                                <p>This item has been successfully returned to its owner. Thank you!</p>
                            </div>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default ClaimDetail;
