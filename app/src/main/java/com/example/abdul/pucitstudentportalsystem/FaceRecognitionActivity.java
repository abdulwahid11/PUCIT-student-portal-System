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
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abdul.pucitstudentportalsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
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

import DTO.User;
import FaceDetectionUtils.CameraPreview;
import io.github.silvaren.easyrs.tools.Nv21Image;

public class FaceRecognitionActivity extends AppCompatActivity implements CameraListener {

    //data memebers

    InitOperation iiyInitializationThread;
    TextView tv_res;
    Button  bt_macho;
    ImageView im_pic;
    FrameLayout fl_pic;
    Camera camera;
    CameraPreview cameraPreview;
    Switch sw_learning, sw_setas;

    private ProgressBar progress;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private byte[] image;
    private Bitmap bitmap;
    private Uri imUri;
    private String user;
    private ValueEventListener mDbListener;
    private IsItYouSdk iiy;
    private DatabaseReference mUserDataBase;
    private SharedPreferences myPrefs;
    private SharedPreferences prefs;

    private static  final  String PREF_NAME="myprefs";
    private static  final  String MY_PREF_NAME="sigupPrefs";
    private static final String TAG="abdul_wahid";


    private static  final  String MY_PREF="prefs";

    private String username;

    SharedPreferences sp;
    SharedPreferences.Editor spEdit;


    private boolean vladimirLernen, vladimirAS;


    //method used to rotate bitmap

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    //helper method

    public String getBatch(String email){
        return email.substring(0,7);
    }

    public static Bitmap ResizeBitmap(Bitmap source, int width, int height) {
        return Bitmap.createScaledBitmap(source, width, height, true);
    }

    //typical oncreate method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        //initialize variables

        sp = getSharedPreferences("iiyDemo", 0);
        spEdit = sp.edit();

        progress=findViewById(R.id.progressBar2);
        progress.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        prefs=getSharedPreferences(MY_PREF_NAME, Context.MODE_PRIVATE);
        myPrefs=getSharedPreferences(MY_PREF,Context.MODE_PRIVATE);

        iiy = IsItYouSdk.getInstance(getApplicationContext());
        database=FirebaseDatabase.getInstance();
        ref=database.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUserDataBase=FirebaseDatabase.getInstance().getReference();



        tv_res = findViewById(R.id.tv_res);
        bt_macho = findViewById(R.id.bt_macho);
        im_pic = findViewById(R.id.im_pic);
        fl_pic = findViewById(R.id.fl_pic);
        sw_learning = findViewById(R.id.sw_learning);
        sw_setas = findViewById(R.id.sw_setas);
        initIIYSDK();
    }

    //opens camera view

    private void openCameraView() {
        Log.d("ALON", "Opening camera view");
        cameraPreview = new CameraPreview(this, fl_pic,this,true);
        Log.d("ALON", "initialized");
        fl_pic.addView(cameraPreview);
        Log.d("ALON", "Added");
    }

    //captures a pic

    public void takePic() {

        Log.d("ALON", "Taking frame");
       image = cameraPreview.getCurrentFrame().clone();
        Log.d("ALON", "Length: "+image.length);

        Display display = getWindowManager().getDefaultDisplay();
        int rotation = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                rotation = 90;
                break;
            case Surface.ROTATION_90:
                rotation = 0;
                break;
            case Surface.ROTATION_180:
                rotation = 270;
                break;
            case Surface.ROTATION_270:
                rotation = 180;
                break;
        }

        RenderScript rs = RenderScript.create(this);
        BitmapTools ImageTools=new BitmapTools();
       Bitmap bitmap = com.example.abdul.pucitstudentportalsystem.Utils.getBitmapImageFromYUV(image, 480, 640);

        if (bitmap != null) {
            im_pic.setImageBitmap(bitmap);
        } else {
            Log.d("ALON", "es ist null");
        }
    }

    //method for matching the pip

    public void matchPic() {
        if (image == null) {
            Log.d("ALON", "Image is null");
            return;
        }
        if (user == null || user.equals("")) {
            Log.d("ALON", "User is null");
            return;
        }
        int res = iiy.match(image, 0);
        String path = Environment.getExternalStorageDirectory().toString()+"/IIYLOGS/iiylog"+iiy.getNumOfEnrolls()+".txt";
        try {
            PrintWriter p = new PrintWriter(new FileOutputStream(path, true));
            p.println((System.currentTimeMillis()/1000)+":"+res);
            p.close();
        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        Log.d("ALON", "Result: "+String.valueOf(res));
        Double score = iiy.getScoreDebug();
        String as = iiy.getASReason();
        if (res == IsItYouConstants.SUCCESS) {
            Log.d("ALON", "Finalized with "+res);
            iiy.finalizeMatch(true);
        } else {
            iiy.finalizeMatch(false);
        }
        String enrolls = "Enrolls: "+iiy.getNumOfEnrolls();
        String fnl = "";
        switch(res) {
            case 1:
                fnl = "Success";
                break;
            case 2:
                if (iiy.getAS())
                    fnl = "Second factor";
                else
                    fnl = "Success (SF)";
                break;
            case 3:
                fnl = "Success";
                break;
            case 4:
                fnl = "no enrollments";
                break;
            case 5:
                fnl = "user blocked";
                break;
            case 10:
                fnl = "Face Not detected";
                break;
            case 11:
                fnl = "Someone else";
                break;
        }
        if (res == IsItYouConstants.SUCCESS) {

           setUpForLogIn();

        } else {
            Toast.makeText(this, "face logIn Failed"+fnl+" ", Toast.LENGTH_SHORT).show();
            progress.setVisibility(View.INVISIBLE);
        }
    }

    //onclick method
    public void elMacho(View v) {
        progress.setVisibility(View.VISIBLE);
        takePic();
        matchPic();
    }
    //after success method

    public void  setUpForLogIn(){
        String email=prefs.getString("email","");
        String password=prefs.getString("password","");
        final String Uid= prefs.getString("Uid","");
        final String batch = getBatch(email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

            String deviceToken= FirebaseInstanceId.getInstance().getToken();
            mUserDataBase.child(batch).child("users").child(Uid).child("deviceToken").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    mDbListener = ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            DataSnapshot snap = dataSnapshot.child(batch).child("users").child(Uid);

                            DatabaseReference mRef=FirebaseDatabase.getInstance().getReference().child(batch).child("users").child(Uid);

                            mRef.child("online").setValue(1);
                            progress.setVisibility(View.INVISIBLE);
                            User user = snap.getValue(User.class);
                            if (user.getCR() == false) {

                                SharedPreferences.Editor editor = myPrefs.edit();
                                editor.clear();
                                editor.putString("CR", "false");
                                editor.apply();
                                Toast.makeText(FaceRecognitionActivity.this, "LogIn successfull", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(FaceRecognitionActivity.this, Home.class);
                                startActivity(intent);
                                finish();
                            } else {

                                SharedPreferences.Editor editor = myPrefs.edit();
                                editor.clear();
                                editor.putString("CR", "true");
                                editor.apply();

                                Toast.makeText(FaceRecognitionActivity.this, "LogIn successfull", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(FaceRecognitionActivity.this, Home.class);
                                startActivity(intent);
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progress.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            });
                        } else {
                            progress.setVisibility(View.GONE);
                            Toast.makeText(FaceRecognitionActivity.this, "authentication failed", Toast.LENGTH_SHORT).show();

                        }

                    }
                });


    }

    //init the open cv SDK

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
            return initResult;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            String initUser = iiy.initUser("Guest");
            user = iiy.getCurrentUser();
            iiy.setMaxFails(10);
            final String enrolls = "Enrolls: "+iiy.getNumOfEnrolls();
            openCameraView();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_res.setText("Init user "+user + " "+enrolls);
                    bt_macho.setEnabled(true);
                }
            });
        }

    }


}
