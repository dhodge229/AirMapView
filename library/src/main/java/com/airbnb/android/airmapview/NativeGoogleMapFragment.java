package com.airbnb.android.airmapview;

import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.android.airmapview.listeners.InfoWindowCreator;
import com.airbnb.android.airmapview.listeners.OnCameraChangeListener;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnLatLngScreenLocationCallback;
import com.airbnb.android.airmapview.listeners.OnMapBoundsCallback;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapLoadedListener;
import com.airbnb.android.airmapview.listeners.OnMapLongClickListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class NativeGoogleMapFragment extends SupportMapFragment implements AirMapInterface {

    private GoogleMap googleMap;
    private OnMapLoadedListener onMapLoadedListener;

    public static NativeGoogleMapFragment newInstance(AirGoogleMapOptions options) {
        return new NativeGoogleMapFragment().setArguments(options);
    }

    public NativeGoogleMapFragment setArguments(AirGoogleMapOptions options) {
        setArguments(options.toBundle());
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        init();

        return v;
    }

    public void init() {
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (googleMap != null && getActivity() != null) {
                    NativeGoogleMapFragment.this.googleMap = googleMap;
                    UiSettings settings = NativeGoogleMapFragment.this.googleMap.getUiSettings();
                    settings.setZoomControlsEnabled(false);
                    settings.setMyLocationButtonEnabled(false);
                    setMyLocationEnabled(true);

                    if (onMapLoadedListener != null) {
                        onMapLoadedListener.onMapLoaded();
                    }
                }
            }
        });
    }

    @Override
    public boolean isInitialized() {
        return googleMap != null && getActivity() != null;
    }

    @Override
    public void clearMarkers() {
        googleMap.clear();
    }

    @Override
    public void addMarker(AirMapMarker airMarker) {
        Marker marker = googleMap.addMarker(airMarker.getMarkerOptions());
        airMarker.setGoogleMarker(marker);
    }

    @Override
    public void removeMarker(AirMapMarker marker) {
        Marker nativeMarker = marker.getMarker();
        if (nativeMarker != null) {
            nativeMarker.remove();
        }
    }

    @Override
    public void setOnInfoWindowClickListener(final OnInfoWindowClickListener listener) {
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                listener.onInfoWindowClick(marker);
            }
        });
    }

    @Override
    public void setInfoWindowCreator(GoogleMap.InfoWindowAdapter adapter,
                                     InfoWindowCreator creator) {
        googleMap.setInfoWindowAdapter(adapter);
    }

    @Override
    public void drawCircle(LatLng latLng, int radius) {
        drawCircle(latLng, radius, CIRCLE_BORDER_COLOR);
    }

    @Override
    public void drawCircle(LatLng latLng, int radius, int borderColor) {
        drawCircle(latLng, radius, borderColor, CIRCLE_BORDER_WIDTH);
    }

    @Override
    public void drawCircle(LatLng latLng, int radius, int borderColor, int borderWidth) {
        drawCircle(latLng, radius, borderColor, borderWidth, CIRCLE_FILL_COLOR);
    }

    @Override
    public void drawCircle(LatLng latLng, int radius, int borderColor, int borderWidth,
                           int fillColor) {
        googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .strokeColor(borderColor)
                .strokeWidth(borderWidth)
                .fillColor(fillColor)
                .radius(radius));
    }

    @Override
    public void getMapScreenBounds(OnMapBoundsCallback callback) {
        final Projection projection = googleMap.getProjection();
        int hOffset = getResources().getDimensionPixelOffset(R.dimen.map_horizontal_padding);
        int vOffset = getResources().getDimensionPixelOffset(R.dimen.map_vertical_padding);

        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(projection.fromScreenLocation(new Point(hOffset, vOffset))); // top-left
        builder.include(projection.fromScreenLocation(
                new Point(getView().getWidth() - hOffset, vOffset))); // top-right
        builder.include(projection.fromScreenLocation(
                new Point(hOffset, getView().getHeight() - vOffset))); // bottom-left
        builder.include(projection.fromScreenLocation(new Point(getView().getWidth() - hOffset,
                getView().getHeight() - vOffset))); // bottom-right

        callback.onMapBoundsReady(builder.build());
    }

    @Override
    public void getScreenLocation(LatLng latLng, OnLatLngScreenLocationCallback callback) {
        callback.onLatLngScreenLocationReady(googleMap.getProjection().toScreenLocation(latLng));
    }

    @Override
    public void setCenter(LatLngBounds latLngBounds, int boundsPadding) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, boundsPadding));
    }

    @Override
    public void setZoom(int zoom) {
        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, zoom));
    }

    @Override
    public void animateCenter(LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void setCenter(LatLng latLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public LatLng getCenter() {
        return googleMap.getCameraPosition().target;
    }

    @Override
    public int getZoom() {
        return (int) googleMap.getCameraPosition().zoom;
    }

    @Override
    public void setOnCameraChangeListener(final OnCameraChangeListener onCameraChangeListener) {
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                // camera change can occur programmatically.
                if (isResumed()) {
                    onCameraChangeListener.onCameraChanged(cameraPosition.target, (int) cameraPosition.zoom);
                }
            }
        });
    }

    @Override
    public void setOnMapLoadedListener(OnMapLoadedListener onMapLoadedListener) {
        this.onMapLoadedListener = onMapLoadedListener;
    }

    @Override
    public void setCenterZoom(LatLng latLng, int zoom) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void animateCenterZoom(LatLng latLng, int zoom) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void setOnMarkerClickListener(final OnMapMarkerClickListener listener) {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                listener.onMapMarkerClick(marker);
                return false;
            }
        });
    }

    @Override
    public void setOnMapClickListener(final OnMapClickListener listener) {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                listener.onMapClick(latLng);
            }
        });
    }

    @Override
    public void setOnMapLongClickListener(final OnMapLongClickListener listener) {
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                listener.onMapLongClick(latLng);
            }
        });
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        googleMap.setPadding(left, top, right, bottom);
    }

    @Override
    public void setMyLocationEnabled(boolean enabled) {
        googleMap.setMyLocationEnabled(enabled);
    }

    @Override
    public boolean isMyLocationEnabled() {
        return googleMap.isMyLocationEnabled();
    }

    @Override
    public void setMapToolbarEnabled(boolean enabled) {
        googleMap.getUiSettings().setMapToolbarEnabled(enabled);
    }

    @Override
    public void addPolyline(AirMapPolyline polyline) {
        polyline.addToGoogleMap(googleMap);
    }

    @Override
    public void removePolyline(AirMapPolyline polyline) {
        polyline.removeFromGoogleMap();
    }

    @Override
    public void setMapType(MapType type) {
        int nativeType;
        if (type == MapType.MAP_TYPE_NORMAL) {
            nativeType = GoogleMap.MAP_TYPE_NORMAL;
        } else if (type == MapType.MAP_TYPE_SATELLITE) {
            nativeType = GoogleMap.MAP_TYPE_SATELLITE;
        } else if (type == MapType.MAP_TYPE_TERRAIN) {
            nativeType = GoogleMap.MAP_TYPE_TERRAIN;
        } else {
            nativeType = GoogleMap.MAP_TYPE_NORMAL;
        }
        googleMap.setMapType(nativeType);
    }

    /**
     * This method will return the google map if initialized. Will return null otherwise
     *
     * @return returns google map if initialized
     */
    public GoogleMap getGoogleMap() {
        return googleMap;
    }
}
