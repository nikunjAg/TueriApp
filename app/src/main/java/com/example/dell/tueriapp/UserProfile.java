package com.example.dell.tueriapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class UserProfile extends AppCompatActivity {

    private static final int PICK_IMAGE = 123;
    private ImageView userImage;
    private EditText Username;
    private Button submit;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private Uri uri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null)
        {

            uri = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                userImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userImage = findViewById(R.id.userImage);
        Username = findViewById(R.id.username);
        submit = findViewById(R.id.Submit);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Get Image"),PICK_IMAGE);

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = Username.getText().toString().trim();
                if (!TextUtils.isEmpty(username))
                {
                    submitDetails(username);
                }
                else {
                    Toast.makeText(UserProfile.this, "Please enter the Username", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void submitDetails(String username) {

        final ProgressDialog progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Uploading Data ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if(firebaseAuth.getCurrentUser() != null) {
            DatabaseReference databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("Username");

            databaseReference.setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {

                        progressDialog.dismiss();
                        Toast.makeText(UserProfile.this, "Data Uploaded", Toast.LENGTH_LONG).show();

                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(UserProfile.this, "Error " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                }
            });

            progressDialog.setMessage("Storing Image");
            progressDialog.setCancelable(false);
            progressDialog.show();

            StorageReference storageReference1 = storageReference.child(firebaseAuth.getCurrentUser().getUid()).child("Images").child("Profile Pic");
            UploadTask uploadTask = storageReference1.putFile(uri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    Toast.makeText(UserProfile.this, "Error , Unable To Upload The Image " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();
                    Toast.makeText(UserProfile.this, "Image Uploaded Successfully ", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(UserProfile.this, ChatWithAdmin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }
            });

        }
        else {
            Toast.makeText(this, "Login First", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(UserProfile.this, Login.class));
        }

    }
}
