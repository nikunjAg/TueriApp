package com.example.dell.tueriapp;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private EditText EmailId, Password, confirmPassword;
    private Button register;
    private TextView login;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EmailId = findViewById(R.id.EmailId);
        Password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

        register = findViewById(R.id.SignUpButton);

        login = findViewById(R.id.Login);

        firebaseAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SignUp.this, Login.class));

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String myEmailId = EmailId.getText().toString().trim();
                String myPassword = Password.getText().toString().trim();
                String myConfirmPassword = confirmPassword.getText().toString().trim();

                if(!TextUtils.isEmpty(myEmailId) && !TextUtils.isEmpty(myPassword) && !TextUtils.isEmpty(myConfirmPassword))
                {
                    if(myPassword.equals(myConfirmPassword))
                    {
                        registerUser(myEmailId, myPassword);
                    }
                    else
                    {
                        Toast.makeText(SignUp.this, "Both the passwords are not matching", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(SignUp.this, "Please Fill All The Fields", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void registerUser(String myEmailId, String myPassword) {

        final ProgressDialog progressDialog = new ProgressDialog(SignUp.this,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Registering ...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(myEmailId, myPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    sendEmailVerification();
                }
                else{

                    progressDialog.dismiss();
                    Toast.makeText(SignUp.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void sendEmailVerification() {

        final ProgressDialog progressDialog1 = new ProgressDialog(SignUp.this, ProgressDialog.STYLE_SPINNER);
        progressDialog1.setMessage("Sending Email Verification ...");
        progressDialog1.show();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        firebaseAuth.signOut();
                        progressDialog1.dismiss();
                        Toast.makeText(SignUp.this, "Verification Mail Sent", Toast.LENGTH_LONG).show();
                        Toast.makeText(SignUp.this, "Verify The Mail Sent to Proceed further", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignUp.this,Login.class));
                    }
                    else {
                        firebaseAuth.signOut();
                        progressDialog1.dismiss();
                        Toast.makeText(SignUp.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }
}
