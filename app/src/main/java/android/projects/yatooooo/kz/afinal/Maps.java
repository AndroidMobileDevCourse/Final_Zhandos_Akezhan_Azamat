package android.projects.yatooooo.kz.afinal;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.projects.yatooooo.kz.afinal.backendless.BackendlessApplication;
import android.projects.yatooooo.kz.afinal.model.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.servercode.BackendlessService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    // map
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // view
    private Button btnLogout;
    Location currentLocation = null;
    public List<Image> imageSet = new ArrayList<>();
    Polyline drawnPolyline = null;
    ImageView imageView;
    ProgressDialog pd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (isGooglePlayServicesOK()) {
            getLocationPermission();
            initView();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            System.out.println("getDeviceLocation");
            getDeviceLocation();
            getBackendlessImageLocations();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }

    public void getBackendlessImageLocations() {
//        String whereClause = "userId = '" + Backendless.UserService.CurrentUser().getObjectId();
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
//        dataQueryBuilder.setWhereClause(whereClause);

        Backendless.Persistence.of(Image.class).find(dataQueryBuilder, new AsyncCallback<List<Image>>() {
            @Override
            public void handleResponse(List<Image> response) {
                System.out.println(response);
                if (response.size() > 0) {
                    imageSet = response;

                    Spinner dropdown = findViewById(R.id.spinner1);
                    ArrayAdapter<Image> adapter = new ArrayAdapter<>(
                            Maps.this, android.R.layout.simple_spinner_dropdown_item, response);
                    dropdown.setAdapter(adapter);

                    dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                            // polyline
                            if (drawnPolyline != null) {
                                drawnPolyline.remove();
                            }

                            Image selectedImage = imageSet.get(position);

                            ArrayList<LatLng> points = new ArrayList<>();
                            PolylineOptions lineOptions = new PolylineOptions();

                            points.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                            points.add(new LatLng(
                                    Double.parseDouble(selectedImage.getLatitude()),
                                    Double.parseDouble(selectedImage.getLongitude())
                            ));
                            lineOptions.addAll(points);
                            lineOptions.width(10);
                            lineOptions.color(Color.RED);

                            drawnPolyline = mMap.addPolyline(lineOptions);

                            // image view
                            new DownloadImageTask(imageView).execute(selectedImage);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            Toast.makeText(Maps.this, "No image sleected", Toast.LENGTH_SHORT);
                        }

                    });
                } else {
                    Toast.makeText(Maps.this, "Image set is empty", Toast.LENGTH_SHORT);
                }
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(Maps.this, "Cannot get image set", Toast.LENGTH_SHORT);
            }
        });
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM);
                            }
                        } else {
                            Toast.makeText(Maps.this,
                                    "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(Maps.this);
    }

    private void initView() {
        imageView = findViewById(R.id.capturedImageView);

        pd = new ProgressDialog(this);
        pd.setMessage("Downloading Image");

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Maps.this, "Loading...", Toast.LENGTH_SHORT).show();
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Toast.makeText(Maps.this, "You logged out!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Maps.this, SingIn.class));
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(Maps.this, "Failed to log out!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    private static final int ERROR_DIALOG_REQUEST = 9001;

    public boolean isGooglePlayServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Maps.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                    Maps.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    class DownloadImageTask extends AsyncTask<Image, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(Image... images) {
            String imageURL = "https://backendlessappcontent.com/" +
                    BackendlessApplication.applicationId +
                    "/console/cbgkbzxamjhqeqfmtzicysnvbxlsxepsncne/files/view/" +
                    images[0].getPath() + "/" +
                    images[0].getName();
            System.out.println(imageURL);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            pd.dismiss();
            bmImage.setImageBitmap(result);
        }
    }
}
