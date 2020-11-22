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
import com.jbol.dailydrops.database.FirebaseDatabaseHelper;
import com.jbol.dailydrops.database.SQLiteDatabaseHelper;
import com.jbol.dailydrops.models.FirebaseDropModel;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.BarChartDecimalFormatter;
import com.jbol.dailydrops.services.DateService;

import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;

public class StatisticsActivity extends AppCompatActivity {
    private ArrayList<GlobalDropModel> dropModels = new ArrayList<>();
    private ArrayList<FirebaseDropModel> firebaseDropModels = new ArrayList<>();
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
            String date = DateService.epochMilliToDefTimeZoneDDMM(now + (day * i));
            int dropsCount = findDropsForDay(DateService.epochMilliToDefTimeZoneDDMMYYYY(now + (day * i)));
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
            String dropDate = DateService.epochMilliToDefTimeZoneDDMMYYYY(drop.getDate() + drop.getTime());;
            if (dropDate.equals(day)) {
                dropCount = dropCount + 1;
            }
        }
        return dropCount;
    }

    private void initializeDrops() {
        ArrayList<SQLiteDropModel> sqLiteDropModels = (ArrayList<SQLiteDropModel>) SQLiteDatabaseHelper.getHelper(this).getAllDropsFromLocal();

        for (SQLiteDropModel sqLiteDropModel : sqLiteDropModels) {
            dropModels.add(new GlobalDropModel(sqLiteDropModel));
        }
        for (FirebaseDropModel firebaseDrop : firebaseDropModels) {
            dropModels.add(new GlobalDropModel(firebaseDrop));
        }

        visualizeData();
    }

    private void initializeFirebase() {
        DatabaseReference fbDropsReference = FirebaseDatabase.getInstance().getReference();

        fbDropsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    FirebaseDatabaseHelper fbHelper = new FirebaseDatabaseHelper();
                    firebaseDropModels = fbHelper.collectFirebaseDrops(dataSnapshot.getValue());
                }
                initializeDrops();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("dev", "Failed to read value.", error.toException());
            }
        });
    }

    private void initializeBackBtn() {
        ImageView iv_back_btn = findViewById(R.id.iv_back_btn);

        iv_back_btn.setOnClickListener(v ->
                super.onBackPressed());
    }

}
