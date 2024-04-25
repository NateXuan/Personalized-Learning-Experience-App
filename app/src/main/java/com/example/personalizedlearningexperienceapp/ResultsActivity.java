package com.example.personalizedlearningexperienceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        ArrayList<Boolean> results = (ArrayList<Boolean>) getIntent().getSerializableExtra("results");
        LinearLayout resultsContainer = findViewById(R.id.resultsContainer);

        for (int i = 0; i < results.size(); i++) {
            // Create a TextView for the question number
            TextView questionView = new TextView(this);
            questionView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            questionView.setText(String.format(Locale.getDefault(), "Question %d", i + 1));
            questionView.setTextSize(18);
            questionView.setPadding(0, 10, 0, 10);
            resultsContainer.addView(questionView);

            // Create a TextView for the answer correctness
            TextView resultView = new TextView(this);
            resultView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            resultView.setText(results.get(i) ? "Correct" : "Incorrect");
            resultView.setTextSize(16);
            resultView.setTextColor(results.get(i) ? getColor(R.color.correct_answer_color) : getColor(R.color.wrong_answer_color));
            resultView.setPadding(0, 0, 0, 10);
            resultsContainer.addView(resultView);
        }

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, HomePageActivity.class);
            // FLAG_ACTIVITY_CLEAR_TOP ensures that all other activities on top of the HomePageActivity are cleared.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }
}

