package com.jbol.dailydrops;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jbol.dailydrops.database.DataBaseHelper;
import com.jbol.dailydrops.models.DropModel;
import com.jbol.dailydrops.services.DateService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {
    Button btn_save;
    EditText et_note, et_title, et_date;

    final Calendar dateCalendar = Calendar.getInstance();

    private DropModel drop;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    DataBaseHelper dataBaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        drop = (DropModel) intent.getSerializableExtra("drop");

        btn_save = findViewById(R.id.btn_save);
        et_title = findViewById(R.id.et_title);
        et_note = findViewById(R.id.et_note);

        dataBaseHelper = DataBaseHelper.getHelper(EditActivity.this);

        initializeValues();
        initializeDatePicker();
        initializeSaveBtn();
    }

    private void initializeValues() {
        et_title.setText(drop.getTitle());
        et_note.setText(drop.getNote());
//        et_date.setText(DateService.EpochMilliToDateString(drop.getDate(), FormatStyle.SHORT));
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
                new DatePickerDialog(EditActivity.this, date, dateCalendar.get(Calendar.YEAR),
                        dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH))
                        .show());
    }

    private void initializeSaveBtn() {
        btn_save.setOnClickListener(v -> {
            try {
                drop.setTitle(et_title.getText().toString());
                drop.setNote(et_note.getText().toString());
                drop.setDate(DateService.dateStringToEpochMilli(EditActivity.this, et_date.getText().toString()));


                Toast.makeText(EditActivity.this, drop.toString(), Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(EditActivity.this, "Error updating drop", Toast.LENGTH_SHORT).show();
//                drop = new DropModel(-1, "error", "error", 0L);
            }

            DataBaseHelper dataBaseHelper = DataBaseHelper.getHelper(EditActivity.this);

            boolean success = dataBaseHelper.addDrop(drop);

            Toast.makeText(EditActivity.this, "Success= " + success, Toast.LENGTH_SHORT).show();

        });
    }
}
