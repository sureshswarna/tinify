package uk.ac.tees.aad.W9484387;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText email;
    EditText password;
    EditText name;
    EditText mobile;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

         name = findViewById(R.id.name);
        email =  findViewById(R.id.email);
        password = findViewById(R.id.password);
        mobile = findViewById(R.id.mobiler);
        Button button = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                validateandSignup();
            }
        });
    }

    private void validateandSignup() {
        if(!validateINputs())
        {
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            FirebaseDatabase.getInstance().getReference("users").push();
                            User userT =  new User();
                            userT.setEmail(email.getText().toString());
                            userT.setMobile(mobile.getText().toString());
                            userT.setName(name.getText().toString());

                            FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).push().setValue(userT).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                  Toast.makeText(getApplicationContext(),"Registered Successful",Toast.LENGTH_LONG).show();
                                  startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    public boolean validateINputs()
    {
        if(email.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())
        {
            email.setError("Enter valid value");
            return false;
        }
        if(password.getText().toString().length() < 4){
            password.setError("enter valid length password");
            return false;}
        if(mobile.getText().toString().length() < 10){
        mobile.setError("enter valid length password");
        return false;}
        if(name.getText().toString().length() < 4){
            name.setError("enter valid length password");
            return false;}

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}
