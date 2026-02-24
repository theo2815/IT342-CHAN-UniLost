import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  SlidersHorizontal,
  MapPin,
  Navigation,
  Plus,
  Minus,
} from "lucide-react";
import Header from "../../components/Header";
import ItemCard from "../../components/ItemCard";
import { mockItems } from "../../mockData/items";
import "./MapView.css";

const mapCoords = [
  { top: "40%", left: "30%" },
  { top: "60%", left: "55%" },
  { top: "25%", left: "65%" },
  { top: "75%", left: "25%" },
  { top: "80%", left: "70%" },
];

function MapView() {
  const navigate = useNavigate();
  const [activeFilter, setActiveFilter] = useState("All Items");

  // Combine standard mockItems with some visual coordinates for the map
  const displayItems = mockItems.slice(0, 5).map((item, index) => ({
    ...item,
    pinCoords: mapCoords[index],
  }));

  const handleItemClick = (id) => {
    navigate(`/items/${id}`);
  };

  return (
    <div className="map-view-page overflow-hidden">
      <Header />

      <main className="map-view-main">
        {/* Left Panel: Feed (40%) */}
        <div className="map-feed-panel">
          {/* Filters Header */}
          <div className="feed-filters-header glass">
            <div className="filters-top-row">
              <h1 className="feed-title">Nearby Items</h1>
              <button className="icon-btn filter-btn">
                <SlidersHorizontal size={20} />
              </button>
            </div>

            <div className="filters-scroll-row custom-scrollbar">
              <button
                className={`filter-chip ${activeFilter === "All Items" ? "active" : ""}`}
                onClick={() => setActiveFilter("All Items")}
              >
                All Items
              </button>
              <button
                className={`filter-chip ${activeFilter === "Lost Today" ? "active" : ""}`}
                onClick={() => setActiveFilter("Lost Today")}
              >
                Lost Today
              </button>
              <button
                className={`filter-chip ${activeFilter === "Found" ? "active" : ""}`}
                onClick={() => setActiveFilter("Found")}
              >
                Found
              </button>
              <button
                className={`filter-chip ${activeFilter === "Electronics" ? "active" : ""}`}
                onClick={() => setActiveFilter("Electronics")}
              >
                Electronics
              </button>
            </div>
          </div>

          {/* Scrollable Feed */}
          <div className="feed-list custom-scrollbar">
            {displayItems.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                onClick={handleItemClick}
                variant="compact"
              />
            ))}
          </div>
        </div>

        {/* Right Panel: Map (60%) */}
        <div className="map-graphic-panel">
          {/* Simulated Map Background */}
          <div
            className="map-background"
            style={{
              backgroundImage:
                "url('https://images.unsplash.com/photo-1524661135-423995f22d0b?ixlib=rb-4.0.3&auto=format&fit=crop&w=2000&q=80')",
            }}
          ></div>

          {/* Map Overlay Gradient */}
          <div className="map-gradient-overlay"></div>

          {/* Map Controls */}
          <div className="map-controls">
            <button className="map-control-btn glass">
              <Navigation size={20} />
            </button>
            <button className="map-control-btn glass">
              <Plus size={20} />
            </button>
            <button className="map-control-btn glass">
              <Minus size={20} />
            </button>
          </div>

          {/* Map Pins (Simulated) */}
          {displayItems.map((item, index) => {
            if (!item.pinCoords) return null;

            // Make the first item 'active' (bouncing)
            const isActive = index === 0;
            const isFound = item.type === "FOUND";
            const colorClass = isFound ? "success" : "danger";

            return (
              <div
                key={`pin-${item.id}`}
                className={`map-pin-group ${isActive ? "active-pin-group" : "inactive-pin-group"}`}
                style={{ top: item.pinCoords.top, left: item.pinCoords.left }}
                onClick={() => handleItemClick(item.id)}
              >
                <div className="pin-tooltip glass">
                  <p className="tooltip-text">{item.title}</p>
                </div>
                <div
                  className={`pin-marker pin-${colorClass} ${isActive ? "animate-bounce-custom" : ""}`}
                >
                  <MapPin size={isActive ? 16 : 12} />
                </div>
              </div>
            );
          })}

          {/* Bottom Map Legend */}
          <div className="map-legend-container">
            <div className="map-legend glass">
              <div className="legend-item">
                <span className="legend-dot dot-danger"></span>
                <span className="legend-text">Lost Area</span>
              </div>
              <div className="legend-divider"></div>
              <div className="legend-item">
                <span className="legend-dot dot-success"></span>
                <span className="legend-text">Found Item</span>
              </div>
              <div className="legend-divider"></div>
              <div className="legend-item">
                <span className="legend-dot dot-primary"></span>
                <span className="legend-text">Selected</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default MapView;
