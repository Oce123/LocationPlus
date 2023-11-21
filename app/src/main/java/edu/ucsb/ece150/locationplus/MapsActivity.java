package edu.ucsb.ece150.locationplus;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mPendingIntent = null;

    private GnssStatus.Callback mGnssStatusCallback;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Toolbar mToolbar;
    private boolean autoCenteringEnabled = false;
    private Marker destinationMarker;
    private Circle geofenceCircle;
    private LatLng lastKnownLatLng;
    private boolean satelliteInfoVisible = false;
    private List<Satellite> satelliteList = new ArrayList<>();
    private GnssStatus lastGnssStatus;
    private SatelliteAdapter satelliteAdapter;




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up Geofencing Client
        mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);

        // Set up Satellite List
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // [TODO] Ensure that necessary permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

            // [TODO] Implement behavior when the satellite status is updated
            mGnssStatusCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    // Implement behavior when the satellite status is updated
                    // For example, update a list or adapter with satellite information.
                    handleSatelliteStatus();
                }
            };
            mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        // [TODO] Additional setup for viewing satellite information (lists, adapters, etc.)

        // Set up Toolbar
        mToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);

        RecyclerView recyclerViewSatellites = findViewById(R.id.recyclerViewSatellites);
        recyclerViewSatellites.setLayoutManager(new LinearLayoutManager(this));
        satelliteAdapter = new SatelliteAdapter();
        recyclerViewSatellites.setAdapter(satelliteAdapter);

        findViewById(R.id.toggleAutoCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAutoCentering();
            }
        });

        // Initialize your satellite button
        findViewById(R.id.satelliteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                satelliteInfoVisible = !satelliteInfoVisible;

                // Toggle the visibility of the RecyclerView
                recyclerViewSatellites.setVisibility(
                        satelliteInfoVisible ? View.VISIBLE : View.GONE
                );

                // Utilisez la dernière valeur de GnssStatus pour mettre à jour les informations satellites
                if (lastGnssStatus != null) {
                    onSatelliteStatusChanged(lastGnssStatus);
                }
            }
        });

    }

    private void handleSatelliteStatus() {
        // Implementez le comportement en fonction du statut satellite
        // Par exemple, mettez à jour une liste ou un adaptateur avec les informations satellites.
        int satelliteCount = satelliteList.size(); // Utilisez la liste des satellites que vous avez collectée

        List<String> satelliteInfoList = new ArrayList<>();

        for (Satellite satellite : satelliteList) {
            String satelliteInfo = satellite.toString();
            satelliteInfoList.add(satelliteInfo);
        }

        // [TODO] Mettez à jour votre interface utilisateur avec les informations satellites
        // Par exemple, en utilisant un RecyclerView ou d'autres éléments d'interface utilisateur
        // Exemple : En supposant que vous ayez un TextView nommé satelliteInfoTextView
        TextView satelliteInfoTextView = findViewById(R.id.satelliteInfoTextView);
        satelliteInfoTextView.setText(TextUtils.join("\n", satelliteInfoList));
        // Mettez à jour l'adaptateur de la RecyclerView
        satelliteAdapter.setSatelliteList(satelliteList);
    }


    private void onSatelliteStatusChanged(GnssStatus status) {
        Log.d("Satellite", "onSatelliteStatusChanged called");
        // Implémentez le comportement lorsque le statut satellite est mis à jour
        // Par exemple, mettez à jour une liste ou un adaptateur avec les informations satellites.

        satelliteList.clear(); // Effacez la liste actuelle, car elle va être mise à jour

        int satelliteCount = status.getSatelliteCount();

        for (int i = 0; i < satelliteCount; i++) {
            int satelliteType = status.getConstellationType(i);
            int satellitePrn = status.getSvid(i);
            float satelliteCn0 = status.getCn0DbHz(i);

            Satellite satellite = new Satellite(satelliteType, satellitePrn, satelliteCn0);
            satelliteList.add(satellite);
        }

        // Mettez à jour la dernière valeur de GnssStatus
        lastGnssStatus = status;

        // Maintenant que la liste des satellites est mise à jour, appelez la méthode handleSatelliteStatus
        handleSatelliteStatus();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // [TODO] Implement behavior when Google Maps is ready

        // Ajouter un marqueur à la position actuelle de l'utilisateur
        if (lastKnownLatLng != null) {
            mMap.addMarker(new MarkerOptions().position(lastKnownLatLng).title("Ma Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLatLng));
        }else{
            // Ajouter un marqueur à Santa Barbara
            LatLng santaBarbara = new LatLng(34.4208, -119.6982);
            mMap.addMarker(new MarkerOptions().position(santaBarbara).title("Santa Barbara"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(santaBarbara));
        }

        // [TODO] In addition, add a listener for long clicks (which is the starting point for
        // creating a Geofence for the destination and listening for transitions that indicate
        // arrival)
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // [TODO] Handle the long click event
                // This is the starting point for creating a Geofence for the destination
                // and listening for transitions that indicate arrival
                handleMapLongClick(latLng);
            }
        });
    }

    private void handleMapLongClick(LatLng latLng) {
        // [TODO] Implement behavior for long click on the map
        // For example, create a Geofence for the destination and start monitoring for transitions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            Log.d("Geofence", "Before creating Geofence");
            createGeofence(latLng);
            Log.d("Geofence", "After creating Geofence");

        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

    }

    private void createGeofence(LatLng latLng) {
        // [TODO] Create a Geofence for the destination
        mGeofence = new Geofence.Builder()
                .setRequestId("Destination Geofence") // Replace with a unique ID
                .setCircularRegion(latLng.latitude, latLng.longitude, 100) // 100 meters radius
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        // [TODO] Add the Geofence to the GeofencingClient
        addGeofence();
    }

    private void addGeofence() {
        // Check for location permission before adding the Geofence
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(getGeofenceRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(this, task -> {
                        // Geofences added successfully
                        // [TODO] Handle success if needed
                        Toast.makeText(MapsActivity.this, "Geofences added successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(this, e -> {
                        // Failed to add Geofences
                        // [TODO] Handle failure if needed
                        Toast.makeText(MapsActivity.this, "Failed to add geofences", Toast.LENGTH_SHORT).show();
                        Log.e("Geofence", "Failed to add geofences", e);
                    });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // [TODO] Implement behavior when a location update is received
        // Mise à jour de la dernière position connue
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // [TODO] Implementer le comportement lorsque la mise à jour de la position est reçue
        // Afficher un marqueur à la nouvelle position
        if (autoCenteringEnabled) {
            mMap.clear(); // Effacez les marqueurs précédents
            mMap.addMarker(new MarkerOptions().position(lastKnownLatLng).title("Ma Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(lastKnownLatLng));
        }

    }

    private void toggleAutoCentering() {
        autoCenteringEnabled = !autoCenteringEnabled;

        if (autoCenteringEnabled && lastKnownLatLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(lastKnownLatLng));
        }
    }

    /*
     * The following three methods onProviderDisabled(), onProviderEnabled(), and onStatusChanged()
     * do not need to be implemented -- they must be here because this Activity implements
     * LocationListener.
     *
     * You may use them if you need to.
     */
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private GeofencingRequest getGeofenceRequest() {
        // [TODO] Set the initial trigger (i.e. what should be triggered if the user is already
        // inside the Geofence when it is created)

        return new GeofencingRequest.Builder()
                //.setInitialTrigger()  <--  Add triggers here
                .addGeofence(mGeofence)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mPendingIntent != null) {
            return mPendingIntent;
        }

        Intent intent = new Intent(MapsActivity.this, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(
                MapsActivity.this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Add this line
        );

        return mPendingIntent;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() throws SecurityException {
        super.onStart();

        // [TODO] Ensure that necessary permissions are granted (look in AndroidManifest.xml to
        // see what permissions are needed for this app)

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] Data recovery
    }

    @Override
    protected void onPause() {
        super.onPause();

        // [TODO] Data saving
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
    }
}
