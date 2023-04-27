package com.halicon.muspublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Report extends AppCompatActivity {
    String reason;
    RadioButton impersonation;
    RadioButton bullying;
    RadioButton links;
    RadioButton under13;
    RadioButton pfp;
    RadioButton hijack;
    String username;
    FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        db = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("username");
        setContentView(R.layout.reporting);
        Button guidelines = findViewById(R.id.guidelines);
        ImageView back = findViewById(R.id.backtouser);
        if(getIntent().getStringExtra("mode").equals("user")){
            username = getIntent().getStringExtra("username");
            impersonation = findViewById(R.id.radioButton);
            bullying = findViewById(R.id.radioButton3);
            links = findViewById(R.id.radioButton5);
            under13 = findViewById(R.id.radioButton4);
            pfp = findViewById(R.id.radioButton6);
            hijack = findViewById(R.id.radioButton2);
            Button report = findViewById(R.id.reportbutton);
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reportUser();
                }
            });
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Report.this, userProfile.class);
                    intent.putExtra("user", username);
                    startActivity(intent);
                }
            });
            guidelines.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Report.this, Guidelines.class);
                    intent.putExtra("mode", "user");
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
            });
        }
        if(getIntent().getStringExtra("mode").equals("release")){
            TextView txt = findViewById(R.id.reportUser);
            txt.setText("report release");
            impersonation = findViewById(R.id.radioButton);
            impersonation.setText("duplicate of other release");
            bullying = findViewById(R.id.radioButton3);
            bullying.setText("release art violates community guidelines");
            links = findViewById(R.id.radioButton5);
            links.setText("release does not exist");
            under13 = findViewById(R.id.radioButton4);
            under13.setText("release includes links in title");
            pfp = findViewById(R.id.radioButton6);
            pfp.setVisibility(View.GONE);
            hijack = findViewById(R.id.radioButton2);
            hijack.setVisibility(View.GONE);
            Button report = findViewById(R.id.reportbutton);
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reportRelease();
                }
            });
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Report.this, MainApp.class);
                    startActivity(intent);
                }
            });
            guidelines.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Report.this, Guidelines.class);
                    intent.putExtra("mode", "release");
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
            });
        }
    }
    void reportRelease(){
        Random rand = new Random();
        if(impersonation.isChecked()){
            reason = "duplicate";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(bullying.isChecked()){
            reason = "art";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(links.isChecked()){
            reason = "existence";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(under13.isChecked()){
            reason = "links";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        Toast.makeText(Report.this, "your report has been filed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Report.this, MainApp.class);
        startActivity(intent);
    }
    void reportUser(){
        Random rand = new Random();
        if(impersonation.isChecked()){
            reason = "impersonation";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(bullying.isChecked()){
            reason = "bullying";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(links.isChecked()){
            reason = "postinglinks";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(under13.isChecked()){
            reason = "under13";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(pfp.isChecked()){
            reason = "pfp";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        if(hijack.isChecked()){
            reason = "hijacked";
            CollectionReference cr = db.collection("reports");
            DocumentReference reportRef = cr.document(username + " " + user.getDisplayName());
            Map<String, Object> data = new HashMap<>();
            data.put("reason "+rand.nextInt(100), reason);
            reportRef.set(data, SetOptions.merge());
        }
        Toast.makeText(Report.this, "your report has been filed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Report.this, userProfile.class);
        intent.putExtra("user", username);
        startActivity(intent);
    }
}