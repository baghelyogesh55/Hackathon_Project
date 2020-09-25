package com.example.nearbyfuel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    int AUTHUI_REQUEST_CODE=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {

            startActivity(new Intent(this,ProductKeyActivity.class));
            this.finish();
        }
    }

    public void handleLogin(View view) {

        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

        Intent intent= AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls("http://example.com","http://example.com")
                .setLogo(R.drawable.icon)
                .setAlwaysShowSignInMethodScreen(true)
                .build();

        startActivityForResult(intent,AUTHUI_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AUTHUI_REQUEST_CODE)
        {
            if(resultCode==RESULT_OK)
            {
                //User has signed in successfully
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

                //User is new
                if(user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp())
                {
                    Map<String, String> userdata = new HashMap<>();
                    userdata.put("name","");
                    userdata.put("vehicle","");
                    userdata.put("product_key","");
                    Log.d(TAG, "onActivityResult: Entered");

                    Log.d(TAG, "onActivityResult: "+FirebaseFirestore.getInstance());

                   FirebaseFirestore.getInstance().collection("users")
                           .document(user.getUid())
                           .set(userdata)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   Log.d(TAG, "onSuccess: Done");
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Log.d(TAG, "onFailure: Still having some problem "+e);
                       }
                   });

                    HashMap<String,Object> pk = new HashMap<>();
                    pk.put("Vehicle","Car");

                    Map<String,HashMap<String,Object>> account = new HashMap<>();
                    account.put("Car",pk);

                    FirebaseFirestore.getInstance().collection("product_key")
                            .document(user.getUid())
                            .set(account)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: Success produt key");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: failed product key");
                                }
                            });


                    Toast.makeText(this, "Welcome new User", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this,ProductKeyActivity.class));
                    this.finish();
                }
                //User already has an account
                else
                {
                    Intent intent=new Intent(this,MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
                }
                this.finish();      //Not to comeback on pressing back button
            }
            //Sign Failed
            else
            {
                IdpResponse response=IdpResponse.fromResultIntent(data);
                if(response!=null)
                {
                    Toast.makeText(this, "Error : "+response.getError(), Toast.LENGTH_SHORT).show();
                }
                //else user has cancelled the sign in request
            }
        }
    }
}