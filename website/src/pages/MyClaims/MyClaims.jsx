import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, CheckCircle, Clock, XCircle, ArrowRight, HandHelping } from 'lucide-react';
import Header from '../../components/Header';
import StatusBadge from '../../components/StatusBadge';
import EmptyState from '../../components/EmptyState';
import { getMyOutgoingClaims, timeAgo } from '../../mockData/claims';
import './MyClaims.css';

const TABS = [
    { key: 'ALL', label: 'All' },
    { key: 'PENDING', label: 'Pending' },
    { key: 'APPROVED', label: 'Approved' },
    { key: 'REJECTED', label: 'Rejected' },
    { key: 'HANDED_OVER', label: 'Handed Over' },
];

function MyClaims() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('ALL');

    // Mock: get claims submitted by user u1
    const myClaims = useMemo(() => {
        return getMyOutgoingClaims('u1');
    }, []);

    const filteredClaims = useMemo(() => {
        if (activeTab === 'ALL') return myClaims;
        return myClaims.filter((claim) => claim.status === activeTab);
    }, [myClaims, activeTab]);

    const stats = useMemo(() => ({
        PENDING: myClaims.filter((c) => c.status === 'PENDING').length,
        APPROVED: myClaims.filter((c) => c.status === 'APPROVED').length,
        REJECTED: myClaims.filter((c) => c.status === 'REJECTED').length,
        HANDED_OVER: myClaims.filter((c) => c.status === 'HANDED_OVER').length,
    }), [myClaims]);

    const getHandoverProgress = (claim) => {
        if (claim.status !== 'APPROVED') return null;
        const { posterConfirmed, claimantConfirmed } = claim.handover;
        if (posterConfirmed && claimantConfirmed) return 'Both confirmed';
        if (posterConfirmed) return 'Poster confirmed - Your turn';
        if (claimantConfirmed) return 'You confirmed - Waiting for poster';
        return 'Awaiting confirmations';
    };

    return (
        <div className="my-claims-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <div className="page-header">
                        <div className="page-header-text">
                            <h1>My Claims</h1>
                            <p>Track claims you've submitted on other people's items.</p>
                        </div>
                    </div>

                    {/* Tabs */}
                    <div className="tabs">
                        {TABS.map((tab) => (
                            <button
                                key={tab.key}
                                className={`tab ${activeTab === tab.key ? 'active' : ''}`}
                                onClick={() => setActiveTab(tab.key)}
                            >
                                {tab.label}
                                {tab.key !== 'ALL' && stats[tab.key] > 0 && (
                                    <span className="tab-count">{stats[tab.key]}</span>
                                )}
                            </button>
                        ))}
                    </div>

                    {/* Claims List */}
                    {filteredClaims.length > 0 ? (
                        <div className="claims-list">
                            {filteredClaims.map((claim, index) => (
                                <div
                                    key={claim.id}
                                    className="claim-row glass"
                                    style={{ animationDelay: `${index * 0.05}s` }}
                                    onClick={() => navigate(`/claims/${claim.id}`)}
                                >
                                    <div className="claim-row-image">
                                        <img src={claim.itemImageUrl} alt={claim.itemTitle} />
                                        <span className={`type-badge ${claim.itemType.toLowerCase()}`}>
                                            {claim.itemType}
                                        </span>
                                    </div>

                                    <div className="claim-row-info">
                                        <div className="claim-row-top">
                                            <h3>{claim.itemTitle}</h3>
                                            <StatusBadge status={claim.status} />
                                        </div>
                                        <p className="claim-row-message">{claim.message}</p>
                                        <div className="claim-row-meta">
                                            <span>Submitted {timeAgo(claim.createdAt)}</span>
                                            <span>&middot;</span>
                                            <span>Posted by {claim.posterName}</span>
                                        </div>

                                        {/* Handover progress for APPROVED claims */}
                                        {claim.status === 'APPROVED' && (
                                            <div className="handover-progress">
                                                <HandHelping size={14} />
                                                <span>{getHandoverProgress(claim)}</span>
                                            </div>
                                        )}
                                    </div>

                                    <div className="claim-row-arrow">
                                        <ArrowRight size={18} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <EmptyState
                            title={activeTab === 'ALL' ? 'No claims yet' : `No ${activeTab.toLowerCase().replace('_', ' ')} claims`}
                            message="When you submit a claim on an item, it will appear here."
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
