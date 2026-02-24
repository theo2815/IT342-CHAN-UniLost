import React from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Search,
  PlusCircle,
  CheckCircle,
  FileEdit,
  Lock,
  Handshake,
  Activity,
  ArrowRight,
} from "lucide-react";
import Header from "../../components/Header";
import Footer from "../../components/Footer";
import ItemCard from "../../components/ItemCard";
import { mockItems } from "../../mockData/items";
import "./Landing.css";

const Landing = () => {
  const navigate = useNavigate();
  const recentFoundItems = mockItems
    .filter((item) => item.type === "FOUND")
    .slice(0, 4);

  return (
    <div className="landing-page">
      <Header />
      <div className="layout-container">
        {/* Main Body */}
        <main className="landing-main">
          <div className="main-content-wrapper">
            {/* Hero Section */}
            <section className="hero-section">
              <div className="hero-bg-image">
                <img
                  src="https://images.unsplash.com/photo-1541339907198-e08756dedf3f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80"
                  alt="University campus hallway scene"
                  className="bg-img"
                />
                <div className="hero-overlay"></div>
              </div>

              <div className="hero-content">
                <h1 className="hero-title">
                  Reuniting <span className="text-highlight">Cebu's</span>{" "}
                  Campus Communities
                </h1>
                <h2 className="hero-subtitle">
                  The centralized lost and found for CIT-U, USC, USJ-R, and
                  more. Securely report and reclaim items with confidence.
                </h2>

                {/* Search Bar */}
                <div className="hero-search glass">
                  <div className="search-icon">
                    <Search size={20} />
                  </div>
                  <input
                    type="text"
                    placeholder="Search for lost items (e.g., 'Blue Flask USC')"
                    className="search-input"
                  />
                </div>

                {/* CTA Buttons */}
                <div className="hero-ctas">
                  <Link
                    to="/post-item"
                    className="btn btn-primary btn-lg pulse-on-hover"
                  >
                    <PlusCircle size={20} className="btn-icon" />
                    <span>Report Lost Item</span>
                  </Link>
                  <Link
                    to="/post-item?type=found"
                    className="btn btn-secondary btn-lg pulse-on-hover"
                  >
                    <CheckCircle size={20} className="btn-icon" />
                    <span>I Found Something</span>
                  </Link>
                </div>
              </div>
            </section>

            {/* How it Works Section */}
            <section className="how-it-works">
              <div className="feature-card glass">
                <div className="feature-icon-wrapper">
                  <FileEdit size={32} className="feature-icon" />
                </div>
                <h3 className="feature-title">1. Report</h3>
                <p className="feature-desc">
                  Post a lost item or report something you found. Add details
                  like location and time.
                </p>
              </div>
              <div className="feature-card glass">
                <div className="feature-icon-wrapper">
                  <Lock size={32} className="feature-icon" />
                </div>
                <h3 className="feature-title">2. Verify</h3>
                <p className="feature-desc">
                  Our "Secret Detail" system ensures only the true owner can
                  claim high-value items.
                </p>
              </div>
              <div className="feature-card glass">
                <div className="feature-icon-wrapper">
                  <Handshake size={32} className="feature-icon" />
                </div>
                <h3 className="feature-title">3. Reclaim</h3>
                <p className="feature-desc">
                  Meet securely at designated campus spots to reunite with your
                  belongings.
                </p>
              </div>
            </section>

            {/* Live Feed Section */}
            <section className="live-feed">
              <div className="live-feed-header">
                <div className="live-feed-title-wrap">
                  <div className="live-indicator">
                    <Activity size={20} />
                  </div>
                  <h2 className="section-title">Live Feed: Recently Found</h2>
                </div>
                <Link to="/items" className="view-all-link">
                  View All <ArrowRight size={16} />
                </Link>
              </div>

              <div className="feed-grid">
                {recentFoundItems.map((item) => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    onClick={(id) => navigate(`/items/${id}`)}
                  />
                ))}
              </div>
            </section>
          </div>
        </main>

        {/* Reusable Global Footer */}
        <Footer />
      </div>
    </div>
  );
};

export default Landing;
