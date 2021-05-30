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
    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    // Views
    private ListView mListView;
    private EditText mEdtMessage;
    // Values
    private ChatAdapter mAdapter;
    private String userName;

    ///////////////
    DatabaseReference mDatabase ;
    private FirebaseAuth mAuth;
    private static final String TAG = "UserInfoFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();
        initFirebaseDatabase();
        initValues();

    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.list_message);
        mAdapter = new ChatAdapter(this, 0);
        mListView.setAdapter(mAdapter);

        mEdtMessage = (EditText) findViewById(R.id.edit_message);
        findViewById(R.id.btn_send).setOnClickListener(this);
    }

    private void initFirebaseDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("message");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatData chatData = dataSnapshot.getValue(ChatData.class);
                chatData.firebaseKey = dataSnapshot.getKey();
                mAdapter.add(chatData);
                mListView.smoothScrollToPosition(mAdapter.getCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String firebaseKey = dataSnapshot.getKey();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (mAdapter.getItem(i).firebaseKey.equals(firebaseKey)) {
                        mAdapter.remove(mAdapter.getItem(i));
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private void initValues() {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    userName = document.getData().get("name").toString();
                }
            }
        });
    }

//
//    @Override
//    protected void onDestory() {
//        super.onDestory();
//        mDatabaseReference.removeEventListener(mChildEventListener);
//    }

    @Override
    public void onClick(View v) {
        String message = mEdtMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            mEdtMessage.setText("");
            ChatData chatData = new ChatData();
            chatData.userName = userName;
            chatData.message = message;
            chatData.time = System.currentTimeMillis();
            mDatabaseReference.push().setValue(chatData);
        }
    }
}
