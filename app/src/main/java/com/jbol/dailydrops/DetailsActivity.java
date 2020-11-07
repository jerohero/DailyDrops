package com.jbol.dailydrops;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jbol.dailydrops.database.DataBaseHelper;
import com.jbol.dailydrops.models.DropModel;
import com.jbol.dailydrops.services.DateService;
import java.time.format.FormatStyle;

public class DetailsActivity extends AppCompatActivity {

    private TextView tv_title, tv_date;
    private Button btn_delete, btn_edit;

    private DropModel drop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        drop = (DropModel) intent.getSerializableExtra("drop");

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(drop.getTitle());

        initializeDeleteBtn();
        initializeEditBtn();
        initializeDate();
    }

    private void initializeDate() {
        tv_date = findViewById(R.id.tv_date);

        tv_date.setText(DateService.EpochMilliToDateString(drop.getDate(), FormatStyle.FULL));
    }

    private void initializeDeleteBtn() {
        btn_delete = findViewById(R.id.btn_delete);

        btn_delete.setOnClickListener(v -> {
            DataBaseHelper dataBaseHelper = DataBaseHelper.getHelper(DetailsActivity.this);
            boolean success = dataBaseHelper.deleteDrop(drop);

            Toast.makeText(DetailsActivity.this, "Success= " + success, Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeEditBtn() {
        btn_edit = findViewById(R.id.btn_edit);

        btn_edit.setOnClickListener(v -> {
            Intent i = new Intent(DetailsActivity.this, EditActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("drop", drop);
            DetailsActivity.this.startActivity(i);
        });
    }
}
