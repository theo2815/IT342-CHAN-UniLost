import { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, Clock, Tag, User, Share2, Flag, Hand, Search } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import ItemCard from '../../components/ItemCard';
import ClaimModal from '../../components/ClaimModal';
import { mockItems, timeAgo, daysUntilExpiry } from '../../mockData/items';
import authService from '../../services/authService';
import './ItemDetail.css';

function ItemDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const currentUser = authService.getCurrentUser();

    const item = mockItems.find((i) => i.id === id);

    const relatedItems = useMemo(() => {
        if (!item) return [];
        return mockItems
            .filter((i) => i.id !== item.id && i.status === 'ACTIVE' && (i.category === item.category || i.school?.shortName === item.school?.shortName))
            .slice(0, 3);
    }, [item]);

    if (!item) {
        return (
            <div className="item-detail-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="not-found-state">
                            <h2>Item Not Found</h2>
                            <p>The item you are looking for does not exist or has been removed.</p>
                            <button className="back-btn" onClick={() => navigate('/items')}>
                                <ArrowLeft size={18} /> Back to Feed
                            </button>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    const [showClaimModal, setShowClaimModal] = useState(false);
    const isOwner = currentUser && item.postedBy?.id === 'u1'; // Mock: assume current user is u1
    const isFound = item.type === 'FOUND';
    const daysLeft = daysUntilExpiry(item.expiresAt);

    const getInitials = (firstName, lastName) => {
        return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`.toUpperCase();
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
                                    src={item.imageUrl}
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
                                    <span className="meta-value">{item.locationDescription}</span>
                                </div>
                                <div className="meta-row">
                                    <Calendar size={16} />
                                    <span className="meta-label">Posted</span>
                                    <span className="meta-value">{timeAgo(item.createdAt)}</span>
                                </div>
                                <div className="meta-row">
                                    <Clock size={16} />
                                    <span className="meta-label">Expires in</span>
                                    <span className="meta-value">{daysLeft > 0 ? `${daysLeft} days` : 'Expired'}</span>
                                </div>
                            </div>

                            {/* Poster Info */}
                            <div className="poster-card glass">
                                <div className="poster-avatar">
                                    {getInitials(item.postedBy?.firstName, item.postedBy?.lastName)}
                                </div>
                                <div className="poster-info">
                                    <span className="poster-name">
                                        {item.postedBy?.firstName} {item.postedBy?.lastName}
                                    </span>
                                    <span className="poster-school">{item.school?.name}</span>
                                </div>
                            </div>

                            {/* Action Buttons */}
                            <div className="detail-actions">
                                {isOwner ? (
                                    <>
                                        <button className="action-btn primary">Edit Item</button>
                                        <button className="action-btn success">Mark as Recovered</button>
                                        <button className="action-btn danger">Cancel Listing</button>
                                    </>
                                ) : (
                                    <>
                                        <button className="action-btn primary" onClick={() => setShowClaimModal(true)}>
                                            {isFound ? (
                                                <><Hand size={18} /> I Think This Is Mine</>
                                            ) : (
                                                <><Search size={18} /> I Found This</>
                                            )}
                                        </button>
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
