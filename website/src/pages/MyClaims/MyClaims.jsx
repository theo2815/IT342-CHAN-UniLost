import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import claimService from '../../services/claimService';
import { timeAgo } from '../../utils/timeAgo';
import './MyClaims.css';

function MyClaims() {
    const navigate = useNavigate();
    const [claims, setClaims] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const controller = new AbortController();
        const fetchClaims = async () => {
            setLoading(true);
            setError('');
            const result = await claimService.getMyClaims();
            if (controller.signal.aborted) return;
            if (result.success) {
                setClaims(result.data);
            } else {
                setError(result.error);
            }
            setLoading(false);
        };
        fetchClaims();
        return () => controller.abort();
    }, []);

    const getInitials = (name) => {
        if (!name) return '??';
        return name.split(' ').map(p => p.charAt(0)).join('').substring(0, 2).toUpperCase();
    };

    return (
        <div className="my-claims-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <button className="back-link" onClick={() => navigate('/dashboard')}>
                        <ArrowLeft size={18} /> Back to Dashboard
                    </button>

                    <h1 className="page-title">My Claims</h1>
                    <p className="page-subtitle">Track the status of your submitted claims</p>

                    {loading ? (
                        <div className="not-found-state"><p>Loading...</p></div>
                    ) : error ? (
                        <div className="not-found-state">
                            <h2>Something went wrong</h2>
                            <p>{error}</p>
                            <button className="back-btn" onClick={() => window.location.reload()}>Try Again</button>
                        </div>
                    ) : claims.length > 0 ? (
                        <div className="mc-claims-list">
                            {claims.map((claim, index) => (
                                <div
                                    key={claim.id}
                                    className="mc-claim-card glass"
                                    style={{ animationDelay: `${index * 0.05}s` }}
                                    onClick={() => navigate(`/claims/${claim.id}`)}
                                >
                                    <img
                                        src={claim.itemImageUrl || 'https://picsum.photos/seed/placeholder/400/300'}
                                        alt={claim.itemTitle}
                                        className="mc-item-thumb"
                                    />
                                    <div className="mc-claim-info">
                                        <div className="mc-claim-badges">
                                            <StatusBadge type={claim.itemType} />
                                            <StatusBadge status={claim.status} />
                                        </div>
                                        <h3 className="mc-item-title">{claim.itemTitle}</h3>
                                        <p className="mc-claim-meta">
                                            Posted by {claim.finderName} &middot; {timeAgo(claim.createdAt)}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <EmptyState
                            title="No claims yet"
                            message="You haven't submitted any claims. Browse the feed to find your lost items."
                            actionLabel="Browse Items"
                            onAction={() => navigate('/items')}
                        />
                    )}
                </div>
            </main>
        </div>
    );
}

export default MyClaims;
