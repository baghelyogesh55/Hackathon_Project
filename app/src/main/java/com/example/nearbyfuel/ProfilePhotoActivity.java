package com.example.nearbyfuel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfilePhotoActivity extends AppCompatActivity {

    String currentPhotoPath=null;
    private static final int CAMERA_IMAGE =100 ;
    private static final int GALLERY_IMAGE=101;
    FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    ImageView profileSelectedImage;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo);

        profileSelectedImage = findViewById(R.id.photo_selected);
        progressBar = findViewById(R.id.progress_bar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getPhotoUrl() != null) {


            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(profileSelectedImage);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,ProfileActivity.class));
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()== android.R.id.home){
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_edit_photo) {

            final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
            View bottomSheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.bottom_sheet_chooser,(LinearLayout)findViewById(R.id.bottomSheetChooser));

            bottomSheetView.findViewById(R.id.choose_camera).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    camera();
                    bottomSheetDialog.dismiss();
                }
            });
            bottomSheetView.findViewById(R.id.choose_gallery).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gallery();
                    bottomSheetDialog.dismiss();
                }
            });
            bottomSheetDialog.setTitle("Choose image from");
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();

            //camera();
        }
        return true;
    }

    private void gallery() {
        Intent pictureActionIntent = null;

        pictureActionIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pictureActionIntent, GALLERY_IMAGE);
    }

    private void camera()
    {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            File imageFile=null;
            try {
                imageFile=getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(imageFile!=null)
            {
                Uri imgUri=FileProvider.getUriForFile(this,"com.example.nearbyfuel.fileprovider",imageFile);
                // Uri imgUri=FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
                startActivityForResult(intent,CAMERA_IMAGE);
            }
        }
    }

    private File getImageFile() throws IOException
    {
        File storageDir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imgFile=File.createTempFile("profilePic",".jpg",storageDir);
        currentPhotoPath=imgFile.getAbsolutePath();
        DocumentReference ref=firestore.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String,Object> map=new HashMap<>();
        map.put("path",currentPhotoPath);
        ref.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("ProfilePhotoActivity","updated--------------");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ProfilePhotoActivity","failed to update--------------");
            }
        });
        return imgFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CAMERA_IMAGE && resultCode==RESULT_OK)
        {
            progressBar.setVisibility(View.VISIBLE);
            Bitmap bitmap= BitmapFactory.decodeFile(currentPhotoPath);
            ImageView imageView=findViewById(R.id.photo_selected);
            imageView.setImageBitmap(bitmap);
            uploadToFirebase(bitmap);
        }

        if (requestCode == GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            progressBar.setVisibility(View.VISIBLE);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                ImageView imageView=findViewById(R.id.photo_selected);
                imageView.setImageBitmap(bitmap);
                uploadToFirebase(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void uploadToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);

        final StorageReference reference= FirebaseStorage.getInstance().getReference().child("profilepics")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid() +".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfilePhotoActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfilePhotoActivity.this, "Failed to upload "+e.getCause(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //uri is Download image url
                        setUserProfileUrl(uri);
                    }
                });
    }
    private void setUserProfileUrl(Uri uri){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfilePhotoActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfilePhotoActivity.this, "Failed ....", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}