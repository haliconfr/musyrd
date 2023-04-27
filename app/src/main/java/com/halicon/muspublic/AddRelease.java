package com.halicon.muspublic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddRelease extends AppCompatActivity {
    ImageView albumArt;
    FirebaseFirestore db;
    Uri artUri;
    File outputFile, outputIcon;
    String currentDlLink, iconLink;
    TextView title, artist;
    FirebaseUser user;
    Button post, cancel;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_screen);
        getSupportActionBar().hide();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        title = findViewById(R.id.releaseTitle);
        artist = findViewById(R.id.artistName);
        post = findViewById(R.id.addRelease);
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddRelease.this, ExplorePage.class);
                startActivity(intent);
                finish();
            }
        });
        albumArt = findViewById(R.id.addArt);
        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                securityCheck();
            }
        });
    }
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    albumArt.setImageURI(uri);
                    artUri = uri;
                    albumArt.setClickable(true);
                }
            });
    void securityCheck(){
        Log.d("aa", "secheck");
        db.collection("releases")
                .whereEqualTo("Artist", artist.getText().toString())
                .whereEqualTo("Title", title.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()){
                                try {
                                    if(title.length() <= 40){
                                        if(!title.getText().toString().isEmpty()){
                                            if(!artist.getText().toString().isEmpty()){
                                                if(artist.length() <= 30){
                                                    createIcon();
                                                }else{
                                                    Toast.makeText(AddRelease.this, "artist name too long", Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                Toast.makeText(AddRelease.this, "artist name cannot be empty", Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            Toast.makeText(AddRelease.this, "title cannot be empty", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(AddRelease.this, "title too long", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Toast.makeText(AddRelease.this, "release already exists", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("error!", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    void uploadToServer(String iconLink, File icon){
        Map<String, Object> data = new HashMap<>();
        data.put("Artist", artist.getText().toString());
        data.put("Reviews", 0);
        data.put("Title", title.getText().toString());
        data.put("UserUpBy", user.getDisplayName());
        data.put("Art", iconLink);
        data.put("deleted", "false");
        db.collection("releases").document(java.util.UUID.randomUUID().toString()).set(data, SetOptions.merge());
        Intent intent = new Intent(AddRelease.this, ExplorePage.class);
        startActivity(intent);
        finish();
        icon.delete();
    }
    void createIcon() throws IOException{
        Context context = LoginScreen.AppContext;
        if(artUri == null){
            Toast.makeText(AddRelease.this, "release art is required", Toast.LENGTH_SHORT).show();
        }else{
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), artUri);
            File outputDir = context.getCacheDir();
            outputIcon = File.createTempFile("prefix", ".extension", outputDir);
            FileOutputStream fos = new FileOutputStream(outputIcon);
            Bitmap artImage;
            if (bitmap.getWidth() >= bitmap.getHeight()){

                artImage = Bitmap.createBitmap(
                        bitmap,
                        bitmap.getWidth()/2 - bitmap.getHeight()/2,
                        0,
                        bitmap.getHeight(),
                        bitmap.getHeight()
                );

            }else{

                artImage = Bitmap.createBitmap(
                        bitmap,
                        0,
                        bitmap.getHeight()/2 - bitmap.getWidth()/2,
                        bitmap.getWidth(),
                        bitmap.getWidth()
                );
            }
            artImage = Bitmap.createScaledBitmap(artImage, 90, 90, true);
            artImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            fos.flush();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            iconLink  = "AlbumArt/" + "icon"+java.util.UUID.randomUUID().toString();
            UploadTask iconUpTask = storageRef.child(iconLink).putFile(Uri.fromFile(outputIcon));
            iconUpTask.addOnSuccessListener(tskSnap -> {
                Task<Uri> fbaseUri = tskSnap.getStorage().getDownloadUrl();
                fbaseUri.addOnSuccessListener(uri -> {
                    iconLink = uri.toString();
                    uploadToServer(iconLink, outputIcon);
                });
            });
        }
    }
}