import { MapPin, Clock, EyeOff } from "lucide-react";
import { timeAgo } from "../utils/timeAgo";
import "./ItemCard.css";

function ItemCard({ item, onClick, variant = "default" }) {
  const isFound = item.type === "FOUND";
  // Support both API (imageUrls array) and mock (imageUrl string) data shapes
  const imageUrl = item.imageUrls?.[0] || item.imageUrl || "https://picsum.photos/seed/placeholder/400/300";
  // Support both API (campus object) and mock (school object) data shapes
  const schoolName = item.campus?.name || item.school?.shortName || "";
  // Support both API (location string) and mock (locationDescription string)
  const locationText = item.location || item.locationDescription || "";

  return (
    <div
      className={`item-card glass ${variant === "compact" ? "compact-card" : ""} ${variant === "snapshot" ? "snapshot-card" : ""}`}
      onClick={() => onClick?.(item.id)}
    >
      <div className={`item-card-image ${variant === "snapshot" && isFound ? "snapshot-reveal" : ""}`}>
        <img
          src={imageUrl}
          alt={item.title}
          className={isFound ? "blurred" : ""}
          loading="lazy"
        />
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
        <h3 className="item-card-title">{item.title}</h3>
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
