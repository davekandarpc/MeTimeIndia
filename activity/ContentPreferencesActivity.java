package com.metime.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;

import com.metime.R;
import com.metime.adapter.VideoLanguageAdapter;
import com.metime.model.VideoLanguagesModel;

import java.util.ArrayList;

public class ContentPreferencesActivity extends Activity {

    private RecyclerView recyclerViewlanguage;
    private RecyclerView.LayoutManager manager;
    private RecyclerView.Adapter mAdepter;
    public static String LANLIST []={
            "English",
            "Hindi",
            "Gujarati",
            "Marathi"
    };
    ArrayList<VideoLanguagesModel> languages_list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preferences);

        recyclerViewlanguage = (RecyclerView) findViewById(R.id.recyclerViewlanguages);


        for(int i = 0 ; i < LANLIST.length ; i++){
            VideoLanguagesModel videoLanguagesModel = new VideoLanguagesModel();
            videoLanguagesModel.setLanguageName(LANLIST[i]);
            languages_list.add(videoLanguagesModel);
        }
        recyclerViewlanguage.setHasFixedSize(true);
        manager = new LinearLayoutManager(getApplicationContext());
        recyclerViewlanguage.setLayoutManager(manager);
        mAdepter = new VideoLanguageAdapter(getApplicationContext(), languages_list);
        recyclerViewlanguage.setAdapter(mAdepter);

    }
}