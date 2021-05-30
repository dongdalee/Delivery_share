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

public class PostActivity extends BasicActivity {
    private PostInfo postInfo;
    private FirebaseHelper firebaseHelper;
    private ReadContentsVIew readContentsVIew;
    private LinearLayout contentsLayout;
    private Button messengerBtn;


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    postInfo = (PostInfo)data.getSerializableExtra("postinfo");
                    contentsLayout.removeAllViews();
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

    private void uiUpdate(){
        setToolbarTitle(postInfo.getTitle());
        readContentsVIew.setPostInfo(postInfo);
    }

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
                    myStartActivity(ChatActivity.class);
                    break;
            }
        }
    };

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

}
