package com.example.abdul.pucitstudentportalsystem;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import Adapters.SectionPagerAdapter;

public class ConversationsActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolBar;


    DatabaseReference mRef;
    FirebaseAuth mAuth;
    private ViewPager viewPager;
    private SectionPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private String currentUserId;
    private String batch;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){

            mRef= FirebaseDatabase.getInstance().getReference().child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid());
        }

        mToolBar=(android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("PSP");


        viewPager=(ViewPager)findViewById(R.id.tabPager);
        mSectionsPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout) findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(viewPager);





    }

    public String getBatch(String email){
        return email.substring(0,7);
    }






}
