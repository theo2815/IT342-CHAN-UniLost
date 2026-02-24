import { useState } from 'react';
import { X, CheckCircle, Lock, MessageSquare, Mail } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import './ClaimModal.css';

function ClaimModal({ item, onClose }) {
    const navigate = useNavigate();
    const [secretAnswer, setSecretAnswer] = useState('');
    const [message, setMessage] = useState('');
    const [showEmail, setShowEmail] = useState(false);
    const [submitted, setSubmitted] = useState(false);

    const isFound = item.type === 'FOUND';
    const isFormValid = message.trim().length > 0;

    const handleSubmit = () => {
        setSubmitted(true);
    };

    const handleBackdropClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    if (submitted) {
        return (
            <div className="claim-modal-backdrop" onClick={handleBackdropClick}>
                <div className="claim-modal glass">
                    <div className="claim-modal-success">
                        <div className="success-icon">
                            <CheckCircle size={48} />
                        </div>
                        <h2>Claim Submitted!</h2>
                        <p>The poster will review your claim and get back to you.</p>
                        <div className="success-actions">
                            <button
                                className="btn-primary"
                                onClick={() => { onClose(); navigate('/my-claims'); }}
                            >
                                View My Claims
                            </button>
                            <button className="btn-secondary" onClick={onClose}>
                                Back to Item
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="claim-modal-backdrop" onClick={handleBackdropClick}>
            <div className="claim-modal glass">
                {/* Header */}
                <div className="claim-modal-header">
                    <h2>Submit a Claim</h2>
                    <button className="modal-close-btn" onClick={onClose}>
                        <X size={20} />
                    </button>
                </div>

                {/* Item summary */}
                <div className="claim-item-summary">
                    <img src={item.imageUrl} alt={item.title} className="claim-item-thumb" />
                    <div className="claim-item-info">
                        <span className={`claim-type-badge ${item.type.toLowerCase()}`}>{item.type}</span>
                        <h3>{item.title}</h3>
                        <p>{item.locationDescription}</p>
                    </div>
                </div>

                <div className="claim-modal-body">
                    {/* Secret Detail Answer (FOUND items only) */}
                    {isFound && (
                        <div className="claim-field secret-field">
                            <label>
                                <Lock size={14} />
                                Secret Detail Answer
                            </label>
                            <input
                                type="text"
                                value={secretAnswer}
                                onChange={(e) => setSecretAnswer(e.target.value)}
                                placeholder="Describe a unique feature only the owner would know"
                                className="claim-input"
                            />
                            <span className="field-hint">
                                This helps the poster verify you are the true owner.
                            </span>
                        </div>
                    )}

                    {/* Message */}
                    <div className="claim-field">
                        <label>
                            <MessageSquare size={14} />
                            Your Message *
                        </label>
                        <textarea
                            value={message}
                            onChange={(e) => { if (e.target.value.length <= 500) setMessage(e.target.value); }}
                            placeholder="Why do you think this is your item? Provide details to help the poster verify your claim."
                            className="claim-textarea"
                            rows={4}
                        />
                        <span className="field-hint">{message.length}/500</span>
                    </div>

                    {/* Contact preference */}
                    <label className="claim-checkbox">
                        <input
                            type="checkbox"
                            checked={showEmail}
                            onChange={(e) => setShowEmail(e.target.checked)}
                        />
                        <Mail size={14} />
                        <span>Show my email to the poster if approved</span>
                    </label>
                </div>

                {/* Actions */}
                <div className="claim-modal-actions">
                    <button className="btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                    <button
                        className="btn-primary"
                        disabled={!isFormValid}
                        onClick={handleSubmit}
                    >
                        Submit Claim
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ClaimModal;
