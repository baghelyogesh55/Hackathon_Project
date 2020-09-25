package com.example.nearbyfuel;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class DisFragment extends Fragment {

    private static final String TAG = "DisFragment";
    TextView showDis;
    EditText mileage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dis, container, false);
        showDis = v.findViewById(R.id.showDis);
        mileage = v.findViewById(R.id.mileage);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mileage.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                double fuel = HomeFragment.fuel;
                if (fuel >= 0) {
                    if(mileage.getText().toString().matches(""))
                    {
                        mileage.setError("Please enter some value");
                    }
                    else
                        {
                        String val = mileage.getText().toString();
                        double value = fuel * Float.parseFloat(val);
                        showDis.setText(String.format("%.3f",value) + " m");
                    }
                    } else {
                    showDis.setError("Please fetch the fuel in first fragment");
                    showDis.setText("Please fetch the fuel in first fragment");
                }

            }
        });
        return v;
    }
}
