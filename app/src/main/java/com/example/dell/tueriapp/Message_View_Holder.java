package com.example.dell.tueriapp;

import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class Message_View_Holder extends RecyclerView.ViewHolder {

    View myView;
    CardView cardView_User, cardView_Admin, cardView_UserImage;
    TextView user, message, admin, adminMessage, imageUser;
    ImageView imageView;

    public Message_View_Holder(View itemView) {
        super(itemView);
        myView = itemView;

        user = myView.findViewById(R.id.user);
        imageUser = myView.findViewById(R.id.userofImage);
        message = myView.findViewById(R.id.message);
        admin = myView.findViewById(R.id.admin);
        adminMessage = myView.findViewById(R.id.adminMessage);
        imageView = myView.findViewById(R.id.imageSent);
        cardView_User = myView.findViewById(R.id.cardViewUser);
        cardView_Admin = myView.findViewById(R.id.cardViewAdmin);
        cardView_UserImage = myView.findViewById(R.id.CardViewforImage);

    }

    public void setImageUser(String imageUser) {
        this.imageUser.setText(imageUser);
    }

    public void setImageView(String imageView) {

        Picasso.get().load(imageView).fit().centerCrop().into(this.imageView);

    }

    public void setUser(String user) {
        this.user.setText(user);
    }


    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setAdmin(String admin) {
        this.admin.setText(admin);
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage.setText(adminMessage);
    }

    public void VisibiltyForCardView_User(Boolean flag)
    {

        if (flag)
        {
            cardView_User.setVisibility(View.VISIBLE);
        }
        else
        {
            cardView_User.setVisibility(View.GONE);

        }

    }

    public void VisibiltyForCardView_Admin(Boolean flag)
    {

        if (flag)
        {
            cardView_Admin.setVisibility(View.VISIBLE);
        }
        else
        {
            cardView_Admin.setVisibility(View.GONE);

        }

    }

    public void VisibiltyForCardView_UserImage(Boolean flag)
    {

        if (flag)
        {
            cardView_UserImage.setVisibility(View.VISIBLE);
        }
        else
        {
            cardView_UserImage.setVisibility(View.GONE);

        }

    }

}
