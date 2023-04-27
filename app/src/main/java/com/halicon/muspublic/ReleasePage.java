package com.halicon.muspublic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleasePage extends AppCompatActivity {
    ArrayList<ReviewInfo> reviewInfo;
    RecyclerView release;
    double starNumber;
    TextView relav;
    ArrayList<ReviewInfo> revinf = new ArrayList<ReviewInfo>();
    DocumentSnapshot dcs;
    int check;
    int scrollcheck = 0;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    List<Integer> starsList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.releasepage);
        relav = findViewById(R.id.relAverage);
        relav.setText("0.0");
        getSupportActionBar().hide();
        TextView artist = findViewById(R.id.relArtist);
        artist.setText(getIntent().getStringExtra("artist"));
        release = findViewById(R.id.relView);
        TextView alTitle = findViewById(R.id.relTitle);
        alTitle.setText(getIntent().getStringExtra("album"));
        ImageView art = findViewById(R.id.relArt);
        ImageView back = findViewById(R.id.backbuttonExp);
        NDSpinner spinner = findViewById(R.id.relSpinner);
        List<String> categories = new ArrayList<String>();
        categories.add("report");
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
                            categories.add("delete release");
                            check++;
                        }
                    } else {
                        Log.d("Oops!", "document doesn't exist");
                    }
                }
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ReleasePage.this,
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
                        Intent intent = new Intent(ReleasePage.this, Report.class);
                        intent.putExtra("mode", "release");
                        intent.putExtra("user", user.getDisplayName());
                        intent.putExtra("username", alTitle.getText());
                        startActivity(intent);
                    }
                    if(adapter.getItem(position).equals("delete release")){
                        Toast.makeText(ReleasePage.this, "deleting", Toast.LENGTH_SHORT).show();
                        db.collection("releases")
                                .document(getIntent().getStringExtra("id"))
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot document = task.getResult();
                                        if(task.isSuccessful()){
                                            Toast.makeText(ReleasePage.this, "useruploaded: " + document.getString("UserUpBy"), Toast.LENGTH_LONG).show();
                                            String id = document.getId();
                                            CollectionReference dc = db.collection("releases");
                                            DocumentReference meRef = dc.document(id);
                                            meRef.delete();
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("deleted", "true");
                                            meRef.set(data, SetOptions.merge());
                                            FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(document.getString("Art")).delete();
                                            id = null;
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReleasePage.this, MainApp.class);
                startActivity(intent);
                finish();
            }
        });
        Picasso.get().load(getIntent().getStringExtra("art")).fit().centerCrop()
                .placeholder(R.drawable.icontemp)
                .into(art);
        Button review = findViewById(R.id.makereview);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReleasePage.this, UploadReview.class);
                intent.putExtra("art", getIntent().getStringExtra("art"));
                intent.putExtra("album", getIntent().getStringExtra("album"));
                intent.putExtra("artist", getIntent().getStringExtra("artist"));
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
                finish();
            }
        });
        NestedScrollView scrollView = findViewById(R.id.nestedscrollview3);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Thread scrollRev = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
                            if(scrollcheck == 1){
                                scrollcheck = 0;
                                Log.d("bottom", "hit");
                                db.collection("reviews")
                                        .whereEqualTo("Id", getIntent().getStringExtra("id"))
                                        .orderBy("Date", Query.Direction.DESCENDING)
                                        .startAfter(dcs)
                                        .limit(9)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        scrollcheck = 0;
                                                        dcs = document;
                                                        reviewInfo = new ArrayList<ReviewInfo>();
                                                        ReviewInfo revInf = new ReviewInfo();
                                                        revInf.artUrl = document.getString("Id");
                                                        revInf.revText = document.getString("ReviewText");;
                                                        revInf.revUsr = document.getString("User");
                                                        revInf.stars = Math.toIntExact((Long) document.get("Stars"));
                                                        Date date1 = new Date ();
                                                        date1.setTime((long)Integer.valueOf(document.getString("Date"))*1000);
                                                        revInf.date = new SimpleDateFormat("dd/MM/yyyy").format(date1);
                                                        revInf.releaseId = document.getString("Id");
                                                        starsList.add(revInf.stars);
                                                        starNumber = calculateAverage(starsList);
                                                        setStarImage();
                                                        db.collection("users")
                                                                .document(revInf.revUsr)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot document = task.getResult();
                                                                            revInf.artUrl = document.getString("pfp");
                                                                            handleResults(revInf);
                                                                        } else {
                                                                            Log.d("error!", "Error getting documents: ", task.getException());
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
                scrollRev.start();
            }
        });
        Thread getRev = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection("reviews")
                        .whereEqualTo("Id", getIntent().getStringExtra("id"))
                        .orderBy("Date", Query.Direction.DESCENDING)
                        .limit(9)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        scrollcheck = 0;
                                        dcs = document;
                                        reviewInfo = new ArrayList<ReviewInfo>();
                                        ReviewInfo revInf = new ReviewInfo();
                                        revInf.artUrl = document.getString("Id");
                                        revInf.revText = document.getString("ReviewText");;
                                        revInf.revUsr = document.getString("User");
                                        revInf.stars = Math.toIntExact((Long) document.get("Stars"));
                                        Date date1 = new Date ();
                                        date1.setTime((long)Integer.valueOf(document.getString("Date"))*1000);
                                        revInf.date = new SimpleDateFormat("dd/MM/yyyy").format(date1);
                                        revInf.releaseId = document.getString("Id");
                                        starsList.add(revInf.stars);
                                        starNumber = calculateAverage(starsList);
                                        setStarImage();
                                        db.collection("users")
                                                .document(revInf.revUsr)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            revInf.artUrl = document.getString("pfp");
                                                            handleResults(revInf);
                                                        } else {
                                                            Log.d("error!", "Error getting documents: ", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Log.d("error!", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
        getRev.start();
    }
    void handleResults(ReviewInfo revInfo){
        revinf.add(revInfo);
        release.setHasFixedSize(true);
        release.setItemViewCacheSize(15);
        release.setDrawingCacheEnabled(true);
        release.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        RecyclerView.Adapter adapter = new ReleaseAdapter(this, revinf, release);
        release.setAdapter(adapter);
        setStarImage();
        scrollcheck = 1;
    }
    void setStarImage(){
        int starCount = (int) starNumber;
        relav.setText(String.format("%.2f", starNumber));
        ImageView stars = findViewById(R.id.relStars);
        stars.setImageResource(R.drawable.onestar);
        if(starCount == 1){
            stars.setImageResource(R.drawable.onestar);
        }
        if(starCount == 2){
            stars.setImageResource(R.drawable.twostar);
        }
        if(starCount == 3){
            stars.setImageResource(R.drawable.threestar);
        }
        if(starCount == 4){
            stars.setImageResource(R.drawable.fourstar);
        }
        if(starCount == 5){
            stars.setImageResource(R.drawable.fivestar);
        }
    }
    private double calculateAverage(List <Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
}
