package com.jbol.dailydrops.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageService {

    static String currentPhotoPath;

    public static File createCameraImageFile(Context ctx) throws IOException {
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "DAILYDROPS_" + timeStamp + "_";
        File storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void saveDropImageToInternalStorage(Context ctx, Bitmap bitmapImage, int id){
        File directory = getDropImagesDir(ctx);
        File imgFile = new File(directory,id + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imgFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap loadDropImageFromStorage(Context ctx, int id) {
        File directory = getDropImagesDir(ctx);
        File imgFile = new File(directory, id + ".png");
        try {
            return BitmapFactory.decodeStream(new FileInputStream(imgFile));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean deleteDropImageFromStorage(Context ctx, int id) {
        File directory = getDropImagesDir(ctx);
        File imgFile = new File(directory, id + ".png");

        return imgFile.delete();
    }

    public static File getDropImagesDir(Context ctx) {
        ContextWrapper cw = new ContextWrapper(ctx);

        return cw.getDir("images", Context.MODE_PRIVATE); // path to /data/user/0/com.jbol.dailydrops/app_images
    }

    public static Bitmap getImageFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null; // No image
        }
    }

}
