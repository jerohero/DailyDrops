package com.jbol.dailydrops;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.jbol.dailydrops.database.DataBaseHelper;
import com.jbol.dailydrops.models.DropModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    Button btn_add;
    EditText et_note, et_title, et_date;
    CalendarView cv_date;

    final Calendar dateCalendar = Calendar.getInstance();

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

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

        initializeDatePicker();
        initializeAddBtn();
    }

    private void initializeDatePicker() {
        et_date = findViewById(R.id.et_date);
        DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
            dateCalendar.set(Calendar.YEAR, year);
            dateCalendar.set(Calendar.MONTH, month);
            dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            et_date.setText(sdf.format(dateCalendar.getTime()));
        };

        et_date.setOnClickListener(v ->
                new DatePickerDialog(AddActivity.this, date, dateCalendar.get(Calendar.YEAR),
                        dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH))
                        .show());
    }

    private long dateStringToEpochMilli(String dateString) {
        try {
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : 0L;
        } catch (Exception e) {
            Toast.makeText(AddActivity.this, "An error occurred while parsing the date. Please try again.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return 0L;
    }

    private void initializeAddBtn() {
        btn_add.setOnClickListener(v -> {
            DropModel drop;
            try {
                drop = new DropModel(
                        -1, et_title.getText().toString(), et_note.getText().toString(), dateStringToEpochMilli(et_date.getText().toString()));
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
