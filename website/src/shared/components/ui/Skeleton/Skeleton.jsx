import './Skeleton.css';

function Skeleton({ variant = 'text', width, height, className = '', count = 1 }) {
  const style = {};
  if (width) style.width = width;
  if (height) style.height = height;

  const items = Array.from({ length: count }, (_, i) => (
    <div
      key={i}
      className={`ui-skeleton ui-skeleton--${variant} ${className}`}
      style={style}
      aria-hidden="true"
    />
  ));

  return count === 1 ? items[0] : <>{items}</>;
}

function SkeletonCard() {
  return (
    <div className="ui-skeleton-card" aria-hidden="true">
      <Skeleton variant="thumbnail" />
      <div className="ui-skeleton-card__lines">
        <Skeleton variant="title" />
        <Skeleton variant="text" />
        <Skeleton variant="text" width="75%" />
      </div>
    </div>
  );
}

Skeleton.Card = SkeletonCard;

export default Skeleton;
