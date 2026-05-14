import { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Upload, X, Eye, Image, MapPin, Calendar, Lock, ArrowLeft, Loader, ChevronDown, AlertCircle } from 'lucide-react';
import Header from '../../../shared/layout/Header/Header';
import ItemCard from '../ItemCard/ItemCard';
import LocationPicker from '../../../shared/components/LocationPicker/LocationPicker';
import { Input, Dropdown } from '../../../shared/components/ui';
import { ITEM_CATEGORIES, CATEGORY_LABELS } from '../../../shared/constants/categories';
import authService from '../../auth/authService';
import itemService from '../itemService';
import './PostItem.css';

function PostItem() {
    const navigate = useNavigate();
    const location = useLocation();
    const fileInputRef = useRef(null);
    const user = authService.getCurrentUser();

    // Check for "Edit Mode"
    const searchParams = new URLSearchParams(location.search);
    const editId = searchParams.get('edit');
    const isEditMode = !!editId;

    const [formData, setFormData] = useState({
        type: 'LOST',
        title: '',
        description: '',
        category: '',
        locationDescription: '',
        latitude: null,
        longitude: null,
        date: '',
        secretDetail: '',
    });
    const [images, setImages] = useState([]);
    const [showPreview, setShowPreview] = useState(true);
    const [submitted, setSubmitted] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [loadingData, setLoadingData] = useState(isEditMode);
    const [error, setError] = useState('');
    const [fieldErrors, setFieldErrors] = useState({});
    const [touched, setTouched] = useState({});

    const MAX_IMAGES = 3;
    const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

    useEffect(() => {
        if (!editId) {
            setFormData({
                type: 'LOST',
                title: '',
                description: '',
                category: '',
                locationDescription: '',
                latitude: null,
                longitude: null,
                date: '',
                secretDetail: '',
            });
            setImages([]);
            setFieldErrors({});
            setTouched({});
            setError('');
            return;
        }

        const loadItemData = async () => {
            setLoadingData(true);
            const result = await itemService.getItemById(editId);
            if (result.success) {
                const item = result.data;
                let formattedDate = '';
                if (item.dateLostFound) {
                    formattedDate = item.dateLostFound.split('T')[0];
                }

                setFormData({
                    type: item.type || 'LOST',
                    title: item.title || '',
                    description: item.description || '',
                    category: item.category || '',
                    locationDescription: item.location || '',
                    latitude: item.latitude || null,
                    longitude: item.longitude || null,
                    date: formattedDate,
                    secretDetail: item.secretDetailQuestion || '',
                });

                if (item.imageUrls && item.imageUrls.length > 0) {
                    const loadedImages = item.imageUrls.map((url, i) => ({
                        file: null, // No fresh File object for existing URLs
                        preview: url,
                        name: `Existing Image ${i + 1}`,
                        isExisting: true
                    }));
                    setImages(loadedImages);
                }
            } else {
                setError('Failed to load item data: ' + result.error);
            }
            setLoadingData(false);
        };
        loadItemData();
    }, [editId]);

    const handleGoBack = () => {
        if (window.history.state && window.history.state.idx > 0) {
            navigate(-1);
        } else {
            navigate(isEditMode ? `/items/${editId}` : '/items');
        }
    };

    // H11: Revoke object URLs on unmount to prevent memory leaks
    useEffect(() => {
        return () => {
            images.forEach((img) => URL.revokeObjectURL(img.preview));
        };
    }, [images]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        // Clear field error when user types
        if (fieldErrors[name]) {
            setFieldErrors((prev) => ({ ...prev, [name]: '' }));
        }
    };

    const handleBlur = (e) => {
        const { name } = e.target;
        setTouched((prev) => ({ ...prev, [name]: true }));
    };

    const handleTypeToggle = (type) => {
        setFormData((prev) => ({ ...prev, type, secretDetail: '' }));
    };

    const MAX_FILE_SIZE_MB = 5;

    const handleImageDrop = (e) => {
        e.preventDefault();
        const files = Array.from(e.dataTransfer?.files || e.target?.files || []);

        // Filter for valid image types
        const imageFiles = files.filter((f) => ALLOWED_TYPES.includes(f.type));
        const invalidFiles = files.filter((f) => !f.type.startsWith('image/'));

        if (invalidFiles.length > 0) {
            setFieldErrors((prev) => ({ ...prev, images: `Only image files (JPG, PNG, GIF, WebP) are allowed. "${invalidFiles[0].name}" is not a valid image.` }));
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }

        const remaining = MAX_IMAGES - images.length;
        if (remaining <= 0) {
            setFieldErrors((prev) => ({ ...prev, images: `You can upload up to ${MAX_IMAGES} images only. Remove an existing image first.` }));
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }

        if (imageFiles.length > remaining) {
            setFieldErrors((prev) => ({ ...prev, images: `You can upload up to ${MAX_IMAGES} images only. You tried to add ${imageFiles.length} but only ${remaining} slot${remaining > 1 ? 's' : ''} remaining.` }));
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }

        const oversized = imageFiles.find((f) => f.size > MAX_FILE_SIZE_MB * 1024 * 1024);
        if (oversized) {
            setFieldErrors((prev) => ({ ...prev, images: `"${oversized.name}" exceeds the ${MAX_FILE_SIZE_MB}MB limit. Please choose a smaller file.` }));
            if (fileInputRef.current) fileInputRef.current.value = '';
            return;
        }

        setFieldErrors((prev) => ({ ...prev, images: '' }));
        setError('');
        const newImages = imageFiles.map((file) => ({
            file,
            preview: URL.createObjectURL(file),
            name: file.name,
        }));
        setImages((prev) => [...prev, ...newImages]);
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const removeImage = (index) => {
        setImages((prev) => {
            const updated = [...prev];
            URL.revokeObjectURL(updated[index].preview);
            updated.splice(index, 1);
            return updated;
        });
    };

    const validateForm = () => {
        const errors = {};
        if (!formData.title.trim()) errors.title = 'Item name is required.';
        if (!formData.description.trim()) errors.description = 'Description is required.';
        if (!formData.category) errors.category = 'Please select a category.';
        if (!formData.locationDescription.trim()) errors.locationDescription = 'Location description is required.';
        if (formData.latitude == null || formData.longitude == null) {
            errors.location = 'Please drop a pin on the map to mark the exact location.';
        }
        if (!formData.date) {
            errors.date = `Please provide the date when the item was ${formData.type === 'LOST' ? 'lost' : 'found'}.`;
        }
        if (images.length === 0 && !isEditMode) {
            errors.images = 'Please upload at least one photo of the item.';
        } else if (images.length === 0 && isEditMode) {
             // In edit mode we also require at least one photo (could be existing)
             errors.images = 'Please keep at least one photo of the item.';
        }

        setFieldErrors(errors);
        // Mark all as touched so errors show
        setTouched({ title: true, description: true, category: true, locationDescription: true, location: true, images: true, date: true });
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            setError('Please fix the errors below before submitting.');
            return;
        }

        setSubmitting(true);
        setError('');

        const itemData = {
            title: formData.title,
            description: formData.description,
            type: formData.type,
            category: formData.category,
            location: formData.locationDescription,
            latitude: formData.latitude,
            longitude: formData.longitude,
            secretDetailQuestion: formData.type === 'FOUND' ? formData.secretDetail : null,
            dateLostFound: formData.date ? `${formData.date}T00:00:00` : null,
        };

        const imageFiles = images.map((img) => img.file).filter(file => file !== null);
        
        let result;
        if (isEditMode) {
            result = await itemService.updateItem(editId, itemData, imageFiles);
        } else {
            result = await itemService.createItem(itemData, imageFiles);
        }

        if (result.success) {
            setSubmitted(true);
            setTimeout(() => {
                navigate(isEditMode ? `/items/${editId}` : '/items');
            }, 2000);
        } else {
            setError(result.error);
        }

        setSubmitting(false);
    };

    // Build a preview item for the ItemCard
    const previewItem = {
        id: 'preview',
        type: formData.type,
        title: formData.title || 'Item Title',
        description: formData.description || 'Item description...',
        category: formData.category || 'Category',
        status: 'ACTIVE',
        imageUrls: images.length > 0 ? images.map(img => img.preview) : [],
        imageUrl: images[0]?.preview || 'https://placehold.co/600x400/png?text=No+Photo+Uploaded',
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
                            <h2>Item {isEditMode ? 'Updated' : 'Posted'} Successfully!</h2>
                            <p>Your {formData.type.toLowerCase()} item report has been {isEditMode ? 'updated' : 'submitted'}. Redirecting...</p>
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
                    <button className="back-link" onClick={handleGoBack}>
                        <ArrowLeft size={18} /> Go Back
                    </button>

                    <div className="post-layout">
                        {/* Form */}
                        <div className="post-form-section">
                            <h1>{isEditMode ? 'Edit Item' : 'Report an Item'}</h1>
                            <p className="post-subtitle">
                                {isEditMode ? 'Update the details of your item report.' : 'Help reunite lost items with their owners.'}
                            </p>

                            {error && (
                                <div className="form-error-banner">{error}</div>
                            )}

                            {loadingData ? (
                                <div className="loading-state" style={{ padding: '40px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
                                    <Loader className="spin" size={24} style={{ margin: '0 auto 12px' }} />
                                    <p>Loading item details...</p>
                                </div>
                            ) : (
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
                                <div className="form-field-wrapper">
                                    <Input
                                        id="title"
                                        name="title"
                                        label="Title"
                                        required
                                        placeholder="e.g. Black Samsung Galaxy S24"
                                        value={formData.title}
                                        onChange={handleChange}
                                        onBlur={handleBlur}
                                        maxLength={100}
                                    />
                                    {touched.title && fieldErrors.title && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.title}</span>
                                    )}
                                </div>

                                {/* Description */}
                                <div className="form-field-wrapper">
                                    <Input
                                        id="description"
                                        name="description"
                                        label="Description"
                                        required
                                        textarea
                                        placeholder="Describe the item in detail — color, brand, distinguishing marks..."
                                        value={formData.description}
                                        onChange={handleChange}
                                        onBlur={handleBlur}
                                        rows={4}
                                        maxLength={1000}
                                    />
                                    {touched.description && fieldErrors.description && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.description}</span>
                                    )}
                                </div>

                                {/* Category */}
                                <div className="form-group-wrapper">
                                    <label className="ui-input__label ui-input__label--required" htmlFor="category">Category</label>
                                    <Dropdown
                                        id="category"
                                        trigger={(isOpen) => (
                                            <div
                                                className={`ui-select-group ui-select-group--md ${isOpen ? "focus-within" : ""}${touched.category && fieldErrors.category ? " field-has-error" : ""}`}
                                                style={{ cursor: "pointer", background: "var(--color-bg-card)", width: "100%" }}
                                            >
                                                <div className="ui-select__field" style={{ display: "flex", alignItems: "center" }}>
                                                    {formData.category
                                                        ? CATEGORY_LABELS[formData.category] || formData.category
                                                        : "Select a category"}
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
                                                    className={!formData.category ? "active" : ""}
                                                    onClick={() => {
                                                        handleChange({ target: { name: 'category', value: '' } });
                                                        close();
                                                    }}
                                                >
                                                    Select a category
                                                </Dropdown.Item>
                                                {ITEM_CATEGORIES.map((cat) => (
                                                    <Dropdown.Item
                                                        key={cat}
                                                        className={formData.category === cat ? "active" : ""}
                                                        onClick={() => {
                                                            handleChange({ target: { name: 'category', value: cat } });
                                                            close();
                                                        }}
                                                    >
                                                        {CATEGORY_LABELS[cat] || cat}
                                                    </Dropdown.Item>
                                                ))}
                                            </>
                                        )}
                                    </Dropdown>
                                    {touched.category && fieldErrors.category && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.category}</span>
                                    )}
                                </div>

                                {/* Location */}
                                <div className="form-group-wrapper">
                                    <Input
                                        id="locationDescription"
                                        name="locationDescription"
                                        label="Location"
                                        required
                                        icon={MapPin}
                                        placeholder="e.g. CIT-U Main Library, 2nd Floor"
                                        value={formData.locationDescription}
                                        onChange={handleChange}
                                        onBlur={handleBlur}
                                    />
                                    {touched.locationDescription && fieldErrors.locationDescription && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.locationDescription}</span>
                                    )}
                                    <LocationPicker
                                        latitude={formData.latitude}
                                        longitude={formData.longitude}
                                        onChange={({ latitude, longitude }) => {
                                            setFormData((prev) => ({ ...prev, latitude, longitude }));
                                            setFieldErrors((prev) => ({ ...prev, location: '' }));
                                        }}
                                    />
                                    {touched.location && fieldErrors.location && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.location}</span>
                                    )}
                                </div>

                                {/* Date */}
                                <div className="form-field-wrapper">
                                    <Input
                                        id="date"
                                        name="date"
                                        type="date"
                                        required
                                        label={`When was it ${formData.type === 'LOST' ? 'lost' : 'found'}?`}
                                        icon={Calendar}
                                        value={formData.date}
                                        onChange={handleChange}
                                        onBlur={handleBlur}
                                    />
                                    {touched.date && fieldErrors.date && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.date}</span>
                                    )}
                                </div>

                                {/* Image Upload */}
                                <div className="form-group">
                                    <label className="ui-input__label ui-input__label--required">
                                        <Image size={14} style={{ display: 'inline', verticalAlign: 'middle', marginRight: '4px' }} /> Photos
                                        <span className="image-count-badge">{images.length} / {MAX_IMAGES}</span>
                                    </label>
                                    {images.length < MAX_IMAGES ? (
                                        <div
                                            className={`image-upload-zone${fieldErrors.images ? ' has-error' : ''}`}
                                            onDragOver={(e) => e.preventDefault()}
                                            onDrop={handleImageDrop}
                                            onClick={() => fileInputRef.current?.click()}
                                        >
                                            <Upload size={32} />
                                            <span>Drag & drop images or click to browse</span>
                                            <span className="upload-hint">JPG, PNG, GIF, WebP — max {MAX_FILE_SIZE_MB}MB each</span>
                                            <input
                                                ref={fileInputRef}
                                                type="file"
                                                accept="image/jpeg,image/png,image/gif,image/webp"
                                                multiple
                                                hidden
                                                onChange={handleImageDrop}
                                            />
                                        </div>
                                    ) : (
                                        <div className="image-upload-zone upload-full">
                                            <span>Maximum {MAX_IMAGES} images reached. Remove an image to add a new one.</span>
                                        </div>
                                    )}
                                    {fieldErrors.images && (
                                        <span className="field-error"><AlertCircle size={13} /> {fieldErrors.images}</span>
                                    )}
                                    {images.length > 0 && (
                                        <div className="image-previews">
                                            {images.map((img, i) => (
                                                <div key={i} className="image-thumb">
                                                    <img src={img.preview} alt={img.name} />
                                                    <button type="button" className="remove-img" onClick={() => {
                                                        removeImage(i);
                                                        setFieldErrors((prev) => ({ ...prev, images: '' }));
                                                    }}>
                                                        <X size={14} />
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* Secret Detail (FOUND only) */}
                                {formData.type === 'FOUND' && (
                                    <div className="secret-detail-section">
                                        <Input
                                            id="secretDetail"
                                            name="secretDetail"
                                            label="Secret Detail"
                                            icon={Lock}
                                            placeholder="e.g. There is a sticker on the back"
                                            value={formData.secretDetail}
                                            onChange={handleChange}
                                            helper="Enter a detail only the true owner would know. This helps verify claims."
                                        />
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
                                        disabled={submitting}
                                    >
                                        {submitting ? (
                                            <><Loader size={16} className="spin" /> {isEditMode ? 'Saving...' : 'Posting...'}</>
                                        ) : (
                                            isEditMode ? 'Save Changes' : 'Post Item'
                                        )}
                                    </button>
                                </div>
                            </form>
                            )}
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
