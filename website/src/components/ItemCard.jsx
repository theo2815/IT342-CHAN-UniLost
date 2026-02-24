import { MapPin, Clock, EyeOff } from "lucide-react";
import { timeAgo } from "../mockData/items";
import "./ItemCard.css";

function ItemCard({ item, onClick, variant = "default" }) {
  const isFound = item.type === "FOUND";

  return (
    <div
      className={`item-card glass ${variant === "compact" ? "compact-card" : ""} ${variant === "snapshot" ? "snapshot-card" : ""}`}
      onClick={() => onClick?.(item.id)}
    >
      <div className={`item-card-image ${variant === "snapshot" && isFound ? "snapshot-reveal" : ""}`}>
        <img
          src={item.imageUrl}
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
