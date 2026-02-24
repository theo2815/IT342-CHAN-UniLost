import { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, AlertTriangle, Check, X, Lock, MessageSquare, User } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import { mockItems } from '../../mockData/items';
import { getClaimsForItem, timeAgo } from '../../mockData/claims';
import './IncomingClaims.css';

function IncomingClaims() {
    const { itemId } = useParams();
    const navigate = useNavigate();

    const item = mockItems.find((i) => i.id === itemId);
    const claims = useMemo(() => getClaimsForItem(itemId), [itemId]);

    // Mock secret detail for the item (would come from backend)
    const itemSecretDetail = item?.type === 'FOUND'
        ? 'There is a small scratch on the top-right corner of the screen'
        : null;

    const hasApproved = claims.some((c) => c.status === 'APPROVED' || c.status === 'HANDED_OVER');

    if (!item) {
        return (
            <div className="incoming-claims-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <h2>Item Not Found</h2>
                            <button className="back-btn" onClick={() => navigate('/my-items')}>
                                <ArrowLeft size={18} /> Back to My Items
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const getInitials = (name) => {
        const parts = name.split(' ');
        return parts.map((p) => p.charAt(0)).join('').toUpperCase();
    };

    const handleApprove = (claimId) => {
        alert(`Claim ${claimId} approved! (Mock action)`);
    };

    const handleReject = (claimId) => {
        alert(`Claim ${claimId} rejected. (Mock action)`);
    };

    return (
        <div className="incoming-claims-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <button className="back-link" onClick={() => navigate('/my-items')}>
                        <ArrowLeft size={18} /> Back to My Items
                    </button>

                    {/* Item Summary */}
                    <div className="ic-item-summary glass">
                        <img src={item.imageUrl} alt={item.title} className="ic-item-thumb" />
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
                    {!hasApproved && claims.length > 0 && (
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
                                    {item.type === 'FOUND' && claim.secretDetailAnswer && (
                                        <div className="ic-secret-comparison">
                                            <div className="ic-secret-col">
                                                <div className="ic-secret-label">
                                                    <Lock size={12} /> Your Secret Detail
                                                </div>
                                                <div className="ic-secret-value yours">
                                                    {itemSecretDetail || 'Not set'}
                                                </div>
                                            </div>
                                            <div className="ic-secret-col">
                                                <div className="ic-secret-label">
                                                    <User size={12} /> Their Answer
                                                </div>
                                                <div className="ic-secret-value theirs">
                                                    {claim.secretDetailAnswer}
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
                                    {claim.status === 'PENDING' && !hasApproved && (
                                        <div className="ic-claim-actions">
                                            <button
                                                className="ic-approve-btn"
                                                onClick={() => handleApprove(claim.id)}
                                            >
                                                <Check size={16} /> Approve
                                            </button>
                                            <button
                                                className="ic-reject-btn"
                                                onClick={() => handleReject(claim.id)}
                                            >
                                                <X size={16} /> Reject
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
                            actionLabel="Back to My Items"
                            onAction={() => navigate('/my-items')}
                        />
                    )}
                </div>
            </main>
        </div>
    );
}

export default IncomingClaims;
