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
import android.widget.Toast;

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

public class LocationActivity extends BasicActivity {
    private static final String TAG = "LocationActivity";
    private FirebaseUser user;
    public String profilePath;
    public String name;
    public String phoneNumber;
    public String birthDay;
    public String address;
    public String address1 = null; // 마커 위치 정보 저장
    public String address2 = null; // GPS 위치 정보 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setToolbarTitle("현재 위치 설정");

        findViewById(R.id.GPSButton).setOnClickListener(onClickListener);
        findViewById(R.id.MarkButton).setOnClickListener(onClickListener);
        findViewById(R.id.locationChkBtn).setOnClickListener(onClickListener);

        Intent intent = getIntent(); /*데이터 수신*/
        address1 = intent.getStringExtra("사용자 주소1"); // Maps에서 가져온 주소 정보
        address2 = intent.getStringExtra("사용자 주소2"); // GPS에서 가져온 주소 정보
        TextView MarkTextView = findViewById(R.id.MarkTextView);
        TextView GPSTextView = findViewById(R.id.GPSTextView);
        MarkTextView.setText(address1);
        GPSTextView.setText(address2);

        // <================================= 현재 사용자의 데이터 가져옴 =================================>
        // input: -     output: -
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").
                document(FirebaseAuth.getInstance().getCurrentUser().getUid()); // 현재 사용자에 대한 document 진입점
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) { // 현재 저장된 사용자의 데이터 가져와 변수에 저장
                        name = document.getData().get("name").toString();
                        phoneNumber = document.getData().get("phoneNumber").toString();
                        birthDay = document.getData().get("birthDay").toString();

                        // 유저 정보에 사진이 있을 경우 사진 URL을 가져옴
                        if(document.getData().get("photoUrl") != null){
                            profilePath = document.getData().get("photoUrl").toString();
                        }

                        if(address1 == null && address2 != null){ // GPS로 위치 정보 가져올 때
                            address = address2;
                        } else if (address2 == null && address1 != null) { //마커로 위치 정보 가져올 때
                            address = address1;
                        }  else {
                            address = document.getData().get("address").toString(); // 회원가입 시 입력한 주소 정보 가져올 때
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException()); // 사용자 정보를 못 가져왔을 때
                }
            }
        });
    }

    // <================================= 버튼 클릭 시 이동 =================================>
    // input: -     output: -
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.GPSButton: // GPS 자동 추적 버튼 클릭 시
                    myStartActivity(GPSActivity.class);
                    break;
                case R.id.MarkButton: // Marker 선택 버튼 클릭 시
                    myStartActivity(MapsActivity.class);
                    break;
                case R.id.locationChkBtn: // 주소 설정 완료 버튼 클릭 시
                    addressUploader(); // 변경된 주소로 인스턴스 생성
                    myStartActivity(MainActivity.class);
                    break;

            }
        }
    };

    //<================================= 변경된 주소 정보 인스턴스 생성 =================================>
    // input: -     output: -
    private void addressUploader() {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // firestore DB 접근할 수 있는 진입점
        StorageReference storageRef = storage.getReference(); // firestore DB로 이동
        user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 유저 인스턴스
        final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");

        if (profilePath == null) { // 프로필 이미지가 없을 때
            UserInfo userInfo = new UserInfo(name, phoneNumber, birthDay, address);
            storeUploader(userInfo); // DB에 업로드
        } else { // 프로필 이미지가 있을 때
            UserInfo userInfo = new UserInfo(name, phoneNumber, birthDay, address, profilePath);
            storeUploader(userInfo); // DB에 업로드
        }
    }

    //<================================= 유저 정보 DB 업로드 =================================>
    // input: userInfo 인스턴스      output: -
    private void storeUploader(UserInfo userInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // firestore DB 접근할 수 있는 진입점
        db.collection("users").document(user.getUid()).set(userInfo) // DB 정보 수정
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) { // DB 업데이트 완료 시
                      Toast.makeText(LocationActivity.this, "위치설정 완료", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { // DB 업데이트 실패 시
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 0);
    }
}