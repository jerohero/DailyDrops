package com.jbol.dailydrops;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jbol.dailydrops.database.SQLiteDatabaseHelper;
import com.jbol.dailydrops.models.FirebaseDropList;
import com.jbol.dailydrops.models.FirebaseDropModel;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.BarChartDecimalFormatter;
import com.jbol.dailydrops.services.DateService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    private ArrayList<GlobalDropModel> dropModels = new ArrayList<>();
    private DatabaseReference fbDropsReference;
    private ArrayList<FirebaseDropModel> firebaseDropModels = new ArrayList<>();
    private ArrayList<SQLiteDropModel> sqLiteDropModels = new ArrayList<>();

    private ArrayList<BarEntry> dropData = new ArrayList<>();

    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        chart = findViewById(R.id.barchart);

        initializeBackBtn();

        startProcess();
    }

    private void startProcess() {
        initializeFirebase();
    }

    private void visualizeData() {
        ArrayList<String> days = new ArrayList<>();

        long day = DateService.getDayInEpochMilli();
        long now = DateService.getNowInEpochMilli();
        for (int i = 0; i < 14; i++) {
            String date = DateService.epochMilliToDDMM(now + (day * i));
            int dropsCount = findDropsForDay(DateService.epochMilliToFullDateString(now + (day * i)));
            dropData.add(new BarEntry(dropsCount, i));
            days.add(date);
        }

        BarDataSet bardataset = new BarDataSet(dropData, "Drops");
        chart.animateY(1500);
        BarData data = new BarData(days, bardataset);

        data.setValueTextSize(13f);
        chart.getLegend().setTextSize(13f);
        bardataset.setColors(ColorTemplate.createColors(new int[]{ getResources().getColor(R.color.colorPrimary) }));

        chart.setData(data);
        data.setValueFormatter(new BarChartDecimalFormatter());
    }

    private int findDropsForDay(String day) {
        int dropCount = 0;
        for (GlobalDropModel drop : dropModels) {
            String dropDate = DateService.epochMilliToFullDateString(drop.getDate());
            if (dropDate.equals(day)) {
                dropCount = dropCount + 1;
            }
        }
        return dropCount;
    }

    private void initializeDrops() {
        sqLiteDropModels = (ArrayList<SQLiteDropModel>) SQLiteDatabaseHelper.getHelper(this).getAllDropsFromLocal();

        for (SQLiteDropModel sqLiteDropModel : sqLiteDropModels) {
            dropModels.add(new GlobalDropModel(sqLiteDropModel));
        }
        for (FirebaseDropModel firebaseDrop : firebaseDropModels) {
            dropModels.add(new GlobalDropModel(firebaseDrop));
        }

        visualizeData();
    }

    private void initializeFirebase() {
        fbDropsReference = FirebaseDatabase.getInstance().getReference();

        fbDropsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    firebaseDropModels = collectFirebaseDrops(dataSnapshot.getValue());
                }
                initializeDrops();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("dev", "Failed to read value.", error.toException());
            }
        });
    }

    private ArrayList<FirebaseDropModel> collectFirebaseDrops(Object snapshotValue) {
        ArrayList<FirebaseDropModel> drops = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(snapshotValue);
        FirebaseDropList dropList = gson.fromJson(jsonElement, FirebaseDropList.class);

        Iterator<Map.Entry<String, FirebaseDropModel>> it = dropList.getDrops().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, FirebaseDropModel> pair = it.next();
            FirebaseDropModel drop = pair.getValue();
            drop.setId(pair.getKey());
            drops.add(drop);
            it.remove();
        }

        return drops;
    }

    private void initializeBackBtn() {
        ImageView iv_back_btn = findViewById(R.id.iv_back_btn);

        iv_back_btn.setOnClickListener(v ->
                super.onBackPressed());
    }
}
