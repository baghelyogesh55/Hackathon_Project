package com.example.nearbyfuel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG ="ProfileActivity" ;
    TextView name;
    EditText editText;
    TextView add;
    FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference docRef ;

    TextView name_value,email_value,phone_value,vehicle_value;

  String[] arr=HomeFragment.accounts;
    LinearLayout name_layout,email_layout,phone_layout,vehicle_layout;

    CircleImageView profile_pic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name=findViewById(R.id.field_name);
        editText=findViewById(R.id.value_name);
        name_layout=findViewById(R.id.name_layout);
        email_layout=findViewById(R.id.email_layout);
        phone_layout=findViewById(R.id.phone_layout);
        vehicle_layout=findViewById(R.id.vehicle_layout);
        profile_pic=findViewById(R.id.profile_pic);

        add=findViewById(R.id.add_acc);
        name_value=findViewById(R.id.name_value);
        email_value=findViewById(R.id.email_value);
        phone_value=findViewById(R.id.phone_value);
        vehicle_value=findViewById(R.id.vehicle_value);


        add.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

                                       LayoutInflater inflater = getLayoutInflater();
                                       View dialogView = inflater.inflate(R.layout.add_account, null);

                                       builder.setView(dialogView);

                                       final EditText vehicle_name=dialogView.findViewById(R.id.veh_name);
                                       final EditText product_name=dialogView.findViewById(R.id.pro_name);
                                       Button add = dialogView.findViewById(R.id.btn_add);
                                       Button cancel = dialogView.findViewById(R.id.btn_cancel);

                                       final AlertDialog dialog = builder.create();

                                       add.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {

                                               String vehicleName = vehicle_name.getText().toString();
                                               String productName=product_name.getText().toString();
                                             //  addAccount(vehicleName,productName);
                                               Toast.makeText(ProfileActivity.this, "Added", Toast.LENGTH_SHORT).show();
//                                               getdata();
                                               dialog.cancel();
                                           }
                                       });

                                       cancel.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                           dialog.cancel();
                                           }
                                       });

                                       // Display the custom alert dialog on interface
                                       dialog.show();
                                   }
                               }
            );





//        ArrayAdapter adapter = new ArrayAdapter<String>(this,
//                R.layout.account_list, HomeFragment.accounts);
//        ListView listView = (ListView) findViewById(R.id.listView);
//        listView.setAdapter(adapter);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            if(user.getDisplayName()!=null)
                name_value.setText(user.getDisplayName());
            if(user.getEmail()!=null) {
                phone_layout.setVisibility(View.GONE);
                email_value.setText(user.getEmail());
            }
            else {
                //if(user.getPhoneNumber()!=null)
                email_layout.setVisibility(View.GONE);
                phone_value.setText(user.getPhoneNumber());
            }
            if(user.getPhotoUrl()!=null)
            {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(profile_pic);
            }
            docRef=firestore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    name_value.setText(snapshot.getString("name"));
                    vehicle_value.setText(snapshot.getString("vehicle"));
                }
            });
        }
    }

    private void getdata() {
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
                            String[] str = new String[arr.length - 1];
                            for (int j = 1; j < arr.length; j++) {
                                int index = arr[j].indexOf("}");
                                str[j - 1] = arr[j].substring(0, index);
                            }
                            arr=str;

                        }
                    }
                });
    }

    private void addAccount(String vehicleName, String productName) {
    HashMap<String,String> map = new HashMap<>();
    map.put("Vehicle",vehicleName);
    HashMap<String,Object> m = new HashMap<>();
    m.put(productName,map);

    FirebaseFirestore.getInstance().collection("product_key")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .update(m)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(ProfileActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(ProfileActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
        }
    });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }

    public void handleImage(View view)
    {
        startActivityForResult(new Intent(this,ProfilePhotoActivity.class),0);
    }


    public void handleNameLayout(View view) {
        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.bottom_sheet_dialog,(LinearLayout)findViewById(R.id.bottomSheetContainer));

        final TextView field_name=bottomSheetView.findViewById(R.id.field_name);
        final EditText value_name=bottomSheetView.findViewById(R.id.value_name);
        name_value=findViewById(R.id.name_value);
        field_name.setText("Enter your name");
        value_name.setText(name_value.getText());
        value_name.setSelection(name_value.getText().length());
        bottomSheetView.findViewById(R.id.btnsave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name_value.setText(value_name.getText().toString());
                updateProfile();
                updateName();
                Toast.makeText(ProfileActivity.this, "Save pressed", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.btncancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void updateName() {
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .update("name",name_value.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Name Updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Name Failed");
            }
        });
    }

    public void handleVehicleLayout(View view) {
        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.bottom_sheet_dialog,(LinearLayout)findViewById(R.id.bottomSheetContainer));

        final TextView field_name=bottomSheetView.findViewById(R.id.field_name);
        final EditText value_name=bottomSheetView.findViewById(R.id.value_name);
        vehicle_value=findViewById(R.id.vehicle_value);
        field_name.setText("Enter your vehicle name");
        value_name.setText(vehicle_value.getText());
        value_name.setSelection(vehicle_value.getText().length());
        bottomSheetView.findViewById(R.id.btnsave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vehicle_value.setText(value_name.getText().toString());
                updateVehicleName();
                Toast.makeText(ProfileActivity.this, "Save pressed", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.btncancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void updateVehicleName() {
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .update("vehicle",vehicle_value.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Name Updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Name Failed");
            }
        });
    }

    public void handlePhoneLayout(View view) {
        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.bottom_sheet_dialog,(LinearLayout)findViewById(R.id.bottomSheetContainer));

        final TextView field_name=bottomSheetView.findViewById(R.id.field_name);
        final EditText value_name=bottomSheetView.findViewById(R.id.value_name);
        name_value=findViewById(R.id.phone_value);
        field_name.setText("Enter your phone number");
        value_name.setText(name_value.getText());
        value_name.setSelection(name_value.getText().length());
        bottomSheetView.findViewById(R.id.btnsave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name_value.setText(value_name.getText().toString());
                Toast.makeText(ProfileActivity.this, "Save pressed", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.btncancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void handleEmailLayout(View view) {

        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.bottom_sheet_dialog,(LinearLayout)findViewById(R.id.bottomSheetContainer));

        final TextView field_name=bottomSheetView.findViewById(R.id.field_name);
        final EditText value_name=bottomSheetView.findViewById(R.id.value_name);
        name_value=findViewById(R.id.email_value);
        field_name.setText("Enter your Email");
        value_name.setText(name_value.getText());
        value_name.setSelection(name_value.getText().length());
        bottomSheetView.findViewById(R.id.btnsave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                name_value.setText(value_name.getText().toString());
                if(!name_value.getText().toString().equals(user.getEmail()))
                {
                    if(user.getEmail()==null)
                        Toast.makeText(ProfileActivity.this, "null", Toast.LENGTH_SHORT).show();
                    else {

                        user.updateEmail(name_value.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Email already exist", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.btncancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void updateProfile()
    {
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setDisplayName(name_value.getText().toString())
                .build();
        firebaseUser.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Name updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

}