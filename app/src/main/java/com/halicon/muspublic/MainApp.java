package com.halicon.muspublic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainApp extends AppCompatActivity {
    RecyclerView reviewItem;
    List<ReviewInfo> reviewInfo;
    List<String> followedUsers = new ArrayList<>();
    FirebaseFirestore db;
    FirebaseUser user;
    NestedScrollView scrollView;
    String artUrl, revUsr, revText, revArtist, revAlbum, id, date;
    int stars;
    ArrayList<String> ids;
    ArrayList<String> otherids;
    DocumentSnapshot documentSnapshot;
    ArrayList<recommendedUser> rUser = new ArrayList<>();
    RecyclerView users;
    int check = 0;
    int scrollcheck = 0;
    ArrayList<String> dates;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        reviewInfo = new ArrayList<>();
        setContentView(R.layout.main_app);
        dates = new ArrayList<>();
        scrollView = findViewById(R.id.nestedScrollView);
        reviewItem = findViewById(R.id.relView);
        reviewItem.setNestedScrollingEnabled(false);
        Spinner spinner = findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("log out");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if(check == 1){
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainApp.this, LoginScreen.class);
                    spinner.setVisibility(View.GONE);
                    startActivity(intent);
                }
            }
        });
        setUI();
        getUserInfo();
        setRecUsers();
    }

    void getUserInfo(){
        db.collection("users")
                .document(user.getDisplayName())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        followedUsers = (List<String>) document.get("following");
                        getReviews();
                        check++;
                    } else {
                        Log.d("Oops!", "document doesn't exist");
                    }
                }
            }
        });
    }
    private void getReviews() {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
                        if(scrollcheck == 1){
                            scrollcheck = 0;
                            Log.d("bottom", "hit");
                            db.collection("reviews")
                                    .whereIn("User", followedUsers)
                                    .orderBy("Date", Query.Direction.DESCENDING)
                                    .startAfter(documentSnapshot)
                                    .limit(9)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    documentSnapshot = document;
                                                    revUsr = document.getString("User");
                                                    revText = document.getString("ReviewText");
                                                    id = document.getString("Id");
                                                    stars = Math.toIntExact((Long) document.get("Stars"));
                                                    revAlbum = document.getString("Title");
                                                    revArtist = document.getString("Artist");
                                                    artUrl = document.getString("Art");
                                                    date = document.getString("Date");
                                                    handleResults();
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
        Thread getRev = new Thread(new Runnable() {
            @Override
            public void run() {
                scrollcheck = 0;
                db.collection("reviews")
                        .whereIn("User", followedUsers)
                        .orderBy("Date", Query.Direction.DESCENDING)
                        .limit(9)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        documentSnapshot = document;
                                        revUsr = document.getString("User");
                                        revText = document.getString("ReviewText");
                                        id = document.getString("Id");
                                        stars = Math.toIntExact((Long) document.get("Stars"));
                                        revAlbum = document.getString("Title");
                                        revArtist = document.getString("Artist");
                                        artUrl = document.getString("Art");
                                        date = document.getString("Date");
                                        handleResults();
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
    void handleResults(){
        TextView instruct = findViewById(R.id.instructionsFol);
        if(revText != null && !revText.isEmpty()){
            instruct.setVisibility(View.GONE);
            ReviewInfo revInf = new ReviewInfo();
            revInf.revText = revText;
            revInf.artUrl = artUrl;
            revInf.revAlbum = revAlbum;
            revInf.revArtist = revArtist;
            revInf.revUsr = revUsr;
            revInf.stars = stars;
            revInf.releaseId = id;
            revInf.date = date;
            reviewInfo.add(revInf);
            reviewItem.setHasFixedSize(true);
            reviewItem.setItemViewCacheSize(15);
            reviewItem.setDrawingCacheEnabled(true);
            reviewItem.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            RecyclerView.Adapter adapter = new ReviewAdapter(this, reviewInfo, reviewItem);
            reviewItem.setAdapter(adapter);
            scrollcheck = 1;
        }else{
            instruct.setVisibility(View.VISIBLE);
        }
    }
    void setRecUsers(){
        users = findViewById(R.id.recUsers);
        db.collection("users")
                .document(user.getDisplayName())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ids = (ArrayList<String>) document.get("ids");
                        TextView instruct = findViewById(R.id.instructionsRec);
                        if (ids != null && !ids.isEmpty()) {
                            instruct.setVisibility(View.GONE);
                            try {
                                FileInputStream fileInputStream = getApplicationContext().openFileInput("ids");
                                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                                ArrayList<String> fileids = (ArrayList<String>) objectInputStream.readObject();
                                objectInputStream.close();
                                fileInputStream.close();
                                if (fileids.equals(ids)) {
                                    FileInputStream fis = getApplicationContext().openFileInput("otherids");
                                    ObjectInputStream is = new ObjectInputStream(fis);
                                    rUser = (ArrayList<recommendedUser>) is.readObject();
                                    is.close();
                                    fis.close();
                                    setAdapter();
                                }else{
                                    compareLists();
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                compareLists();
                            }
                        }else{
                            instruct.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d("Oops!", "document doesn't exist");
                    }
                }
            }
        });
    }
    class ResultComparator implements Comparator<recommendedUser>
    {
        public int compare(recommendedUser left, recommendedUser right) {
            return left.percentage - right.percentage;
        }
    }
    void compareLists(){
        Log.d("firebase moment", "yuh");
        db.collection("users")
                .whereNotEqualTo(FieldPath.documentId(), user.getDisplayName())
                .whereArrayContainsAny("ids", ids)
                .limit(15)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(!followedUsers.contains(document.getId())){
                                    otherids = (ArrayList<String>) document.get("ids");
                                    recommendedUser recUsr = new recommendedUser();
                                    double ttl = 0;
                                    for(int i=0;i<otherids.size();i++) {
                                        if (ids.contains((otherids.get(i))))
                                            ttl++;
                                    }
                                    int pct =(int) ((ttl/otherids.size())*100d);
                                    recUsr.percentage = pct;
                                    recUsr.username = document.getId();
                                    recUsr.userpfp = document.getString("pfp");
                                    rUser.add(recUsr);
                                    rUser.sort(new ResultComparator());
                                    try{
                                        FileOutputStream fos = getApplicationContext().openFileOutput("otherids", Context.MODE_PRIVATE);
                                        ObjectOutputStream os = new ObjectOutputStream(fos);
                                        os.writeObject(rUser);
                                        os.close();
                                        fos.close();
                                        FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("ids", Context.MODE_PRIVATE);
                                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                                        objectOutputStream.writeObject(ids);
                                        objectOutputStream.close();
                                        fileOutputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    setAdapter();
                                }
                            }
                        } else {
                            Log.d("error!", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    void setAdapter(){
        users.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        users.setAdapter(new UserAdapter(this, rUser, users));
    }

    void setUI(){
        Button search = findViewById(R.id.gotoSearch);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSearch(MainApp.this);
            }
        });
        Button profile = findViewById(R.id.gotoProfile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainApp.this, userProfile.class);
                intent.putExtra("user", user.getDisplayName());
                startActivity(intent);
                finish();
            }
        });
    }
    void gotoSearch(Context context){
        Intent intent = new Intent(context, ExplorePage.class);
        startActivity(intent);
        finish();
    }
}