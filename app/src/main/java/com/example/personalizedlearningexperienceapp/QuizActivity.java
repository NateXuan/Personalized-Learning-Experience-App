package com.example.personalizedlearningexperienceapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {
    private LinearLayout quizContainer;
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizContainer = findViewById(R.id.quizContainer);
        String topic = getIntent().getStringExtra("topic");

        if (topic != null && !topic.isEmpty()) {
            fetchQuiz(topic);
        }
    }

    private void fetchQuiz(String interest) {
        int maxQuestions = 3;
        if (currentQuestionIndex >= maxQuestions) {
            addSubmitButton();
            return;
        }

//        quizContainer.removeAllViews();
        String encodedInterest = Uri.encode(interest);
        String url = "http://10.0.2.2:5000/getQuiz?topic=" + encodedInterest;
        Log.d("QuizActivity", "Fetching quiz with URL: " + url);


        JsonObjectRequest quizRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("QuizActivity", "Response received: " + response.toString());
                    try {
                        JSONArray quizQuestions = response.getJSONArray("quiz");
                        Log.d("QuizActivity", "Total questions received: " + quizQuestions.length());
                        if (quizQuestions.length() > 0) {
                                JSONObject questionObject = quizQuestions.getJSONObject(0);
                                String questionText = questionObject.getString("question");
                                JSONArray options = questionObject.getJSONArray("options");
                                String correctAnswer = questionObject.getString("correct_answer");
                                Log.d("QuizActivity", "Correct Answer " + correctAnswer);
                                Log.d("QuizActivity", "Adding question " + (currentQuestionIndex + 1) + ": " + questionText);
                                addQuestionView(questionText, options, correctAnswer, currentQuestionIndex + 1);
                                currentQuestionIndex++;
                                fetchQuiz(interest);
                        } else {
                            addSubmitButton();
                        }
                    } catch (JSONException e) {
                        Log.e("QuizActivity", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(QuizActivity.this, "Error parsing quiz data", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("QuizActivity", "Request error: " + error.toString());
                    Toast.makeText(QuizActivity.this, "Internet error", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(
                        180000, // 3 minutes
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(quizRequest);
    }

    private void addQuestionView(String questionText, JSONArray options, String correctAnswer, int questionNumber) throws JSONException {
        TextView questionView = new TextView(this);
        questionView.setText(questionText);
        questionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        quizContainer.addView(questionView);

        //Create RadioGroup
        RadioGroup optionsGroup = new RadioGroup(this);
        optionsGroup.setTag("Question" + questionNumber);
        optionsGroup.setOrientation(RadioGroup.VERTICAL);

        int correctIndex = correctAnswer.charAt(0) - 'A';
        for (int j = 0; j < options.length(); j++) {
            RadioButton optionButton = new RadioButton(this);
            optionButton.setId(ViewCompat.generateViewId());
            optionButton.setText(options.getString(j));

            // Check if the current index is the correct answer index
            if (j == correctIndex) {
                optionButton.setTag("correct");
                Log.d("QuizActivity", "Option: " + options.getString(j) + ", Tag: correct");
            } else {
                optionButton.setTag("incorrect");
                Log.d("QuizActivity", "Option: " + options.getString(j) + ", Tag: incorrect");
            }
            optionsGroup.addView(optionButton);
        }

        quizContainer.addView(optionsGroup);
    }

    private void addSubmitButton() {
        Button submitButton = new Button(this);
        submitButton.setText("Submit");
        submitButton.setOnClickListener(v -> {
            ArrayList<Boolean> results = checkAnswers();
            Intent resultsIntent = new Intent(QuizActivity.this, ResultsActivity.class);
            resultsIntent.putExtra("results", results);
            startActivity(resultsIntent);
        });
        quizContainer.addView(submitButton);
    }

    private ArrayList<Boolean> checkAnswers() {
        ArrayList<Boolean> results = new ArrayList<>();
        for (int i = 1; i <= quizContainer.getChildCount(); i++) {
            RadioGroup optionsGroup = quizContainer.findViewWithTag("Question" + i);
            if (optionsGroup != null) {
                int selectedId = optionsGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton selectedOption = findViewById(selectedId);
                    boolean isCorrect = "correct".equals(selectedOption.getTag());
                    results.add(isCorrect);
                    Log.d("QuizActivity", "Question " + i + ": " + (isCorrect ? "Correct" : "Incorrect"));
                } else {
                    results.add(false);
                }
            }
        }
        return results;
    }
}

