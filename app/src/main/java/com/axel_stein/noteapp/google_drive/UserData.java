package com.axel_stein.noteapp.google_drive;

import android.net.Uri;

public class UserData {
    private Uri photo;
    private String name;
    private String email;

    UserData(Uri photo, String name, String email) {
        this.photo = photo;
        this.name = name;
        this.email = email;
    }

    public Uri getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}
