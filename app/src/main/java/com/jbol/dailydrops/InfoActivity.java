package com.jbol.dailydrops;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        initializeBackBtn();
    }

    private void initializeBackBtn() {
        ImageView iv_back_btn = findViewById(R.id.iv_back_btn);

        iv_back_btn.setOnClickListener(v ->
                super.onBackPressed());
    }
}
