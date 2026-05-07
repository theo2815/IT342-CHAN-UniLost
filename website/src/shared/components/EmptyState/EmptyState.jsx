import { PackageX } from "lucide-react";
import "./EmptyState.css";

function EmptyState({ icon, title, message, actionLabel, onAction }) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon || <PackageX size={48} />}</div>
      <h3 className="empty-state-title">{title || "Nothing here yet"}</h3>
      <p className="empty-state-message">{message || "No items to display."}</p>
      {actionLabel && onAction && (
        <button className="empty-state-btn" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}

export default EmptyState;
