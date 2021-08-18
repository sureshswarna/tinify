package uk.ac.tees.aad.W9484387;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IntroScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        Button read = findViewById(R.id.read);

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("intro", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor  = sharedPreferences.edit();
                editor.putString("played","yes");
                editor.apply();
                editor.commit();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });


    }
}
