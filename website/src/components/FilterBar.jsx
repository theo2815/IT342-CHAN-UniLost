import { Search } from "lucide-react";
import { mockCategories, mockSchools } from "../mockData/items";
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
            {mockCategories.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>

          <select
            className="filter-select"
            value={activeSchool}
            onChange={(e) => onSchoolChange(e.target.value)}
          >
            <option value="">All Schools</option>
            {mockSchools.map((school) => (
              <option key={school.id} value={school.shortName}>
                {school.shortName}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}

export default FilterBar;
