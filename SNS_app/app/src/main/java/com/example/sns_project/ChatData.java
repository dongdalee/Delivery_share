package com.example.sns_project;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatData {

    public String firebaseKey; // Firebase Realtime Database 에 등록된 Key 값
    public String userName; // 사용자 이름
    public String message; // 작성한 메시지
    public long time; // 작성한 시간

    // public String postNum // postKey로 구분할 변수
}
