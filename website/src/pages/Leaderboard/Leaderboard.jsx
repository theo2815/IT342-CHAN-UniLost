import React, { useState, useEffect } from "react";
import {
  Trophy,
  CornerDownLeft,
  School,
  Star,
  User,
  HeartHandshake,
  Loader,
  Search,
} from "lucide-react";
import { Link } from "react-router-dom";
import Header from "../../components/Header";
import userService from "../../services/userService";
import campusService from "../../services/campusService";
import authService from "../../services/authService";
import "./Leaderboard.css";

function Leaderboard() {
  const [leaderboardData, setLeaderboardData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [campuses, setCampuses] = useState([]);
  const [activeCampus, setActiveCampus] = useState("");
  const currentUser = authService.getCurrentUser();

  // Load campuses once on mount
  useEffect(() => {
    campusService.getAllCampuses().then((result) => {
      if (result.success) setCampuses(result.data);
    });
  }, []);

  // Reload leaderboard when campus filter changes
  useEffect(() => {
    const loadLeaderboard = async () => {
      setLoading(true);
      const result = await userService.getLeaderboard(20, activeCampus || undefined);
      if (result.success) setLeaderboardData(result.data);
      setLoading(false);
    };
    loadLeaderboard();
  }, [activeCampus]);

  const topScore = leaderboardData[0]?.karmaScore || 0;

  // Find current user's rank in the leaderboard
  const currentUserRank = currentUser
    ? leaderboardData.findIndex((u) => u.id === currentUser.id) + 1
    : 0;
  const currentUserEntry = currentUserRank > 0 ? leaderboardData[currentUserRank - 1] : null;
  const nextRankScore = currentUserRank > 1 ? leaderboardData[currentUserRank - 2].karmaScore : null;
  const pointsToNext = nextRankScore != null && currentUserEntry
    ? nextRankScore - currentUserEntry.karmaScore
    : null;

  const getRankStyle = (index) => {
    if (index === 0) return { rankClass: "rank-badge-gold", scoreClass: "score-gold" };
    if (index === 1) return { rankClass: "rank-badge-silver", scoreClass: "score-silver" };
    if (index === 2) return { rankClass: "rank-badge-bronze", scoreClass: "score-bronze" };
    return { rankClass: "", scoreClass: "score-normal" };
  };

  const getInitials = (fullName) => {
    if (!fullName) return "?";
    return fullName.split(" ").map((n) => n.charAt(0)).join("").substring(0, 2).toUpperCase();
  };

  const getBorderColor = (index) => {
    if (index === 0) return "#eab308";
    if (index === 1) return "#9ca3af";
    if (index === 2) return "#ea580c";
    return "transparent";
  };

  return (
    <div className="leaderboard-page">
      <Header />

      <main className="leaderboard-main">
        {/* Hero Section */}
        <section className="leaderboard-hero">
          <div className="hero-titles">
            <h1 className="hero-heading">
              {activeCampus
                ? `${campuses.find((c) => c.id === activeCampus)?.name || "Campus"} Leaderboard`
                : "Global Karma Leaderboard"}
            </h1>
            <p className="hero-subheading">
              Recognizing the top finders making a difference across Cebu City
              universities. Your honesty builds our community.
            </p>
          </div>
          <div className="leaderboard-filter">
            <School size={16} />
            <select
              value={activeCampus}
              onChange={(e) => setActiveCampus(e.target.value)}
              className="campus-select"
            >
              <option value="">All Universities</option>
              {campuses.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
        </section>

        {/* Stats Grid */}
        <section className="stats-grid">
          <div className="stat-card">
            <div className="stat-card-header">
              <div className="stat-icon-wrapper success">
                <CornerDownLeft size={20} />
              </div>
              <span className="stat-label">Ranked Users</span>
            </div>
            <p className="stat-value">{leaderboardData.length}</p>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <div className="stat-icon-wrapper primary">
                <School size={20} />
              </div>
              <span className="stat-label">Universities</span>
            </div>
            <p className="stat-value">{campuses.length}</p>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <div className="stat-icon-wrapper warning">
                <Star size={20} />
              </div>
              <span className="stat-label">Top Score</span>
            </div>
            <p className="stat-value">{topScore}</p>
          </div>

          <div className="stat-card rank-card">
            <div className="glow-blur"></div>
            <div className="stat-card-header">
              <div className="stat-icon-wrapper">
                <User size={20} />
              </div>
              <span className="stat-label">Your Rank</span>
            </div>
            <p className="stat-value">
              {currentUserRank > 0 ? `#${currentUserRank}` : "\u2014"}
            </p>
          </div>
        </section>

        {/* Table Section */}
        <section className="leaderboard-table-container">
          <div className="table-wrapper">
            {loading ? (
              <div className="leaderboard-loading">
                <Loader size={24} className="spin" />
                <span>Loading leaderboard...</span>
              </div>
            ) : leaderboardData.length === 0 ? (
              <div className="leaderboard-empty-state">
                <div className="empty-icon-wrapper">
                  <Trophy size={48} />
                </div>
                <h3 className="empty-title">
                  {activeCampus
                    ? `No rankings yet for ${campuses.find((c) => c.id === activeCampus)?.name || "this university"}`
                    : "No rankings yet"}
                </h3>
                <p className="empty-description">
                  {activeCampus
                    ? "No one from this university has earned Karma points yet. Be the first to return a found item and claim the top spot!"
                    : "The leaderboard is waiting for its first hero. Return a found item to earn Karma points and get recognized."}
                </p>
                <div className="empty-actions">
                  <Link to="/items" className="btn btn-primary empty-cta">
                    <Search size={16} />
                    Browse Found Items
                  </Link>
                  {activeCampus && (
                    <button
                      className="btn btn-outline"
                      onClick={() => setActiveCampus("")}
                    >
                      View All Universities
                    </button>
                  )}
                </div>
              </div>
            ) : (
              <>
                <div className="scroll-container">
                  <table className="leaderboard-table">
                    <thead>
                      <tr className="table-head-row">
                        <th className="table-header" style={{ width: "100px" }}>Rank</th>
                        <th className="table-header">Finder</th>
                        <th className="table-header">University</th>
                        <th className="table-header text-right">Karma Score</th>
                      </tr>
                    </thead>
                    <tbody>
                      {leaderboardData.map((user, index) => {
                        const { rankClass, scoreClass } = getRankStyle(index);
                        const isTop = index < 3;
                        return (
                          <tr
                            key={user.id}
                            className={`table-row ${currentUser && user.id === currentUser.id ? "current-user-row" : ""}`}
                          >
                            <td className="table-cell">
                              {isTop ? (
                                <div className={`rank-badge-container ${rankClass}`}>
                                  {index === 0 ? <Trophy size={16} /> : <span>{index + 1}</span>}
                                </div>
                              ) : (
                                <span className="rank-text-muted">{index + 1}</span>
                              )}
                            </td>
                            <td className="table-cell">
                              <div className="user-info">
                                <div className="avatar-wrapper">
                                  <div
                                    className="user-avatar user-avatar-initials"
                                    style={{ borderColor: getBorderColor(index) }}
                                  >
                                    {getInitials(user.fullName)}
                                  </div>
                                  {index === 0 && <div className="avatar-rank-number">1</div>}
                                </div>
                                <div className="user-name-group">
                                  <span className="user-name">{user.fullName}</span>
                                </div>
                              </div>
                            </td>
                            <td className="table-cell">
                              <span className="university-cell">
                                {user.campus?.name || "Unknown"}
                              </span>
                            </td>
                            <td className="table-cell text-right">
                              <span className={`score-badge ${scoreClass}`}>
                                {user.karmaScore} pts
                              </span>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                {/* Current User Strip */}
                {currentUser && currentUserEntry && (
                  <div className="current-user-strip">
                    <div className="current-user-left">
                      <div className="current-user-rank">{currentUserRank}</div>
                      <div className="user-info">
                        <div className="avatar-wrapper">
                          <div
                            className="user-avatar user-avatar-initials"
                            style={{ borderColor: "var(--color-primary)" }}
                          >
                            {getInitials(currentUserEntry.fullName)}
                          </div>
                        </div>
                        <div className="user-name-group">
                          <div className="user-name" style={{ fontWeight: "700" }}>
                            You <span className="you-badge">Current Rank</span>
                          </div>
                          <span className="user-subtext">
                            {currentUserEntry.campus?.name || ""}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div className="current-user-right">
                      {pointsToNext != null && pointsToNext > 0 && (
                        <div className="next-rank-info">
                          <span className="next-rank-label">Points to next rank</span>
                          <span className="next-rank-value">+{pointsToNext} pts</span>
                        </div>
                      )}
                      <div className="current-user-score">{currentUserEntry.karmaScore} pts</div>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        </section>

        {/* CTA Section */}
        <section className="cta-banner-container">
          <div className="cta-banner">
            <div className="cta-icon-bg">
              <HeartHandshake size={200} strokeWidth={1} color="var(--color-primary)" />
            </div>
            <div className="cta-content">
              <h3 className="cta-title">Want to climb the ranks?</h3>
              <p className="cta-description">
                Every returned item earns you Karma Points. Help your community and get recognized on the Global Leaderboard.
              </p>
            </div>
            <div className="cta-actions">
              <button className="btn btn-primary" onClick={() => window.location.href = "/items"}>
                Start Finding
              </button>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default Leaderboard;
