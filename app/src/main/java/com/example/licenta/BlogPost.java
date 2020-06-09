package com.example.licenta;

import java.util.Date;

public class BlogPost extends BlogPostID {
    public String user_id, image_url, text, thumb_url, category;
    public Date timestamp;


    public BlogPost(){

    }

    public BlogPost(String user_id, String image_url, String desc, String thumb_url, Date timestamp, String category) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.text = desc;
        this.thumb_url = thumb_url;
        this.timestamp = timestamp;
        this.category = category;

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return text;
    }

    public void setDesc(String desc) {
        this.text = desc;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
