package com.example.licenta;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;

public class RetrieveFeedClass extends AsyncTask<String[],String[],String[]> {
    public AsyncResponse delegate = null;


    @Override
    protected String[] doInBackground(String[]... strings) {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://writerlicenta.co.uk/").get();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements info1 = doc.select("p#poetry");
        String infoPoetry = info1.text();
        Elements info2 = doc.select("p#horror");
        String infoHorror = info2.text();
        Elements info3 = doc.select("p#scifi");
        String infoScifi = info3.text();

        String result[] = {infoPoetry,infoHorror,infoScifi};
        return result;
    }

    @Override
    protected void onPostExecute(String[] result) {
        delegate.processFinish(result);
        System.out.println(result);
    }
}
