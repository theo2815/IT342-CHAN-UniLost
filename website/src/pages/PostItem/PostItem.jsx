import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Upload, X, Eye, Image, MapPin, Calendar, Lock, ArrowLeft, Loader } from 'lucide-react';
import Header from '../../components/Header';
import ItemCard from '../../components/ItemCard';
import { ITEM_CATEGORIES, CATEGORY_LABELS } from '../../constants/categories';
import authService from '../../services/authService';
import itemService from '../../services/itemService';
import './PostItem.css';

function PostItem() {
    const navigate = useNavigate();
    const fileInputRef = useRef(null);
    const user = authService.getCurrentUser();

    const [formData, setFormData] = useState({
        type: 'LOST',
        title: '',
        description: '',
        category: '',
        locationDescription: '',
        date: '',
        secretDetail: '',
    });
    const [images, setImages] = useState([]);
    const [showPreview, setShowPreview] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    // H11: Revoke object URLs on unmount to prevent memory leaks
    useEffect(() => {
        return () => {
            images.forEach((img) => URL.revokeObjectURL(img.preview));
        };
    }, [images]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleTypeToggle = (type) => {
        setFormData((prev) => ({ ...prev, type, secretDetail: '' }));
    };

    const handleImageDrop = (e) => {
        e.preventDefault();
        const files = Array.from(e.dataTransfer?.files || e.target?.files || []);
        const imageFiles = files.filter((f) => f.type.startsWith('image/'));
        if (images.length + imageFiles.length > 3) {
            alert('Maximum 3 images allowed');
            return;
        }
        const newImages = imageFiles.map((file) => ({
            file,
            preview: URL.createObjectURL(file),
            name: file.name,
        }));
        setImages((prev) => [...prev, ...newImages]);
    };

    const removeImage = (index) => {
        setImages((prev) => {
            const updated = [...prev];
            URL.revokeObjectURL(updated[index].preview);
            updated.splice(index, 1);
            return updated;
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError('');

        const itemData = {
            title: formData.title,
            description: formData.description,
            type: formData.type,
            category: formData.category,
            location: formData.locationDescription,
            secretDetailQuestion: formData.type === 'FOUND' ? formData.secretDetail : null,
            dateLostFound: formData.date ? `${formData.date}T00:00:00` : null,
        };

        const imageFiles = images.map((img) => img.file);
        const result = await itemService.createItem(itemData, imageFiles);

        if (result.success) {
            setSubmitted(true);
            setTimeout(() => {
                navigate('/items');
            }, 2000);
        } else {
            setError(result.error);
        }

        setSubmitting(false);
    };

    const isFormValid = formData.title && formData.description && formData.category && formData.locationDescription;

    // Build a preview item for the ItemCard
    const previewItem = {
        id: 'preview',
        type: formData.type,
        title: formData.title || 'Item Title',
        description: formData.description || 'Item description...',
        category: formData.category || 'Category',
        status: 'ACTIVE',
        imageUrls: images[0] ? [images[0].preview] : [],
        imageUrl: images[0]?.preview || 'https://picsum.photos/seed/preview/400/300',
        campus: { name: user?.campus?.name || 'Your School' },
        school: { shortName: user?.campus?.name || 'Your School' },
        location: formData.locationDescription || 'Location',
        locationDescription: formData.locationDescription || 'Location',
        createdAt: new Date().toISOString(),
    };

    if (submitted) {
        return (
            <div className="post-item-page">
                <Header />
                <main className="main-content">
                    <div className="content-wrapper">
                        <div className="success-state">
                            <div className="success-icon">&#10003;</div>
                            <h2>Item Posted Successfully!</h2>
                            <p>Your {formData.type.toLowerCase()} item report has been submitted. Redirecting to items feed...</p>
                        </div>
                    </div>
                </main>
            </div>
        );
    }

    return (
        <div className="post-item-page">
            <Header />

            <main className="main-content">
                <div className="content-wrapper">
                    <button className="back-link" onClick={() => navigate('/items')}>
                        <ArrowLeft size={18} /> Back to Feed
                    </button>

                    <div className="post-layout">
                        {/* Form */}
                        <div className="post-form-section">
                            <h1>Report an Item</h1>
                            <p className="post-subtitle">Help reunite lost items with their owners.</p>

                            {error && (
                                <div className="form-error-banner">{error}</div>
                            )}

                            <form onSubmit={handleSubmit}>
                                {/* Type Toggle */}
                                <div className="type-toggle">
                                    <button
                                        type="button"
                                        className={`type-option ${formData.type === 'LOST' ? 'active lost' : ''}`}
                                        onClick={() => handleTypeToggle('LOST')}
                                    >
                                        I Lost Something
                                    </button>
                                    <button
                                        type="button"
                                        className={`type-option ${formData.type === 'FOUND' ? 'active found' : ''}`}
                                        onClick={() => handleTypeToggle('FOUND')}
                                    >
                                        I Found Something
                                    </button>
                                </div>

                                {/* Title */}
                                <div className="form-group">
                                    <label htmlFor="title">Title <span className="required">*</span></label>
                                    <input
                                        id="title"
                                        name="title"
                                        type="text"
                                        placeholder="e.g. Black Samsung Galaxy S24"
                                        value={formData.title}
                                        onChange={handleChange}
                                        maxLength={100}
                                        required
                                    />
                                </div>

                                {/* Description */}
                                <div className="form-group">
                                    <label htmlFor="description">Description <span className="required">*</span></label>
                                    <textarea
                                        id="description"
                                        name="description"
                                        placeholder="Describe the item in detail — color, brand, distinguishing marks..."
                                        value={formData.description}
                                        onChange={handleChange}
                                        rows={4}
                                        maxLength={1000}
                                        required
                                    />
                                    <span className="char-count">{formData.description.length}/1000</span>
                                </div>

                                {/* Category */}
                                <div className="form-group">
                                    <label htmlFor="category">Category <span className="required">*</span></label>
                                    <select
                                        id="category"
                                        name="category"
                                        value={formData.category}
                                        onChange={handleChange}
                                        required
                                    >
                                        <option value="">Select a category</option>
                                        {ITEM_CATEGORIES.map((cat) => (
                                            <option key={cat} value={cat}>{CATEGORY_LABELS[cat] || cat}</option>
                                        ))}
                                    </select>
                                </div>

                                {/* Location */}
                                <div className="form-group">
                                    <label htmlFor="locationDescription">
                                        <MapPin size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> Location <span className="required">*</span>
                                    </label>
                                    <input
                                        id="locationDescription"
                                        name="locationDescription"
                                        type="text"
                                        placeholder="e.g. CIT-U Main Library, 2nd Floor"
                                        value={formData.locationDescription}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>

                                {/* Date */}
                                <div className="form-group">
                                    <label htmlFor="date">
                                        <Calendar size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> When was it {formData.type === 'LOST' ? 'lost' : 'found'}?
                                    </label>
                                    <input
                                        id="date"
                                        name="date"
                                        type="date"
                                        value={formData.date}
                                        onChange={handleChange}
                                    />
                                </div>

                                {/* Image Upload */}
                                <div className="form-group">
                                    <label><Image size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> Photos (max 3)</label>
                                    <div
                                        className="image-upload-zone"
                                        onDragOver={(e) => e.preventDefault()}
                                        onDrop={handleImageDrop}
                                        onClick={() => fileInputRef.current?.click()}
                                    >
                                        <Upload size={32} />
                                        <span>Drag & drop images or click to browse</span>
                                        <input
                                            ref={fileInputRef}
                                            type="file"
                                            accept="image/*"
                                            multiple
                                            hidden
                                            onChange={handleImageDrop}
                                        />
                                    </div>
                                    {images.length > 0 && (
                                        <div className="image-previews">
                                            {images.map((img, i) => (
                                                <div key={i} className="image-thumb">
                                                    <img src={img.preview} alt={img.name} />
                                                    <button type="button" className="remove-img" onClick={() => removeImage(i)}>
                                                        <X size={14} />
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* Secret Detail (FOUND only) */}
                                {formData.type === 'FOUND' && (
                                    <div className="form-group secret-detail-group">
                                        <label htmlFor="secretDetail">
                                            <Lock size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> Secret Detail
                                        </label>
                                        <input
                                            id="secretDetail"
                                            name="secretDetail"
                                            type="text"
                                            placeholder="e.g. There is a sticker on the back"
                                            value={formData.secretDetail}
                                            onChange={handleChange}
                                        />
                                        <span className="helper-text">Enter a detail only the true owner would know. This helps verify claims.</span>
                                    </div>
                                )}

                                {/* Actions */}
                                <div className="form-actions">
                                    <button
                                        type="button"
                                        className="preview-btn"
                                        onClick={() => setShowPreview(!showPreview)}
                                    >
                                        <Eye size={16} /> {showPreview ? 'Hide' : 'Show'} Preview
                                    </button>
                                    <button
                                        type="submit"
                                        className="submit-btn"
                                        disabled={!isFormValid || submitting}
                                    >
                                        {submitting ? <><Loader size={16} className="spin" /> Posting...</> : 'Post Item'}
                                    </button>
                                </div>
                            </form>
                        </div>

                        {/* Preview */}
                        {showPreview && (
                            <div className="post-preview-section">
                                <h3>Preview</h3>
                                <p className="preview-hint">This is how your item will appear in the feed.</p>
                                <ItemCard item={previewItem} />
                            </div>
                        )}
                    </div>
                </div>
            </main>
        </div>
    );
}

export default PostItem;
