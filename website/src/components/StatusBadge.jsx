import "./StatusBadge.css";

const statusConfig = {
  ACTIVE: { label: "Active", className: "status-active" },
  CLAIMED: { label: "Claimed", className: "status-claimed" },
  HANDED_OVER: { label: "Handed Over", className: "status-handed-over" },
  EXPIRED: { label: "Expired", className: "status-expired" },
  CANCELLED: { label: "Cancelled", className: "status-cancelled" },
  LOST: { label: "Lost", className: "status-lost" },
  FOUND: { label: "Found", className: "status-found" },
  PENDING: { label: "Pending", className: "status-pending" },
  ACCEPTED: { label: "Accepted", className: "status-approved" },
  APPROVED: { label: "Approved", className: "status-approved" },
  REJECTED: { label: "Rejected", className: "status-rejected" },
  TURNED_OVER_TO_OFFICE: { label: "At Office", className: "status-turned-over" },
  RETURNED: { label: "Returned", className: "status-returned" },
  HIDDEN: { label: "Hidden", className: "status-hidden" },
};

function StatusBadge({ status, type }) {
  const key = type || status;
  const config = statusConfig[key] || {
    label: key,
    className: "status-default",
  };

  return (
    <span className={`status-badge ${config.className}`}>{config.label}</span>
  );
}

export default StatusBadge;
