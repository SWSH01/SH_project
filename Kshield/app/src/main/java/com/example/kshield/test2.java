package com.example.kshield;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class test2 extends AppCompatActivity {
    private JSONArray problemsArray;
    private LinearLayout questionContainer;
    private Button submitButton;
    private ViewPager2 imagePager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        // XML에서 정의한 요소와 연결
        questionContainer = findViewById(R.id.questionContainer);
        imagePager = findViewById(R.id.imagePager); // ViewPager2 연결
        tabLayout = findViewById(R.id.tabLayout); // TabLayout 연결 (동그라미 표시용)
        submitButton = findViewById(R.id.submitButton);

        // 이전 액티비티에서 전달된 레벨 정보 가져오기
        int level = getIntent().getIntExtra("level", 1);

        // JSON 파일에서 해당 레벨의 문제 데이터 불러옴
        loadProblemsForLevel(level);

        // 모든 문제를 동적으로 화면에 표시
        displayAllProblems();

        // 제출 버튼을 눌렀을 때 정답 확인
        submitButton.setOnClickListener(v -> checkAllAnswers());
    }

    // JSON 파일에서 문제 데이터를 불러오는 메소드
    private void loadProblemsForLevel(int level) {
        try {
            InputStream is = getResources().openRawResource(R.raw.problems);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonString = jsonBuilder.toString();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray levelsArray = jsonObject.getJSONArray("levels");

            // 해당 레벨의 문제들을 찾음
            for (int i = 0; i < levelsArray.length(); i++) {
                JSONObject levelObject = levelsArray.getJSONObject(i);
                if (levelObject.getInt("level") == level) {
                    problemsArray = levelObject.getJSONArray("problems");

                    // 이미지 추가
                    JSONArray imagesArray = levelObject.optJSONArray("images");
                    if (imagesArray != null) {
                        addLevelImages(imagesArray);
                    }
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 레벨 상단의 이미지를 추가하는 메소드 (ViewPager2 설정)
    private void addLevelImages(JSONArray imagesArray) {
        try {
            String[] imageNames = new String[imagesArray.length()];
            for (int i = 0; i < imagesArray.length(); i++) {
                imageNames[i] = imagesArray.getString(i);
            }

            ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageNames);
            imagePager.setAdapter(adapter);

            // TabLayout과 ViewPager2를 연결하여 동그라미 표시
            new TabLayoutMediator(tabLayout, imagePager, (tab, position) -> {}).attach();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 모든 문제를 화면에 동적으로 추가하는 메소드
    private void displayAllProblems() {
        try {
            for (int i = 0; i < problemsArray.length(); i++) {
                JSONObject problem = problemsArray.getJSONObject(i);

                // FrameLayout 생성 (질문과 이미지를 겹치게 배치)
                FrameLayout frameLayout = new FrameLayout(this);
                frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // 질문을 표시할 TextView 생성
                TextView questionTextView = new TextView(this);
                questionTextView.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                questionTextView.setText(problem.getString("question"));
                questionTextView.setTextSize(18);
                questionTextView.setPadding(16, 16, 16, 16);

                // 선택지를 추가할 RadioGroup 생성
                RadioGroup choicesGroup = new RadioGroup(this);
                choicesGroup.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                choicesGroup.setOrientation(RadioGroup.VERTICAL);

                // 선택지 추가
                JSONArray choicesArray = problem.getJSONArray("choices");
                for (int j = 0; j < choicesArray.length(); j++) {
                    RadioButton choiceButton = new RadioButton(this);
                    choiceButton.setText(choicesArray.getString(j));
                    choicesGroup.addView(choiceButton);
                }

                // 채점 이미지를 겹쳐서 추가할 ImageView 생성
                ImageView resultImage = new ImageView(this);
                resultImage.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                resultImage.setVisibility(View.GONE); // 초기에는 보이지 않게 설정

                // FrameLayout에 질문 텍스트와 채점 이미지를 추가
                frameLayout.addView(questionTextView);
                frameLayout.addView(resultImage);

                // 문제와 선택지를 Layout에 추가
                questionContainer.addView(frameLayout);
                questionContainer.addView(choicesGroup);

                // 각 문제마다 RadioGroup에 태그로 인덱스를 설정해두기 (나중에 정답 확인용)
                choicesGroup.setTag(i);

                // 정답을 확인할 때 이미지를 표시하기 위해 resultImage를 태그로 저장
                choicesGroup.setTag(R.id.resultImage, resultImage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 모든 문제의 정답을 확인하는 메소드
    private void checkAllAnswers() {
        try {
            boolean allCorrect = true;

            // questionContainer의 모든 자식 요소를 검사
            for (int i = 0; i < questionContainer.getChildCount(); i++) {
                View child = questionContainer.getChildAt(i);

                // 만약 이 자식 뷰가 RadioGroup일 경우에만 처리
                if (child instanceof RadioGroup) {
                    RadioGroup choicesGroup = (RadioGroup) child;
                    int problemIndex = (int) choicesGroup.getTag();

                    // 선택한 답 확인
                    int selectedId = choicesGroup.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        Toast.makeText(this, "모든 문제에 답을 선택해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RadioButton selectedButton = findViewById(selectedId);
                    int selectedAnswerIndex = choicesGroup.indexOfChild(selectedButton);

                    // 정답 확인
                    JSONObject problem = problemsArray.getJSONObject(problemIndex);
                    int correctAnswer = problem.getInt("correctAnswer");

                    // 태그로 저장해둔 resultImage를 가져옴
                    ImageView resultImage = (ImageView) choicesGroup.getTag(R.id.resultImage);
                    resultImage.setVisibility(View.VISIBLE); // 이미지 표시

                    // 정답 여부에 따라 채점 이미지 설정
                    if (selectedAnswerIndex == correctAnswer) {
                        resultImage.setImageResource(R.drawable.correct);  // 정답 이미지
                        allCorrect = allCorrect && true;
                    } else {
                        resultImage.setImageResource(R.drawable.wrong);  // 오답 이미지
                        allCorrect = false;
                    }
                }
            }

            if (allCorrect) {
                Toast.makeText(this, "모든 문제의 답이 맞습니다!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "틀린 답이 있습니다. 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
