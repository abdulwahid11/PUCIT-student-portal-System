package com.example.abdul.pucitstudentportalsystem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapTools {

    public static Bitmap toBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data , 0, data.length);
    }

    public static Bitmap rotate(Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }
}