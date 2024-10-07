package com.example.kshield;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 박스 1 버튼 클릭 시 (시스템 해킹)
        Button box1 = findViewById(R.id.box1);
        box1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, subpage1.class);
                startActivity(intent);
            }
        });

        // 박스 2 버튼 클릭 시 (웹 해킹)
        Button box2 = findViewById(R.id.box2);
        box2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, subpage2.class);
                startActivity(intent);
            }
        });

        // 박스 3 버튼 클릭 시 (심화문제)
        Button box3 = findViewById(R.id.box3);
        box3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, subpage3.class);
                startActivity(intent);
            }
        });


        // DisplayMetrics 객체를 사용해 화면 밀도 가져오기
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float density = displayMetrics.density;

        // 1080x2280 px을 dp로 변환
        int widthPx = 1080;
        int heightPx = 2280;

        // 픽셀을 dp로 변환
        float widthDp = widthPx / density;
        float heightDp = heightPx / density;

        // 로그로 결과 출력
        Log.d("ScreenSize", "화면 너비 (dp): " + widthDp);
        Log.d("ScreenSize", "화면 높이 (dp): " + heightDp);
    }
}
