package tortel.fr.mapscannerclient;


import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

import tortel.fr.mapscannerclient.adapter.CustomMapView;
import tortel.fr.mapscannerclient.bean.Place;


public class PlaceFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_PLACE = "place";

    private Place place;
    private CustomMapView mapView;
    private GoogleMap map;
    private Toolbar toolbar;

    private OnPlaceFragmentInteractionListener listener;

    private TextView hoursView;
    private TextView popularHoursView;

    public PlaceFragment() {
        // Required empty public constructor
    }


    public static PlaceFragment newInstance(Place place) {
        PlaceFragment fragment = new PlaceFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLACE, place);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.place = (Place) getArguments().getSerializable(ARG_PLACE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_place, container, false);
        // Gets the MapView from the XML layout and creates it
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(this);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView nameTv = view.findViewById(R.id.name);
        TextView categoryTv = view.findViewById(R.id.category);
        TextView addressTv = view.findViewById(R.id.address);
        TextView distanceTv = view.findViewById(R.id.distance);
        ImageView imgView = view.findViewById(R.id.placeImg);
        hoursView = view.findViewById(R.id.hoursView);
        popularHoursView = view.findViewById(R.id.popularHoursView);

        nameTv.setText(place.getName());
        categoryTv.setText(place.getCategory());
        addressTv.setText("Address: " + place.getFullAddress());
        distanceTv.setText(place.getDistance() + "m away from your current location");
        imgView.setImageBitmap(place.getImage());

        if (place.getWeekHours() != null) {
            setHoursSection();
        }

        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnPlaceFragmentBackBtnInteraction();
            }
        });
    }

    private void setHoursSection() {
        if (place == null) {
            return;
        }

        StringBuilder hoursBuilder = new StringBuilder();
        StringBuilder popularHoursBuilder = new StringBuilder();

        for (Map.Entry<Integer, String> entry : place.getWeekHours().getRegularHours().entrySet()) {
            hoursBuilder.append(getDayFromInt(entry.getKey()));
            hoursBuilder.append(" ");
            hoursBuilder.append(entry.getValue());
            hoursBuilder.append("\n");
        }

        for (Map.Entry<Integer, String> entry : place.getWeekHours().getPopularHours().entrySet()) {
            popularHoursBuilder.append(getDayFromInt(entry.getKey()));
            popularHoursBuilder.append(" ");
            popularHoursBuilder.append(entry.getValue());
            popularHoursBuilder.append("\n");
        }

        hoursView.setText(hoursBuilder.toString());
        popularHoursView.setText(popularHoursBuilder.toString());
    }

    private String getDayFromInt(int day) {
        switch(day) {
            case 1:
                return "Mon.";

            case 2:
                return "Tue.";

            case 3:
                return "Wed.";

            case 4:
                return "Thu.";

            case 5:
                return "Fri.";

            case 6:
                return "Sat.";

            case 7:
                return "Sun.";
        }

        return "error";
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);

        // Check the permissions
        if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                map.setMyLocationEnabled(true);
            }
        }

        // Get the coordinates
        LatLng latLng = new LatLng(place.getLat(), place.getLng());
        // Add marker
        map.addMarker(new MarkerOptions().position(latLng).title(place.getName()));

        // Move the camera to the given coordinates and set up the camera zoom
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.setMinZoomPreference(15);
        map.setMaxZoomPreference(20);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlaceFragmentInteractionListener) {
            listener = (OnPlaceFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void reloadPlaceHours() {
        setHoursSection();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    public interface OnPlaceFragmentInteractionListener {
        void OnPlaceFragmentBackBtnInteraction();
    }
}
