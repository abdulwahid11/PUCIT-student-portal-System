package com.example.abdul.pucitstudentportalsystem;


import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import DTO.User;
import id.zelory.compressor.Compressor;

public class SignUpActivity extends AppCompatActivity {

   private EditText email,password,name;
   TextView dob;
   private Button nextBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String TAG = "MainActivity";
    private ProgressBar progress;
    private ImageView profileImage;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private StorageReference mStorageRef;
    private static final int GALLERY_INTENT=2;
    private static final int FILE_SELECT_CODE = 86;
    private static  final int PERMISSION_REQUEST_CODE=9;
    private ProgressDialog mProgress;
    private String imageUrl;
    private String thummbUrl;
    private StorageTask mUploadTask;
    private Uri mImageUri;
    private TextView refresh;
    private Bitmap mImageBitmap;
    private SharedPreferences prefs;

    private static  final  String MY_PREF="sigupPrefs";
    private ValueEventListener mDbListener;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        progress= (ProgressBar)findViewById(R.id.progress_bar);
        mProgress= new ProgressDialog(this);
        refresh=(TextView)findViewById(R.id.refresh);
        imageUrl="";
        refresh.setVisibility(View.GONE);
        prefs=getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);






        email=(EditText) findViewById(R.id.editUserEmail);
        password=(EditText) findViewById(R.id.editUserPassword);
        name=(EditText)findViewById(R.id.editUserName);
        dob=(TextView)findViewById(R.id.dobTxt);
        profileImage=(ImageView) findViewById(R.id.profile_image);

        nextBtn=(Button) findViewById(R.id.signUpButton);














        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(SignUpActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT < 23) {
                        //Do not need to check the permission
                    } else {
                        if (checkPermission()) {
                            //If you have already permitted the permission
                            // Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
                            exectueAdterPermissionsGranted();
                        }
                    }



                }




            }
        });




        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        SignUpActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;
                dob.setText(date);
            }
        };




}

    public boolean checkPermission(){
        int perm2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);


        int perm1 = ContextCompat.checkSelfPermission(this,


                Manifest.permission.READ_EXTERNAL_STORAGE);

        int perm3 = ContextCompat.checkSelfPermission(this,


                Manifest.permission.READ_PHONE_STATE);
      /*  int perm4 = ContextCompat.checkSelfPermission(this,


                Manifest.permission.ACCESS_NETWORK_STATE);

        int perm5 = ContextCompat.checkSelfPermission(this,


                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int perm6 = ContextCompat.checkSelfPermission(this,


                Manifest.permission.USE_FINGERPRINT);

        int perm7 = ContextCompat.checkSelfPermission(this,


                Manifest.permission.INTERNET);

*/
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (perm1!= PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (perm2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (perm3 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
/*
        if (perm4 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (perm5 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (perm6 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.USE_FINGERPRINT);
        }

        if (perm7 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        */
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,


                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_CAMERA);
            return false;
        }

        return true;


    }
    public void exectueAdterPermissionsGranted(){

        if(!validateUser()==true)
        {
            return;
        }
        progress.setVisibility(View.VISIBLE);
        uploadFile();
    }

    @Override    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED&&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED&&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED){

                    Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
                    exectueAdterPermissionsGranted();
                    //Permission Granted Successfully. Write working code here.
                } else {
                    //You did not accept the request can not use the functionality.
                    Toast.makeText(this, "Please give permissions to proceed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_INTENT);
    }

    private void registerUser(EditText em,EditText pass){

        final String userEmail,userPassword;
        userEmail=email.getText().toString().trim();
        userPassword=password.getText().toString().trim();


            mAuth.createUserWithEmailAndPassword(userEmail,userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                onAuthSuccess(task.getResult().getUser());
                                progress.setVisibility(View.GONE);
                                SharedPreferences.Editor editor=prefs.edit();
                                editor.putString("email",userEmail);

                                editor.putString("password",userPassword);
                                editor.putString("Uid",mAuth.getCurrentUser().getUid());
                                editor.apply();
                                sendVerificationEmail();


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                progress.setVisibility(View.GONE);
                            }

                            // ...
                        }
                    });


    }
    public void sendVerificationEmail(){
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
/*
                    Toast.makeText(SignUpActivity.this, "verification email is sent to "+mAuth.getCurrentUser().getEmail()+" please verify it to signIn", Toast.LENGTH_LONG).show();
                   Intent intent=new Intent(SignUpActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    */





                      startActivity(new Intent(SignUpActivity.this,FaceEnrollmentActivity.class));
                }
                else {
                    Toast.makeText(SignUpActivity.this, "failed", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    public boolean validateUser(){

        String em=email.getText().toString().trim();
        String pass= password.getText().toString().trim();
        String nm= name.getText().toString().trim();
        String db=dob.getText().toString().trim();

        if(em==""||pass=="")
        {
            Toast.makeText(this, "please enter details first", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            password.requestFocus();
            return false;
        }
        else if(!(em.contains("pucit.edu.pk")))
        {
            Toast.makeText(this, "please sign up using pucit email", Toast.LENGTH_SHORT).show();
            email.setText("");
            email.requestFocus();
            return false;
        }
        else if (!(Patterns.EMAIL_ADDRESS.matcher(em).matches())){
            Toast.makeText(this, "please enter a valid email", Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return false;

        }
        else if (pass.length()<=6){
            Toast.makeText(this, "password length should be more than 6 ", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return false;

        }
        else if (TextUtils.isEmpty(nm)||TextUtils.equals(db,"Date Of Birth"))
        {
            Toast.makeText(this, "please enter the date of birth and name first", Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;

    }
    private void onAuthSuccess(FirebaseUser user){
        String userName=name.getText().toString().trim();
        String userDob=dob.getText().toString();
        User mUser =new User();
        mUser.setEmail(user.getEmail());
        mUser.setId(user.getUid());
        mUser.setUserName(userName);
        mUser.setPassword(password.getText().toString().trim());
        mUser.setDateOfBirth(userDob);
        mUser.setCR(false);
        mUser.setThumbPath(thummbUrl);
        mUser.setOnline(1);
        String deviceToken= FirebaseInstanceId.getInstance().getToken();
        mUser.setDeviceToken(deviceToken);
        writeUser(mUser);
    }
    public void writeUser(User user){

        String batch=getBatch(user.getEmail());

        mDatabase.child(batch).child("users").child(user.getId()).setValue(user);

    }

    public String getBatch(String email){
        return email.substring(0,7);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .start(SignUpActivity.this);


            Picasso.get().load(mImageUri).placeholder(R.drawable.profile).into(profileImage);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumbnailFile= new File(resultUri.getPath());
                try {
                    Bitmap compressedImageBitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumbnailFile);
                    mImageBitmap=compressedImageBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mImageUri=resultUri;
                Picasso.get().load(mImageUri).into(profileImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadFile() {
        if (mImageBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
            mImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            final byte[]data=byteArrayOutputStream.toByteArray();
            String fileName=System.currentTimeMillis()+".jpg";
            final StorageReference storageReference=mStorageRef.child("thumbs").child(fileName);


            mUploadTask=storageReference.putBytes(data).
                    addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                String url=task.getResult().getDownloadUrl().toString();
                                thummbUrl=url;
                                if(task.isSuccessful())
                                {

                                    registerUser(email, password);
                                }
                                else{
                                    Toast.makeText(SignUpActivity.this, "error uploading thumbnail", Toast.LENGTH_SHORT).show();
                                }

                        }
                    });
        }
        else{
            thummbUrl="";
            registerUser(email, password);

        }
    }
}
