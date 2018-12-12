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



        sw_learning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                iiy.setLearning(isChecked);
            }
        });

        sw_setas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                iiy.setAS(isChecked);
            }
        });


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
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Toast.makeText(FaceEnrollmentActivity.this, "i'm clicked", Toast.LENGTH_SHORT).show();
                Toast.makeText(FaceEnrollmentActivity.this, "verification email is sent to "+mAuth.getCurrentUser().getEmail()+" please verify it to signIn", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(FaceEnrollmentActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        });
    }

    private void openCameraView() {
        Log.d("ALON", "Opening camera view");
        cameraPreview = new CameraPreview(this, fl_pic,this,true);
        Log.d("ALON", "initialized");
       fl_pic.addView(cameraPreview);
        Log.d("ALON", "Added");
    }

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

    public void enrollPic() {
        if (image == null) {
            Log.d("ALON", "Image is null");
            return;
        }
        if (user == null || user.equals("")) {
            Log.d("ALON", "User is null");
            return;
        }
                /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                image = stream.toByteArray();*/
        //image = Utils.getNV21(480, 640, bitmap);
                /*try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/sdcard/jaja.yuv"));
                    bos.write(image);
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    Log.e("ALON", e.toString());
                }*/
        int res = IsItYouSdk.getInstance(getApplicationContext()).saveEnrollment(image, 0);
        Log.d("ALON", "Result: "+String.valueOf(res));
        String enrolls = "Enrolls: "+iiy.getNumOfEnrolls();
        tv_res.setText("Result: "+String.valueOf(res)+" "+enrolls);

    }

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
            //int initResult = IsItYouSdk.getInstance(getApplicationContext()).init("AlonGoldenberg10", 0);
            int initResult = iiy.init("R3JoeBpA271NzmQ8",90);
            Log.d("ALON", "initResult: "+initResult);
            /*File file = new File("/sdcard/lulu.yuv");
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int res = IsItYouSdk.getInstance(getApplicationContext()).saveEnrollment(bytes, 0);
            Log.d("ALON", "Result: "+String.valueOf(res));*/
            return initResult;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            String initUser = iiy.initUser("Guest");
            //sw_learning.setChecked(iiy.getIsLearning());
            user = iiy.getCurrentUser();
            sw_learning.setChecked(iiy.getIsLearning());
            sw_setas.setChecked(iiy.getAS());
            final String enrolls = "Enrolls: "+iiy.getNumOfEnrolls();
            openCameraView();
            /*File file = new File("/sdcard/lulu.yuv");
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }iiy
            int res = IsItYouSdk.getInstance(getApplicationContext()).saveEnrollment(bytes, 0);
            Log.d("ALON", "Result: "+String.valueOf(res));*/
            //IsItYouSdk.getInstance(getApplicationContext()).setASEnroll(false);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tv_res.setText("Init user "+user + " "+enrolls);
                    bt_s.setEnabled(true);
                  //  bt_macho.setEnabled(true);
                   // bt_reset.setEnabled(true);
                }
            });
        }

    }

    public class ImageOperation extends AsyncTask<Integer, Void, Integer>
    {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            tv_res.setText("Processing...");

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imUri);
                /*try {
                    Log.d("ALON", original.getColorSpace().toString());
                } catch (Exception e) {

                }*/
                Bitmap rotated = RotateBitmap(original, 90);
                Bitmap stretched = ResizeBitmap(rotated, 480, 640);
                bitmap = stretched;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        im_pic.setImageBitmap(bitmap);
                    }
                });
                /*Log.d("ALON", "Image size is "+image.length);
                Mat mat = new Mat(480, 640, CvType.CV_8UC3);
                mat.put(0, 0, image);
                Mat yuv = new Mat();
                Imgproc.cvtColor(mat, yuv, Imgproc.COLOR_RGB2YUV);
                Mat nv21 = new Mat();
                Imgproc.cvtColor(yuv, nv21, Imgproc.COLOR_YUV2RGB_NV21);
                */
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tv_res.setText("Processed");
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ALON", String.format("RC %d RES %d", requestCode, resultCode));

        if (data == null || data.getData() == null) {
            Log.d("ALON", String.format("data: %b getData: %b", data == null, data.getData() == null));
        }

        if (requestCode == 10 && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            imUri = uri;

            ImageOperation imageOperation = new ImageOperation();
            imageOperation.execute();
        }

        if (requestCode == 20 && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            //im_pic.setImageBitmap(bitmap);
            //Log.d("ALON", bitmap.getColorSpace().toString());
            byte[] buffer = bitmapToBytes(bitmap);
            int dodo = iiy.match(buffer, 1);
            Log.d("ALON", "Result: "+dodo);
            if (dodo == 1)
                iiy.finalizeMatch(true);
            else
                iiy.finalizeMatch(false);
        }
    }

    private byte[] bitmapToBytes(Bitmap source) {
        /*Bitmap bitmap = ResizeBitmap(source, 480, 640);
        Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, mat);
        byte[] buffer = new byte[(int) (mat.total() * mat.channels())];
        mat.get(0, 0, buffer);
        return buffer;*/

        Log.d("ALON", "Conf: "+source.getConfig().toString());
        //Bitmap b = source;
        source = ResizeBitmap(source, 480, 640);
        int[] pixels = new int[480 * 640];
        source.getPixels(pixels, 0, 480, 0, 0, 480, 640);
        byte[] array = new byte[pixels.length * 3 / 2];
        com.example.abdul.pucitstudentportalsystem.Utils.encodeYUV420SP(array, pixels, 480, 640);
        /*int bytes = b.getByteCount();
        //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
        //int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        b.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        byte[] array = buffer.array(); //Get the underlying array containing the data.
        byte[] converted = new byte[array.length * 3 / 2];
        biz.isityou.demo.Utils.encodeYUV420SP(converted, array, 480, 640);*/
        return array;

    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            image = bytes;
            Bitmap mipap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Bitmap rot = RotateBitmap(mipap, 90);
            im_pic.setImageBitmap(rot);
            camera.startPreview();
        }
    };
}
