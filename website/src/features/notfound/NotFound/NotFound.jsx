import { useNavigate } from 'react-router-dom';
import { Compass } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import EmptyState from '../../../shared/components/EmptyState/EmptyState';
import './NotFound.css';

function NotFound() {
    const navigate = useNavigate();

    return (
        <div className="not-found-page">
            <Header />
            <main className="main-content">
                <div className="content-wrapper">
                    <EmptyState
                        icon={<Compass size={48} />}
                        title="Page not found"
                        message="The page you're looking for doesn't exist or may have moved. Let's get you back on track."
                        actionLabel="Go to Home"
                        onAction={() => navigate('/')}
                    />
                </div>
            </main>
        </div>
    );
}

export default NotFound;
