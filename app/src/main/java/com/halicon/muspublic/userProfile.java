package com.halicon.muspublic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class userProfile extends AppCompatActivity{
    String usersPfp, usersBio, username, currentDlLink;
    Button savebutton, back, followbutton;
    Uri pfpUri;
    NDSpinner spinner;
    TextView followers, following, error, bioProf, usernameProf;
    ImageView profileImage, changePfp;
    EditText bio;
    ArrayList<ReviewInfo> revinf = new ArrayList<ReviewInfo>();
    File outputFile;
    RecyclerView main;
    Context context;
    DocumentSnapshot dcs;
    int check = 0;
    int scrollcheck = 0;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(policy);
        db = FirebaseFirestore.getInstance();
        getSupportActionBar().hide();
        Intent iin = getIntent();
        username = iin.getStringExtra("user");
        setContentView(R.layout.userprofile);
        setUI();
        getUserInfo();
        spinner = findViewById(R.id.spinner2);
        List<String> categories = new ArrayList<String>();
        if(!username.equals(user.getDisplayName())) {
            db.collection("users")
                    .document(user.getDisplayName())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        ArrayList<String> blocked = (ArrayList<String>) document.get("blocked");
                        if(blocked != null && blocked.isEmpty()){
                            Toast.makeText(userProfile.this, "empty", Toast.LENGTH_LONG);
                        }
                        if(blocked != null && blocked.contains(username)) {
                            categories.add("block");
                            categories.add("report");
                            check++;
                            setSpinner(categories, spinner);
                        }else{
                            categories.add("unblock");
                            categories.add("report");
                            check++;
                            setSpinner(categories, spinner);
                        }
                        db.collection("users")
                                .document(user.getDisplayName())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String admin = document.getString("admin");
                                        if(admin != null && admin.equals("true")){
                                            ImageView delete = findViewById(R.id.deleteuser);
                                            delete.setVisibility(View.VISIBLE);
                                            delete.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    deleteUser();
                                                }
                                            });
                                        }else{
                                            ImageView delete = findViewById(R.id.deleteuser);
                                            delete.setVisibility(View.GONE);
                                        }
                                    } else {
                                        Log.d("Oops!", "document doesn't exist");
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
        context = this;
        db.collection("users")
                .document(username)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            List<String> blocked = (List<String>) document.get("blocked");
                            if(blocked != null && blocked.contains(user.getDisplayName())){
                                Toast.makeText(userProfile.this, "you have been blocked by this user", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(userProfile.this, MainApp.class);
                                startActivity(intent);
                            }
                        }
                    }
                });
        followers = findViewById(R.id.userProfFollowers);
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(userProfile.this, FollowList.class);
                intent.putExtra("follow", "followers");
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
        main = findViewById(R.id.relView);
        followbutton = findViewById(R.id.followbutton);
        profileImage = findViewById(R.id.userPfpProf);
        usernameProf = findViewById(R.id.profileName);
        bioProf = findViewById(R.id.userProfBio);
        following = findViewById(R.id.userProfFollowing);
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(userProfile.this, FollowList.class);
                intent.putExtra("follow", "following");
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
        error = findViewById(R.id.noposts);
        error.setVisibility(View.GONE);
        Picasso.get().load(usersPfp).fit().centerCrop()
                .placeholder(R.drawable.icontemp)
                .into(profileImage);
        usernameProf.setText(username);
        bioProf.setText(usersBio);
        if(!username.equals(user.getDisplayName())){
            setfollowbutton();
        }else{
            followbutton.setText("edit profile");
            followbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editProfile();
                }
            });
        }
        db.collection("reviews")
                .whereEqualTo("User", username)
                .orderBy("Date", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                scrollcheck = 0;
                                dcs = document;
                                ReviewInfo revInfo = new ReviewInfo();
                                revInfo.revUsr = username;
                                revInfo.revText = document.getString("ReviewText");
                                revInfo.releaseId = document.getString("Id");
                                revInfo.stars = Math.toIntExact((Long) document.get("Stars"));
                                revInfo.revAlbum = document.getString("Title");
                                revInfo.revArtist = document.getString("Artist");
                                revInfo.artUrl = document.getString("Art");
                                revInfo.date = document.getString("Date");
                                setAdapter(revInfo);
                            }
                        } else {
                            error.setVisibility(View.VISIBLE);
                        }
                    }
                });
        NestedScrollView scrollView = findViewById(R.id.nestedScrollView2);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
                    if(scrollcheck == 1){
                        scrollcheck = 0;
                        Log.d("bottom", "hit");
                        db.collection("reviews")
                                .whereEqualTo("User", username)
                                .orderBy("Date", Query.Direction.DESCENDING)
                                .startAfter(dcs)
                                .limit(9)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                dcs = document;
                                                ReviewInfo revInfo = new ReviewInfo();
                                                revInfo.revUsr = username;
                                                revInfo.revText = document.getString("ReviewText");
                                                revInfo.releaseId = document.getString("Id");
                                                revInfo.stars = Math.toIntExact((Long) document.get("Stars"));
                                                revInfo.revAlbum = document.getString("Title");
                                                revInfo.revArtist = document.getString("Artist");
                                                revInfo.artUrl = document.getString("Art");
                                                revInfo.date = document.getString("Date");
                                                setAdapter(revInfo);
                                            }
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }
    void setAdapter(ReviewInfo revInfo){
        revinf.add(revInfo);
        main.setHasFixedSize(true);
        main.setItemViewCacheSize(15);
        main.setDrawingCacheEnabled(true);
        main.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        main.setAdapter(new ReviewAdapter(this, revinf, main));
        scrollcheck = 1;
    }
    public void editProfile() {
        setContentView(R.layout.editprofile);
        changePfp = findViewById(R.id.changePfp);
        savebutton = findViewById(R.id.savebutton);
        bio = findViewById(R.id.editBio);
        back = findViewById(R.id.backbutton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editBack();
            }
        });
        bio.setText(usersBio);
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });
        if (usersPfp != null) {
            Picasso.get().load(usersPfp).fit().centerCrop()
                    .placeholder(R.drawable.icontemp)
                    .into(profileImage);
        }
        changePfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });
    }
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    ImageButton pfpChange = findViewById(R.id.changePfp);
                    Picasso.get().load(uri).fit().centerCrop()
                            .placeholder(R.drawable.icontemp)
                            .into(pfpChange);
                    pfpUri = uri;
                    savebutton.setClickable(true);
                }
            });

    public void saveChanges() {
        if (pfpUri != null) {
            try {
                Context context = LoginScreen.AppContext;
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pfpUri);
                File outputDir = context.getCacheDir();
                outputFile = File.createTempFile("prefix", ".extension", outputDir);
                FileOutputStream fos = new FileOutputStream(outputFile);
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
                artImage = Bitmap.createScaledBitmap(artImage, 180, 180, true);
                artImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentDlLink = "userPfps/" + user.getDisplayName() + "/" + "profilepicture";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            UploadTask uploadTask = storageRef.child(currentDlLink).putFile(Uri.fromFile(outputFile));
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                firebaseUri.addOnSuccessListener(uri -> {
                    currentDlLink = uri.toString();
                    Map<String, Object> data = new HashMap<>();
                    data.put("pfp", currentDlLink);
                    db.collection("users").document(user.getDisplayName()).set(data, SetOptions.merge());
                    Toast.makeText(userProfile.this, "profile updated", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(uri).fit().centerCrop()
                            .placeholder(R.drawable.icontemp)
                            .into(changePfp);
                });
            });
        }
        if (!bio.getText().toString().equals(usersBio)) {
            if (!bio.getText().toString().isEmpty()) {
                if (bio.getText().toString().length() <= 60) {
                    CollectionReference dc = db.collection("users");
                    DocumentReference meRef = dc.document(user.getDisplayName());
                    Map<String, Object> map = new HashMap<>();
                    map.put("bio", bio.getText().toString());
                    meRef.set(map, SetOptions.merge());
                    Toast.makeText(userProfile.this, "profile updated", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(userProfile.this, "bio too long!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void editBack() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void getUserInfo() {
        String userToGet = username;
        db.collection("users")
                .document(userToGet)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            usersPfp = document.getString("pfp");
                            usersBio = document.getString("bio");
                            if(document.get("followers") != null){
                                List<String> followersList = (List<String>) document.get("followers");
                                followers.setText(String.valueOf(followersList.size()));
                            }
                            if(document.get("following") != null) {
                                List<String> followingList = (List<String>) document.get("following");
                                following.setText(String.valueOf(followingList.size() - 1));
                            }
                            if(usersBio == null){
                                usersBio = "this user hasn't written their bio yet!";
                            }
                            if(usersPfp != null) {
                                Picasso.get().load(usersPfp).fit().centerCrop()
                                        .placeholder(R.drawable.icontemp)
                                        .into(profileImage);
                            }
                            bioProf.setText(usersBio);
                            usernameProf.setText("@" + userToGet);
                        }
                    }
                });
    }
    void setfollowbutton(){
        db.collection("users")
                .document(user.getDisplayName())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> currentlyFollowing = (List<String>) document.get("following");
                        if (currentlyFollowing.contains(username)) {
                            followbutton.setText("unfollow");
                            followbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    unfollowuser();
                                }
                            });
                        } else {
                            followbutton.setText("follow");
                            followbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    followuser();
                                }
                            });
                        }
                    }
                }
            }
        });
    }
    public void followuser() {
        //tells the server that this user followed the user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userFollow = username;
        CollectionReference cr = db.collection("users");
        DocumentReference userToFollowRef = cr.document(userFollow);
        userToFollowRef.update("followers", FieldValue.arrayUnion(user.getDisplayName()));
        CollectionReference dc = db.collection("users");
        DocumentReference meRef = dc.document(user.getDisplayName());
        meRef.update("following", FieldValue.arrayUnion(userFollow));
        followbutton.setText("unfollow");
        followbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unfollowuser();
            }
        });
    }

    public void unfollowuser() {
        //tells the server that this user unfollowed the user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userFollow = username;
        CollectionReference cr = db.collection("users");
        DocumentReference userToFollowRef = cr.document(userFollow);
        userToFollowRef.update("followers", FieldValue.arrayRemove(user.getDisplayName()));
        CollectionReference dc = db.collection("users");
        DocumentReference meRef = dc.document(user.getDisplayName());
        meRef.update("following", FieldValue.arrayRemove(userFollow));
        followbutton.setText("follow");
        followbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followuser();
            }
        });
    }
    void setUI(){
        Button profile = findViewById(R.id.gotoProfileProf);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentprof = new Intent(userProfile.this, userProfile.class);
                intentprof.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentprof.putExtra("user", user.getDisplayName());
                startActivity(intentprof);
                finish();
            }
        });
        Button home = findViewById(R.id.gotoMainProf);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainApp.class);
                startActivity(intent);
                finish();
            }
        });
        Button search = findViewById(R.id.gotoSearchProf);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentsrch = new Intent(context, ExplorePage.class);
                startActivity(intentsrch);
                finish();
            }
        });
    }
    void setSpinner(List<String> categories, NDSpinner spinner){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(check == 1){
                    Log.d("item", adapter.getItem(position));
                    if(adapter.getItem(position).equals("report")){
                        reportUser();
                    }
                    if(adapter.getItem(position).equals("block")){
                        blockUser(categories, adapter);
                    }
                    if(adapter.getItem(position).equals("unblock")){
                        unblockUser(categories, adapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    void blockUser(List<String> categories, ArrayAdapter<String> adapter){
        Log.d("method yuh", "block");
        check = 0;
        CollectionReference dc = db.collection("users");
        DocumentReference meRef = dc.document(user.getDisplayName());
        meRef.update("blocked", FieldValue.arrayUnion(username)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                categories.clear();
                categories.add("unblock");
                categories.add("report");
                adapter.notifyDataSetChanged();
                check = 1;
            }
        });
    }
    void unblockUser(List<String> categories, ArrayAdapter<String> adapter){
        CollectionReference dc = db.collection("users");
        DocumentReference meRef = dc.document(user.getDisplayName());
        meRef.update("blocked", FieldValue.arrayRemove(username));
        categories.removeAll(categories);
        categories.add("block");
        categories.add("report");
        adapter.notifyDataSetChanged();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void deleteUser(){
        db.collection("users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(document.getString("pfp")).delete();
                    db.collection("users").document(username).delete();
                    CollectionReference dc = db.collection("users");
                    DocumentReference meRef = dc.document(username);
                    meRef.set("banned", SetOptions.merge());
                    meRef.update("banned", "true");
                }
            }
        });
        db.collection("reviews")
                .whereEqualTo("User", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot document : task.getResult()){
                            String id = document.getId();
                            db.collection("reviews").document(id).delete();
                            id = null;
                        }
                    }
                });
    }
    void reportUser(){
        Intent intent = new Intent(userProfile.this, Report.class);
        intent.putExtra("username", username);
        intent.putExtra("mode", "user");
        startActivity(intent);
    }
}