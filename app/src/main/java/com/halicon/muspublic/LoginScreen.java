package com.halicon.muspublic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import android.net.Uri;


import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private EditText username;
    public static Context AppContext;
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        getSupportActionBar().hide();
        AppContext = getApplicationContext();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginScreen.this, MainApp.class));
            finish();
        }
        setContentView(R.layout.login_screen);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPass);
        Button register = findViewById(R.id.createacc);
        username = findViewById(R.id.txtUsername);
        register.setOnClickListener(v -> {
            if(username.getText().toString().length() > 20){
                Toast.makeText(LoginScreen.this, "username is too long", Toast.LENGTH_SHORT).show();
            }else{
                registerAccount();
            }
        });
        Button login = findViewById(R.id.signin);
        login.setOnClickListener(v1 -> {
            login();
        });
        Button tos = findViewById(R.id.tos);
        Button priv = findViewById(R.id.privacy);
        tos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://pages.flycricket.io/mus-yrd/terms.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        priv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://pages.flycricket.io/mus-yrd/privacy.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
    void login() {
        String getEmail = email.getText().toString();
        String getPassword = password.getText().toString();
        if (getEmail.isEmpty()) {
            Toast.makeText(LoginScreen.this, "email cannot be empty!", Toast.LENGTH_LONG).show();
        } else {
            if (getPassword.equals("")) {
                Toast.makeText(LoginScreen.this, "password cannot be empty!", Toast.LENGTH_LONG).show();
            } else {
                mAuth.signInWithEmailAndPassword(getEmail, getPassword)
                        .addOnSuccessListener(authResult -> {
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            String name = user.getDisplayName();
                            db.collection("users")
                                    .document(name)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot content = task.getResult();
                                        if (content.exists()) {
                                            String banned = content.getString("banned");
                                            if (!banned.equals("true")) {
                                                Toast.makeText(LoginScreen.this, "login to " + name + " successful!", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(LoginScreen.this, MainApp.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(LoginScreen.this, "user has been banned", Toast.LENGTH_LONG).show();
                                                FirebaseAuth.getInstance().signOut();
                                            }
                                        }
                                    }
                                }
                            });
                        }).addOnFailureListener(e -> Toast.makeText(LoginScreen.this, "error logging in: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }
    }
    void registerAccount() {
        String getUsername = username.getText().toString();
        String getEmail = email.getText().toString();
        String getPassword = password.getText().toString();
        if (getEmail.isEmpty()) {
            Toast.makeText(LoginScreen.this, "email cannot be empty!", Toast.LENGTH_LONG).show();
        } else {
            if (getPassword.equals("")) {
                Toast.makeText(LoginScreen.this, "password cannot be empty!", Toast.LENGTH_LONG).show();
            } else {
                if (getUsername.equals("")) {
                    Toast.makeText(LoginScreen.this, "username cannot be empty!", Toast.LENGTH_LONG).show();
                } else {
                    db.collection("users")
                            .document(getUsername)
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> user) {
                            if (user.isSuccessful()) {
                                DocumentSnapshot content = user.getResult();
                                if (content.exists()) {
                                    Toast.makeText(LoginScreen.this, "username already in use!", Toast.LENGTH_LONG).show();
                                } else {
                                    mAuth.createUserWithEmailAndPassword(getEmail, getPassword)
                                            .addOnSuccessListener(authResult -> {
                                                db = FirebaseFirestore.getInstance();
                                                Toast.makeText(LoginScreen.this, "account " + getUsername + " created!", Toast.LENGTH_SHORT).show();
                                                createProfile(getUsername, authResult.getUser());
                                                login();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(LoginScreen.this, "error creating account: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                }
                            }
                        }
                    });
                }
            }
        }
    }
    void createProfile(String getUsername, FirebaseUser user){
        CollectionReference cr = db.collection("users");
        DocumentReference meRef = cr.document(getUsername);
        Map<String, Object> data = new HashMap<>();
        data.put("following", getUsername);
        data.put("blocked", getUsername);
        data.put("followers", getUsername);
        meRef.set(data, SetOptions.merge());
        meRef.update("following", FieldValue.arrayUnion(getUsername));
        meRef.update("blocked", FieldValue.arrayRemove(getUsername));
        meRef.update("followers", FieldValue.arrayRemove(getUsername));
        meRef.update("banned", "false");
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(getUsername)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginScreen.this, "user profile " + user.getDisplayName() + " successfully created!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}