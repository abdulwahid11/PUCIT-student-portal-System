package com.example.abdul.pucitstudentportalsystem;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Type;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.isityou.sdk.IsItYouConstants;
import com.isityou.sdk.IsItYouSdk;
import com.isityou.sdk.interfaces.CameraListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.sql.Time;
import org.opencv.android.Utils;

import FaceDetectionUtils.CameraPreview;

public class FaceEnrollmentActivity extends AppCompatActivity implements CameraListener {

    InitOperation iiyInitializationThread;
    TextView tv_res;
    Button bt_s,bt_done;// bt_pico, bt_nani, bt_cap, bt_macho, bt_reset;
    ImageView im_pic;
    FrameLayout fl_pic;
    Camera camera;
    CameraPreview cameraPreview;
    Switch sw_learning, sw_setas;
    private FirebaseAuth mAuth;

    private byte[] image;
    private Bitmap bitmap;
    private Uri imUri;
    private String user;
    private IsItYouSdk iiy;

    private String username;

    SharedPreferences sp;
    SharedPreferences.Editor spEdit;

    private boolean vladimirLernen, vladimirAS;

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap ResizeBitmap(Bitmap source, int width, int height) {
        return Bitmap.createScaledBitmap(source, width, height, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_enrollment);

        sp = getSharedPreferences("iiyDemo", 0);
        spEdit = sp.edit();

        iiy = IsItYouSdk.getInstance(getApplicationContext());


        tv_res = findViewById(R.id.tv_res);
        bt_s = findViewById(R.id.bt_s);
        bt_done=(Button)findViewById(R.id.bt_d);
        im_pic = findViewById(R.id.im_pic);
        fl_pic = findViewById(R.id.fl_pic);
        sw_learning = findViewById(R.id.sw_learning);
        sw_setas = findViewById(R.id.sw_setas);
        mAuth = FirebaseAuth.getInstance();
        /*camera = Camera.open();
        showCamera = new ShowCamera(getApplicationContext(), camera);
        fl_pic.addView(showCamera);*/


        sw_learning.setChecked(true);
        sw_setas.setChecked(true);




        bt_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                takePic();
                enrollPic();
            }
        });


        initIIYSDK();
        iiy.resetAppUser("Guest");
        tv_res.setText("Init user Guest Enrolls: 0");


        //when done
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FaceEnrollmentActivity.this, "verification email is sent to "+mAuth.getCurrentUser().getEmail()+" please verify it to signIn", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(FaceEnrollmentActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        });
    }

    //opens camera view
    private void openCameraView() {
        Log.d("ALON", "Opening camera view");
        cameraPreview = new CameraPreview(this, fl_pic,this,true);
        Log.d("ALON", "initialized");
       fl_pic.addView(cameraPreview);
        Log.d("ALON", "Added");
    }


    //takes pic

    public void takePic() {
        Log.d("ALON", "Taking frame");
        image = cameraPreview.getCurrentFrame().clone();
        Log.d("ALON", "Length: "+image.length);
        Bitmap mipap = com.example.abdul.pucitstudentportalsystem.Utils.getBitmapImageFromYUV(image, 480, 640);
        if (mipap != null) {
            //Bitmap rot = RotateBitmap(mipap, 90);
            im_pic.setImageBitmap(mipap);
        } else {
            Log.d("ALON", "es ist null");
        }
    }

    //enrolls a pic

    public void enrollPic() {
        if (image == null) {
            Log.d("ALON", "Image is null");
            return;
        }
        if (user == null || user.equals("")) {
            Log.d("ALON", "User is null");
            return;
        }
        int res = IsItYouSdk.getInstance(getApplicationContext()).saveEnrollment(image, 0);
        Log.d("ALON", "Result: "+String.valueOf(res));
        String enrolls = "Enrolls: "+iiy.getNumOfEnrolls();
        tv_res.setText("Result: "+String.valueOf(res)+" "+enrolls);

    }

    //init the sdk
    public void initIIYSDK()
    {
        iiyInitializationThread = new InitOperation();
        iiyInitializationThread.execute();
    }

    @Override
    public void onImageTaken(byte[] bytes) {
        image = bytes;
    }

    @Override
    public boolean startCapture(byte[] bytes) {
        return false;
    }

    @Override
    public boolean onSurfcaeDestroy() {
        return false;
    }

    public class InitOperation extends AsyncTask<Integer, Void, Integer>
    {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Integer doInBackground(Integer... params) {

            int initResult = iiy.init("R3JoeBpA271NzmQ8",90);
            Log.d("ALON", "initResult: "+initResult);

            return initResult;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            String initUser = iiy.initUser("Guest");
            user = iiy.getCurrentUser();
            sw_learning.setChecked(true);
            sw_setas.setChecked(true);
            final String enrolls = "Enrolls: "+iiy.getNumOfEnrolls();
            openCameraView();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tv_res.setText("Init user "+user + " "+enrolls);
                    bt_s.setEnabled(true);
                }
            });
        }

    }

}
