package com.example.sns_project.activity;
//201736009 컴퓨터공학과 이정재

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.sns_project.R;
import com.example.sns_project.fragment.HomeFragment;
import com.example.sns_project.fragment.UserInfoFragment;
import com.example.sns_project.fragment.UserListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BasicActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle(getResources().getString(R.string.app_name));
        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                init();
                break;
        }
    }

    private void init(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            myStartActivity(SignUpActivity.class);
        } else {
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid()); // 사용자 고유 해쉬값을 받아온다, 사용자 정보가 있는지 확인하기위해 사용
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                //Log.d(TAG, "No such document");
                                myStartActivity(MemberInitActivity.class); //데이터 베이스로 부터 받아온 사용자 정보가 없을 경우 MemberInitActivity 호출한다.
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            // <================================= 포스트 프래그먼트 설정 =================================>
            HomeFragment homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFragment)
                    .commit();

            // 하단 네비게이션 바 설정
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.home: // 포스트 메인 화면
                            HomeFragment homeFragment = new HomeFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, homeFragment)
                                    .commit();
                            return true;
                        case R.id.myInfo: //사용자 정보
                            UserInfoFragment userInfoFragment = new UserInfoFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, userInfoFragment)
                                    .commit();
                            return true;
                        case R.id.userList: //사용자 리스트
                            UserListFragment userListFragment = new UserListFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, userListFragment)
                                    .commit();
                            return true;
                        case R.id.modifyUserLocation://사용자 위치 재설정, 위치 설정의 경우 사용자가 설정 완료 버튼을 누르도록 유도 하기위해 프래그 먼트 대신 액티비티 사용, 화면 전체 출력
                            myStartActivity(LocationActivity.class);
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 1);
    }
}