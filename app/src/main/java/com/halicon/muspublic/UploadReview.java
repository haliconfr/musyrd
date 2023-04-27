package com.halicon.muspublic;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UploadReview extends AppCompatActivity {
    int starNumber;
    TextView reviewBox;
    FirebaseFirestore db;
    FirebaseUser user;
    TextView title;
    String relArtist, id, relTitle, relArt;
    Button post;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_upload);
        starNumber = 1;
        StrictMode.setThreadPolicy(policy);
        getSupportActionBar().hide();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        relTitle = intent.getStringExtra("album");
        relArt = intent.getStringExtra("art");
        relArtist = intent.getStringExtra("artist");
        id = intent.getStringExtra("id");
        title = findViewById(R.id.reviewAlbTitle);
        ImageView cover = findViewById(R.id.reviewAlbCover);
        reviewBox = findViewById(R.id.reviewbox);
        Picasso.get().load(relArt).fit().centerCrop()
                .placeholder(R.drawable.icontemp)
                .into(cover);
        title.setText(relTitle);
        ImageView back = findViewById(R.id.backbuttonRev);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadReview.this, ExplorePage.class);
                startActivity(intent);
            }
        });
        setStars();
    }
    void setStars(){
        ImageView stars = findViewById(R.id.stars);
        TextView star1 = findViewById(R.id.oneStar);
        star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stars.setImageResource(R.drawable.onestar);
                starNumber = 1;
            }
        });
        TextView star2 = findViewById(R.id.twoStar);
        star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stars.setImageResource(R.drawable.twostar);
                starNumber = 2;
            }
        });
        TextView star3 = findViewById(R.id.threeStar);
        star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stars.setImageResource(R.drawable.threestar);
                starNumber = 3;
            }
        });
        TextView star4 = findViewById(R.id.fourStar);
        star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stars.setImageResource(R.drawable.fourstar);
                starNumber = 4;
            }
        });
        TextView star5 = findViewById(R.id.fiveStar);
        star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stars.setImageResource(R.drawable.fivestar);
                starNumber = 5;
            }
        });
        post = findViewById(R.id.postreview);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                securityCheck();
            }
        });
    }
    void securityCheck(){
        if(reviewBox.getText().toString().contains(".com")){
            Toast.makeText(UploadReview.this, "links are not permitted in reviews", Toast.LENGTH_LONG).show();
        }else{
            if(reviewBox.getText().length() <= 500){
                uploadReview();
            }
        }
    }
    void uploadReview(){
        post.setActivated(false);
        Map<String, Object> data = new HashMap<>();
        data.put("ReviewText", reviewBox.getText().toString());
        data.put("User", user.getDisplayName());
        data.put("Stars", starNumber);
        data.put("Title", relTitle);
        data.put("Artist", relArtist);
        data.put("Art", relArt);
        data.put("Id", id);
        data.put("Date", String.valueOf(System.currentTimeMillis() / 1000L));
        db.collection("reviews").document(java.util.UUID.randomUUID().toString()).set(data, SetOptions.merge());
        CollectionReference dc = db.collection("releases");
        DocumentReference tg = dc.document(id);
        db.collection("releases")
                .document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        double currentReviews = task.getResult().getDouble("Reviews");
                        int serverReviews = (int) currentReviews;
                        String newViewsStr = Integer.toString(serverReviews + 1);
                        int newReviews = Integer.parseInt(newViewsStr);
                        Map<String, Object> map = new HashMap<>();
                        map.put("Reviews", newReviews);
                        tg.set(map, SetOptions.merge());
                        CollectionReference cr = db.collection("users");
                        DocumentReference meRef = cr.document(user.getDisplayName());
                        meRef.update("ids", FieldValue.arrayUnion(id));
                        Toast.makeText(UploadReview.this, "review uploaded!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadReview.this, MainApp.class);
                        startActivity(intent);
                    } else {
                        Log.d("Oops!", "document doesn't exist");
                    }
                }
            }
        });
    }
}
