package com.wildnet.firebasechatkotlin.activity;

import android.location.Address;
import android.location.Geocoder;
import android.support.constraint.solver.ArrayLinkedVariables;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wildnet.firebasechatkotlin.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final Geocoder geocoder;
        // addresses=null ;

        geocoder = new Geocoder(this, Locale.getDefault());


        final EditText lat=findViewById(R.id.et_lat);
        final EditText lon=findViewById(R.id.et_long);
        final TextView location=findViewById(R.id.tv_address);
        Button submit=findViewById(R.id.btn_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latitude=lat.getText().toString().trim();
                String longitude=lon.getText().toString().trim();
                List<Address> addresses = new ArrayList();

                try {
                    addresses = geocoder.getFromLocation(Double.parseDouble(latitude),
                            Double.parseDouble(longitude), 1);

                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    Log.e("Location", address+" : "+city);
                    location.setText(address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
