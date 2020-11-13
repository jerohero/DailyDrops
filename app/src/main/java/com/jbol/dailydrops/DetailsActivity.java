package com.jbol.dailydrops;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.FileService;

import java.time.format.FormatStyle;

public class DetailsActivity extends AppCompatActivity {

    private TextView tv_title, tv_date;
    private Button btn_delete, btn_edit, btn_back;
    private ImageView iv_image;

    private GlobalDropModel drop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        drop = (GlobalDropModel) intent.getSerializableExtra("drop");

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(drop.getTitle());

        initializeImage();
        initializeBackBtn();
        initializeDeleteBtn();
        initializeEditBtn();
        initializeDate();
    }

    private void initializeImage() {
        iv_image = findViewById(R.id.iv_image);

        if (drop.getImage() == null) {
            return;
        }

        Bitmap image = null;
        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) {
            image = FileService.loadImageFromStorage(this, Integer.parseInt(drop.getImage()));
        }
        iv_image.setImageBitmap(image);
    }

    private void initializeDate() {
        tv_date = findViewById(R.id.tv_date);

        tv_date.setText(DateService.EpochMilliToDateString(drop.getDate(), FormatStyle.FULL));
    }

    // Can only be executed if it's a local drop
    private void initializeDeleteBtn() {
        btn_delete = findViewById(R.id.btn_delete);

        btn_delete.setOnClickListener(v -> {
            SQLiteDataBaseHelper sqldbHelper = SQLiteDataBaseHelper.getHelper(DetailsActivity.this);
            boolean success = sqldbHelper.deleteDrop(Integer.parseInt(drop.getId()));
            FileService.deleteImageFromStorage(this, Integer.parseInt(drop.getId()));

            Toast.makeText(DetailsActivity.this, "Success= " + success, Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeBackBtn() {
        btn_back = findViewById(R.id.btn_back);

        btn_back.setOnClickListener(v -> {
            Intent i = new Intent(DetailsActivity.this, MainActivity.class);
            DetailsActivity.this.startActivity(i);
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
