package edu.byu.cs240.familymapclient.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import edu.byu.cs240.familymapclient.R;
import edu.byu.cs240.familymapclient.model.DataCache;
import models.Event;
import models.Person;

import static edu.byu.cs240.familymapclient.helpers.Stringify.stringifyEventDetails;
import static edu.byu.cs240.familymapclient.helpers.Stringify.stringifyFullLocation;

public class MapFragment extends Fragment {

    // FIXME - TAKES AN EVENT ID AS A PARAMETER, IF NONE IS PASSED (MAIN ACTIVITY) THEN DON'T FOCUS ON ANYTHING
        // FIXME - FOR EVENT ACTIVITY, IT WILL HAVE AN EVENT ID, YOU'LL FOCUS ON THAT

    private GoogleMap gMap;
    private TextView mapDetailBar;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            gMap = googleMap;
            gMap.getUiSettings().setMapToolbarEnabled(false);

            addEventMarkers();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapDetailBar = view.findViewById(R.id.tvMapDetailBar);
        mapDetailBar.setOnClickListener(v -> onDetailBarClick());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
        // Add search icon
        menu.findItem(R.id.searchMenuItem).setIcon(
                new IconDrawable(getActivity(), FontAwesomeIcons.fa_search).colorRes(R.color.white).actionBarSize());
        // Add settings icon
        menu.findItem(R.id.settingsMenuItem).setIcon(
                new IconDrawable(getActivity(), FontAwesomeIcons.fa_gear).colorRes(R.color.white).actionBarSize());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchMenuItem:
                goToSearch();
                return true;

            case R.id.settingsMenuItem:
                goToSettings();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSearch() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

    private void goToSettings() {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Start person activity with a person ID when marker is clicked.
     */
    private void onDetailBarClick() {
        Event selectedEvent = (Event) mapDetailBar.getTag();
        if (selectedEvent == null) return;

        String personID = selectedEvent.getPersonID();

        Intent intent = new Intent(getActivity(), PersonActivity.class);
        intent.putExtra("PERSON_ID", personID);
        startActivity(intent);
    }

    private void addEventMarkers() {
        for (Event event : DataCache.getEvents().values()) {
            LatLng userBirthLoc = new LatLng(event.getLatitude(), event.getLongitude());

            addEventMarker(event, userBirthLoc);

            gMap.setOnMarkerClickListener(marker -> {
                displayMarkerDetails(marker);
                addLinesFromMarker(marker);
                return false;
            });

            // Center map on logged-in user
            if (event.getPersonID().equals(DataCache.getUser().getPersonID())) {
                gMap.moveCamera(CameraUpdateFactory.newLatLng(userBirthLoc));
            }
        }
    }

    private void addEventMarker(Event event, LatLng userBirthLoc) {
        float markerColor = DataCache.getEventColors().get(event.getEventType().toLowerCase());

        Marker newMarker = gMap.addMarker(new MarkerOptions()
                .position(userBirthLoc)
                .title(stringifyFullLocation(event))
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        newMarker.setTag(event);
    }

    /**
     * Adds event details to map detail bar
     */
    private void displayMarkerDetails(Marker marker) {
        Event event = (Event) marker.getTag();
        assert event != null;
        Person person = DataCache.getPersons().get(event.getPersonID());
        assert person != null;

        // Set icon to male by default
        FontAwesomeIcons iconType = FontAwesomeIcons.fa_male;
        int iconColor = R.color.male_blue;
        // Switch to female icon accordingly
        if (person.getGender().equals("f")) {
            iconType = FontAwesomeIcons.fa_female;
            iconColor = R.color.female_pink;
        }

        // Update icon
        Drawable genderIcon = new IconDrawable(getActivity(), iconType).
        colorRes(iconColor).sizeDp(40);
        mapDetailBar.setCompoundDrawables(genderIcon, null, null, null);

        // Update detail bar text
        mapDetailBar.setText(stringifyEventDetails(event, person));

        // Update detail bar tag
        mapDetailBar.setTag(event);
    }

    /**
     * Adds lines eminating from the marker
     */
    private void addLinesFromMarker(Marker marker) {

    }
}