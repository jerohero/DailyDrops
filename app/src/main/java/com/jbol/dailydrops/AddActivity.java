package com.jbol.dailydrops;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.jbol.dailydrops.database.DataBaseHelper;
import com.jbol.dailydrops.models.DropModel;

public class AddActivity extends AppCompatActivity {

    Button btn_add;
    EditText et_note, et_title;
    CalendarView cv_date;

    DataBaseHelper dataBaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        btn_add = findViewById(R.id.btn_add);
        et_title = findViewById(R.id.et_title);
        et_note = findViewById(R.id.et_note);
        cv_date = findViewById(R.id.cv_date);

        dataBaseHelper = DataBaseHelper.getHelper(AddActivity.this);

        initializeAddBtn();
    }

    private void initializeAddBtn() {
        btn_add.setOnClickListener(v -> {
            DropModel drop;
            try {
                drop = new DropModel(-1, et_title.getText().toString(), et_note.getText().toString(), cv_date.getDate());
                Toast.makeText(AddActivity.this, drop.toString(), Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(AddActivity.this, "Error creating drop", Toast.LENGTH_SHORT).show();
                drop = new DropModel(-1, "error", "error", 0L);
            }

            DataBaseHelper dataBaseHelper = DataBaseHelper.getHelper(AddActivity.this);

            boolean success = dataBaseHelper.addDrop(drop);

            Toast.makeText(AddActivity.this, "Success= " + success, Toast.LENGTH_SHORT).show();

        });
    }
}

//    private void showCustomersOnListView(DataBaseHelper dataBaseHelper2) {
//        customerArrayAdapter = new ArrayAdapter<CustomerModel>(MainActivity.this, android.R.layout.simple_list_item_1, dataBaseHelper2.getEveryone());
//        lv_customerList.setAdapter(customerArrayAdapter);
//    }
