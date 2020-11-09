package com.jbol.dailydrops.services;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.jbol.dailydrops.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileService {

    public static String saveToInternalStorage(Context ctx, Bitmap bitmapImage, int id){
        File directory = getImagesDir(ctx);
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

        return directory.getAbsolutePath();
    }

    public static Bitmap loadImageFromStorage(Context ctx, int id) {
        File directory = getImagesDir(ctx);
        File imgFile = new File(directory, id + ".png");
        try {
            return BitmapFactory.decodeStream(new FileInputStream(imgFile));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteImageFromStorage(Context ctx, int id) {
        File directory = getImagesDir(ctx);
        File imgFile = new File(directory, id + ".png");
        return imgFile.delete();
    }

    public static File getImagesDir(Context ctx) {
        ContextWrapper cw = new ContextWrapper(ctx);
        // path to /data/user/0/com.jbol.dailydrops/app_images
        return cw.getDir("images", Context.MODE_PRIVATE);
    }
}
