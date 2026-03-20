import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, Clock, Tag, Share2, Flag, Hand, Search } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import ItemCard from '../../components/ItemCard';
import ClaimModal from '../../components/ClaimModal';
import { timeAgo } from '../../utils/timeAgo';
import authService from '../../services/authService';
import itemService from '../../services/itemService';
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
                                        <button className="action-btn secondary">
                                            <Flag size={18} /> Report
                                        </button>
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
        </div>
    );
}

export default ItemDetail;
