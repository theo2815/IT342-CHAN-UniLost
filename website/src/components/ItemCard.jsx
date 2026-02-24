import { MapPin, Clock } from 'lucide-react';
import { timeAgo } from '../mockData/items';
import './ItemCard.css';

function ItemCard({ item, onClick }) {
    const isFound = item.type === 'FOUND';

    return (
        <div className="item-card glass" onClick={() => onClick?.(item.id)}>
            <div className="item-card-image">
                <img
                    src={item.imageUrl}
                    alt={item.title}
                    className={isFound ? 'blurred' : ''}
                    loading="lazy"
                />
                <span className={`type-badge ${item.type.toLowerCase()}`}>
                    {item.type}
                </span>
                {isFound && (
                    <span className="blur-label">Image protected</span>
                )}
            </div>
            <div className="item-card-body">
                <h3 className="item-card-title">{item.title}</h3>
                <div className="item-card-tags">
                    <span className="tag category-tag">{item.category}</span>
                    <span className="tag school-tag">{item.school?.shortName}</span>
                </div>
                <div className="item-card-meta">
                    <span className="meta-item">
                        <MapPin size={14} />
                        {item.locationDescription}
                    </span>
                    <span className="meta-item">
                        <Clock size={14} />
                        {timeAgo(item.createdAt)}
                    </span>
                </div>
            </div>
        </div>
    );
}

export default ItemCard;
