import { Search, ChevronDown } from "lucide-react";
import { ITEM_CATEGORIES, CATEGORY_LABELS } from "../constants/categories";
import { useCampuses } from "../context/CampusContext";
import { Input, Dropdown } from "./ui";
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
  const { campuses } = useCampuses();

  return (
    <div className="filter-bar">
      <div className="search-row">
        <Input
          icon={Search}
          placeholder="Search items..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="filter-search-input"
        />
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
          <Dropdown
            width={220}
            trigger={(isOpen) => (
              <div
                className={`ui-select-group ui-select-group--md ${isOpen ? "focus-within" : ""}`}
                style={{ cursor: "pointer", background: "var(--color-bg-card)", minWidth: "160px" }}
              >
                <div className="ui-select__field" style={{ display: "flex", alignItems: "center" }}>
                  {activeCategory ? CATEGORY_LABELS[activeCategory] || activeCategory : "All Categories"}
                </div>
                <span className="ui-select__chevron" style={{ color: isOpen ? "var(--color-primary)" : "" }}>
                  <ChevronDown size={16} className={`ui-dropdown-chevron ${isOpen ? "open" : ""}`} />
                </span>
              </div>
            )}
          >
            {({ close }) => (
              <>
                <Dropdown.Item
                  className={!activeCategory ? "active" : ""}
                  onClick={() => {
                    onCategoryChange("");
                    close();
                  }}
                >
                  All Categories
                </Dropdown.Item>
                {ITEM_CATEGORIES.map((cat) => (
                  <Dropdown.Item
                    key={cat}
                    className={activeCategory === cat ? "active" : ""}
                    onClick={() => {
                      onCategoryChange(cat);
                      close();
                    }}
                  >
                    {CATEGORY_LABELS[cat] || cat}
                  </Dropdown.Item>
                ))}
              </>
            )}
          </Dropdown>

          <Dropdown
            width={240}
            trigger={(isOpen) => (
              <div
                className={`ui-select-group ui-select-group--md ${isOpen ? "focus-within" : ""}`}
                style={{ cursor: "pointer", background: "var(--color-bg-card)", minWidth: "160px" }}
              >
                <div className="ui-select__field" style={{ display: "flex", alignItems: "center" }}>
                  {activeSchool
                    ? campuses.find((c) => c.id === activeSchool)?.name || "All Schools"
                    : "All Schools"}
                </div>
                <span className="ui-select__chevron" style={{ color: isOpen ? "var(--color-primary)" : "" }}>
                  <ChevronDown size={16} className={`ui-dropdown-chevron ${isOpen ? "open" : ""}`} />
                </span>
              </div>
            )}
          >
            {({ close }) => (
              <>
                <Dropdown.Item
                  className={!activeSchool ? "active" : ""}
                  onClick={() => {
                    onSchoolChange("");
                    close();
                  }}
                >
                  All Schools
                </Dropdown.Item>
                {campuses.map((campus) => (
                  <Dropdown.Item
                    key={campus.id}
                    className={activeSchool === campus.id ? "active" : ""}
                    onClick={() => {
                      onSchoolChange(campus.id);
                      close();
                    }}
                  >
                    {campus.name}
                  </Dropdown.Item>
                ))}
              </>
            )}
          </Dropdown>
        </div>
      </div>
    </div>
  );
}

export default FilterBar;
