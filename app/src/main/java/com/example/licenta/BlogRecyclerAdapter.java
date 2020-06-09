package com.example.licenta;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostID;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);
        String image_url = blog_list.get(position).getImage_url();

        String thumbUri = blog_list.get(position).getThumb_url();
        holder.setBlogImage(image_url, thumbUri);

        String categoryText = blog_list.get(position).getCategory();
        holder.setCategory(categoryText);



        String user_id = blog_list.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName,userImage);


                } else {
                    //error toast
                }
            }
        });

        long milliseconds = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.getDateInstance().format(milliseconds);
        holder.setDate(dateString);


        //get likes number
        firebaseFirestore.collection("Posts/" +blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e==null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int count = queryDocumentSnapshots.size();
                        holder.updateLikesCount(count);
                    } else {
                        holder.updateLikesCount(0);
                    }
                }
            }
        });

        //get likes
        firebaseFirestore.collection("Posts/" +blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if(e == null) {

                    if (documentSnapshot.exists()) {
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.baseline_favorite_black_18dp));
                    } else {
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.baseline_favorite_border_black_18dp));

                    }
                }
            }
        });


        //Likes
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" +blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            if(task.isSuccessful()) {
                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                likesMap.put("index", 0);
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                                firebaseFirestore.collection("Posts/" +blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if(e==null) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                int count = queryDocumentSnapshots.size();
                                                holder.updateLikesCount(count);
                                                Map<String, Object> likesMap = new HashMap<>();
                                                likesMap.put("currentNoOfLikes",count);
                                                firebaseFirestore.collection("Posts").document(blogPostId).update(likesMap);
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            firebaseFirestore.collection("Posts/" +blogPostId + "/Likes").document(currentUserId).delete();
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if(e==null) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            Map<String, Object> likesMap = new HashMap<>();
                                            likesMap.put("currentNoOfLikes",0);
                                            firebaseFirestore.collection("Posts").document(blogPostId).update(likesMap);
                                        }
                                    }


                                }
                            });

                        }
                    }
                });

            }
        });
    }


    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private TextView categoryText;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);

        }
        public void setDescText(String descText){
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }
        public void setBlogImage(String downloadUri, String thumbUri) {
            blogImageView = mView.findViewById(R.id.blog_image);
            Glide.with(context).load(downloadUri).thumbnail(Glide.with(context).load(thumbUri)).into(blogImageView);

        }
        public void setDate(String date){
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }
        public void setUserData(String name, String image){
            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.baseline_panorama_fish_eye_black_18dp);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);
        }

        public void updateLikesCount(int count){
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            if(count == 1) {
                blogLikeCount.setText(count + " Like");
            } else {
                blogLikeCount.setText(count + " Likes");
            }
        }

        public void setCategory(String catTxt) {
            categoryText = mView.findViewById(R.id.category_text);
            categoryText.setText("Posted in "+catTxt);
        }


    }


}
