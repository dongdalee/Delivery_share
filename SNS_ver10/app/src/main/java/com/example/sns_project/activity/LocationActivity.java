package com.example.sns_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.example.sns_project.activity.MapsActivity;
import androidx.appcompat.app.AppCompatActivity;


import com.example.sns_project.R;
import com.example.sns_project.adapter.GpsTracker;
import com.example.sns_project.adapter.HomeAdapter;
import com.example.sns_project.fragment.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;

public class LocationActivity extends AppCompatActivity {


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
                    myStartActivity(MainActivity.class);
                    break;

            }
        }
    };

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 0);
    }
}