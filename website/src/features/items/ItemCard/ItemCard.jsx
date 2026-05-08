import { useState } from "react";
import { MapPin, Clock, EyeOff, ChevronLeft, ChevronRight } from "lucide-react";
import { timeAgo } from "../../../shared/utils/timeAgo";
import StatusBadge from "../../../shared/components/StatusBadge/StatusBadge";
import "./ItemCard.css";

function ItemCard({ item, onClick, variant = "default" }) {
  const [activeIndex, setActiveIndex] = useState(0);

  const isFound = item.type === "FOUND";
  
  // Get all images if available, otherwise fallback to single image or placeholder
  const images = item.imageUrls?.length > 0 ? item.imageUrls : (item.imageUrl ? [item.imageUrl] : ["https://picsum.photos/seed/placeholder/400/300"]);
  const hasMultipleImages = images.length > 1;
  const activeImage = images[activeIndex] || images[0];

  // Support both API (campus object) and mock (school object) data shapes
  const schoolName = item.campus?.name || item.school?.shortName || "";
  // Support both API (location string) and mock (locationDescription string)
  const locationText = item.location || item.locationDescription || "";

  const handlePrevImage = (e) => {
    e.stopPropagation();
    setActiveIndex((prev) => (prev === 0 ? images.length - 1 : prev - 1));
  };

  const handleNextImage = (e) => {
    e.stopPropagation();
    setActiveIndex((prev) => (prev === images.length - 1 ? 0 : prev + 1));
  };

  return (
    <div
      className={`item-card glass ${variant === "compact" ? "compact-card" : ""} ${variant === "snapshot" ? "snapshot-card" : ""}`}
      onClick={() => onClick?.(item.id)}
    >
      <div className={`item-card-image ${variant === "snapshot" && isFound ? "snapshot-reveal" : ""}`}>
        <img
          src={activeImage}
          alt={item.title}
          className={isFound ? "blurred" : ""}
          loading="lazy"
        />
        
        {/* Gallery Navigation overlay */}
        {hasMultipleImages && (
          <>
            <button type="button" className="card-gallery-nav prev" onClick={handlePrevImage} aria-label="Previous image">
              <ChevronLeft size={16} />
            </button>
            <button type="button" className="card-gallery-nav next" onClick={handleNextImage} aria-label="Next image">
              <ChevronRight size={16} />
            </button>
            <div className="card-gallery-dots">
              {images.map((_, idx) => (
                <span key={idx} className={`card-gallery-dot ${idx === activeIndex ? "active" : ""}`} />
              ))}
            </div>
          </>
        )}
        <span className={`type-badge ${item.type.toLowerCase()}`}>
          {item.type}
        </span>
        {isFound && variant !== "snapshot" && <span className="blur-label">Image protected</span>}
        {isFound && variant === "snapshot" && (
          <div className="reveal-overlay">
            <div className="reveal-badge">
              <EyeOff size={14} /> Click to reveal
            </div>
          </div>
        )}
      </div>
      <div className="item-card-body">
        <div className="item-card-header">
          <h3 className="item-card-title">{item.title}</h3>
          {item.status && item.status !== 'ACTIVE' && (
            <StatusBadge status={item.status} />
          )}
        </div>
        <div className="item-card-tags">
          <span className="tag category-tag">{item.category}</span>
          {schoolName && <span className="tag school-tag">{schoolName}</span>}
        </div>
        <div className="item-card-meta">
          {locationText && (
            <span className="meta-item">
              <MapPin size={14} />
              {locationText}
            </span>
          )}
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
