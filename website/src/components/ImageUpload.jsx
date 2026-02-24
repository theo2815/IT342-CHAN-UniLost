import { useRef, useState } from 'react';
import { Upload, X, Image } from 'lucide-react';
import './ImageUpload.css';

function ImageUpload({
    images = [],
    onChange,
    maxImages = 5,
    label = 'Upload Images'
}) {
    const [isDragOver, setIsDragOver] = useState(false);
    const fileInputRef = useRef(null);

    const processFiles = (files) => {
        const fileArray = Array.from(files).filter(f => f.type.startsWith('image/'));
        const remaining = maxImages - images.length;
        const newFiles = fileArray.slice(0, remaining).map(file => ({
            file,
            name: file.name,
            preview: URL.createObjectURL(file)
        }));

        if (newFiles.length > 0) {
            onChange([...images, ...newFiles]);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setIsDragOver(false);

        if (e.dataTransfer?.files) {
            processFiles(e.dataTransfer.files);
        }
    };

    const handleFileChange = (e) => {
        if (e.target.files) {
            processFiles(e.target.files);
        }
        // Reset input so same file can be selected again
        e.target.value = '';
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setIsDragOver(true);
    };

    const handleDragLeave = (e) => {
        e.preventDefault();
        setIsDragOver(false);
    };

    const removeImage = (index) => {
        const updated = images.filter((_, i) => i !== index);
        onChange(updated);
    };

    const canAddMore = images.length < maxImages;

    return (
        <div className="image-upload-component">
            {label && (
                <label className="image-upload-label">
                    <Image size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />
                    {' '}{label} (max {maxImages})
                </label>
            )}

            {canAddMore && (
                <div
                    className={`image-upload-dropzone ${isDragOver ? 'drag-over' : ''}`}
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                    onClick={() => fileInputRef.current?.click()}
                >
                    <Upload size={32} />
                    <span className="dropzone-text">Drag & drop images or click to browse</span>
                    <span className="dropzone-hint">
                        {images.length > 0
                            ? `${images.length} of ${maxImages} uploaded`
                            : `Up to ${maxImages} images`
                        }
                    </span>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        multiple
                        hidden
                        onChange={handleFileChange}
                    />
                </div>
            )}

            {images.length > 0 && (
                <div className="image-upload-previews">
                    {images.map((img, i) => (
                        <div key={i} className="image-upload-thumb">
                            <img
                                src={img.preview || img}
                                alt={img.name || `Image ${i + 1}`}
                            />
                            <button
                                type="button"
                                className="image-upload-remove"
                                onClick={() => removeImage(i)}
                            >
                                <X size={14} />
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default ImageUpload;
