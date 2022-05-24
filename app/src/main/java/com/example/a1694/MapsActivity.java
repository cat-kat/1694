package com.example.a1694;

import static com.example.a1694.R.color.black;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.a1694.databinding.ActivityMapsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    /*
    обозначения для метро
    мини боттом шит диалог
    зум движениие по карте
    чаты нажимаются  но пустые
    ТОЧКИ В НУЖНОМ!!!! месте
     */
public float tag;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    public static ArrayList<Pair<LatLng, Place>> places;
    ArrayList<MarkerOptions> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
      /*  if (Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        } */
        places = new ArrayList<>();
        markers = new ArrayList<>();
        tag = 0;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("MAPS_DEB", "Go to get places");

        getPlaces();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(59.950002, 30.316672), 10));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng clickerPoint = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(clickerPoint));
                Place place = find(clickerPoint);
                try {
                    showBottomSheetDialog(place);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    public Place find(LatLng latlng) {
        for (int i = 0; i < places.size(); i++) {
            if ((places.get(i).first.latitude == latlng.latitude) && (places.get(i).first.longitude == latlng.longitude)) {
                return (Place) places.get(i).second;
            }
        }
        return null;
    }

    public void getPlaces() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("places");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnap : snapshot.getChildren()) {

                    String name = childSnap.child("name").getValue().toString();
                    String label = childSnap.child("label").getValue().toString();
                    String address = childSnap.child("address").getValue().toString();
                    String photoUrl = childSnap.child("photoUrl").getValue().toString();
                    String metro = childSnap.child("metro").getValue().toString();
                    String time = childSnap.child("time").getValue().toString();
                    String site = childSnap.child("site").getValue().toString();
                    Pair<Double, Double> coord = null;
                    try {
                        coord = findCoord(address);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Double first_coord = coord.first;
                    Double second_coord = coord.second;
                    Place place = new Place(name, label, address, first_coord, second_coord, metro, time, site, photoUrl, (String.valueOf(first_coord) + "!" + String.valueOf(second_coord)).replaceAll("\\.", "-"));
                    Log.d("MAPS_DEB", "Взял место из бд" + place.toString());
                    addPlaceToLocal(place);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void addPlaceToLocal(Place place) {
        Log.d("MAPS_DEB", "Пришел добавлять место");

        LatLng mapPoint = new LatLng(place.first_coord, place.second_coord);

        BitmapDescriptor chooseIcon = null;
        Bitmap res = null;
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.arch_marker);
        switch (place.label) {
            case "traditional food":
                image = BitmapFactory.decodeResource(getResources(), R.drawable.food_mark);
                break;
            case "religious monument":
                image = BitmapFactory.decodeResource(getResources(), R.drawable.religion);
                break;
            case "park":
                image = BitmapFactory.decodeResource(getResources(), R.drawable.park_marker);
                break;
            case "museum":
                image = BitmapFactory.decodeResource(getResources(), R.drawable.museum_marker);
                break;
            case "theater":
                image = BitmapFactory.decodeResource(getResources(), R.drawable.theater_marker);
                break;
            case "architectural monument":
                image = BitmapFactory.decodeResource(getResources(), R.drawable.arch_marker);
                break;


        }

        res = Bitmap.createScaledBitmap(image, 105, 150, false);
        chooseIcon = BitmapDescriptorFactory.fromBitmap(res);
        MarkerOptions marker = new MarkerOptions().position(mapPoint).title(place.name).icon(chooseIcon);
        Log.d("MAPS_DEB", "Сделал маркер!!");

        //markers.add(marker);
        mMap.addMarker(marker);
        places.add(new Pair(mapPoint, place));

    }

    public Pair<Double, Double> findCoord(String address) throws IOException {
        String toFind = "Saint-Petersburg " + String.valueOf(address);
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> res = geocoder.getFromLocationName(toFind, 1);
        Double first_coord = res.get(0).getLatitude();
        Double second_coord = res.get(0).getLongitude();
        return new Pair(first_coord, second_coord);
    }

    public void showBottomSheetDialog(Place place) throws IOException {
        tag = 2;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);
        //bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        //bottomSheetDialog.show();

                FrameLayout bottomSheet = bottomSheetDialog.getWindow()
                        .findViewById(R.id.design_bottom_sheet);
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) bottomSheet.getParent();
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheetBehavior.setPeekHeight(400);
                coordinatorLayout.getParent().requestLayout();
                bottomSheetDialog.show();
        TextView textName = (TextView) bottomSheetDialog.findViewById(R.id.name);
        TextView textLabel = (TextView) bottomSheetDialog.findViewById(R.id.label);
        TextView textAddress = (TextView) bottomSheetDialog.findViewById(R.id.address);
        //TextView textMetro = (TextView) bottomSheetDialog.findViewById(R.id.metro);

        ImageView image = (ImageView) bottomSheetDialog.findViewById(R.id.imagePlace);
        TextView textTime = bottomSheetDialog.findViewById(R.id.time);
        TextView textSite = bottomSheetDialog.findViewById(R.id.site);
bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {

    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
textName.setText("ooo");
        if (coordinatorLayout.getHeight() < 600) {
            textName.setText("yep");
        }
        if (slideOffset == 1 && tag == 2) tag = 1;
        Log.d("BOTTOMBAG", String.valueOf(bottomSheetBehavior.getState()) + " " + String.valueOf(slideOffset) + " "+tag);
      //  if (bottomSheetBehavior.getState() < 1000)
        if (slideOffset < 0.4 && slideOffset > 0 && tag == 1)
            bottomSheetDialog.dismiss();

        }
});

        //TextView id = (TextView) bottomSheetDialog.findViewById(R.id.id_place);
        textName.setText(place.name);
        textLabel.setText(place.label);
        textAddress.setText(place.address + "\n" + decorateMetro(place.metro));
        textTime.setText(place.time);
        textSite.setText(place.site);
        //textMetro.setText(place.metro);
        //Picasso.get().load(place.photoUrl).into(image);
        //Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(image);

        URL req = new URL(
                place.photoUrl);
        Bitmap mIcon_val = BitmapFactory.decodeStream(req.openConnection()
                .getInputStream());
        image.setImageBitmap(mIcon_val);
    }

    public String decorateMetro(String station) {
        return station;
    }


}