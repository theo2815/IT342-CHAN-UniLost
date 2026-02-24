import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import Header from '../../components/Header';
import ItemCard from '../../components/ItemCard';
import FilterBar from '../../components/FilterBar';
import EmptyState from '../../components/EmptyState';
import { mockItems } from '../../mockData/items';
import './ItemFeed.css';

const ITEMS_PER_PAGE = 9;

function ItemFeed() {
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [activeType, setActiveType] = useState('All');
    const [activeCategory, setActiveCategory] = useState('');
    const [activeSchool, setActiveSchool] = useState('');
    const [visibleCount, setVisibleCount] = useState(ITEMS_PER_PAGE);

    const filteredItems = useMemo(() => {
        return mockItems.filter((item) => {
            // Only show ACTIVE items in the public feed
            if (item.status !== 'ACTIVE') return false;

            // Type filter
            if (activeType === 'Lost' && item.type !== 'LOST') return false;
            if (activeType === 'Found' && item.type !== 'FOUND') return false;

            // Category filter
            if (activeCategory && item.category !== activeCategory) return false;

            // School filter
            if (activeSchool && item.school?.shortName !== activeSchool) return false;

            // Search filter
            if (searchQuery) {
                const q = searchQuery.toLowerCase();
                return (
                    item.title.toLowerCase().includes(q) ||
                    item.description.toLowerCase().includes(q) ||
                    item.locationDescription.toLowerCase().includes(q)
                );
            }

            return true;
        });
    }, [searchQuery, activeType, activeCategory, activeSchool]);

    const visibleItems = filteredItems.slice(0, visibleCount);
    const hasMore = visibleCount < filteredItems.length;

    const handleItemClick = (id) => {
        navigate(`/items/${id}`);
    };

    const handleLoadMore = () => {
        setVisibleCount((prev) => prev + ITEMS_PER_PAGE);
    };

    return (
        <div className="item-feed-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <div className="page-header-text">
                            <h1>Lost & Found Feed</h1>
                            <p>Browse lost and found items across Cebu City campuses.</p>
                        </div>
                        <button
                            className="post-item-btn"
                            onClick={() => navigate('/post-item')}
                        >
                            <Plus size={18} />
                            Post Item
                        </button>
                    </div>

                    <FilterBar
                        searchQuery={searchQuery}
                        onSearchChange={setSearchQuery}
                        activeType={activeType}
                        onTypeChange={setActiveType}
                        activeCategory={activeCategory}
                        onCategoryChange={setActiveCategory}
                        activeSchool={activeSchool}
                        onSchoolChange={setActiveSchool}
                    />

                    {visibleItems.length > 0 ? (
                        <>
                            <div className="items-count">
                                Showing {visibleItems.length} of {filteredItems.length} items
                            </div>
                            <div className="items-grid">
                                {visibleItems.map((item, index) => (
                                    <div key={item.id} style={{ animationDelay: `${index * 0.05}s` }}>
                                        <ItemCard item={item} onClick={handleItemClick} />
                                    </div>
                                ))}
                            </div>
                            {hasMore && (
                                <div className="load-more-wrapper">
                                    <button className="load-more-btn" onClick={handleLoadMore}>
                                        Load More
                                    </button>
                                </div>
                            )}
                        </>
                    ) : (
                        <EmptyState
                            title="No items found"
                            message="Try adjusting your filters or search query, or be the first to post an item!"
                            actionLabel="Post an Item"
                            onAction={() => navigate('/post-item')}
                        />
                    )}
                </div>
            </main>
        </div>
    );
}

export default ItemFeed;
