package pharmascout.devhacks.pharmascout;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pharmascout.devhacks.pharmascout.API.RestDBApi;
import pharmascout.devhacks.pharmascout.model.FarmacieModel;
import pharmascout.devhacks.pharmascout.singletons.DataHandler;



public class SearchActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    public static final int MAX_DISPLAY_NUMBER = 5;

    static double myLatitude;
    static double myLongitude;

    boolean gpsFound = false;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();

            TextView test = (TextView) findViewById(R.id.textView);
            test.setText(" " + myLongitude + " " + myLatitude );

            TextView searchButtonTextView = (TextView) findViewById(R.id.searchButton);

//            searchButtonTextView.setClickable(true);
            if( gpsFound != true )
                searchButtonTextView.setText("Search");

            gpsFound = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private final LocationListener mmLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();

            TextView test = (TextView) findViewById(R.id.textView);
            test.setText(" " + myLongitude + " " + myLatitude );

            TextView searchButtonTextView = (TextView) findViewById(R.id.searchButton);

            if( gpsFound != true )
                searchButtonTextView.setText("Search2");

            gpsFound = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public ArrayList<String> farmaciiArray;
    public ArrayList<LatLng> coordsArray;
    public ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        progressDialog = new ProgressDialog(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {


            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

            LocationManager mmLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mmLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mmLocationListener);

            TextView test = (TextView) findViewById(R.id.textView);
            test.setText(" " + myLongitude + " " + myLatitude);
        } else {
            // Show rationale and request permission. Joking, sudden nothing here
        }

        //comment pus de Nelu

        ListView listView = (ListView) findViewById(R.id.listView );

        farmaciiArray = new ArrayList<String>();
        coordsArray = new ArrayList<LatLng>();

        adapter = new ArrayAdapter<String>(this,
                                    android.R.layout.simple_list_item_1,
                                    farmaciiArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(18);

                // Generate ListView Item using TextView
                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle coords = new Bundle();
                Random rand = new Random();
                coords.putDouble("latitude", ((LatLng)coordsArray.get(position)).latitude);
                coords.putDouble("longitude", ((LatLng)coordsArray.get(position)).longitude);

                Intent intent = new Intent(SearchActivity.this, MapsActivity.class );
                intent.putExtras(coords);
                startActivity(intent);
            }
        });
    }


//    int number = 0;
//    public void checkNet(View obj){
//        TextView test = (TextView) findViewById(R.id.textView);
//        test.setText("as expected " + number++ );
//
//        try {
//            test.setText(getDistanceFromAsString());
//        }
//        catch (InterruptedException ie){
//
//        }
//        catch (ExecutionException ee){
//
//        }
//    }

    public void searchButton(View view) {
        if(!gpsFound)
            return;

        EditText searchText = (EditText) findViewById(R.id.searchText);

        String text = searchText.getText().toString();
        Toast msg = Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG);
        msg.show();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            search(text);
        } else {
            Toast.makeText(this, "Nu ai net boss!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        if (progressDialog != null) {
            if (loading) {
                progressDialog.setMessage("Se executa cautarea");
                progressDialog.setCancelable(false);
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        }
    }

    private void search(String query) {
        adapter.clear();
        coordsArray.clear();
        setLoading(true);
        (new RestDBApi()).getFarmacii(query, new RestDBApi.CallBack(){
            @Override
            public void onSuccess(List<FarmacieModel> listaFarmacii) {
                setLoading(false);
                FarmacieModel primaFarmacie;
                Toast.makeText(SearchActivity.this, "Boss! Chiar am luat ceva", Toast.LENGTH_SHORT).show();
                listaFarmacii = DataHandler.getInstance().filterOutFarmaciiInchise(listaFarmacii);
                if (listaFarmacii != null && !listaFarmacii.isEmpty()) {

                    if (listaFarmacii.size() > MAX_DISPLAY_NUMBER) {
                        listaFarmacii = handleTooManyResults(listaFarmacii);

                    }
                    int index = 0;

                    for( FarmacieModel farmacie : listaFarmacii ){
                        LatLng coord = new LatLng(farmacie.getLatitudine(), farmacie.getLongitudine());

                        coordsArray.add(index++, coord);
                        adapter.add("           \u2022" + farmacie.getNume() + "\nAdresa: " + farmacie.getAdresaFarmacie() + "\n "
                                                + "Orar: " + farmacie.getProgram() );
                    }

                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(SearchActivity.this, "Mare eroare ce ai luat", Toast.LENGTH_SHORT).show();
                Log.e(SearchActivity.class.getName(), message);
            }
        });
    }

    private List<FarmacieModel> handleTooManyResults(List<FarmacieModel> listaFarmacii) {
        HashMap<FarmacieModel, Integer> farmacieToDistanceMap = new HashMap<>();

        for (FarmacieModel farmacie : listaFarmacii) {
            farmacieToDistanceMap.put(farmacie, DataHandler.getInstance().getDistanceTo(farmacie.getLongitudine(),
                    farmacie.getLatitudine(), myLongitude, myLatitude));
        }

        List<Integer> smallestDistances = new ArrayList<>();
        for (int i = 0; i < MAX_DISPLAY_NUMBER; i++) {
            int min = 0;
            for (Integer currentDistance : farmacieToDistanceMap.values()) {
                if (min == 0 && !smallestDistances.contains(currentDistance)) {
                    min = currentDistance;
                }

                if (currentDistance < min && !smallestDistances.contains(currentDistance)) {
                    min = currentDistance;
                }
            }

            smallestDistances.add(min);
        }

        Log.e("Distante", smallestDistances.toString());
        Log.e("Distante2", farmacieToDistanceMap.values().toString());

        List<FarmacieModel> farmaciiApropiate = new ArrayList<>();

        for(Integer smallDistance : smallestDistances) {
            if (farmaciiApropiate.size() >= MAX_DISPLAY_NUMBER) {
                break;
            }

            for (FarmacieModel farmacieApropiata : farmacieToDistanceMap.keySet()) {
                if (farmacieToDistanceMap.get(farmacieApropiata).equals(smallDistance)) {
                    farmaciiApropiate.add(farmacieApropiata);
                }
            }
        }

        return farmaciiApropiate;
    }
}

