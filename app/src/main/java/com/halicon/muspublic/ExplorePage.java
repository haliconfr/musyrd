package com.halicon.muspublic;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExplorePage extends AppCompatActivity {
    Index index, userindex;
    String search;
    EditText searchBar;
    public RecyclerView result, userlist;
    List<SearchRes> searchRes;
    String alArtist, alTitle, alArt, alId;
    int count;
    TextView emptyerror;
    FirebaseUser user;
    Button releases, usr;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean rel;
    TextView nouserserror;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        rel = true;
        setContentView(R.layout.explore);
        user = FirebaseAuth.getInstance().getCurrentUser();
        setUI();
        Client client = new Client("8IRHBM7KN3", "bdddf878d84e1d44bfc0041f2d929f22");
        index = client.getIndex("musyrd_releases");
        nouserserror = findViewById(R.id.nousernameerror);
        userindex = client.getIndex("musyrd_users");
        searchBar = findViewById(R.id.search_bar);
        emptyerror = findViewById(R.id.newrelease);
        emptyerror.setVisibility(View.VISIBLE);
        releases = findViewById(R.id.releaseList);
        usr = findViewById(R.id.userList);
        userlist = findViewById(R.id.usersList);
        releases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rel = true;
                nouserserror.setVisibility(View.INVISIBLE);
                emptyerror.setVisibility(View.VISIBLE);
            }
        });
        usr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rel = false;
                emptyerror.setVisibility(View.GONE);
                userlist.setVisibility(View.VISIBLE);
                result.setVisibility(View.GONE);
                if(search != null){
                    if(!search.isEmpty()){
                        searchUsers();
                    }
                }
            }
        });
        emptyerror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExplorePage.this, AddRelease.class);
                startActivity(intent);
                finish();
            }
        });
        searchRes = new ArrayList<>();
        result = findViewById(R.id.followUsersList);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    emptyerror.setVisibility(View.GONE);
                    if(rel){
                        userlist.setVisibility(View.GONE);
                        result.setVisibility(View.VISIBLE);
                        search = searchBar.getText().toString();
                        if(!search.isEmpty()){
                            search();
                        }
                    }
                }
                return false;
            }
        });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("got", "got to here");
                clearlists();
                if(!rel){
                    result.setVisibility(View.GONE);
                    userlist.setVisibility(View.VISIBLE);
                    search = searchBar.getText().toString();
                    if(!search.isEmpty()){
                        searchUsers();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    void search(){
        Thread searchReleases = new Thread(new Runnable() {
            @Override
            public void run() {
                Query query = new Query(search)
                        .setHitsPerPage(10);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                        emptyerror.setVisibility(View.GONE);
                        try{
                            JSONArray hits = jsonObject.getJSONArray("hits");
                            for (int i = 0; i < hits.length(); i++){
                                if(hits.length() == 0){
                                    emptyerror.setVisibility(View.VISIBLE);
                                    searchRes.clear();
                                    if(result.getAdapter() != null) {
                                        result.getAdapter().notifyDataSetChanged();
                                    }
                                }
                                JSONObject content = hits.getJSONObject(i);
                                if(!content.getString("Title").isEmpty()){
                                    alTitle = content.getString("Title");
                                    alArtist = content.getString("Artist");
                                    alArt = content.getString("Art");
                                    alId = content.getString("objectID");
                                    handleResults();
                                }
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                            emptyerror.setVisibility(View.VISIBLE);
                            clearlists();
                        }
                    }
                });
            }
        });
        searchReleases.start();
    }
    void searchUsers(){
        nouserserror.setVisibility(View.GONE);
        db.collection("users")
                .document(search)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot content = task.getResult();
                    if (content.exists()) {
                        if(content.getString("banned") != null && !content.getString("banned").equals("true")){
                            alTitle = search;
                            alArt = content.getString("pfp");
                            ArrayList<String> temp = (ArrayList<String>) content.get("ids");
                            count = temp.size();
                            handleResults();
                        }
                    } else {
                        nouserserror.setVisibility(View.VISIBLE);
                        clearlists();
                    }
                }
            }
        });
    }
    void handleResults(){
        if(rel){
            SearchRes sResults = new SearchRes();
            sResults.sAlbum = alTitle;
            sResults.sArtist = alArtist;
            sResults.sArtUrl = alArt;
            sResults.sId = alId;
            searchRes.add(sResults);
            Log.d("res", searchRes.toString());
            result.setHasFixedSize(true);
            result.setItemViewCacheSize(15);
            result.setDrawingCacheEnabled(true);
            result.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            VideosAdapter adapter = new VideosAdapter(this, searchRes, result);
            result.setAdapter(adapter);
            if(result.getAdapter() == null){
                clearlists();
                emptyerror.setVisibility(View.VISIBLE);
            }
        }else{
            nouserserror.setVisibility(View.GONE);
            SearchRes sResults = new SearchRes();
            sResults.sAlbum = alTitle;
            sResults.sArtUrl = alArt;
            sResults.number = String.valueOf(count);
            searchRes.add(sResults);
            SearchUserAdapter adapter = new SearchUserAdapter(this, searchRes, userlist);
            userlist.setAdapter(adapter);
        }
    }
    void setUI(){
        Button home = findViewById(R.id.gotoMainExp);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExplorePage.this, MainApp.class);
                startActivity(intent);
                finish();
            }
        });
        Button profile = findViewById(R.id.gotoProfileExp);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExplorePage.this, userProfile.class);
                intent.putExtra("user", user.getDisplayName());
                startActivity(intent);
                finish();
            }
        });
    }
    void clearlists(){
        searchRes.clear();
        result.setAdapter(null);
        userlist.setAdapter(null);
    }
}