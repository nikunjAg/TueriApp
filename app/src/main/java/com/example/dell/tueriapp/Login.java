package com.example.dell.tueriapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.oob.SignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText EmailId, Password;
    private Button login;
    private TextView Register;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EmailId = findViewById(R.id.userEmailId);
        Password = findViewById(R.id.userPassword);
        login = findViewById(R.id.userLogin);
        Register = findViewById(R.id.Register);

        firebaseAuth = FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, com.example.dell.tueriapp.SignUp.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myEmailId = EmailId.getText().toString().trim();
                String myPassword = Password.getText().toString().trim();
                login(myEmailId, myPassword);
            }
        });

    }

    private void login(String myEmailId, String myPassword) {

        final ProgressDialog progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(myEmailId, myPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    progressDialog.dismiss();
                    checkEmailVerification();
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void checkEmailVerification() {

        ProgressDialog progressDialog1 = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog1.setMessage("Logging In...");
        progressDialog1.show();
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser.isEmailVerified()){
            progressDialog1.dismiss();
            finish();
            Intent intent = new Intent(Login.this, UserProfile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else {
            progressDialog1.dismiss();
            Toast.makeText(this, "Please Do The Email Verification", Toast.LENGTH_LONG).show();
            firebaseAuth.signOut();
        }

    }
}
