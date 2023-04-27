package com.halicon.muspublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowList extends AppCompatActivity {
    RecyclerView list;
    TextView follow;
    List<SearchRes> searchRes;
    String alTitle, alArt;
    int count;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.followlist);
        getSupportActionBar().hide();
        searchRes = new ArrayList<>();
        ImageView backtoprofile = findViewById(R.id.backtoprofile);
        backtoprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FollowList.this, userProfile.class);
                intent.putExtra("user", getIntent().getStringExtra("username"));
                startActivity(intent);
            }
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String username = getIntent().getStringExtra("username");
        follow = findViewById(R.id.followtitle);
        list = findViewById(R.id.followUsersList);
        String mode = getIntent().getStringExtra("follow");
        follow.setText(mode);
        String temp;
        temp = mode;
        if(temp.equals("followers")){
            mode = "following";
        }
        if(temp.equals("following")){
            mode = "followers";
        }
        db.collection("users")
                .whereArrayContains(mode, username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            alTitle = document.getId();
                            alArt = document.getString("pfp");
                            ArrayList<String> temp = (ArrayList<String>) document.get("ids");
                            if(temp == null){
                                count = 0;
                            }else{
                                count = temp.size();
                            }
                            handleResults();
                        }
                    }
                });
    }
    void handleResults(){
        SearchRes sResults = new SearchRes();
        sResults.sAlbum = alTitle;
        sResults.sArtUrl = alArt;
        sResults.number = String.valueOf(count);
        searchRes.add(sResults);
        SearchUserAdapter adapter = new SearchUserAdapter(this, searchRes, list);
        list.setAdapter(adapter);
    }
}
