import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, Clock, Tag, Share2, Flag, Hand, Search, X } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import ItemCard from '../../components/ItemCard';
import ClaimModal from '../../components/ClaimModal';
import { timeAgo } from '../../utils/timeAgo';
import authService from '../../services/authService';
import itemService from '../../services/itemService';
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
                            <button className="back-btn" onClick={() => navigate('/items')}>
                                <ArrowLeft size={18} /> Back to Feed
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const isOwner = currentUser && item.reporterId === currentUser.id;
    const isFound = item.type === 'FOUND';
    const imageUrl = item.imageUrls?.[0] || 'https://picsum.photos/seed/placeholder/600/400';

    const getInitials = (fullName) => {
        if (!fullName) return '??';
        return fullName.split(' ').map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
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
                    <button className="back-link" onClick={() => navigate('/items')}>
                        <ArrowLeft size={18} /> Back to Feed
                    </button>

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
                            <div className="poster-card glass">
                                <div className="poster-avatar">
                                    {getInitials(item.reporter?.fullName)}
                                </div>
                                <div className="poster-info">
                                    <span className="poster-name">
                                        {item.reporter?.fullName || 'Unknown User'}
                                    </span>
                                    <span className="poster-school">{item.campus?.name || ''}</span>
                                </div>
                            </div>

                            {/* Action Buttons */}
                            <div className="detail-actions">
                                {isOwner ? (
                                    <>
                                        <button className="action-btn primary" onClick={() => navigate(`/post-item?edit=${item.id}`)}>Edit Item</button>
                                        <button className="action-btn danger" onClick={handleDelete}>Delete Listing</button>
                                    </>
                                ) : (
                                    <>
                                        {currentUser ? (
                                            <button className="action-btn primary" onClick={() => setShowClaimModal(true)}>
                                                {isFound ? (
                                                    <><Hand size={18} /> I Think This Is Mine</>
                                                ) : (
                                                    <><Search size={18} /> I Found This</>
                                                )}
                                            </button>
                                        ) : (
                                            <button className="action-btn primary" onClick={() => navigate('/login')}>
                                                Log in to Claim
                                            </button>
                                        )}
                                        <button className="action-btn secondary">
                                            <Share2 size={18} /> Share
                                        </button>
                                        {currentUser && (
                                            <button className="action-btn secondary" onClick={() => setShowFlagModal(true)}>
                                                <Flag size={18} /> Report
                                            </button>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>

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
