import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle, Circle, Clock, MapPin, Lock, MessageSquare, User, HandHelping, XCircle } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import claimService from '../../services/claimService';
import authService from '../../services/authService';
import { timeAgo } from '../../utils/timeAgo';
import './ClaimDetail.css';

function ClaimDetail() {
    const { claimId } = useParams();
    const navigate = useNavigate();
    const currentUser = authService.getCurrentUser();

    const [claim, setClaim] = useState(null);
    const [loading, setLoading] = useState(true);
    const [cancelLoading, setCancelLoading] = useState(false);
    const [error, setError] = useState('');

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
                            <button className="back-btn" onClick={() => navigate('/my-claims')}>
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

    // Handover stepper logic (Phase 7 — mock for now)
    const getStepperState = () => {
        if (claim.status !== 'ACCEPTED' && claim.status !== 'HANDED_OVER') return null;
        // Phase 7 will provide real handover data; for now show step 1 (Claim Accepted)
        return {
            currentStep: claim.status === 'HANDED_OVER' ? 4 : 1,
            posterConfirmed: false,
            claimantConfirmed: false,
            canConfirm: false,
        };
    };

    const stepper = getStepperState();

    const steps = [
        { label: 'Claim Accepted', icon: CheckCircle },
        { label: 'Poster Confirms', icon: User },
        { label: 'Claimant Confirms', icon: User },
        { label: 'Handed Over', icon: HandHelping },
    ];

    const handleConfirmHandover = () => {
        alert('Handover confirmed! (Coming in Phase 7)');
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
                    <button className="back-link" onClick={() => navigate('/my-claims')}>
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

                    {/* Handover Stepper (ACCEPTED / HANDED_OVER) — Phase 7 mock */}
                    {stepper && (
                        <div className="cd-handover glass">
                            <h3>Handover Progress</h3>

                            <div className="cd-stepper">
                                {steps.map((step, index) => {
                                    const stepNum = index + 1;
                                    const isCompleted = stepNum < stepper.currentStep || stepper.currentStep === 4;
                                    const isActive = stepNum === stepper.currentStep && stepper.currentStep < 4;

                                    return (
                                        <div key={index} className="cd-step-wrapper">
                                            <div className={`cd-step ${isCompleted ? 'completed' : ''} ${isActive ? 'active' : ''}`}>
                                                <div className="cd-step-circle">
                                                    {isCompleted ? (
                                                        <CheckCircle size={20} />
                                                    ) : (
                                                        <span>{stepNum}</span>
                                                    )}
                                                </div>
                                                <span className="cd-step-label">{step.label}</span>
                                            </div>
                                            {index < steps.length - 1 && (
                                                <div className={`cd-step-line ${isCompleted ? 'completed' : ''}`} />
                                            )}
                                        </div>
                                    );
                                })}
                            </div>

                            {/* Confirm button */}
                            {stepper.canConfirm && (
                                <div className="cd-confirm-section">
                                    <button className="cd-confirm-btn" onClick={handleConfirmHandover}>
                                        <CheckCircle size={18} />
                                        Confirm Handover
                                    </button>
                                    <p className="cd-confirm-hint">
                                        Confirm that you have {isClaimant ? 'received' : 'handed over'} the item.
                                    </p>
                                </div>
                            )}

                            {/* Status text */}
                            {stepper.currentStep === 4 ? (
                                <div className="cd-handover-complete">
                                    <CheckCircle size={18} />
                                    <span>Item successfully handed over!</span>
                                </div>
                            ) : (
                                <p className="cd-waiting-text">Handover process will be available in a future update.</p>
                            )}

                            {/* Suggested location */}
                            <div className="cd-location-hint">
                                <MapPin size={14} />
                                <span>Suggested meetup: Campus Security Office or Student Affairs</span>
                            </div>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default ClaimDetail;
