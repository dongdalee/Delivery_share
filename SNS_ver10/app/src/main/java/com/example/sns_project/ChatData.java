package com.example.sns_project;

// 위에 클래스에서 현재 단계에서 실제로 사용할 값은 firebaseKey, userName, message, time 4개 항목 입니다.
public class ChatData {

    public String firebaseKey; // Firebase Realtime Database 에 등록된 Key 값
    public String userName; // 사용자 이름
    public String userPhotoUrl; // 사용자 사진 URL
    public String userEmail; // 사용자 이메일주소
    public String message; // 작성한 메시지
    public long time; // 작성한 시간


}
