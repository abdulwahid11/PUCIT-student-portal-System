package com.example.abdul.pucitstudentportalsystem;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import DTO.Post;
import DTO.User;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class ProfileFragment extends Fragment {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private ImageView profile;
    private EditText name,oldPass,newPass;
    private TextView date;
    private Button update;
    private static final int GALLERY_INTENT=2;
    private Uri mImageUri;
    private StorageReference mStorage;
    private String imageUrl;
    private StorageTask mUploadTask;
    private ValueEventListener mDbListener;
    private ProgressBar progress;
    private User curr;
    private List<Post> postList;

    private String thummbUrl;
    private Bitmap mImageBitmap;

    private String batch;
    private String currentUserId;
    public static final String DEFAULT_IMAGE="https://firebasestorage.googleapis.com/v0/b/pucitstudentportalsystem-16826.appspot.com/o/uploads%2Fdefault.png?alt=media&token=4d40fcaa-04a5-4f95-827e-255e9a559733";

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,null);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      //  Toast.makeText(getActivity(), "profile fragment", Toast.LENGTH_SHORT).show();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference();
        profile=(ImageView)view.findViewById(R.id.edit_profile_image);
        name=(EditText)view.findViewById(R.id.editNames);
        oldPass=(EditText)view.findViewById(R.id.oldPasssword);
        newPass=(EditText)view.findViewById(R.id.newPassword);
        date=(TextView)view.findViewById(R.id.editdob);
        update=(Button)view.findViewById(R.id.updateRecord);
        mAuth=FirebaseAuth.getInstance();
        mStorage=FirebaseStorage.getInstance().getReference("uploads");
        progress=(ProgressBar)view.findViewById(R.id.progressed);
        postList=new ArrayList<>();
        currentUserId=mAuth.getCurrentUser().getUid();
        batch=getBatch(mAuth.getCurrentUser().getEmail());
        mDbListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                DataSnapshot snap= dataSnapshot.child(batch).child("users").child(currentUserId);


                User user=snap.getValue(User.class);
                if(!user.getThumbPath().equals("")) {
                    Picasso.get().load(user.getThumbPath().toString()).placeholder(R.drawable.profile).fit().centerCrop().into(profile);
                }
                name.setText(user.getUserName());
                date.setText(user.getDateOfBirth());
                curr=user;


                postList.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("posts");
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    Post mPost=data.getValue(Post.class);
                    mPost.setPostId(data.getKey());
                    postList.add(mPost);

                }




                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openFileChooser();
                    }
                });

                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        if (mUploadTask != null && mUploadTask.isInProgress()) {
                            Toast.makeText(getActivity(), "Upload in progress", Toast.LENGTH_SHORT).show();
                        } else {

                            validateUser();

                        }


                    }
                });
                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(
                                getActivity(),
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

                        String da = month + "/" + day + "/" + year;

                        date.setText(da);
                    }
                };






            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }
    public void validateUser(){
        progress.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(oldPass.getText().toString()))
        {
            Toast.makeText(getActivity(), "Please Enter old Password", Toast.LENGTH_SHORT).show();
            return;
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider.getCredential(curr.getEmail(), oldPass.getText().toString());

        final boolean flag=false;
// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            uploadFile();

                        } else {
                            Toast.makeText(getActivity(), "please enter right password", Toast.LENGTH_SHORT).show();
                            progress.setVisibility(View.GONE);

                        }
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_INTENT);
    }


    public String getBatch(String email){
        return email.substring(0,7);
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();


            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .start(getContext(), this);

            Picasso.get().load(mImageUri).placeholder(R.drawable.profile).into(profile);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumbnailFile= new File(resultUri.getPath());
                try {
                    Bitmap compressedImageBitmap = new Compressor(getActivity()).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumbnailFile);
                    mImageBitmap=compressedImageBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mImageUri=resultUri;
                Picasso.get().load(mImageUri).placeholder(R.drawable.profile).into(profile);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadFile() {
        if (mImageBitmap!=null) {

            ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
            mImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            final byte[]data=byteArrayOutputStream.toByteArray();
            String fileName=System.currentTimeMillis()+".jpg";
            final StorageReference storageReference=mStorage.child("thumbs").child(fileName);


            mUploadTask=storageReference.putBytes(data).
                    addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                    String url=task.getResult().getDownloadUrl().toString();
                    thummbUrl=url;
                    if(task.isSuccessful())
                    {

                        FirebaseStorage mtorage=FirebaseStorage.getInstance();

                        if(!curr.getThumbPath().equals(""))
                        {
                            StorageReference ref = mtorage.getReferenceFromUrl(curr.getThumbPath());


                            ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                        }



                            registerUser();

                    }
                    else{
                        Toast.makeText(getActivity(), "error uploading thumbnail", Toast.LENGTH_SHORT).show();
                    }
                }
            });




        }
        else{
            thummbUrl="";
            registerUser();

        }
    }
    public void registerUser()
    {

        if(TextUtils.isEmpty(newPass.getText().toString()))
        {
            onAuthSuccess(mAuth.getCurrentUser());
            progress.setVisibility(View.GONE);
            return;
        }


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider.getCredential(curr.getEmail(), oldPass.getText().toString());

// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        onAuthSuccess(mAuth.getCurrentUser());
                                        progress.setVisibility(View.GONE);

                                    } else {
                                        Toast.makeText(getActivity(), "not updated", Toast.LENGTH_SHORT).show();
                                        progress.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), "auth failed", Toast.LENGTH_SHORT).show();
                            progress.setVisibility(View.GONE);
                        }
                    }
                });


    }
    private void onAuthSuccess(FirebaseUser user){


        String newPas=oldPass.getText().toString();
        if(!(TextUtils.isEmpty(newPass.getText().toString())))
        {
            newPas=newPass.getText().toString();
        }
        String userName=name.getText().toString().trim();
        String userDob=date.getText().toString();
        User mUser =new User();
        mUser.setEmail(curr.getEmail());
        mUser.setId(user.getUid());
        mUser.setUserName(userName);
        mUser.setPassword(newPas);
        mUser.setDateOfBirth(userDob);
        mUser.setCR(curr.getCR());
        mUser.setThumbPath(thummbUrl);

        writeUser(mUser);
    }
    public void writeUser(User user){

        String batch=getBatch(user.getEmail());

        for(int i =0 ; i < postList.size();i++)
        {
            if(postList.get(i).getPostedBy().equals(user.getId())){
                Post post=postList.get(i);
                if(!(post.getPostedByImageUrl().equals(user.getThumbPath()))||!(post.getPostedByName().equals(user.getUserName()))){
                    post.setPostedByImageUrl(user.getThumbPath());
                    post.setPostedByName(user.getUserName());
                    reference.child(batch).child("posts").child(post.getPostId()).setValue(post);
                }
            }
        }

        reference.child(batch).child("users").child(user.getId()).setValue(user);
        Toast.makeText(getActivity(), "user Updated Successfully", Toast.LENGTH_SHORT).show();
       getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
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
