package uk.ac.tees.aad.W9484387;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.request.SourcesRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.kwabenaberko.newsapilib.models.response.SourcesResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private String defaultCountry="India";
    NewsApiClient newsApiClient = new NewsApiClient("3d1b385873b040fcb032f90d676ee94a");
    TextView country;
    ListView listView;
    TextView logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        country = findViewById(R.id.countryCode);
        country.setText(defaultCountry);

        listView = findViewById(R.id.listview);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(),signin.class));
            }
        });


        ActivityCompat.requestPermissions( this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            getCountryLoc();

        }


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences("intro", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("played","no").equals("no"))
        {
            startActivity(new Intent(getApplicationContext(),IntroScreen.class));
        }else{
            if(currentUser == null){
                startActivity(new Intent(getApplicationContext(),signin.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }



    private void getCountryLoc()
    {
        //Checking for permissions
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED

                &&

                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED

        )
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                String country = getGeoCodeCountry(getApplicationContext(),location.getLatitude(),location.getLongitude());

                                getNews(country);
                            }else{
                                Toast.makeText(getApplicationContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void getNews(String country) {

        setCountry(country);


        newsApiClient.getSources(
                new SourcesRequest.Builder()
                        .language("en")
                        .country(getCountryCode(country))
                        .build(),
                new NewsApiClient.SourcesCallback() {
                    @Override
                    public void onSuccess(SourcesResponse response) {
                        getNewsWithSourceProvider(response.getSources().get(0).getId());
                    }
                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }
                }
        );

    }

    private void getNewsWithSourceProvider(String name) {

        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .sources(name)
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        int count = response.getTotalResults();

                        if(count>20){
                            count=20;
                        }
                        String[] title = new String[count] ;
                        String[] images= new String[count] ;
                        String[] description = new String[count] ;
                        try{

                            for (int x =0; x<count;x++)
                            {
                                title[x]=response.getArticles().get(x).getTitle();
                                images[x]=response.getArticles().get(x).getUrlToImage();
                                description[x]=response.getArticles().get(x).getDescription();
                            }
                        }catch (Exception e){}


                        MyAdapter adapter = new MyAdapter(getApplicationContext(),title,description,images);
                        listView.setAdapter(adapter);

                        Toast.makeText(getApplicationContext(),  response.getArticles().get(0).getUrlToImage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                        Toast.makeText(getApplicationContext(),throwable.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
        );
    }


    private void setCountry(String country) {
        this.country.setText("News broadcast From-> "+country);
    }
    private String getCountryCode(String country) {
        if(country.equals("United States"))
            return "us";
        if (country.equals("Afghanistan"))
            return "ae";
        if (country.equals("India"))
            return "in";
        return "gb";

    }


    public static String getGeoCodeCountry(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            return geocoder.getFromLocation(latitude, longitude, 4).get(0).getCountryName();
        } catch (IOException ignored) {
        }
        return null;
    }

}

class MyAdapter extends ArrayAdapter<String> {

    Context context;
    String rTitle[];
    String rDescription[];
    String rimages[];



    MyAdapter (Context c, String title[], String description[], String images[])
    {
        super(c, R.layout.box, R.id.textView1,title);
        this.context = c;
        this.rTitle = title;
        this.rDescription = description;
        this.rimages = images;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.box, parent, false);

        TextView myTitle = row.findViewById(R.id.textView1);
        TextView myDescription = row.findViewById(R.id.textView2);
        ImageView image = row.findViewById(R.id.image);

        myTitle.setText(rTitle[position]);
        myDescription.setText(rDescription[position]);
        Glide.with(row).load(rimages[position]).into(image);

        return row;
    }


}

