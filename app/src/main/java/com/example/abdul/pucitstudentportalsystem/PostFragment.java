package com.example.abdul.pucitstudentportalsystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DTO.Post;
import DTO.User;


public class PostFragment extends Fragment {
    private EditText postText;
    private Button selectBtn,postBtn;
    private TextView notification;
    private static final int FILE_SELECT_CODE = 86;
    private static  final int PERMISSION_REQUEST_CODE=9;
    private FirebaseStorage storage;
    private ValueEventListener mDbListener;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private Uri pdfUri;
    private ProgressDialog progressDialog;
    private String fileName;
    private List<String> deviceTokens=new ArrayList<>();
    private  String batch;
    private String currenUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       return inflater.inflate(R.layout.fragment_post,null);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectBtn= (Button) view.findViewById(R.id.uploadBtn);
        postBtn=(Button)view.findViewById(R.id.postBtn);
        notification=(TextView)view.findViewById(R.id.notification);
        postText=(EditText)view.findViewById(R.id.postEditText);
        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference();
        mAuth=FirebaseAuth.getInstance();
     //   Toast.makeText(getActivity(), "post fragment", Toast.LENGTH_SHORT).show();

        batch=getBatch(mAuth.getCurrentUser().getEmail());
        currenUserId=mAuth.getCurrentUser().getUid();

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    selectPdf();
                }
                else{
                    ActivityCompat.requestPermissions(getActivity(),new String []{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
                }


            }
        });
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfUri!=null) {
                    uploadFile(pdfUri);
                }
                else{
                    Toast.makeText(getActivity(), "please upload a file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile(Uri pdfUri) {
        if(TextUtils.isEmpty(postText.getText().toString())){
            Toast.makeText(getActivity(), "please enter some description", Toast.LENGTH_SHORT).show();
            return ;
        }
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading File...");
        progressDialog.setProgress(0);
        progressDialog.show();

        String fileId=System.currentTimeMillis()+"";
        final Post post=new Post();
        String postTxt=postText.getText().toString();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-M-yy");
        String formattedDate = df.format(c)+" "+c.getHours()+":"+c.getMinutes()+":"+c.getSeconds();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String extention=getFileExtension(pdfUri);
        DatabaseReference myRef=database.getReference();
        mDbListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {




                User user =new User();
                deviceTokens.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("users");
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    User user1=data.getValue(User.class);
                    user1.setId(data.getKey());
                    if(!currenUserId.equals(user1.getId())&&!user1.getDeviceToken().equals("")){

                        deviceTokens.add(user1.getDeviceToken());
                    }
                    else if (mAuth.getCurrentUser().getUid().equals(user1.getId())){
                        user=user1;
                    }

                }



                post.setPostedByName(user.getUserName());
                post.setPostedByImageUrl(user.getThumbPath());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        post.setAttachmentType(extention);
        post.setText(postTxt);
        post.setDateString(formattedDate);
        post.setPostedBy(currentUser.getUid());
        post.setPostId(fileId);


        StorageReference storageReference=storage.getReference();
        storageReference.child("uploads").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url=taskSnapshot.getDownloadUrl().toString();
                        post.setFileUrl(url);
                        DatabaseReference mDataBase=database.getReference().child(getBatch(currentUser.getEmail())).child("postNotifications").push();
                        String id=mDataBase.getKey();
                        Map myMap=new HashMap();
                        myMap.put("deviceTokens",deviceTokens);
                        myMap.put("timestamp", ServerValue.TIMESTAMP);
                        myMap.put("type","post");


                        Map postMap=new HashMap();
                        postMap.put(getBatch(currentUser.getEmail())+"/posts/"+post.getPostId()+"/",post);
                        postMap.put(getBatch(currentUser.getEmail())+"/postNotificatoins/",myMap);

                        reference.updateChildren(postMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(), "File SuccessFullY uploaded", Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();
                                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                            }
                        });



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "file not successfully uploaded", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress=(int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_REQUEST_CODE&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectPdf();

        }
        else{
            Toast.makeText(getActivity(), "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    public String getBatch(String email){
        return email.substring(0,7);
    }


    private void selectPdf() {
        Intent intent=new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,FILE_SELECT_CODE);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==FILE_SELECT_CODE&&resultCode==getActivity().RESULT_OK&&data!=null)
        {
            Uri uri = data.getData();
            pdfUri=uri;
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        notification.setText(displayName);
                        notification.setVisibility(View.VISIBLE);
                        fileName=displayName;
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                notification.setText(displayName);
                notification.setVisibility(View.VISIBLE);
                fileName=displayName;
            }

        }
        else{
            Toast.makeText(getActivity(), "please select a file", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mDbListener!=null)
        {
            reference.removeEventListener(mDbListener);

        }

    }
}
