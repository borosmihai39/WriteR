package com.example.licenta;


import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class BlogPostID {

    @Exclude
    public String BlogPostID;

    public <T extends BlogPostID> T withId(@NonNull final String id) {
        this.BlogPostID = id;
        return (T) this;
    }

}