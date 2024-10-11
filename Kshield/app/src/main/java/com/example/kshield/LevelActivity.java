package com.example.kshield;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LevelActivity extends AppCompatActivity {
    private JSONArray problemsArray;
    private LinearLayout questionContainer;
    private Button submitButton, codeBoxButton;
    private ViewPager2 imagePager;
    private TabLayout tabLayout;
    private String codeBoxContent; // codeBox 내용을 저장할 변수
    private int currentLevel; // 현재 레벨을 저장하는 변수
    private int currentSubpage; // 현재 서브페이지를 저장하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        // Toolbar 요소 가져오기
        Toolbar levelToolbar = findViewById(R.id.levelToolbar);
        setSupportActionBar(levelToolbar);  // setSupportActionBar로 Toolbar 설정
        getSupportActionBar().setDisplayShowTitleEnabled(true); // 타이틀 표시 활성화

        // 이전 액티비티에서 전달된 레벨 정보 가져오기
        currentLevel = getIntent().getIntExtra("level", 1);
        currentSubpage = getIntent().getIntExtra("subpage", 1);

        // Toolbar의 제목을 현재 레벨로 설정
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Level " + currentLevel);
        }

        // XML에서 정의한 요소와 연결
        questionContainer = findViewById(R.id.questionContainer);
        imagePager = findViewById(R.id.imagePager);
        tabLayout = findViewById(R.id.tabLayout);
        submitButton = findViewById(R.id.submitButton);
        codeBoxButton = findViewById(R.id.codeBoxButton);

        // JSON 파일에서 해당 레벨의 문제 데이터 불러옴
        loadProblemsForSubpageAndLevel(currentSubpage, currentLevel);

        // 모든 문제를 동적으로 화면에 표시
        displayAllProblems();

        // 제출 버튼을 눌렀을 때 정답 확인
        submitButton.setOnClickListener(v -> checkAllAnswers());

        // 코드 해설 버튼을 눌렀을 때 codeBox 내용 표시
        codeBoxButton.setOnClickListener(v -> showCodeBoxDialog());
    }

    // JSON 파일에서 문제 데이터를 불러오는 메소드 (subpage와 level을 함께 사용)
    private void loadProblemsForSubpageAndLevel(int subpage, int level) {
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
            JSONArray subpagesArray = jsonObject.getJSONArray("subpages");

            // 해당 subpage와 level의 문제들을 찾음
            for (int i = 0; i < subpagesArray.length(); i++) {
                JSONObject subpageObject = subpagesArray.getJSONObject(i);
                if (subpageObject.getInt("subpage") == subpage) {
                    JSONArray levelsArray = subpageObject.getJSONArray("levels");
                    for (int j = 0; j < levelsArray.length(); j++) {
                        JSONObject levelObject = levelsArray.getJSONObject(j);
                        if (levelObject.getInt("level") == level) {
                            problemsArray = levelObject.getJSONArray("problems");
                            codeBoxContent = levelObject.optString("codeBox", "코드 해설이 없습니다.");
                            JSONArray imagesArray = levelObject.optJSONArray("images");
                            if (imagesArray != null) {
                                addLevelImages(imagesArray);
                            }
                            break;
                        }
                    }
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    // codeBox 내용 가져오기(코드해설)
                    codeBoxContent = levelObject.optString("codeBox", "코드 해설이 없습니다.");
                    // 문제 이미지 추가
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

                // RelativeLayout을 생성하여 TextView와 RadioGroup을 겹치지 않도록 설정
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // 질문을 표시할 TextView 생성
                TextView questionTextView = new TextView(this);
                RelativeLayout.LayoutParams questionParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                questionParams.setMargins(16, 45, 16, 16);  // TextView의 마진 설정
                questionTextView.setLayoutParams(questionParams);
                questionTextView.setId(View.generateViewId());  // TextView에 고유한 ID 설정
                questionTextView.setText(problem.getString("question"));
                questionTextView.setTextSize(17);
                questionTextView.setPadding(16, 16, 16, 16);

                // 선택지를 추가할 RadioGroup 생성
                RadioGroup choicesGroup = new RadioGroup(this);
                RelativeLayout.LayoutParams choicesParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                choicesParams.addRule(RelativeLayout.BELOW, questionTextView.getId());  // TextView 아래에 배치
                choicesParams.setMargins(16, 2, 16, 16);  // RadioGroup과 TextView 사이에 여백 추가
                choicesGroup.setLayoutParams(choicesParams);
                choicesGroup.setOrientation(RadioGroup.VERTICAL);
                choicesGroup.setId(View.generateViewId());  // RadioGroup에 고유한 ID 설정

                // 선택지 추가
                JSONArray choicesArray = problem.getJSONArray("choices");
                for (int j = 0; j < choicesArray.length(); j++) {
                    RadioButton choiceButton = new RadioButton(this);
                    choiceButton.setText(choicesArray.getString(j));
                    choicesGroup.addView(choiceButton);
                }

                // 채점 이미지를 추가할 ImageView 생성
                ImageView resultImage = new ImageView(this);
                RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                        500, 500);  // 이미지 크기 설정
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);  // 왼쪽에 배치
                imageParams.topMargin = 20;
                resultImage.setLayoutParams(imageParams);
                resultImage.setVisibility(View.GONE);  // 초기에는 보이지 않도록 설정

                // 문제풀이 버튼 추가
                Button solutionButton = new Button(this);
                solutionButton.setText("문제풀이");
                RelativeLayout.LayoutParams solutionButtonParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                solutionButtonParams.addRule(RelativeLayout.BELOW, choicesGroup.getId());  // RadioGroup 아래에 배치
                solutionButtonParams.setMargins(16, 32, 16, 50);  // RadioGroup과 문제풀이 버튼 사이의 여백 추가
                solutionButton.setLayoutParams(solutionButtonParams);

                solutionButton.setOnClickListener(v -> {
                    // "문제풀이" 버튼을 클릭하면 commentary 내용을 팝업으로 표시
                    showCommentaryDialog(problem.optString("commentary", "해설이 없습니다."));
                });

                // RelativeLayout에 각 요소 추가
                relativeLayout.addView(questionTextView);
                relativeLayout.addView(choicesGroup);
                relativeLayout.addView(resultImage);
                relativeLayout.addView(solutionButton);

                // 문제와 선택지를 Layout에 추가
                questionContainer.addView(relativeLayout);

                // RadioGroup에 태그로 인덱스 설정해두기 (정답 확인용)
                choicesGroup.setTag(i);

                // 정답을 확인할 때 이미지를 표시하기 위해 resultImage를 태그로 저장
                choicesGroup.setTag(R.id.resultImage, resultImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 코드 해설을 보여주는 다이얼로그
    private void showCodeBoxDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.codeboxbuttons);  // custom_dialog.xml을 사용

        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        dialogMessage.setText(codeBoxContent);  // codeBox 내용을 표시

        // 닫기 버튼 설정 (우측 상단 X 버튼)
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // 다이얼로그 크기와 위치 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(params);
        dialog.show();
    }


    // NEXT!! 버튼 클릭 시 다음 레벨로 이동

    private void goToNextLevel() {
        // 다음 레벨이 존재하는지 확인
        if (currentLevel < 3) {  // 최대 10레벨까지만 있다고 가정 ,수정하기 3으로
            int nextLevel = currentLevel + 1;

            // 다음 레벨 페이지로 이동
            Intent intent = new Intent(LevelActivity.this, LevelActivity.class);
            intent.putExtra("level", nextLevel);
            startActivity(intent);
            finish();  // 현재 액티비티 종료
        } else {
            // 최대 레벨을 넘었을 경우, 메인 화면으로 돌아가기
            Intent intent = new Intent(LevelActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    // 문제풀이 다이얼로그를 보여주는 메소드
    private void showCommentaryDialog(String commentary) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);  // custom_dialog.xml을 사용

        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        dialogMessage.setText(commentary);  // commentary 내용을 표시

        // 닫기 버튼 설정
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // 다이얼로그 크기와 위치 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    // 정답 제출 다이얼로그 생성
    private void showCustomDialog(String message, boolean allCorrect) {
        // 다이얼로그 생성
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);  // custom_dialog.xml을 다이얼로그로 사용
        dialog.setCancelable(true);  // 외부를 탭하면 닫히도록 설정

        // 다이얼로그 메시지 설정
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        dialogMessage.setText(message);

        // 닫기 버튼 설정
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());  // 클릭 시 다이얼로그 닫기

        // NEXT!! 버튼 설정 (정답이 모두 맞았을 경우에만 보임)
        Button nextButton = dialog.findViewById(R.id.nextButton);
        if (allCorrect) {
            nextButton.setVisibility(View.VISIBLE);  // 정답이 모두 맞으면 보이게 설정
            nextButton.setOnClickListener(v -> {
                dialog.dismiss();
                goToNextLevel(); // 다음 레벨로 이동
            });
        } else {
            nextButton.setVisibility(View.GONE);  // 정답이 맞지 않으면 숨김
        }

        // 다이얼로그 크기와 위치 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;  // 화면 중앙에 위치

        dialog.getWindow().setAttributes(params);  // 설정 적용
        dialog.show();  // 다이얼로그 표시
    }


    // 모든 문제의 정답을 확인하는 메소드 내에 다이얼로그 호출
    // 모든 문제의 정답을 확인하는 메소드
    private void checkAllAnswers() {
        try {
            boolean allCorrect = true;
            boolean hasUnansweredQuestions = false;

            // questionContainer에 있는 문제들을 확인
            for (int i = 0; i < questionContainer.getChildCount(); i++) {
                View child = questionContainer.getChildAt(i);

                // RelativeLayout을 사용하여 문제를 처리
                if (child instanceof RelativeLayout) {
                    RelativeLayout relativeLayout = (RelativeLayout) child;
                    RadioGroup choicesGroup = (RadioGroup) relativeLayout.getChildAt(1); // 두 번째 자식인 RadioGroup을 가져옴
                    int problemIndex = (int) choicesGroup.getTag(); // 문제 인덱스 가져오기

                    // 선택한 답을 확인
                    int selectedId = choicesGroup.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        hasUnansweredQuestions = true;  // 답을 선택하지 않은 문제가 있음
                        continue;
                    }

                    RadioButton selectedButton = findViewById(selectedId);
                    int selectedAnswerIndex = choicesGroup.indexOfChild(selectedButton); // 선택된 버튼의 인덱스 가져오기

                    // JSON에서 정답을 확인
                    JSONObject problem = problemsArray.getJSONObject(problemIndex);
                    int correctAnswer = problem.getInt("correctAnswer");

                    // 태그로 저장해둔 resultImage 가져오기
                    ImageView resultImage = (ImageView) choicesGroup.getTag(R.id.resultImage);
                    resultImage.setVisibility(View.VISIBLE);  // 채점 이미지 표시

                    // 정답 여부에 따라 채점 이미지 설정
                    if (selectedAnswerIndex == correctAnswer) {
                        resultImage.setImageResource(R.drawable.correct);  // 정답일 경우
                    } else {
                        resultImage.setImageResource(R.drawable.wrong);  // 오답일 경우
                        allCorrect = false;  // 틀린 답이 있으면 allCorrect를 false로 설정
                    }
                }
            }

            // 결과에 따라 다이얼로그 표시
            if (hasUnansweredQuestions) {
                showCustomDialog("모든 문제에 답을 선택해주세요.", false);
            } else if (allCorrect) {
                showCustomDialog("모든 문제의 답이 맞습니다!", true);
            } else {
                showCustomDialog("틀린 답이 있습니다. 다시 확인해주세요.", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}