import { useApiIsLoaded, useApiLoadingStatus } from "@vis.gl/react-google-maps";

export default function useGoogleMaps() {
  const isLoaded = useApiIsLoaded();
  const status = useApiLoadingStatus();

  return {
    isLoaded,
    loadError: status === "FAILED" ? new Error("Failed to load Google Maps") : null,
  };
}
