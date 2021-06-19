package com.example.sns_project.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.sns_project.FirebaseHelper;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.listener.OnPostListener;
import com.example.sns_project.view.ReadContentsVIew;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostActivity extends BasicActivity {
    private PostInfo postInfo; //postInfo 객체
    private FirebaseHelper firebaseHelper;
    private ReadContentsVIew readContentsVIew;
    private LinearLayout contentsLayout;
    private Button messengerBtn;

    FirebaseDatabase mDatabase;
    DatabaseReference dataRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsVIew = findViewById(R.id.readContentsView);

        findViewById(R.id.messengerBtn).setOnClickListener(onClickListener);

        firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.setOnPostListener(onPostListener);
        uiUpdate();
    }

    // <================================= 서브 액티비티 출력, 게시글 미리보기 =================================>
    // input: -     output: -
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    postInfo = (PostInfo)data.getSerializableExtra("postInfo");
                    contentsLayout.removeAllViews(); // 기존 게시글 삭제
                    uiUpdate();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                firebaseHelper.storageDelete(postInfo);
                return true;
            case R.id.modify:
                myStartActivity(WritePostActivity.class, postInfo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(PostInfo postInfo) {
            Log.e("로그 ","삭제 완료");
        }

        @Override
        public void onModify() {
            Log.e("로그 ","수정 완료");
        }
    };

    // <================================= 게시글 출력 업데이트 =================================>
    // input: -     output: -
    private void uiUpdate(){
        setToolbarTitle(postInfo.getTitle());
        readContentsVIew.setPostInfo(postInfo);
    }

    // <================================= 액티비티 호출, 인텐트로 postInfo 전달 =================================>
    // input: -     output: -
    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("postInfo", postInfo);
        startActivityForResult(intent, 0);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.messengerBtn:
                    mDatabase = FirebaseDatabase.getInstance();
                    dataRef = mDatabase.getReference();
                    //dataRef.removeValue();
                    myStartActivity(ChatActivity.class);
                    break;
            }
        }
    };

    // <================================= 액티비티 호출 =================================>
    // input: -     output: -
    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

}
