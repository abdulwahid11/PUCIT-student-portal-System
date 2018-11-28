package com.example.abdul.pucitstudentportalsystem;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {
    private EditText email;
    private Button btn;
    private ProgressBar bar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        email=(EditText)findViewById(R.id.forgetUserPass);
        btn=(Button)findViewById(R.id.sendEmail);
        bar=(ProgressBar)findViewById(R.id.myProgress);
        mAuth=FirebaseAuth.getInstance();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
                String em=email.getText().toString().trim();
                if(TextUtils.equals(em,""))
                {
                    Toast.makeText(ForgetPassword.this, "please enter the email first", Toast.LENGTH_SHORT).show();

                }
                else {
                    mAuth.sendPasswordResetEmail(em).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                bar.setVisibility(View.GONE);
                                Toast.makeText(ForgetPassword.this, "email sent", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(ForgetPassword.this,MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else{

                                bar.setVisibility(View.GONE);
                                Toast.makeText(ForgetPassword.this, "error in sending email", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });
    }
}
