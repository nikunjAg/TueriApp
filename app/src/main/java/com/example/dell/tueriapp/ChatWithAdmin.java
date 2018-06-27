package com.example.dell.tueriapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatWithAdmin extends AppCompatActivity {

    private static final int PICK_IMAGE = 123;
    private Toolbar mToolbar;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private EditText input;
    private ImageView chatImage;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private Uri uri;
    private Uri DownloadedUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null)
        {

            final ProgressDialog progressDialog1 = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
            progressDialog1.setMessage("Sending Image ...");
            progressDialog1.show();
            progressDialog1.setCancelable(false);

            uri = data.getData();

            // Compressing the image to a small size and then uploading it to storage

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,25,bAOS);
                byte[] byteArray = bAOS.toByteArray();


                final StorageReference storageReference1 = storageReference.child(firebaseAuth.getCurrentUser().getUid()).child("Images").child("Chat Images").child(UUID.randomUUID().toString() + ".jpg");
                UploadTask uploadTask = storageReference1.putBytes(byteArray);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog1.dismiss();
                        Toast.makeText(ChatWithAdmin.this, "Unable To Send The Image " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog1.dismiss();
                        Toast.makeText(ChatWithAdmin.this, "Image Sent Successfully", Toast.LENGTH_SHORT).show();

                        // now getting the url of the image and saving it to database
                        storageReference1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                DownloadedUri = uri;
                                sendImageToDatabase();

                            }
                        });

                    }
                });




            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageToDatabase() {

        ProgressDialog progressDialog = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Sending Image ...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ChatModelClass chatModelClass = new ChatModelClass(firebaseAuth.getCurrentUser().getEmail(), DownloadedUri);
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("Messages");
        databaseReference.push().setValue(chatModelClass);
        Admin_Message admin_message = new Admin_Message("admin","Waiting For Reply...");
        databaseReference.push().setValue(admin_message);
        progressDialog.dismiss();
        input.setText("");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_admin);

        mToolbar = findViewById(R.id.toolbar);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        recyclerView = findViewById(R.id.rectclerView);
        input = findViewById(R.id.inputText);
        chatImage = findViewById(R.id.image);

        setSupportActionBar(mToolbar);

        linearLayoutManager = new LinearLayoutManager(ChatWithAdmin.this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        if (firebaseAuth.getCurrentUser() != null)
        {

            // sending chat text message to firebase database

            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ChatModelClass chatModelClass = new ChatModelClass(firebaseAuth.getCurrentUser().getEmail(),input.getText().toString().trim());
                    DatabaseReference databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("Messages");
                    databaseReference.push().setValue(chatModelClass);
                    Admin_Message admin_message = new Admin_Message("admin","Waiting For Reply...");
                    databaseReference.push().setValue(admin_message);
                    input.setText("");
                }
            });

            // setting on click listener on the send image button
            chatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Get The Image"),PICK_IMAGE);
                }
            });

            displayChatMessage();

        }
        else {
            Intent intent = new Intent(ChatWithAdmin.this,SignUp.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }

    private void displayChatMessage() {

        final ProgressDialog progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading Chat ...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        final DatabaseReference databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("Messages");

        FirebaseRecyclerOptions<ChatModelClass> options = new FirebaseRecyclerOptions.Builder<ChatModelClass>().setQuery(databaseReference,ChatModelClass.class).build();

        FirebaseRecyclerAdapter<ChatModelClass,Message_View_Holder> adapter = new FirebaseRecyclerAdapter<ChatModelClass, Message_View_Holder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final Message_View_Holder holder, int position, @NonNull ChatModelClass model) {

                final String id = getRef(position).getKey();

                recyclerView.smoothScrollToPosition(position);

                databaseReference.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        progressDialog.dismiss();
                        if (dataSnapshot.hasChild("Delete for") && dataSnapshot.hasChild("user") && dataSnapshot.hasChild("message"))
                        {
                            if (dataSnapshot.child("Delete for").getValue().toString().trim().equals(firebaseAuth.getCurrentUser().getEmail()))
                            {
                                holder.VisibiltyForCardView_User(false);
                                holder.VisibiltyForCardView_Admin(false);
                                holder.VisibiltyForCardView_UserImage(false);
                            }
                            else if (dataSnapshot.hasChild("user") && dataSnapshot.hasChild("message")) {
                                String User = dataSnapshot.child("user").getValue().toString().trim();
                                String message = dataSnapshot.child("message").getValue().toString().trim();
                                holder.setUser(User);
                                holder.setMessage(message);
                                holder.VisibiltyForCardView_User(true);
                                holder.VisibiltyForCardView_Admin(false);
                                holder.VisibiltyForCardView_UserImage(false);

                            }

                        }
                        else if (dataSnapshot.hasChild("user") && dataSnapshot.hasChild("message") && !dataSnapshot.hasChild("Delete for")) {
                            String User = dataSnapshot.child("user").getValue().toString().trim();
                            String message = dataSnapshot.child("message").getValue().toString().trim();
                            holder.setUser(User);
                            holder.setMessage(message);
                            holder.VisibiltyForCardView_User(true);
                            holder.VisibiltyForCardView_Admin(false);
                            holder.VisibiltyForCardView_UserImage(false);


                            // for the delete purpose of the chat message of the user

                            holder.cardView_User.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    PopupMenu popupMenu = new PopupMenu(ChatWithAdmin.this,v);
                                    MenuInflater Menuinflater = popupMenu.getMenuInflater();
                                    Menuinflater.inflate(R.menu.chatmenu,popupMenu.getMenu());

                                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {

                                            if (item.getItemId() == R.id.delete)
                                            {
                                                // do what you want to do
                                                DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("Users")
                                                        .child(firebaseAuth.getCurrentUser().getUid()).child("Messages")
                                                        .child(id);

                                                databaseReference1.child("Delete for").setValue(firebaseAuth.getCurrentUser().getEmail());

                                            }

                                            return true;
                                        }
                                    });
                                    popupMenu.show();
                                    }
                            });



                        }

                        if (dataSnapshot.hasChild("Delete for"))
                        {

                            if (dataSnapshot.child("Delete for").getValue().toString().trim().equals(firebaseAuth.getCurrentUser().getEmail())) {
                                holder.VisibiltyForCardView_Admin(false);
                                holder.VisibiltyForCardView_User(false);
                                holder.VisibiltyForCardView_UserImage(false);
                            }

                            else if (dataSnapshot.hasChild("admin") && dataSnapshot.hasChild("adminMessage"))
                            {

                                String admin = dataSnapshot.child("admin").getValue().toString().trim();
                                String adminMessage = dataSnapshot.child("adminMessage").getValue().toString().trim();
                                if (adminMessage.equals("Waiting For Reply...") && admin.equals("admin")) {
                                    holder.VisibiltyForCardView_Admin(false);
                                    holder.VisibiltyForCardView_User(false);
                                    holder.VisibiltyForCardView_UserImage(false);
                                } else {
                                    holder.setAdmin(admin);
                                    holder.setAdminMessage(adminMessage);
                                    holder.VisibiltyForCardView_Admin(true);
                                    holder.VisibiltyForCardView_User(false);
                                    holder.VisibiltyForCardView_UserImage(false);
                                }
                            }
                        }
                        else if (dataSnapshot.hasChild("admin") && dataSnapshot.hasChild("adminMessage") && !dataSnapshot.hasChild("Delete for"))
                        {

                            String admin = dataSnapshot.child("admin").getValue().toString().trim();
                            String adminMessage = dataSnapshot.child("adminMessage").getValue().toString().trim();
                            if (adminMessage.equals("Waiting For Reply...") && admin.equals("admin")) {
                                holder.VisibiltyForCardView_Admin(false);
                                holder.VisibiltyForCardView_User(false);
                                holder.VisibiltyForCardView_UserImage(false);
                            } else {
                                holder.setAdmin(admin);
                                holder.setAdminMessage(adminMessage);
                                holder.VisibiltyForCardView_Admin(true);
                                holder.VisibiltyForCardView_User(false);
                                holder.VisibiltyForCardView_UserImage(false);


                                // for the delete purpose of the chat message of the admin

                                holder.cardView_Admin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        PopupMenu popupMenu = new PopupMenu(ChatWithAdmin.this,v);
                                        MenuInflater Menuinflater = popupMenu.getMenuInflater();
                                        Menuinflater.inflate(R.menu.chatmenu,popupMenu.getMenu());

                                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {

                                                if (item.getItemId() == R.id.delete)
                                                {
                                                    // do what you want to do
                                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("Users")
                                                            .child(firebaseAuth.getCurrentUser().getUid()).child("Messages")
                                                            .child(id);

                                                    databaseReference1.child("Delete for").setValue(firebaseAuth.getCurrentUser().getEmail());
                                                }

                                                return true;
                                            }
                                        });
                                        popupMenu.show();

                                        }
                                });


                            }
                        }



                        if (dataSnapshot.hasChild("Delete for"))
                        {

                            if (dataSnapshot.child("Delete for").getValue().toString().trim().equals(firebaseAuth.getCurrentUser().getEmail()))
                            {
                                holder.VisibiltyForCardView_UserImage(false);
                                holder.VisibiltyForCardView_User(false);
                                holder.VisibiltyForCardView_Admin(false);
                            }
                            else if (dataSnapshot.hasChild("user") && dataSnapshot.hasChild("imageUrl")) {

                                String user = dataSnapshot.child("user").getValue().toString().trim();
                                String uri = dataSnapshot.child("imageUrl").getValue().toString().trim();
                                holder.setImageUser(user);
                                holder.setImageView(uri);
                                holder.VisibiltyForCardView_UserImage(true);
                                holder.VisibiltyForCardView_User(false);
                                holder.VisibiltyForCardView_Admin(false);

                            }
                        }
                        else if (dataSnapshot.hasChild("user") && dataSnapshot.hasChild("imageUrl") && !dataSnapshot.hasChild("Delete for")) {

                            String user = dataSnapshot.child("user").getValue().toString().trim();
                            String uri = dataSnapshot.child("imageUrl").getValue().toString().trim();
                            holder.setImageUser(user);
                            holder.setImageView(uri);
                            holder.VisibiltyForCardView_UserImage(true);
                            holder.VisibiltyForCardView_User(false);
                            holder.VisibiltyForCardView_Admin(false);


                            // for the delete purpose of the chat message of the image send by the user

                            holder.cardView_UserImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    PopupMenu popupMenu = new PopupMenu(ChatWithAdmin.this,v);
                                    MenuInflater Menuinflater = popupMenu.getMenuInflater();
                                    Menuinflater.inflate(R.menu.chatmenu,popupMenu.getMenu());

                                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {

                                            if (item.getItemId() == R.id.delete)
                                            {
                                                // do what you want to do
                                                DatabaseReference databaseReference1 = firebaseDatabase.getReference().child("Users")
                                                        .child(firebaseAuth.getCurrentUser().getUid()).child("Messages")
                                                        .child(id);

                                                databaseReference1.child("Delete for").setValue(firebaseAuth.getCurrentUser().getEmail());
                                            }

                                            return true;
                                        }
                                    });
                                    popupMenu.show();

                                }
                            });

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public Message_View_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
                return new Message_View_Holder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.chat_room_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout :
            {
                logout();
                break;
            }
        }

        return true;

    }

    private void logout() {

        AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.Theme_AppCompat);
        ab.setTitle("LOGOUT");
        ab.setMessage("Are You Sure, You Want To Logout ?");
        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
                firebaseAuth.signOut();
                Intent intent = new Intent(ChatWithAdmin.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });
        ab.setCancelable(false);
        ab.show();

    }
}
