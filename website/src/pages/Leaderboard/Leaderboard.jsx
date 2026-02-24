import React from "react";
import {
  Trophy,
  CornerDownLeft,
  School,
  Star,
  User,
  HeartHandshake,
} from "lucide-react";
import Header from "../../components/Header";
import "./Leaderboard.css";

// Mock data matching the design
const leaderboardData = [
  {
    rank: 1,
    name: "Maria Santos",
    subtext: "Top Finder",
    school: "Cebu Institute of Technology",
    score: 850,
    img: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
    isTop: true,
    rankClass: "rank-badge-gold",
    scoreClass: "score-gold",
  },
  {
    rank: 2,
    name: "John Doe",
    subtext: "Consistent Returner",
    school: "University of San Carlos",
    score: 820,
    img: "https://images.unsplash.com/photo-1599566150163-29194dcaad36?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
    isTop: true,
    rankClass: "rank-badge-silver",
    scoreClass: "score-silver",
  },
  {
    rank: 3,
    name: "Sarah Lee",
    subtext: "Community Helper",
    school: "University of the Philippines",
    score: 795,
    img: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
    isTop: true,
    rankClass: "rank-badge-bronze",
    scoreClass: "score-bronze",
  },
  {
    rank: 4,
    name: "Michael Tan",
    school: "University of Cebu",
    score: 750,
    img: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    rank: 5,
    name: "Lisa Wang",
    school: "Southwestern University",
    score: 710,
    img: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    rank: 6,
    name: "David Chen",
    school: "Cebu Normal University",
    score: 680,
    img: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    rank: 7,
    name: "Emily Rose",
    school: "Velez College",
    score: 650,
    img: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    rank: 8,
    name: "Mark Anthony",
    school: "USJ - Recoletos",
    score: 620,
    img: "https://images.unsplash.com/photo-1527980965255-d3b416303d12?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    rank: 9,
    name: "Sophia Grace",
    school: "Cebu Doctors' University",
    score: 600,
    img: "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
  {
    rank: 10,
    name: "Kevin Ray",
    school: "Asian College of Tech",
    score: 580,
    img: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80",
  },
];

function Leaderboard() {
  return (
    <div className="leaderboard-page">
      <Header />

      <main className="leaderboard-main">
        {/* Hero Section */}
        <section className="leaderboard-hero">
          <div className="hero-titles">
            <h1 className="hero-heading">Global Karma Leaderboard</h1>
            <p className="hero-subheading">
              Recognizing the top finders making a difference across Cebu City
              universities. Your honesty builds our community.
            </p>
          </div>
          <div className="season-badge">
            <Trophy size={16} />
            Season 4 Ends in 12 Days
          </div>
        </section>

        {/* Stats Grid */}
        <section className="stats-grid">
          <div className="stat-card">
            <div className="stat-card-header">
              <div className="stat-icon-wrapper success">
                <CornerDownLeft size={20} />
              </div>
              <span className="stat-label">Total Returns</span>
            </div>
            <p className="stat-value">1,245</p>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <div className="stat-icon-wrapper primary">
                <School size={20} />
              </div>
              <span className="stat-label">Universities</span>
            </div>
            <p className="stat-value">12</p>
          </div>

          <div className="stat-card">
            <div className="stat-card-header">
              <div className="stat-icon-wrapper warning">
                <Star size={20} />
              </div>
              <span className="stat-label">Top Score</span>
            </div>
            <p className="stat-value">850</p>
          </div>

          <div className="stat-card rank-card">
            <div className="glow-blur"></div>
            <div className="stat-card-header">
              <div className="stat-icon-wrapper">
                <User size={20} />
              </div>
              <span className="stat-label">Your Rank</span>
            </div>
            <p className="stat-value">#42</p>
          </div>
        </section>

        {/* Table Section */}
        <section className="leaderboard-table-container">
          <div className="table-wrapper">
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
                  {leaderboardData.map((user, index) => (
                    <tr key={index} className="table-row">
                      <td className="table-cell">
                        {user.isTop ? (
                          <div className={`rank-badge-container ${user.rankClass}`}>
                            {index === 0 ? <Trophy size={16} /> : <span>{user.rank}</span>}
                          </div>
                        ) : (
                          <span className="rank-text-muted">{user.rank}</span>
                        )}
                      </td>
                      <td className="table-cell">
                        <div className="user-info">
                          <div className="avatar-wrapper">
                            <div 
                              className="user-avatar" 
                              style={{ 
                                backgroundImage: `url(${user.img})`,
                                borderColor: user.rankClass === 'rank-badge-gold' ? '#eab308' : 
                                             user.rankClass === 'rank-badge-silver' ? '#9ca3af' :
                                             user.rankClass === 'rank-badge-bronze' ? '#ea580c' : 'transparent'
                              }}
                            ></div>
                            {index === 0 && <div className="avatar-rank-number">1</div>}
                          </div>
                          <div className="user-name-group">
                            <span className="user-name">{user.name}</span>
                            {user.subtext && <span className="user-subtext">{user.subtext}</span>}
                          </div>
                        </div>
                      </td>
                      <td className="table-cell">
                        <span className="university-cell">{user.school}</span>
                      </td>
                      <td className="table-cell text-right">
                        <span className={`score-badge ${user.scoreClass || 'score-normal'}`}>
                          {user.score} pts
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Sticky Current User Strip */}
            <div className="current-user-strip">
              <div className="current-user-left">
                <div className="current-user-rank">42</div>
                <div className="user-info">
                  <div className="avatar-wrapper">
                    <div 
                      className="user-avatar" 
                      style={{ 
                        backgroundImage: `url('https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80')`,
                        borderColor: 'var(--color-primary)'
                      }}
                    ></div>
                  </div>
                  <div className="user-name-group">
                    <div className="user-name" style={{ fontWeight: '700' }}>
                      You <span className="you-badge">Current Rank</span>
                    </div>
                    <span className="user-subtext">University of Cebu</span>
                  </div>
                </div>
              </div>
              <div className="current-user-right">
                <div className="next-rank-info">
                  <span className="next-rank-label">Points to next rank</span>
                  <span className="next-rank-value">+25 pts</span>
                </div>
                <div className="current-user-score">350 pts</div>
              </div>
            </div>
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
              <button className="btn btn-outline">View Rules</button>
              <button className="btn btn-primary">Start Finding</button>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default Leaderboard;
