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
} from "lucide-react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import ItemCard from "../../components/ItemCard";
import authService from "../../services/authService";
import itemService from "../../services/itemService";
import "./Dashboard.css";

function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [recentItems, setRecentItems] = useState([]);
  const [itemsError, setItemsError] = useState('');

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
  }, []);

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

                {/* Simulated Map Markers */}
                <div className="map-marker marker-usc">
                  <div className="marker-dot-container">
                    <span className="marker-ping ping-red"></span>
                    <span className="marker-dot dot-red"></span>
                  </div>
                  <div className="marker-label">USC Talamban</div>
                </div>
                <div className="map-marker marker-it">
                  <div className="marker-dot-container">
                    <span className="marker-ping ping-blue"></span>
                    <span className="marker-dot dot-blue"></span>
                  </div>
                  <div className="marker-label">Cebu IT Park</div>
                </div>
                <div className="map-marker marker-uc">
                  <div className="marker-dot-container">
                    <span className="marker-ping ping-orange"></span>
                    <span className="marker-dot dot-orange"></span>
                  </div>
                  <div className="marker-label">UC Banilad</div>
                </div>
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
                {/* Top 1 */}
                <div className="lb-item lb-1">
                  <div className="lb-watermark">
                    <Medal size={48} />
                  </div>
                  <div className="lb-rank rank-1">1</div>
                  <div className="lb-avatar avatar-1">
                    <img
                      alt="Maria"
                      src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
                    />
                  </div>
                  <div className="lb-info">
                    <h4 className="lb-name">Maria Santos</h4>
                    <div className="lb-meta">USC Main • 42 Returns</div>
                  </div>
                  <div className="lb-score">
                    <div className="lb-points">1,250</div>
                    <div className="lb-points-label">Karma</div>
                  </div>
                </div>

                {/* Top 2 */}
                <div className="lb-item">
                  <div className="lb-rank rank-2">2</div>
                  <div className="lb-avatar avatar-2">
                    <img
                      alt="Juan"
                      src="https://images.unsplash.com/photo-1599566150163-29194dcaad36?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
                    />
                  </div>
                  <div className="lb-info">
                    <h4 className="lb-name">Juan Dela Cruz</h4>
                    <div className="lb-meta">UC Banilad • 38 Returns</div>
                  </div>
                  <div className="lb-score">
                    <div className="lb-points">1,100</div>
                    <div className="lb-points-label">Karma</div>
                  </div>
                </div>

                {/* Top 3 */}
                <div className="lb-item">
                  <div className="lb-rank rank-3">3</div>
                  <div className="lb-avatar avatar-3">
                    <img
                      alt="Anna"
                      src="https://images.unsplash.com/photo-1438761681033-6461ffad8d80?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
                    />
                  </div>
                  <div className="lb-info">
                    <h4 className="lb-name">Anna Lee</h4>
                    <div className="lb-meta">CIT-U • 31 Returns</div>
                  </div>
                  <div className="lb-score">
                    <div className="lb-points">950</div>
                    <div className="lb-points-label">Karma</div>
                  </div>
                </div>

                {/* List Items 4-5 */}
                <div className="lb-item-compact">
                  <div className="lb-rank-compact">4</div>
                  <div className="lb-avatar-compact">
                    <img
                      alt="Pedro"
                      src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
                    />
                  </div>
                  <div className="lb-info">
                    <h4 className="lb-name-compact">Pedro Penduko</h4>
                  </div>
                  <div className="lb-score-compact">
                    <div className="lb-points-compact">800</div>
                  </div>
                </div>

                <div className="lb-item-compact">
                  <div className="lb-rank-compact">5</div>
                  <div className="lb-avatar-compact">
                    <img
                      alt="Liza"
                      src="https://images.unsplash.com/photo-1544005313-94ddf0286df2?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
                    />
                  </div>
                  <div className="lb-info">
                    <h4 className="lb-name-compact">Liza Soberano</h4>
                  </div>
                  <div className="lb-score-compact">
                    <div className="lb-points-compact">650</div>
                  </div>
                </div>
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
          <div
            className="section-header"
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <h2 className="section-title">Community Pulse</h2>
            <Link
              to="/items"
              className="text-primary font-bold flex items-center gap-1"
              style={{
                fontSize: "0.875rem",
                textDecoration: "none",
                display: "flex",
                alignItems: "center",
                gap: "4px",
              }}
            >
              View All Items <ArrowRight size={16} />
            </Link>
          </div>
          <div className="pulse-grid">
            {recentItems.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                onClick={(id) => navigate(`/items/${id}`)}
              />
            ))}
          </div>
        </section>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Dashboard;
