package com.example.videoplayer;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class testClaass {
    public static String EXTRA_VIDEO_KEY = "3242";
    Context context;
    RecyclerView recyclerView = new RecyclerView(context);

    public void setContext(Context context) {
        this.context = context;
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
