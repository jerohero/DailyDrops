package com.jbol.dailydrops;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.GlobalDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.ImageService;
import com.jbol.dailydrops.services.AsyncURLService;
import java.time.format.FormatStyle;

public class DetailsActivity extends AppCompatActivity {
    private TextView tv_title, tv_date, tv_note;
    private ImageButton ib_delete, ib_edit;
    private ImageView iv_image, iv_back_btn;
    private RelativeLayout cl_root;

    private GlobalDropModel drop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        drop = (GlobalDropModel) intent.getSerializableExtra("drop");

        cl_root = findViewById(R.id.cl_root);

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            cl_root.setBackgroundColor(getResources().getColor(R.color.colorOnline));
        } else {
            cl_root.setBackgroundColor(getResources().getColor(R.color.colorLocal));
        }

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(drop.getTitle());

        tv_note = findViewById(R.id.tv_note);
        tv_note.setText(drop.getNote());

        initializeBackBtn();
        initializeDate();
        initializeDeleteBtn();
        initializeEditBtn();
        initializeImage();
    }

    private void initializeImage() {
        iv_image = findViewById(R.id.iv_image);

        if (drop.getImage() == null) {
            iv_image.setVisibility(View.INVISIBLE);
            iv_image.setMaxHeight(100);
            return;
        }

        if (drop.getType().equals(GlobalDropModel.OFFLINE_TYPE)) { // Image is stored locally, so retrieve it from storage
            Bitmap image = ImageService.loadImageFromStorage(this, Integer.parseInt(drop.getImage()));
            iv_image.setImageBitmap(image);
        } else if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) { // Image is stored as a link, so retrieve it from internet
            new AsyncURLService(output -> iv_image.setImageBitmap(output)).execute(drop.getImage());
        }
    }

    private void initializeDate() {
        tv_date = findViewById(R.id.tv_date);

        tv_date.setText(DateService.EpochMilliToDateString(drop.getDate(), FormatStyle.FULL));
    }

    // Can only be executed if it's a local drop
    private void initializeDeleteBtn() {
        ib_delete = findViewById(R.id.ib_delete);

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_delete.setVisibility(View.GONE);
            return;
        }
        ib_delete.setOnClickListener(v -> {
            SQLiteDataBaseHelper sqldbHelper = SQLiteDataBaseHelper.getHelper(DetailsActivity.this);
            boolean success = sqldbHelper.deleteDrop(Integer.parseInt(drop.getId()));
            if (!success) {
                Toast.makeText(this, "Drop couldn't be deleted. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            ImageService.deleteImageFromStorage(this, Integer.parseInt(drop.getId()));
            Intent i = new Intent(DetailsActivity.this, MainActivity.class);
            DetailsActivity.this.startActivity(i);
        });
    }

    private void initializeBackBtn() {
        iv_back_btn = findViewById(R.id.iv_back_btn);

        iv_back_btn.setOnClickListener(v -> {
            Intent i = new Intent(DetailsActivity.this, MainActivity.class);
            DetailsActivity.this.startActivity(i);
        });
    }

    private void initializeEditBtn() {
        ib_edit = findViewById(R.id.ib_edit);

        if (drop.getType().equals(GlobalDropModel.ONLINE_TYPE)) {
            ib_edit.setVisibility(View.GONE);
            return;
        }
        ib_edit.setOnClickListener(v -> {
            Intent i = new Intent(DetailsActivity.this, AddUpdateActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("drop", drop);
            DetailsActivity.this.startActivity(i);
        });
    }
}
