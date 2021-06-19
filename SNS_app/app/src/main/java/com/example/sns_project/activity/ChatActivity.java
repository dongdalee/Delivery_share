package com.example.sns_project.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.sns_project.ChatData;
import com.example.sns_project.R;
import com.example.sns_project.UserInfo;
import com.example.sns_project.adapter.ChatAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ListView mListView;
    private EditText mEdtMessage;

    private ChatAdapter mAdapter;
    private String userName;
    private String postKey = "temp_PostKey"; // 임시 postHash


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();
        initFirebaseDatabase();
        initValues();

    }

    // <================================= 변수 정의 =================================>
    // input: -     output: -
    private void initViews() {
        mListView = (ListView) findViewById(R.id.list_message);
        mAdapter = new ChatAdapter(this, 0);
        mListView.setAdapter(mAdapter); // 어댑터 설정
        mEdtMessage = (EditText) findViewById(R.id.edit_message);
        findViewById(R.id.btn_send).setOnClickListener(this);
    }

    // <================================= realtime DB 설정 =================================>
    // input: -     output: -
    private void initFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // realtime DB 접근할 수 있는 진입점
        mDatabaseReference = mFirebaseDatabase.getReference(postKey); // postKey child(subtree)로 이동
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) { // 데이터가 추가되었을 때
                ChatData chatData = dataSnapshot.getValue(ChatData.class);
                chatData.firebaseKey = dataSnapshot.getKey();
                mAdapter.add(chatData); // 어댑터에 데이터 추가
                mListView.smoothScrollToPosition(mAdapter.getCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // 데이터가 수정되었을 때
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { // 데이터가 제거되었을 때
                String firebaseKey = dataSnapshot.getKey();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter.getItem(i).firebaseKey.equals(firebaseKey)) { // 해당 아이템의 firebasekey가 현 key와 같을 경우
                        mAdapter.remove(mAdapter.getItem(i)); // 어댑터의 데이터 삭제
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // 데이터의 리스트 위치가 변경되었을 때
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터의 오류가 발생하였을 때
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener); // 만든 리스너를 등록
    }

    // <================================= 변수 추가 설정 =================================>
    // input: -     output: -
    private void initValues() {
        DocumentReference documentReference = FirebaseFirestore.getInstance().
                collection("users").document(FirebaseAuth.getInstance().
                getCurrentUser().getUid()); // 접속한 현재 user에 대한 데이터 가져옴
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    userName = document.getData().get("name").toString(); // 현재 user의 name을 채팅시 사용할 userName으로 설정
                }
            }
        });
    }

    // <================================= 전송 버튼 클릭시 DB 업로드=================================>
    // input: -     output: -
    @Override
    public void onClick(View v) {
        mDatabaseReference = mFirebaseDatabase.getReference(); // realtime DB로 이동
        String message = mEdtMessage.getText().toString(); //
        if (!TextUtils.isEmpty(message)) { // message 값이 존재하면
            mEdtMessage.setText("");
            ChatData chatData = new ChatData();
            chatData.userName = userName;
            chatData.message = message;
            chatData.time = System.currentTimeMillis();
            mDatabaseReference.child(postKey).push().setValue(chatData); //postKey라는 child에 chatData 인스턴스 DB 업로드
        }
    }
}
