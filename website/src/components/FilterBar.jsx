import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { ITEM_CATEGORIES, CATEGORY_LABELS } from "../constants/categories";
import campusService from "../services/campusService";
import "./FilterBar.css";

function FilterBar({
  searchQuery,
  onSearchChange,
  activeType,
  onTypeChange,
  activeCategory,
  onCategoryChange,
  activeSchool,
  onSchoolChange,
}) {
  const types = ["All", "Lost", "Found"];
  const [campuses, setCampuses] = useState([]);

  useEffect(() => {
    const fetchCampuses = async () => {
      const result = await campusService.getAllCampuses();
      if (result.success) {
        setCampuses(result.data);
      }
    };
    fetchCampuses();
  }, []);

  return (
    <div className="filter-bar">
      <div className="search-row">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="search-input"
            placeholder="Search items..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
          />
        </div>
      </div>

      <div className="filter-row">
        <div className="type-chips">
          {types.map((type) => (
            <button
              key={type}
              className={`filter-chip ${activeType === type ? "active" : ""} ${type.toLowerCase()}`}
              onClick={() => onTypeChange(type)}
            >
              {type}
            </button>
          ))}
        </div>

        <div className="filter-selects">
          <select
            className="filter-select"
            value={activeCategory}
            onChange={(e) => onCategoryChange(e.target.value)}
          >
            <option value="">All Categories</option>
            {ITEM_CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>
                {CATEGORY_LABELS[cat] || cat}
              </option>
            ))}
          </select>

          <select
            className="filter-select"
            value={activeSchool}
            onChange={(e) => onSchoolChange(e.target.value)}
          >
            <option value="">All Schools</option>
            {campuses.map((campus) => (
              <option key={campus.id} value={campus.id}>
                {campus.name}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}

export default FilterBar;
