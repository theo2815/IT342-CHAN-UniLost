import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Search,
  PlusCircle,
  Trophy,
  Medal,
  ArrowRight,
  Camera,
  ShieldCheck,
  TrendingUp,
  Loader,
} from "lucide-react";
import Header from "../../../shared/layout/Header/Header";
import Footer from "../../../shared/layout/Footer/Footer";
import ItemCard from "../../items/ItemCard/ItemCard";
import EmptyState from "../../../shared/components/EmptyState/EmptyState";
import authService from "../../auth/authService";
import itemService from "../../items/itemService";
import userService from "../userService";
import campusService from "../../../shared/services/campusService";
import { useCampuses } from "../../../shared/context/CampusContext";
import "./Dashboard.css";

function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [recentItems, setRecentItems] = useState([]);
  const [itemsError, setItemsError] = useState('');
  const [leaderboard, setLeaderboard] = useState([]);
  const [leaderboardLoaded, setLeaderboardLoaded] = useState(false);
  const { campuses } = useCampuses();
  const [campusStats, setCampusStats] = useState([]);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      setUser(currentUser);
    }

    const fetchRecentItems = async () => {
      const result = await itemService.getItems({ status: 'ACTIVE', size: 4, page: 0 });
      if (result.success) {
        setRecentItems(result.data.content);
      } else {
        setItemsError(result.error);
      }
    };
    fetchRecentItems();

    userService.getLeaderboard(5).then((result) => {
      if (result.success) setLeaderboard(result.data);
      setLeaderboardLoaded(true);
    });

    campusService.getCampusStats().then((result) => {
      if (result.success) setCampusStats(result.data);
    });
  }, []);

  const getInitials = (fullName) => {
    if (!fullName) return "?";
    return fullName.split(" ").map((n) => n.charAt(0)).join("").substring(0, 2).toUpperCase();
  };

  const markerColors = ["ping-red", "ping-blue", "ping-orange", "ping-red", "ping-blue", "ping-orange"];
  const dotColors = ["dot-red", "dot-blue", "dot-orange", "dot-red", "dot-blue", "dot-orange"];

  const getActivityColor = (count) => {
    if (count >= 5) return { ping: "ping-red", dot: "dot-red" };
    if (count >= 1) return { ping: "ping-orange", dot: "dot-orange" };
    return { ping: "ping-blue", dot: "dot-blue" };
  };

  // Merge campus stats into campus data for the Hot Zones widget
  const campusesWithStats = campuses.slice(0, 6).map((campus) => {
    const stats = campusStats.find((s) => s.id === campus.id);
    return { ...campus, activeItems: stats?.activeItems || 0 };
  });

  return (
    <div className="dashboard-page">
      <Header />

      <main className="dashboard-main">
        {/* Hero Section with Sidebar Layout */}
        <div className="dashboard-hero-layout">
          {/* Main Hero Content (Left/Top) */}
          <div className="hero-main-content">
            {/* Hero Banner */}
            <div className="hero-banner glass">
              <div
                className="hero-bg-img"
                style={{
                  backgroundImage:
                    "url('https://images.unsplash.com/photo-1541339907198-e08756dedf3f?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80')",
                }}
              ></div>
              <div className="hero-gradient"></div>
              <div className="hero-content">
                <div className="live-updates-badge">
                  <span className="pulse-dot"></span>
                  <span className="badge-text">Live Updates</span>
                </div>
                <h1 className="hero-title">
                  Lost something in{" "}
                  <span className="text-highlight">Cebu?</span> We've got you.
                </h1>
                <p className="hero-subtitle">
                  Join the largest lost and found community for Cebu City's
                  academic hubs. Connect, help others, and earn Karma.
                </p>
                <div className="hero-actions">
                  <Link to="/post-item" className="btn btn-primary flex-btn">
                    <PlusCircle size={20} />
                    Report Lost Item
                  </Link>
                  <Link
                    to="/post-item?type=found"
                    className="btn btn-outline flex-btn hero-btn-outline"
                  >
                    <Search size={20} />I Found Something
                  </Link>
                </div>
              </div>
            </div>

            {/* Hot Zones Map Snippet */}
            <div className="hot-zones-widget glass">
              <div className="widget-header">
                <div>
                  <h3 className="widget-title">Hot Zones</h3>
                  <p className="widget-subtitle">
                    Recent activity in Cebu City campuses
                  </p>
                </div>
                <Link to="/map" className="view-link">
                  View Full Map
                </Link>
              </div>
              <div className="hot-zones-map">
                <div className="map-grid-overlay"></div>

                {campuses.length === 0 ? (
                  <div className="hot-zones-loading">
                    <Loader size={20} className="spin" />
                    <span>Loading campuses...</span>
                  </div>
                ) : (
                  campusesWithStats.map((campus, i) => {
                    const colors = getActivityColor(campus.activeItems);
                    return (
                      <Link
                        key={campus.id}
                        to={`/items?campusId=${campus.id}`}
                        className={`map-marker map-marker-dynamic marker-pos-${i}`}
                      >
                        <div className="marker-dot-container">
                          <span className={`marker-ping ${colors.ping}`}></span>
                          <span className={`marker-dot ${colors.dot}`}></span>
                          {campus.activeItems > 0 && (
                            <span className="marker-count">{campus.activeItems}</span>
                          )}
                        </div>
                        <div className="marker-label">
                          {campus.shortLabel || campus.name}
                          {campus.activeItems > 0 && (
                            <span className="marker-label-count">{campus.activeItems} active</span>
                          )}
                        </div>
                      </Link>
                    );
                  })
                )}
              </div>
            </div>
          </div>

          {/* Sidebar / Leaderboard Widget (Right) */}
          <div className="leaderboard-sidebar">
            <div className="leaderboard-card glass">
              <div className="leaderboard-header">
                <div className="header-title-row">
                  <div className="icon-box warning-box">
                    <Trophy size={20} />
                  </div>
                  <h3 className="widget-title">Leaderboard</h3>
                </div>
                <p className="widget-subtitle">
                  Top finders across Cebu this month
                </p>
              </div>

              <div className="leaderboard-list">
                {!leaderboardLoaded ? (
                  <div className="lb-loading">
                    <Loader size={20} className="spin" />
                    <span>Loading rankings...</span>
                  </div>
                ) : leaderboard.length === 0 ? (
                  <div className="lb-empty">
                    <Trophy size={28} />
                    <p>No ranked users yet.</p>
                    <span>Return a found item to earn Karma and claim the top spot!</span>
                  </div>
                ) : (
                  leaderboard.map((entry, index) => {
                    if (index < 3) {
                      return (
                        <div key={entry.id} className={`lb-item ${index === 0 ? "lb-1" : ""}`}>
                          {index === 0 && (
                            <div className="lb-watermark">
                              <Medal size={48} />
                            </div>
                          )}
                          <div className={`lb-rank rank-${index + 1}`}>{index + 1}</div>
                          <div className={`lb-avatar avatar-${index + 1}`}>
                            <div className="lb-avatar-initials">{getInitials(entry.fullName)}</div>
                          </div>
                          <div className="lb-info">
                            <h4 className="lb-name">{entry.fullName}</h4>
                            <div className="lb-meta">{entry.campus?.name || "Unknown"}</div>
                          </div>
                          <div className="lb-score">
                            <div className="lb-points">{entry.karmaScore.toLocaleString()}</div>
                            <div className="lb-points-label">Karma</div>
                          </div>
                        </div>
                      );
                    }
                    return (
                      <div key={entry.id} className="lb-item-compact">
                        <div className="lb-rank-compact">{index + 1}</div>
                        <div className="lb-avatar-compact">
                          <div className="lb-avatar-initials small">{getInitials(entry.fullName)}</div>
                        </div>
                        <div className="lb-info">
                          <h4 className="lb-name-compact">{entry.fullName}</h4>
                        </div>
                        <div className="lb-score-compact">
                          <div className="lb-points-compact">{entry.karmaScore.toLocaleString()}</div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              <div className="leaderboard-footer">
                <Link to="/leaderboard" className="btn-view-all">
                  View All Rankings <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          </div>
        </div>

        {/* How It Works / Engagement Section */}
        <section className="engagement-section">
          <div className="section-header">
            <h2 className="section-title">Get Involved &amp; Earn Rewards</h2>
            <p className="section-subtitle">
              Our community thrives on honesty. Contribute to the ecosystem and
              get recognized.
            </p>
          </div>

          <div className="engagement-grid">
            {/* Card 1 */}
            <div className="engage-card glass hover-primary">
              <div className="card-bg-blob blob-primary"></div>
              <div className="engage-card-content">
                <div className="icon-box primary-box">
                  <Camera size={32} />
                </div>
                <div>
                  <h3 className="card-title">1. Snap &amp; Report</h3>
                  <p className="card-desc">
                    Found an item? Take a photo and post it. Quick reports help
                    owners find their items faster.
                  </p>
                </div>
                <Link to="/post-item" className="card-action text-primary">
                  Start Reporting <ArrowRight size={16} />
                </Link>
              </div>
            </div>

            {/* Card 2 */}
            <div className="engage-card glass hover-success">
              <div className="card-bg-blob blob-success"></div>
              <div className="engage-card-content">
                <div className="icon-box success-box">
                  <ShieldCheck size={32} />
                </div>
                <div>
                  <h3 className="card-title">2. Verify &amp; Connect</h3>
                  <p className="card-desc">
                    Community members help verify ownership. Safe meetups on
                    campus are encouraged.
                  </p>
                </div>
                <a href="#" className="card-action text-success">
                  Read Guidelines <ArrowRight size={16} />
                </a>
              </div>
            </div>

            {/* Card 3 */}
            <div className="engage-card glass hover-warning">
              <div className="card-bg-blob blob-warning"></div>
              <div className="engage-card-content">
                <div className="icon-box warning-box">
                  <TrendingUp size={32} />
                </div>
                <div>
                  <h3 className="card-title">3. Earn Karma</h3>
                  <p className="card-desc">
                    Every successful return earns you Karma Points. Climb the
                    leaderboard and win badges.
                  </p>
                </div>
                <Link to="/leaderboard" className="card-action text-warning">
                  View Rewards <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          </div>
        </section>

        {/* Recent Community Activity */}
        <section className="pulse-section">
          <div className="section-header-row">
            <h2 className="section-title">Community Pulse</h2>
            <Link to="/items" className="view-link">
              View All Items <ArrowRight size={16} />
            </Link>
          </div>
          {itemsError ? (
            <EmptyState
              title="Couldn't load items"
              message={itemsError}
              actionLabel="Try Again"
              onAction={() => window.location.reload()}
            />
          ) : recentItems.length === 0 ? (
            <EmptyState
              title="No items yet"
              message="Be the first to report a lost or found item on campus."
              actionLabel="Post an Item"
              onAction={() => navigate('/post-item')}
            />
          ) : (
            <div className="pulse-grid">
              {recentItems.map((item) => (
                <ItemCard
                  key={item.id}
                  item={item}
                  onClick={(id) => navigate(`/items/${id}`)}
                />
              ))}
            </div>
          )}
        </section>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Dashboard;
