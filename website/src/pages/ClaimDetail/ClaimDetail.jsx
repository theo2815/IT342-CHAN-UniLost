import { useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle, Circle, Clock, MapPin, Lock, MessageSquare, User, HandHelping } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import { getClaimById, timeAgo } from '../../mockData/claims';
import './ClaimDetail.css';

function ClaimDetail() {
    const { claimId } = useParams();
    const navigate = useNavigate();

    const claim = useMemo(() => getClaimById(claimId), [claimId]);

    if (!claim) {
        return (
            <div className="claim-detail-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <h2>Claim Not Found</h2>
                            <button className="back-btn" onClick={() => navigate('/my-claims')}>
                                <ArrowLeft size={18} /> Back to My Claims
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    // Mock: current user is u1
    const isClaimant = claim.claimantId === 'u1';
    const isPoster = claim.posterId === 'u1';

    const getInitials = (name) => {
        const parts = name.split(' ');
        return parts.map((p) => p.charAt(0)).join('').toUpperCase();
    };

    // Handover stepper logic
    const getStepperState = () => {
        if (claim.status !== 'APPROVED' && claim.status !== 'HANDED_OVER') return null;
        const { posterConfirmed, claimantConfirmed } = claim.handover;
        let currentStep = 1; // Claim Approved
        if (posterConfirmed) currentStep = 2;
        if (posterConfirmed && claimantConfirmed) currentStep = 4;
        else if (claimantConfirmed && !posterConfirmed) currentStep = 1;
        if (claim.status === 'HANDED_OVER') currentStep = 4;

        return {
            currentStep,
            posterConfirmed,
            claimantConfirmed,
            canConfirm: (isClaimant && !claimantConfirmed) || (isPoster && !posterConfirmed),
        };
    };

    const stepper = getStepperState();

    const steps = [
        { label: 'Claim Approved', icon: CheckCircle },
        { label: 'Poster Confirms', icon: User },
        { label: 'Claimant Confirms', icon: User },
        { label: 'Handed Over', icon: HandHelping },
    ];

    const handleConfirmHandover = () => {
        alert('Handover confirmed! (Mock action)');
    };

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
                            <img src={claim.itemImageUrl} alt={claim.itemTitle} className="cd-item-thumb" />
                            <div>
                                <span className={`claim-type-badge ${claim.itemType.toLowerCase()}`}>
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
                            <div className="cd-party-avatar poster">{getInitials(claim.posterName)}</div>
                            <div>
                                <span className="cd-party-role">Posted by</span>
                                <span className="cd-party-name">{claim.posterName}</span>
                            </div>
                        </div>
                        <div className="cd-party glass">
                            <div className="cd-party-avatar claimant">{getInitials(claim.claimantName)}</div>
                            <div>
                                <span className="cd-party-role">Claimed by</span>
                                <span className="cd-party-name">{claim.claimantName} ({claim.claimantSchool})</span>
                            </div>
                        </div>
                    </div>

                    {/* Claim Content */}
                    <div className="cd-content glass">
                        {claim.secretDetailAnswer && (
                            <div className="cd-secret">
                                <div className="cd-section-label">
                                    <Lock size={14} /> Secret Detail Answer
                                </div>
                                <p>{claim.secretDetailAnswer}</p>
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

                    {claim.status === 'REJECTED' && (
                        <div className="cd-status-message rejected">
                            <Circle size={20} />
                            <div>
                                <h3>Claim Rejected</h3>
                                <p>The poster did not approve this claim. You may submit claims on other items.</p>
                            </div>
                        </div>
                    )}

                    {/* Handover Stepper (APPROVED / HANDED_OVER) */}
                    {stepper && (
                        <div className="cd-handover glass">
                            <h3>Handover Progress</h3>

                            <div className="cd-stepper">
                                {steps.map((step, index) => {
                                    const stepNum = index + 1;
                                    const isCompleted = stepNum < stepper.currentStep || stepper.currentStep === 4;
                                    const isActive = stepNum === stepper.currentStep && stepper.currentStep < 4;
                                    const StepIcon = step.icon;

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
                            ) : stepper.posterConfirmed && !stepper.claimantConfirmed && isClaimant ? (
                                <p className="cd-waiting-text">The poster has confirmed. Please confirm once you receive the item.</p>
                            ) : stepper.claimantConfirmed && !stepper.posterConfirmed && isPoster ? (
                                <p className="cd-waiting-text">The claimant has confirmed. Please confirm the handover.</p>
                            ) : (
                                <p className="cd-waiting-text">Both parties need to confirm the handover.</p>
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
