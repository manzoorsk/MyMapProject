package com.itshareplus.googlemapdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    Marker marker, marker1, marker2;
    TextView locality_tv, lat_tv, lng_tv, snippet_tv;
    Circle circle;
    Polyline polyLine;
    ArrayList<Marker> markers = new ArrayList<Marker>();
    static final int POLYGON_POINTS = 5;
    Polygon shape;
    private Button next_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        next_activity = (Button) findViewById(R.id.next_page);
        next_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
        if (googleServicesAvailable()) {
            Toast.makeText(this, "It is Connected ", Toast.LENGTH_LONG).show();
            initMap();
        } else {

        }
    }

    public boolean googleServicesAvailable() {

        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    public void geoLocation(View view) throws IOException {
        TextView text = (TextView) findViewById(R.id.text_view);
        EditText editText = (EditText) findViewById(R.id.findLocation);
        String location = editText.getText().toString();

        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter Location address!", Toast.LENGTH_LONG).show();
        } else {
            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocationName(location, 1);
            Address address = list.get(0);
            String streetLine = address.getAddressLine(0);
            String subLocality = address.getSubLocality();
            String locality = address.getLocality();
            String city = address.getCountryName();
            String state = address.getAdminArea();
            String zipCode = address.getPostalCode();


            Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();
            goToLocationZoom(lat, lng, 15);

            setMarker(streetLine, subLocality, locality, city, state, zipCode, lat, lng);
            text.setText(address.getLocality() + "," +
                    address.getAdminArea() + "," +
                    address.getCountryName() + "," +
                    address.getPostalCode() + "\n" + lat + " ," + lng);
        }
        view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


    }


    private void setMarker(String streetLine, String subLocality, String locality, String city, String state, String zipCode, double lat, double lng) {
        if (marker != null) {
            removeEveryThing();
        }

        MarkerOptions options = new MarkerOptions()
                .title(streetLine + "," + subLocality + "," + locality)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .position(new LatLng(lat, lng))
                .snippet("I am Here");

        marker = mGoogleMap.addMarker(options);
    }


    private void removeEveryThing() {
        marker.remove();
        marker = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            Toast.makeText(this, "Can Not Get Location", Toast.LENGTH_LONG).show();
        } else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mGoogleMap.animateCamera(update);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        LatLng nyleTech = new LatLng(17.416353, 78.448047);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nyleTech, 15));
        markers.add(mGoogleMap.addMarker(new MarkerOptions()
                .title("Nyletech Solutions")
                .position(nyleTech)));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        if(mGoogleMap!=null){

           mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
               @Override
               public void onMapClick(LatLng latLng) {
                   TextView text = (TextView) findViewById(R.id.text_view);
                   Geocoder geocoder = new Geocoder(MainActivity.this);
                   List<Address> list;
                   try {
                       list = geocoder.getFromLocation(latLng.latitude,
                               latLng.longitude, 1);
                   } catch (IOException e) {
                       return;
                   }
                   Address address = list.get(0);
                   if (marker != null) {
                       marker.remove();
                   }

                   MarkerOptions options = new MarkerOptions()
                           .title(address.getLocality())
                           .position(new LatLng(latLng.latitude,
                                   latLng.longitude));

                   marker = mGoogleMap.addMarker(options);

         MainActivity.this.setMarker(address.getAddressLine(0),address.getSubLocality(),address.getLocality(),address.getCountryName(),
                           address.getAdminArea(),address.getPostalCode(),latLng.latitude,latLng.longitude);
                   text.setText(address.getAddressLine(0)+","+
                           address.getSubLocality()+","+
                           address.getLocality()+","+
                           address.getAdminArea()+","+
                           address.getCountryName()+","+
                           address.getPostalCode()+","+
                            "\n " + latLng);
                   text.setTextSize(15);
               }
           });

            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    TextView text = (TextView) findViewById(R.id.text_view);
                    Geocoder geocoder=new Geocoder(MainActivity.this);
                    LatLng ll=marker.getPosition();
                    double lat=ll.latitude;
                    double lng=ll.longitude;
                    List<Address> list=null;
                    try {
                        list  =geocoder.getFromLocation(lat,lng,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address=list.get(0);
                    text.setText(address.getAddressLine(0)+","+
                            address.getSubLocality()+","+
                            address.getLocality()+","+
                            address.getAdminArea()+","+
                            address.getCountryName()+","+
                            address.getPostalCode()+","+
                            "\n " + marker);
                    marker.setTitle(address.getLocality());
                    marker.showInfoWindow();
                }
            });
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View view=getLayoutInflater().inflate(R.layout.info_window,null);
                    locality_tv=(TextView)view.findViewById(R.id.tv_locality);
                    lat_tv=(TextView)view.findViewById(R.id.tv_lat);
                    lng_tv=(TextView)view.findViewById(R.id.tv_lng);
                    snippet_tv=(TextView)view.findViewById(R.id.tv_snippet);

                    LatLng ll=marker.getPosition();
                    locality_tv.setText(marker.getTitle());
                    lat_tv.setText("Latitude" +ll.latitude);
                    lng_tv.setText("Longitude" +ll.longitude);
//                    snippet_tv.setText(marker.getSnippet());
                    return view;
                }
            });
        }
//        goToLocationZoom(17.388056, 78.466398, 15);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this).build();
//        mGoogleApiClient.connect();

    }


}
