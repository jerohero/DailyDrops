package com.jbol.dailydrops;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.jbol.dailydrops.database.SQLiteDataBaseHelper;
import com.jbol.dailydrops.models.SQLiteDropModel;
import com.jbol.dailydrops.services.DateService;
import com.jbol.dailydrops.services.FileService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {
    Button btn_add, btn_add_image;
    EditText et_note, et_title, et_date;
    ImageView iv_image;

    final Calendar dateCalendar = Calendar.getInstance();

    //Request code gallery
    private static final int GALLERY_REQUEST = 9;
    //Request code for camera
    private static final int CAMERA_REQUEST = 11;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);

    SQLiteDataBaseHelper sqldbHelper;

    private Bitmap selectedImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        btn_add = findViewById(R.id.btn_save);
        et_title = findViewById(R.id.et_title);
        et_note = findViewById(R.id.et_note);

        sqldbHelper = SQLiteDataBaseHelper.getHelper(AddActivity.this);

        initializeDatePicker();
        initializeImage();
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

    private void initializeImage() {
        iv_image = findViewById(R.id.iv_image);
        btn_add_image = findViewById(R.id.btn_add_image);

        btn_add_image.setOnClickListener(v -> {
            showImageOptionDialog();
        });
    }

    private void showImageOptionDialog(){
        final String[] options = getResources().getStringArray(R.array.image_options);
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle(R.string.dialog_image_options)
                .setItems(options, (dialog, which) -> {
                    switch (which){
                        case 0:
                            getImageFromGallery();
                            break;
                        case 1:
                            capturePictureFromCamera();
                            break;
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void capturePictureFromCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Check if the intent was to pick image, was successful and an image was picked
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null){
            //Get selected image uri from phone gallery
            Uri selectedImage = data.getData();

            try {
                selectedImageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), selectedImage));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Display selected photo in image view
            iv_image.setImageBitmap(selectedImageBitmap);
        }
        //Handle camera request
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null){

            //We need a bitmap variable to store the photo
            selectedImageBitmap = (Bitmap) data.getExtras().get("data");

            //Display taken picture in image view
            iv_image.setImageBitmap(selectedImageBitmap);
        }
    }

    private void initializeAddBtn() {
        btn_add.setOnClickListener(v -> {
            SQLiteDropModel drop;
            boolean hasImage = false;
            if (selectedImageBitmap != null) {
                hasImage = true;
            }
            try {
                drop = new SQLiteDropModel(
                        -1, et_title.getText().toString(), et_note.getText().toString(),
                        DateService.dateStringToEpochMilli(AddActivity.this, et_date.getText().toString()), hasImage);

                Toast.makeText(AddActivity.this, drop.toString(), Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(AddActivity.this, "Error creating drop", Toast.LENGTH_SHORT).show();
                drop = new SQLiteDropModel(-1, "error", "error", 0L, false);
            }

            SQLiteDataBaseHelper mSQLiteDataBaseHelper = SQLiteDataBaseHelper.getHelper(AddActivity.this);

            boolean success = mSQLiteDataBaseHelper.addDrop(drop);

            if (!success) { return; }

            if (hasImage) {
                int lastId = mSQLiteDataBaseHelper.getLastInsertedDropId();
                FileService.saveToInternalStorage(this, selectedImageBitmap, lastId);
            }
        });
    }
}

