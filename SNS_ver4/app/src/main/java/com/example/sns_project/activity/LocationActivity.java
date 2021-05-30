package com.example.sns_project.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sns_project.UserInfo;
import com.example.sns_project.activity.MapsActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.sns_project.R;
import com.example.sns_project.adapter.GpsTracker;
import com.example.sns_project.adapter.HomeAdapter;
import com.example.sns_project.fragment.HomeFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.sns_project.Util.showToast;
import static com.google.android.material.internal.ContextUtils.getActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";
    //==================================================================================
    private FirebaseUser user;
    public String profilePath;
    public String name;
    public String phoneNumber;
    public String birthDay;
    public String address;
    //==================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        findViewById(R.id.GPSButton).setOnClickListener(onClickListener);
        findViewById(R.id.MarkButton).setOnClickListener(onClickListener);
        findViewById(R.id.locationChkBtn).setOnClickListener(onClickListener);

        Intent intent = getIntent(); /*데이터 수신*/
        String address1 = intent.getStringExtra("사용자 주소1");
        String address2 = intent.getStringExtra("사용자 주소2");
        TextView MarkTextView = findViewById(R.id.MarkTextView);
        TextView GPSTextView = findViewById(R.id.GPSTextView);
        MarkTextView.setText(address1);
        GPSTextView.setText(address2);

        //==================================================================================




        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Log.e("URL","실해애애애애ㅐ");
                        name = document.getData().get("name").toString();
                        phoneNumber = document.getData().get("phoneNumber").toString();
                        birthDay = document.getData().get("birthDay").toString();

                        if(document.getData().get("photoUrl") != null){
                            profilePath = document.getData().get("photoUrl").toString();
                            Log.e("URL",profilePath);
                        } else {
                            Log.e("URL","사진 없");
                        }



                        address = address1;
                        if(address1 == null){
                            address = address2;
                        } else if (address2 == null) {
                            address = address1;
                        } else {
                            Log.e("로그","주소 받을 수 없음 ");
                        }
                        //Log.e("address2",address2);
                        //Log.e("address",address);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        //==================================================================================

        //==================================================================================
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                /*
                case R.id.logoutButton:
                    FirebaseAuth.getInstance().signOut();
                    myStartActivity(SignUpActivity.class);
                    break;
                */

                case R.id.GPSButton:
                    myStartActivity(GPSActivity.class);
                    break;
                case R.id.MarkButton:
                    myStartActivity(MapsActivity.class);
                    break;
                case R.id.locationChkBtn:
                    addressUploader();
                    myStartActivity(MainActivity.class);
                    break;

            }
        }
    };

    //==================================================================================
    private void addressUploader() {

        //loaderLayout.setVisibility(View.VISIBLE);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");

        if (profilePath == null) {
            UserInfo userInfo = new UserInfo(name, phoneNumber, birthDay, address);
            storeUploader(userInfo);
        } else {
            UserInfo userInfo = new UserInfo(name, phoneNumber, birthDay, address, profilePath);
            storeUploader(userInfo);
        }
    }

    private void storeUploader(UserInfo userInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        showToast(MemberInitActivity.this, "회원정보 등록을 성공하였습니다.");
//                        loaderLayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        showToast(MemberInitActivity.this, "회원정보 등록에 실패하였습니다.");
//                        loaderLayout.setVisibility(View.GONE);
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    //==================================================================================




    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 0);
    }
}