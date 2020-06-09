package com.example.licenta;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view; //part12
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private FirebaseAuth firebaseAuth;
    private Boolean isFirstPageLoaded = true;
    private String current_user_id;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);


        blog_list_view = view.findViewById(R.id.blog_list_view);
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.genFC) {
           getFC();
        }
        else if (id==R.id.genHorror)
        {
            getHorror();
        } else if (id==R.id.genPoet){
            getPoetry();
        } else if (id==R.id.genScifi){
            getSciFi();
        }  else if (id==R.id.likeHL){
            getLikesHL();
        } else if (id==R.id.show_All){
            getAll();
        } else if (id==R.id.topHorror){
            getLikesHLHorror();
        } else if (id==R.id.topPoetry) {
            getLikesHLPoetry();
        } else if (id==R.id.topScifi){
            getLikesHLScifi();
        } else if (id==R.id.topFC){
            getLikesHLFC();
        } else if(id==R.id.genQRea){
            getQRea();
        } else if(id==R.id.topQRea){
            getLikesQRea();
        } else if(id==R.id.myPosts){
            getMyPosts();
        }

        return super.onOptionsItemSelected(item);
    }

    public void getAll(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
    }
    public void getHorror(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Horror").orderBy("timestamp", Query.Direction.DESCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getPoetry(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();



        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Poetry"); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();

    }
    public void getSciFi(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Sci-Fi"); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getFC(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Free creation"); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();

    }
    public void getQRea(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","QRea"); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getLikesHL(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("currentNoOfLikes", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getLikesHLHorror(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Horror").orderBy("currentNoOfLikes", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getLikesHLPoetry(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Poetry").orderBy("currentNoOfLikes", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getLikesHLScifi(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Sci-Fi").orderBy("currentNoOfLikes", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getLikesHLFC(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","Free creation").orderBy("currentNoOfLikes", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getLikesQRea(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("category","QRea").orderBy("currentNoOfLikes", Query.Direction.ASCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void getMyPosts(){
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //if we reach bottom
                    if(reachedBottom){

                        loadMore();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("user_id",current_user_id).orderBy("timestamp", Query.Direction.DESCENDING); //show newest posts first
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isFirstPageLoaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                blog_list.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    if (isFirstPageLoaded) {
                                        blog_list.add(blogPost);
                                    } else {
                                        blog_list.add(0, blogPost);
                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            isFirstPageLoaded = false;
                        }
                    }
                }
            });
        }
        blog_list.clear();
        blogRecyclerAdapter.notifyDataSetChanged();
    }
    public void loadMore(){
        Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible); //show newest posts first
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(e==null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                blog_list.add(blogPost);
                                blogRecyclerAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                }
            }
        });
    }

}
