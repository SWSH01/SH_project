package com.example.kshield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class test9_241004_subpage1 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_subpage1);

        // Find the back button and set a click listener
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the current activity (similar to a back action)
                finish();
            }
        });

        // Find the LinearLayout that will hold the level buttons
        LinearLayout levelContainer = findViewById(R.id.levelContainer);

        // Create 10 buttons with level text
        for (int i = 1; i <= 10; i++) {
            // Create a new Button for each level
            Button levelButton = new Button(this);
            levelButton.setText("레벨 " + i);
            levelButton.setTextSize(18);
            levelButton.setPadding(16, 16, 16, 16);

            // Set layout parameters for each button
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);  // Add some margin between buttons
            levelButton.setLayoutParams(params);

            // Set a click listener for each button
            final int level = i;  // Store the current level
            levelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start LevelActivity with the selected level
                    Intent intent = new Intent(test9_241004_subpage1.this, LevelActivity.class);
                    intent.putExtra("level", level);
                    startActivity(intent);
                }
            });

            // Add each level Button to the container
            levelContainer.addView(levelButton);
        }
    }
}
