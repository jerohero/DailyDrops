package com.jbol.dailydrops;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jbol.dailydrops.database.DataBaseHelper;
import com.jbol.dailydrops.models.DropModel;
import com.jbol.dailydrops.views.DropAdapter;
import com.jbol.dailydrops.views.DropClickListener;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    DataBaseHelper dataBaseHelper;

    private RecyclerView recyclerView;
    private DropAdapter adapter;
    private ArrayList<DropModel> dropModelArrayList;

    private static MainActivity instance;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        MainActivity.context = getApplicationContext();

        myRef.setValue("Hello, World!");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("dev", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("dev", "Failed to read value.", error.toException());
            }
        });

        dataBaseHelper = DataBaseHelper.getHelper(MainActivity.this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dropModelArrayList = new ArrayList<>();
        DropClickListener dropClickListener = new DropClickListener(this, recyclerView, dropModelArrayList);

        adapter = new DropAdapter(this, dropModelArrayList, dropClickListener);
        recyclerView.setAdapter(adapter);
        updateListData();

        Button btn_addOne = findViewById(R.id.btn_addOne);
        btn_addOne.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListData();
    }

    public static Context getContext() {
        return MainActivity.context.getApplicationContext();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void showDetails(DropModel item) {
        // Show details of clicked item
        Intent i = new Intent(MainActivity.getContext(), DetailsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("item", item);
        MainActivity.getContext().startActivity(i);
    }

    private void updateListData() {
        List<DropModel> drops = dataBaseHelper.getAllDrops();
        dropModelArrayList.clear();
        dropModelArrayList.addAll(drops);
//        customerClickListener.setCustomerModelArrayList(customerModelArrayList);
        adapter.notifyDataSetChanged();
    }
}