import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Plus, AlertCircle } from 'lucide-react';
import Header from '../../components/Header';
import Footer from '../../components/Footer';
import ItemCard from '../../components/ItemCard';
import FilterBar from '../../components/FilterBar';
import EmptyState from '../../components/EmptyState';
import { Spinner } from '../../components/ui';
import itemService from '../../services/itemService';
import './ItemFeed.css';

const ITEMS_PER_PAGE = 9;

function ItemFeed() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [searchQuery, setSearchQuery] = useState('');
    const [debouncedSearch, setDebouncedSearch] = useState('');
    const [activeType, setActiveType] = useState('All');
    const [activeCategory, setActiveCategory] = useState('');
    const [activeSchool, setActiveSchool] = useState(() => searchParams.get('campusId') || '');
    const [items, setItems] = useState([]);
    const [totalItems, setTotalItems] = useState(0);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const abortControllerRef = useRef(null);

    // Debounce search input by 400ms
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearch(searchQuery);
        }, 400);
        return () => clearTimeout(timer);
    }, [searchQuery]);

    const fetchItems = useCallback(async (pageNum = 0, append = false) => {
        // Cancel any in-flight request
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }
        const controller = new AbortController();
        abortControllerRef.current = controller;

        setLoading(true);
        setError('');
        const params = {
            page: pageNum,
            size: ITEMS_PER_PAGE,
            status: 'ACTIVE,CLAIMED,PENDING_OWNER_CONFIRMATION',
        };

        if (debouncedSearch) params.keyword = debouncedSearch;
        if (activeType === 'Lost') params.type = 'LOST';
        else if (activeType === 'Found') params.type = 'FOUND';
        if (activeCategory) params.category = activeCategory;
        if (activeSchool) params.campusId = activeSchool;

        const result = await itemService.getItems(params);

        // Ignore if this request was aborted
        if (controller.signal.aborted) return;

        if (result.success) {
            const pageData = result.data;
            if (append) {
                setItems(prev => [...prev, ...pageData.content]);
            } else {
                setItems(pageData.content);
            }
            setTotalItems(pageData.totalElements);
        } else {
            setError(result.error);
        }
        setLoading(false);
    }, [debouncedSearch, activeType, activeCategory, activeSchool]);

    // Re-fetch when filters change
    useEffect(() => {
        setPage(0);
        fetchItems(0);
        return () => {
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
        };
    }, [fetchItems]);

    const hasMore = items.length < totalItems;

    const handleItemClick = (id) => {
        navigate(`/items/${id}`);
    };

    const handleLoadMore = () => {
        const nextPage = page + 1;
        setPage(nextPage);
        fetchItems(nextPage, true);
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

                    {error && (
                        <div className="feed-error">
                            <AlertCircle size={16} />
                            Failed to load items. Please try again.
                        </div>
                    )}

                    {items.length > 0 ? (
                        <>
                            <div className="items-count">
                                Showing {items.length} of {totalItems} items
                            </div>
                            <div className="items-grid">
                                {items.map((item) => (
                                    <ItemCard key={item.id} item={item} onClick={handleItemClick} />
                                ))}
                            </div>
                            {hasMore && (
                                <div className="load-more-wrapper">
                                    <button className="load-more-btn" onClick={handleLoadMore} disabled={loading}>
                                        {loading ? 'Loading...' : 'Load More'}
                                    </button>
                                </div>
                            )}
                        </>
                    ) : loading ? (
                        <div className="feed-loading">
                            <Spinner size="md" label="Loading items..." />
                        </div>
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

            <Footer />
        </div>
    );
}

export default ItemFeed;
