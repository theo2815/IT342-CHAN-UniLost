import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, Clock, Tag, Share2, Flag, Hand, Search, X, CheckCircle2, User, MessageSquare, Edit3, Trash2 } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import ItemCard from '../../components/ItemCard';
import ClaimModal from '../../components/ClaimModal';
import { Alert, Button, Card } from '../../components/ui';
import { timeAgo } from '../../utils/timeAgo';
import authService from '../../services/authService';
import itemService from '../../services/itemService';
import claimService from '../../services/claimService';
import adminService from '../../services/adminService';
import './ItemDetail.css';

function ItemDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const currentUser = authService.getCurrentUser();

    const [item, setItem] = useState(null);
    const [relatedItems, setRelatedItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [fetchError, setFetchError] = useState('');
    const [existingClaim, setExistingClaim] = useState(null);
    const [showClaimModal, setShowClaimModal] = useState(false);
    const [showFlagModal, setShowFlagModal] = useState(false);
    const [flagReason, setFlagReason] = useState('');
    const [flagLoading, setFlagLoading] = useState(false);
    const [flagMessage, setFlagMessage] = useState('');

    useEffect(() => {
        const fetchItem = async () => {
            setLoading(true);
            setFetchError('');
            const result = await itemService.getItemById(id);
            if (result.success) {
                setItem(result.data);

                // Fetch existing claims to see if user already claimed this
                if (currentUser && result.data.reporterId !== currentUser.id) {
                    const myClaimsResult = await claimService.getMyClaims(0, 100);
                    if (myClaimsResult.success && Array.isArray(myClaimsResult.data)) {
                        const claim = myClaimsResult.data.find(c => c.itemId === id);
                        if (claim) {
                            setExistingClaim(claim);
                        }
                    }
                }

                const relatedResult = await itemService.getItems({
                    category: result.data.category,
                    status: 'ACTIVE',
                    size: 4,
                });
                if (relatedResult.success) {
                    setRelatedItems(
                        relatedResult.data.content.filter((i) => i.id !== id).slice(0, 3)
                    );
                }
            } else {
                setFetchError(result.error);
            }
            setLoading(false);
        };
        fetchItem();
    }, [id]);

    if (loading) {
        return (
            <div className="item-detail-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <p>Loading item...</p>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    if (!item) {
        return (
            <div className="item-detail-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <h2>{fetchError ? 'Something went wrong' : 'Item Not Found'}</h2>
                            <p>{fetchError || 'The item you are looking for does not exist or has been removed.'}</p>
                            <button className="back-btn" onClick={handleGoBack}>
                                <ArrowLeft size={18} /> Go Back
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const isOwner = currentUser && item.reporterId === currentUser.id;
    const isFound = item.type === 'FOUND';
    const isResolved = item.status === 'RETURNED' || item.status === 'COMPLETED';
    const imageUrl = item.imageUrls?.[0] || 'https://picsum.photos/seed/placeholder/600/400';

    const getInitials = (fullName) => {
        if (!fullName) return '??';
        return fullName.split(' ').map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
    };

    const handleGoBack = () => {
        if (window.history.state && window.history.state.idx > 0) {
            navigate(-1);
        } else {
            navigate('/items');
        }
    };

    const handleFlag = async () => {
        if (!flagReason) return;
        setFlagLoading(true);
        setFlagMessage('');
        const result = await adminService.flagItem(id, flagReason);
        if (result.success) {
            setFlagMessage('Item reported successfully. An admin will review it.');
            setTimeout(() => { setShowFlagModal(false); setFlagMessage(''); setFlagReason(''); }, 2000);
        } else {
            setFlagMessage(typeof result.error === 'string' ? result.error : 'Failed to report item');
        }
        setFlagLoading(false);
    };

    const handleDelete = async () => {
        if (window.confirm('Are you sure you want to delete this item?')) {
            const result = await itemService.deleteItem(id);
            if (result.success) {
                navigate('/items');
            }
        }
    };

    return (
        <div className="item-detail-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <button className="back-link" onClick={handleGoBack}>
                        <ArrowLeft size={18} /> Go Back
                    </button>

                    {isResolved ? (
                        /* Completed / Returned State - Read-only history view */
                        <div className="detail-layout resolved-layout">
                            {/* Left: Image */}
                            <div className="detail-image-section">
                                <div className="detail-image-wrapper resolved-image">
                                    <img
                                        src={imageUrl}
                                        alt={item.title}
                                    />
                                    <span className={`type-badge ${item.type.toLowerCase()}`}>
                                        {item.type}
                                    </span>
                                </div>
                            </div>

                            {/* Right: Completed Details */}
                            <div className="detail-info-section">
                                {/* Resolved Banner */}
                                <Alert
                                    type="success"
                                    icon={CheckCircle2}
                                    title={item.status === 'RETURNED'
                                        ? 'Item Successfully Returned'
                                        : 'Item Marked as Completed'}
                                    className="resolved-banner"
                                >
                                    This item has been resolved and is no longer active.
                                </Alert>

                                <div className="detail-badges">
                                    <StatusBadge type={item.type} />
                                    <StatusBadge status={item.status} />
                                </div>

                                <h1 className="detail-title">{item.title}</h1>

                                <p className="detail-description">{item.description}</p>

                                {/* Completion Info */}
                                <Card glass padded className="resolved-info-card">
                                    <h3 className="resolved-info-title">Completion Details</h3>
                                    <div className="meta-row">
                                        <Calendar size={16} />
                                        <span className="meta-label">Posted</span>
                                        <span className="meta-value">{timeAgo(item.createdAt)}</span>
                                    </div>
                                    {item.updatedAt && (
                                        <div className="meta-row">
                                            <CheckCircle2 size={16} />
                                            <span className="meta-label">Resolved</span>
                                            <span className="meta-value">{timeAgo(item.updatedAt)}</span>
                                        </div>
                                    )}
                                    <div className="meta-row">
                                        <Tag size={16} />
                                        <span className="meta-label">Category</span>
                                        <span className="meta-value">{item.category}</span>
                                    </div>
                                    <div className="meta-row">
                                        <MapPin size={16} />
                                        <span className="meta-label">Location</span>
                                        <span className="meta-value">{item.location || 'Not specified'}</span>
                                    </div>
                                    {item.campus && (
                                        <div className="meta-row">
                                            <Clock size={16} />
                                            <span className="meta-label">Campus</span>
                                            <span className="meta-value">{item.campus.name}</span>
                                        </div>
                                    )}
                                </Card>

                                {/* Poster Info */}
                                <Card glass padded className="poster-card">
                                    <div className="poster-avatar">
                                        {getInitials(item.reporter?.fullName)}
                                    </div>
                                    <div className="poster-info">
                                        <span className="poster-name">
                                            {item.reporter?.fullName || 'Unknown User'}
                                        </span>
                                        <span className="poster-school">{item.campus?.name || ''}</span>
                                    </div>
                                </Card>

                                {/* Read-only actions */}
                                <div className="detail-actions resolved-actions">
            
                                    {isOwner && (
                                        <Button variant="secondary" icon={MessageSquare} onClick={() => navigate(`/my-items/${item.id}/claims`)}>
                                            View Claims
                                        </Button>
                                    )}
                                </div>
                            </div>
                        </div>
                    ) : (
                    <div className="detail-layout">
                        {/* Left: Image */}
                        <div className="detail-image-section">
                            <div className="detail-image-wrapper">
                                <img
                                    src={imageUrl}
                                    alt={item.title}
                                    className={isFound ? 'blurred' : ''}
                                />
                                {isFound && (
                                    <div className="blur-overlay">
                                        <span>Image blurred to protect the owner</span>
                                    </div>
                                )}
                                <span className={`type-badge ${item.type.toLowerCase()}`}>
                                    {item.type}
                                </span>
                            </div>
                        </div>

                        {/* Right: Details */}
                        <div className="detail-info-section">
                            <div className="detail-badges">
                                <StatusBadge type={item.type} />
                                <StatusBadge status={item.status} />
                            </div>

                            <h1 className="detail-title">{item.title}</h1>

                            <p className="detail-description">{item.description}</p>

                            <div className="detail-meta glass">
                                <div className="meta-row">
                                    <Tag size={16} />
                                    <span className="meta-label">Category</span>
                                    <span className="meta-value">{item.category}</span>
                                </div>
                                <div className="meta-row">
                                    <MapPin size={16} />
                                    <span className="meta-label">Location</span>
                                    <span className="meta-value">{item.location || 'Not specified'}</span>
                                </div>
                                <div className="meta-row">
                                    <Calendar size={16} />
                                    <span className="meta-label">Posted</span>
                                    <span className="meta-value">{timeAgo(item.createdAt)}</span>
                                </div>
                                {item.campus && (
                                    <div className="meta-row">
                                        <Clock size={16} />
                                        <span className="meta-label">Campus</span>
                                        <span className="meta-value">{item.campus.name}</span>
                                    </div>
                                )}
                            </div>

                            {/* Poster Info */}
                            <Card glass padded className="poster-card">
                                <div className="poster-avatar">
                                    {getInitials(item.reporter?.fullName)}
                                </div>
                                <div className="poster-info">
                                    <span className="poster-name">
                                        {item.reporter?.fullName || 'Unknown User'}
                                    </span>
                                    <span className="poster-school">{item.campus?.name || ''}</span>
                                </div>
                            </Card>

                            {/* Action Buttons */}
                            <div className="detail-actions">
                                {isOwner ? (
                                    <>
                                        <Button variant="primary" icon={Edit3} onClick={() => navigate(`/post-item?edit=${item.id}`)}>Edit Item</Button>
                                        <Button variant="danger" icon={Trash2} onClick={handleDelete}>Delete Listing</Button>
                                    </>
                                ) : (
                                    <>
                                        {currentUser ? (
                                            existingClaim ? (
                                                <Button 
                                                    variant="primary" 
                                                    icon={MessageSquare} 
                                                    onClick={() => navigate(existingClaim.chatId ? `/messages?chatId=${existingClaim.chatId}` : `/profile?tab=claims`)}
                                                >
                                                    {existingClaim.chatId ? 'Open Chat' : 'Claim Submitted'}
                                                </Button>
                                            ) : (
                                                <Button variant="primary" icon={isFound ? Hand : Search} onClick={() => setShowClaimModal(true)}>
                                                    {isFound ? "I Think This Is Mine" : "I Found This"}
                                                </Button>
                                            )
                                        ) : (
                                            <Button variant="primary" onClick={() => navigate('/login', { state: { from: `/items/${id}` } })}>
                                                Log in to Claim
                                            </Button>
                                        )}
                                        <Button variant="secondary" icon={Share2}>
                                            Share
                                        </Button>
                                        {currentUser && (
                                            <Button variant="secondary" icon={Flag} onClick={() => setShowFlagModal(true)}>
                                                Report
                                            </Button>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                    )}

                    {/* Related Items */}
                    {relatedItems.length > 0 && (
                        <section className="related-section">
                            <h2>Related Items</h2>
                            <div className="related-grid">
                                {relatedItems.map((relItem) => (
                                    <ItemCard
                                        key={relItem.id}
                                        item={relItem}
                                        onClick={(itemId) => navigate(`/items/${itemId}`)}
                                    />
                                ))}
                            </div>
                        </section>
                    )}
                </div>
            </main>

            {/* Claim Modal */}
            {showClaimModal && (
                <ClaimModal
                    item={item}
                    onClose={() => setShowClaimModal(false)}
                />
            )}

            {/* Flag/Report Modal */}
            {showFlagModal && (
                <div className="modal-overlay" onClick={() => setShowFlagModal(false)}>
                    <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <Flag size={20} color="#ef4444" />
                            <h3>Report Item</h3>
                            <button className="modal-close" onClick={() => setShowFlagModal(false)}>
                                <X size={18} />
                            </button>
                        </div>
                        <div className="modal-body">
                            <p>Why are you reporting this item?</p>
                            <div className="flag-options">
                                {['SPAM', 'INAPPROPRIATE', 'FAKE', 'DUPLICATE'].map(reason => (
                                    <label key={reason} className={`flag-option ${flagReason === reason ? 'selected' : ''}`}>
                                        <input
                                            type="radio"
                                            name="flagReason"
                                            value={reason}
                                            checked={flagReason === reason}
                                            onChange={(e) => setFlagReason(e.target.value)}
                                        />
                                        <span>{reason.charAt(0) + reason.slice(1).toLowerCase()}</span>
                                    </label>
                                ))}
                            </div>
                            {flagMessage && (
                                <p className={`flag-message ${flagMessage.includes('success') ? 'success' : 'error'}`}>
                                    {flagMessage}
                                </p>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn-secondary" onClick={() => setShowFlagModal(false)}>Cancel</button>
                            <button className="btn-danger" onClick={handleFlag} disabled={!flagReason || flagLoading}>
                                {flagLoading ? 'Submitting...' : 'Submit Report'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ItemDetail;
