import { useState, useCallback } from "react";
import { Map, AdvancedMarker } from "@vis.gl/react-google-maps";
import { MapPin, Loader, X } from "lucide-react";
import useGoogleMaps from "../../hooks/useGoogleMaps";
import "./LocationPicker.css";

const CEBU_CENTER = { lat: 10.3157, lng: 123.8854 };
const MAP_ID = import.meta.env.VITE_GOOGLE_MAPS_MAP_ID || undefined;

function LocationPicker({ latitude, longitude, onChange }) {
  const [showMap, setShowMap] = useState(false);

  const { isLoaded } = useGoogleMaps();

  const hasPin = latitude != null && longitude != null;

  const center = hasPin
    ? { lat: latitude, lng: longitude }
    : CEBU_CENTER;

  const handleMapClick = useCallback(
    (e) => {
      const latLng = e.detail?.latLng;
      if (!latLng) return;
      onChange({ latitude: latLng.lat, longitude: latLng.lng });
    },
    [onChange]
  );

  const clearPin = () => {
    onChange({ latitude: null, longitude: null });
  };

  if (!showMap) {
    return (
      <div className="location-picker-toggle">
        <button
          type="button"
          className="pin-location-btn"
          onClick={() => setShowMap(true)}
        >
          <MapPin size={16} />
          {hasPin ? "Change Map Pin" : "Add Map Pin"}
        </button>
        {hasPin && (
          <span className="pin-coords">
            {latitude.toFixed(4)}, {longitude.toFixed(4)}
            <button type="button" className="clear-pin-btn" onClick={clearPin}>
              <X size={12} />
            </button>
          </span>
        )}
      </div>
    );
  }

  return (
    <div className="location-picker-container">
      <div className="location-picker-header">
        <span className="picker-label">
          <MapPin size={14} /> Click the map to place a pin
        </span>
        <div className="picker-actions">
          {hasPin && (
            <button type="button" className="clear-pin-btn-text" onClick={clearPin}>
              Clear
            </button>
          )}
          <button
            type="button"
            className="close-picker-btn"
            onClick={() => setShowMap(false)}
          >
            Done
          </button>
        </div>
      </div>

      <div className="location-picker-map">
        {!isLoaded ? (
          <div className="picker-loading">
            <Loader size={20} className="spin" />
          </div>
        ) : (
          <Map
            mapId={MAP_ID}
            className="picker-map-container"
            defaultCenter={center}
            defaultZoom={hasPin ? 17 : 14}
            disableDefaultUI={true}
            zoomControl={true}
            mapTypeControl={false}
            streetViewControl={false}
            fullscreenControl={false}
            clickableIcons={false}
            onClick={handleMapClick}
          >
            {hasPin && (
              <AdvancedMarker position={{ lat: latitude, lng: longitude }} />
            )}
          </Map>
        )}
      </div>

      {hasPin && (
        <p className="pin-confirmation">
          Pin placed at {latitude.toFixed(5)}, {longitude.toFixed(5)}
        </p>
      )}
    </div>
  );
}

export default LocationPicker;
