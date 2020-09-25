package com.example.nearbyfuel;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductKeyActivity extends AppCompatActivity {

    public static String[] accounts;
    public static String[] acc;
    String data;
    private static final String TAG = "ProductKeyActivity";
    EditText name,vehicle,productKey;
    Button btnVerify;
    boolean matched;
    public static  String pKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_key);

        name = findViewById(R.id.et_name);
        vehicle = findViewById(R.id.et_vehicle_name);
        productKey = findViewById(R.id.et_product_key);
        btnVerify = findViewById(R.id.btn_verify);

        getData();
        matched=false;
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DocumentReference ref= FirebaseFirestore.getInstance().collection("product_key").
                        document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                
                        ref.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(final DocumentSnapshot snapshot) {
                                Log.d(TAG, "onSuccess: data" + snapshot.getData());
                                 data = snapshot.getData().toString();
                                 pKey=productKey.getText().toString();
                               int i = data.indexOf(pKey);
                                if(i>=0) {
                                    Log.d(TAG, "onSuccess: Matched");
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("Vehicle",vehicle.getText().toString());
                                    ref.update(pKey,map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                               data=snapshot.getData().toString();
                                               String[] arr = data.split("Vehicle=");
                                               acc=new String[arr.length-1];
                                               for(int j=1;j<arr.length;j++)
                                               {
                                                  int index=arr[j].indexOf("}");
                                                   acc[j-1] = arr[j].substring(0,index);
                                                   Log.d(TAG, "onSuccess: Accounts = "+acc.length+" , "+acc[0]);
                                               }
                                               isMatched();
                                                }
                                            });

                                   matched=true;

                                    } else
                                        Toast.makeText(ProductKeyActivity.this, "No such key found", Toast.LENGTH_SHORT).show();
                                }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed Line 53");
                    }
                });

            }

        });
    }
    public void getData() {
        final DocumentReference ref = FirebaseFirestore.getInstance().collection("product_key").
                document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot snapshot) {

                        String data = snapshot.getData().toString();
                        int i = data.indexOf("=");
                        String pK=data.substring(1,i);
                        i = data.indexOf(pK);
                        if (i >= 0) {
                            String[] arr = data.split("Vehicle=");
                            accounts = new String[arr.length - 1];
                            for (int j = 1; j < arr.length; j++) {
                                int index = arr[j].indexOf("}");
                                accounts[j - 1] = arr[j].substring(0, index);
                            }

                        }
                    }
                });
    }
    public void isMatched()
    {
        if(matched)
        {
            startActivity(new Intent(ProductKeyActivity.this,MainActivity.class));
            this.finish();
        }
    }
}