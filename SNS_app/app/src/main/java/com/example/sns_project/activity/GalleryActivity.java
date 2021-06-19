package com.example.sns_project.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.R;
import com.example.sns_project.adapter.GalleryAdapter;

import java.util.ArrayList;

import static com.example.sns_project.Util.GALLERY_IMAGE;
import static com.example.sns_project.Util.GALLERY_VIDEO;
import static com.example.sns_project.Util.INTENT_MEDIA;

public class GalleryActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setToolbarTitle("갤러리");

        if (ContextCompat.checkSelfPermission(GalleryActivity.this, // 권한이 있을 경우
                Manifest.permission.READ_EXTERNAL_STORAGE) // SD 권한을 가젼온다.
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GalleryActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
            if (ActivityCompat.shouldShowRequestPermissionRationale(GalleryActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
//                showToast(GalleryActivity.this, getResources().getString(R.string.please_grant_permission));
            }
        } else {
            recyclerInit();
        }
    }
    // <================================= 사용자 권한 요청 =================================>
    //input: 권한정보, 권한 허용 여부     output: -
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recyclerInit();
                } else {
                    finish();
//                    showToast(GalleryActivity.this, getResources().getString(R.string.please_grant_permission));
                }
            }
        }
    }

    // <================================= 사진 출력 리사이클 뷰 =================================>
    // input: -     output: -
    private void recyclerInit(){
        final int numberOfColumns = 3; //한줄에 사진 3개씩 출력

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); //레이아웃 넓이 변경 때문에 사용
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        RecyclerView.Adapter mAdapter = new GalleryAdapter(this, getImagesPath(this));
        recyclerView.setAdapter(mAdapter); // 어뎁터에 이미지 resizing 구현, glide api 사용
    }

    // <================================= 이미지 URL 획득 =================================>
    // input: recyclerInit() 액티비티     output: 이미지 URL
    public ArrayList<String> getImagesPath(Activity activity) {
        Uri uri;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        Cursor cursor;
        int column_index_data;
        String PathOfImage = null;
        String[] projection;

        Intent intent = getIntent();
        final int media = intent.getIntExtra(INTENT_MEDIA, GALLERY_IMAGE);
        if(media == GALLERY_VIDEO){
            uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            projection = new String[] { MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME }; // 비디오 파일 받기 위해 사용, exoplayer2 사용 예정
        }else{
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projection = new String[] { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME }; // 이미지 파일 받기 위해 사용
        }

        cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(PathOfImage);
        }
        return listOfAllImages;
    }
}
