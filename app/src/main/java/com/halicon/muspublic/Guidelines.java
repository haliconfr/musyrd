package com.halicon.muspublic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Guidelines extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comguidelines);
        getSupportActionBar().hide();
        ImageView back = findViewById(R.id.backtouser2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Guidelines.this, Report.class);
                intent.putExtra("mode", getIntent().getStringExtra("mode"));
                intent.putExtra("username", getIntent().getStringExtra("username"));
                startActivity(intent);
            }
        });
    }
}
