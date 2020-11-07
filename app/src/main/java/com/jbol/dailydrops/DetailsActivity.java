package com.jbol.dailydrops;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jbol.dailydrops.database.DataBaseHelper;
import com.jbol.dailydrops.models.DropModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity {

    private TextView tv_title, tv_date;
    private Button btn_delete_drop;

    private DropModel drop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        drop = (DropModel) intent.getSerializableExtra("item");

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(drop.getTitle());

        initializeDeleteBtn();
        initializeDate();
    }

    private void initializeDate() {
        Log.d("dev", "Date: " + drop.getDate());

        tv_date = findViewById(R.id.tv_date);

        Instant instant = Instant.ofEpochMilli(drop.getDate());

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, MainActivity.getZoneId());

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);

        tv_date.setText(zdt.format(formatter));
    }

    private void initializeDeleteBtn() {
        btn_delete_drop = findViewById(R.id.btn_delete_drop);

        btn_delete_drop.setOnClickListener(v -> {
            DataBaseHelper dataBaseHelper = DataBaseHelper.getHelper(DetailsActivity.this);
            boolean success = dataBaseHelper.deleteDrop(drop);

            Toast.makeText(DetailsActivity.this, "Success= " + success, Toast.LENGTH_SHORT).show();
        });
    }
}
