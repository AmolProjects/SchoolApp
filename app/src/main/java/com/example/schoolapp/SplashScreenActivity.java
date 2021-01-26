package com.example.schoolapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.uberclone.Model.DriverInfoModel;
//import com.example.uberclone.Utils.UserUtils;
//import com.firebase.ui.auth.AuthMethodPickerLayout;
//import com.firebase.ui.auth.AuthUI;
//import com.firebase.ui.auth.IdpResponse;
import com.example.schoolapp.Model.StudentInfoModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE=7171; //any no
    private List<AuthUI.IdpConfig>providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    FirebaseDatabase database;
    DatabaseReference studentInfoRef;


    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        if (firebaseAuth!=null && listener!=null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
                     init();

    }

            private void init() {
//
                ButterKnife.bind(this);
                database=FirebaseDatabase.getInstance();
                studentInfoRef=database.getReference(COMMON.STUDENT_INFO_REFERENCE);
//
                 providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
//
             firebaseAuth= FirebaseAuth.getInstance();
             listener=myFirebaseAuth->{
                 FirebaseUser user=myFirebaseAuth.getCurrentUser();
//
                 if (user!=null)
                    {
//                       // Update token
//                        FirebaseInstanceId.getInstance()
//                                .getInstanceId()
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(com.example.uberclone.SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                }).addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
//                            @Override
//                            public void onSuccess(InstanceIdResult instanceIdResult) {
//                                Log.d("TOKEN",instanceIdResult.getToken());
//                                UserUtils.updateToken(com.example.uberclone.SplashScreenActivity.this,instanceIdResult.getToken());
//                            }
//                        });
//                        delaySplashScreen();
                        checkUserFromFirebase();

                    }
                 else
                     showLoginLayout();
             };

    }

    private void checkUserFromFirebase() {
        studentInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
//                            Toast.makeText(SplashScreenActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                            StudentInfoModel studentInfoModel=snapshot.getValue(StudentInfoModel.class);
                            gotoHomeActivity(studentInfoModel);
                        }
                        else
                        {
                            showRegisterLayout();
                            Toast.makeText(SplashScreenActivity.this, "hello", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreenActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void gotoHomeActivity(StudentInfoModel studentInfoModel) {
        COMMON.currentUser=studentInfoModel; //Init value
        startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
        finish();
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.DialogTheme);
        View view= LayoutInflater.from(this).inflate(R.layout.layout_register,null);

        TextInputEditText edt_first_name=(TextInputEditText)view.findViewById(R.id.first_name);
        TextInputEditText edt_last_name=(TextInputEditText)view.findViewById(R.id.last_name);
        TextInputEditText edt_phone=(TextInputEditText)view.findViewById(R.id.phone_number);

        Button btn_continue=(Button)view.findViewById(R.id.btn_register);
//
        //set data
        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() !=null &&
            !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))

            edt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

//        //set view
        builder.setView(view);
        AlertDialog dialog=builder.create();
        dialog.show();

        btn_continue.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edt_first_name.getText().toString())) {
                Toast.makeText(SplashScreenActivity.this, "Please Enter First Name", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(edt_last_name.getText().toString())) {
                Toast.makeText(SplashScreenActivity.this, "Please Enter Last Name", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(edt_phone.getText().toString())) {
                Toast.makeText(SplashScreenActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();

                return;
            } else {
                StudentInfoModel model = new StudentInfoModel();
                model.setFirstName(edt_first_name.getText().toString());
                model.setLastName(edt_last_name.getText().toString());
                model.setPhoneNumber(edt_phone.getText().toString());

//
                studentInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(model)
                        .addOnFailureListener(e ->

                        {
                            dialog.dismiss();
                            Toast.makeText(SplashScreenActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        })
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SplashScreenActivity.this, "Register Succesfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                           gotoHomeActivity(model);
//
                        });
            }
        });

    }

                private void showLoginLayout () {

                    AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                            .Builder(R.layout.signin_layout)
                            .setPhoneButtonId(R.id.btn_phone_sign_in)
                            .setGoogleButtonId(R.id.btn_google_sign_in)
                            .build();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setIsSmartLockEnabled(false)
                                    .setTheme(R.style.LoginTheme)
                                    .setAuthMethodPickerLayout(authMethodPickerLayout)
                                    .build(),
                            LOGIN_REQUEST_CODE);
                }


    private void delaySplashScreen() {
       progress_bar.setVisibility(View.VISIBLE);
        Completable.timer(3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(() ->
                        firebaseAuth.addAuthStateListener(listener));

    }
//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==LOGIN_REQUEST_CODE)
        {
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if (resultCode==RESULT_OK){
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
               Toast.makeText(this, "Login Failed"+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
