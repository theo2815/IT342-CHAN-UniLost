import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, Clock, Tag, Share2, Flag, Hand, Search, X, CheckCircle2, User, MessageSquare, Edit3, Trash2, ExternalLink, ChevronLeft, ChevronRight, Shield, EyeOff, RotateCcw, AlertTriangle } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import StatusBadge from '../../../shared/components/StatusBadge/StatusBadge';
import ItemCard from '../ItemCard/ItemCard';
import ClaimModal from '../../claims/ClaimModal/ClaimModal';
import { Alert, Button, Card } from '../../../shared/components/ui';
import { timeAgo } from '../../../shared/utils/timeAgo';
import authService from '../../auth/authService';
import itemService from '../itemService';
import claimService from '../../claims/claimService';
import adminService from '../../admin/adminService';
import { ROLES } from '../../../shared/constants/roles';
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
    const [activeImageIndex, setActiveImageIndex] = useState(0);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteLoading, setDeleteLoading] = useState(false);

    // Admin state
    const isAdmin = currentUser?.role === ROLES.ADMIN;
    const [adminActionLoading, setAdminActionLoading] = useState(false);
    const [showAdminDeleteModal, setShowAdminDeleteModal] = useState(false);
    const [adminDeleteReason, setAdminDeleteReason] = useState('');

    useEffect(() => {
        const fetchItem = async () => {
            setLoading(true);
            setFetchError('');
            setActiveImageIndex(0);
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
    const images = item.imageUrls?.length > 0 ? item.imageUrls : ['https://picsum.photos/seed/placeholder/600/400'];
    const hasMultipleImages = images.length > 1;
    const activeImage = images[activeImageIndex] || images[0];

    const handlePrevImage = (e) => {
        e.stopPropagation();
        setActiveImageIndex((prev) => (prev === 0 ? images.length - 1 : prev - 1));
    };

    const handleNextImage = (e) => {
        e.stopPropagation();
        setActiveImageIndex((prev) => (prev === images.length - 1 ? 0 : prev + 1));
    };

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

    const handleDelete = () => {
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        setDeleteLoading(true);
        const result = await itemService.deleteItem(id);
        if (result.success) {
            navigate('/items');
        } else {
            console.error('Failed to delete item:', result.error);
            setDeleteLoading(false);
            setShowDeleteModal(false);
        }
    };

    const handleViewLocation = () => {
        const params = new URLSearchParams({
            lat: item.latitude,
            lng: item.longitude,
            itemId: item.id,
        });
        window.open(`/map?${params.toString()}`, '_blank');
    };

    // Admin actions
    const handleAdminStatusChange = async (newStatus) => {
        setAdminActionLoading(true);
        const result = await adminService.updateItemStatus(id, newStatus);
        if (result.success) {
            setItem(prev => ({ ...prev, status: newStatus }));
        }
        setAdminActionLoading(false);
    };

    const handleAdminDelete = async () => {
        setAdminActionLoading(true);
        const result = await adminService.forceDeleteItem(id);
        if (result.success) {
            navigate('/admin/items');
        }
        setAdminActionLoading(false);
        setShowAdminDeleteModal(false);
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
                            {/* Left: Image Gallery */}
                            <div className="detail-image-section">
                                <div className="detail-image-wrapper resolved-image">
                                    <img
                                        src={activeImage}
                                        alt={`${item.title} - Photo ${activeImageIndex + 1}`}
                                    />
                                    <span className={`type-badge ${item.type.toLowerCase()}`}>
                                        {item.type}
                                    </span>
                                    {hasMultipleImages && (
                                        <>
                                            <button className="gallery-nav gallery-prev" onClick={handlePrevImage} aria-label="Previous image">
                                                <ChevronLeft size={20} />
                                            </button>
                                            <button className="gallery-nav gallery-next" onClick={handleNextImage} aria-label="Next image">
                                                <ChevronRight size={20} />
                                            </button>
                                            <span className="gallery-counter">{activeImageIndex + 1} / {images.length}</span>
                                        </>
                                    )}
                                </div>
                                {hasMultipleImages && (
                                    <div className="gallery-thumbnails">
                                        {images.map((url, idx) => (
                                            <button
                                                key={idx}
                                                className={`gallery-thumb${idx === activeImageIndex ? ' active' : ''}`}
                                                onClick={() => setActiveImageIndex(idx)}
                                            >
                                                <img src={url} alt={`Thumbnail ${idx + 1}`} />
                                            </button>
                                        ))}
                                    </div>
                                )}
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
                                    {item.latitude != null && item.longitude != null && (
                                        <Button variant="secondary" icon={MapPin} onClick={handleViewLocation}>
                                            View Location
                                        </Button>
                                    )}
                                    {isOwner && (
                                        <Button variant="secondary" icon={MessageSquare} onClick={() => navigate(`/my-items/${item.id}/claims`)}>
                                            View Claims
                                        </Button>
                                    )}
                                </div>

                                {/* Admin Panel (Resolved) */}
                                {isAdmin && (
                                    <div className="admin-panel glass">
                                        <div className="admin-panel-header">
                                            <Shield size={18} />
                                            <h3>Admin Actions</h3>
                                        </div>
                                        {item.flagCount > 0 && (
                                            <div className="admin-report-section">
                                                <div className="report-header">
                                                    <AlertTriangle size={16} />
                                                    <span>{item.flagCount} Report{item.flagCount !== 1 ? 's' : ''}</span>
                                                </div>
                                                {item.flagReasons?.length > 0 && (
                                                    <div className="report-reasons">
                                                        {item.flagReasons.map((reason, i) => (
                                                            <span key={i} className="reason-badge">{reason}</span>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        )}
                                        <div className="admin-actions-grid">
                                            <Button
                                                variant="danger"
                                                icon={Trash2}
                                                onClick={() => setShowAdminDeleteModal(true)}
                                                disabled={adminActionLoading}
                                            >
                                                Delete Item
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    ) : (
                    <div className="detail-layout">
                        {/* Left: Image Gallery */}
                        <div className="detail-image-section">
                            <div className="detail-image-wrapper">
                                <img
                                    src={activeImage}
                                    alt={`${item.title} - Photo ${activeImageIndex + 1}`}
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
                                {hasMultipleImages && (
                                    <>
                                        <button className="gallery-nav gallery-prev" onClick={handlePrevImage} aria-label="Previous image">
                                            <ChevronLeft size={20} />
                                        </button>
                                        <button className="gallery-nav gallery-next" onClick={handleNextImage} aria-label="Next image">
                                            <ChevronRight size={20} />
                                        </button>
                                        <span className="gallery-counter">{activeImageIndex + 1} / {images.length}</span>
                                    </>
                                )}
                            </div>
                            {hasMultipleImages && (
                                <div className="gallery-thumbnails">
                                    {images.map((url, idx) => (
                                        <button
                                            key={idx}
                                            className={`gallery-thumb${idx === activeImageIndex ? ' active' : ''}`}
                                            onClick={() => setActiveImageIndex(idx)}
                                        >
                                            <img src={url} alt={`Thumbnail ${idx + 1}`} className={isFound ? 'blurred' : ''} />
                                        </button>
                                    ))}
                                </div>
                            )}
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
                                            ) : item.status === 'ACTIVE' ? (
                                                <Button variant="primary" icon={isFound ? Hand : Search} onClick={() => setShowClaimModal(true)}>
                                                    {isFound ? "I Think This Is Mine" : "I Found This"}
                                                </Button>
                                            ) : null
                                        ) : item.status === 'ACTIVE' ? (
                                            <Button variant="primary" onClick={() => navigate('/login', { state: { from: `/items/${id}` } })}>
                                                Log in to Claim
                                            </Button>
                                        ) : null}
                                        {item.latitude != null && item.longitude != null && (
                                            <Button variant="secondary" icon={MapPin} onClick={handleViewLocation}>
                                                View Location
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

                            {/* Admin Panel */}
                            {isAdmin && (
                                <div className="admin-panel glass">
                                    <div className="admin-panel-header">
                                        <Shield size={18} />
                                        <h3>Admin Actions</h3>
                                    </div>

                                    {/* Flag Report Section */}
                                    {item.flagCount > 0 && (
                                        <div className="admin-report-section">
                                            <div className="report-header">
                                                <AlertTriangle size={16} />
                                                <span>{item.flagCount} Report{item.flagCount !== 1 ? 's' : ''}</span>
                                            </div>
                                            {item.flagReasons?.length > 0 && (
                                                <div className="report-reasons">
                                                    {item.flagReasons.map((reason, i) => (
                                                        <span key={i} className="reason-badge">{reason}</span>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    )}

                                    {/* Status Actions */}
                                    <div className="admin-actions-grid">
                                        <Button
                                            variant="primary"
                                            icon={Edit3}
                                            onClick={() => navigate(`/post-item?edit=${item.id}`)}
                                        >
                                            Edit Item
                                        </Button>
                                        {item.status === 'ACTIVE' && (
                                            <Button
                                                variant="secondary"
                                                icon={EyeOff}
                                                onClick={() => handleAdminStatusChange('HIDDEN')}
                                                disabled={adminActionLoading}
                                            >
                                                Hide Item
                                            </Button>
                                        )}
                                        {item.status === 'HIDDEN' && (
                                            <Button
                                                variant="secondary"
                                                icon={RotateCcw}
                                                onClick={() => handleAdminStatusChange('ACTIVE')}
                                                disabled={adminActionLoading}
                                            >
                                                Unhide Item
                                            </Button>
                                        )}
                                        <Button
                                            variant="danger"
                                            icon={Trash2}
                                            onClick={() => setShowAdminDeleteModal(true)}
                                            disabled={adminActionLoading}
                                        >
                                            Delete Item
                                        </Button>
                                    </div>
                                </div>
                            )}
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

            {/* Delete Confirmation Modal */}
            {showDeleteModal && (
                <div className="modal-overlay" onClick={() => !deleteLoading && setShowDeleteModal(false)}>
                    <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <Trash2 size={20} color="#ef4444" />
                            <h3>Delete Listing</h3>
                            <button className="modal-close" onClick={() => !deleteLoading && setShowDeleteModal(false)} disabled={deleteLoading}>
                                <X size={18} />
                            </button>
                        </div>
                        <div className="modal-body">
                            <p style={{ marginBottom: '16px' }}>
                                Are you sure you want to delete <strong>"{item.title}"</strong>?
                            </p>
                            <p className="upload-hint">
                                This action cannot be undone. All claims and messages associated with this item will be removed.
                            </p>
                        </div>
                        <div className="modal-footer" style={{ marginTop: '24px' }}>
                            <button className="btn-secondary" onClick={() => setShowDeleteModal(false)} disabled={deleteLoading}>
                                Cancel
                            </button>
                            <button className="btn-danger" onClick={confirmDelete} disabled={deleteLoading}>
                                {deleteLoading ? <><span className="spin">⟳</span> Deleting...</> : 'Delete Item'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Admin Delete Confirmation Modal */}
            {showAdminDeleteModal && (
                <div className="modal-overlay" onClick={() => !adminActionLoading && setShowAdminDeleteModal(false)}>
                    <div className="modal-card glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <Shield size={20} color="#ef4444" />
                            <h3>Admin: Remove Item</h3>
                            <button className="modal-close" onClick={() => !adminActionLoading && setShowAdminDeleteModal(false)} disabled={adminActionLoading}>
                                <X size={18} />
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="admin-delete-summary">
                                <img src={item.imageUrls?.[0] || 'https://picsum.photos/seed/placeholder/600/400'} alt="" />
                                <div>
                                    <strong>{item.title}</strong>
                                    <span>Posted by {item.reporter?.fullName || 'Unknown'}</span>
                                </div>
                            </div>
                            <label>Reason for removal (optional)</label>
                            <textarea
                                value={adminDeleteReason}
                                onChange={(e) => setAdminDeleteReason(e.target.value)}
                                placeholder="Enter reason for removing this item..."
                                rows={3}
                            />
                        </div>
                        <div className="modal-footer">
                            <button className="btn-secondary" onClick={() => setShowAdminDeleteModal(false)} disabled={adminActionLoading}>
                                Cancel
                            </button>
                            <button className="btn-danger" onClick={handleAdminDelete} disabled={adminActionLoading}>
                                {adminActionLoading ? 'Removing...' : 'Confirm Remove'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ItemDetail;
