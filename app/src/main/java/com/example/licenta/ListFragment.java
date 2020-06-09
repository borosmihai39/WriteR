package com.example.licenta;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements AsyncResponse{

    private TextView poetryFeed;
    private TextView horrorFeed;
    private TextView scifiFeed;
    private Button btn1;

    RetrieveFeedClass asyncTask =new RetrieveFeedClass();

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
//        btn1 = view.findViewById(R.id.test1);
        asyncTask.delegate = this;
        asyncTask.execute();

        poetryFeed = view.findViewById(R.id.poetryText);
        horrorFeed = view.findViewById(R.id.horrorText);
        scifiFeed = view.findViewById(R.id.scifiText);



        return view;

    }

    @Override
    public void processFinish(String[] output) {
        poetryFeed.setText(output[0]);
        horrorFeed.setText(output[1]);
        scifiFeed.setText(output[2]);
    }
}
