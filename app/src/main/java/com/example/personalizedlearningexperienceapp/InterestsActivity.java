package com.example.personalizedlearningexperienceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;

public class InterestsActivity extends AppCompatActivity {
    private int userId;
    HashSet<Integer> selectedInterests = new HashSet<>();
    TextView textView1, textView2, textView3, textView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        // Initialize all choices
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);

        // Set click listeners
        textView1.setOnClickListener(view -> handleInterestSelection(1, textView1));
        textView2.setOnClickListener(view -> handleInterestSelection(2, textView2));
        textView3.setOnClickListener(view -> handleInterestSelection(3, textView3));
        textView4.setOnClickListener(view -> handleInterestSelection(4, textView4));

        userId = getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // The Next button
        Button nextButton = findViewById(R.id.buttonNext);
        nextButton.setOnClickListener(view -> {
            if (!selectedInterests.isEmpty()) {
                saveUserInterests();
            } else {
                Toast.makeText(InterestsActivity.this, "Please select at least one interest.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getInt("userId", -1);
    }

    private void handleInterestSelection(int interestId, TextView textView) {
        textView.setSelected(!textView.isSelected());
        updateTextViewBackground(textView);
        if (textView.isSelected()) {
            selectedInterests.add(interestId);
        } else {
            selectedInterests.remove(interestId);
        }
    }

    private void updateTextViewBackground(TextView textView) {
        textView.setBackgroundResource(R.drawable.selector_interest_background);
    }

    private void saveUserInterests() {
        JSONObject params = new JSONObject();
        try {
            JSONArray interestsArray = new JSONArray();
            for (Integer interest : selectedInterests) {
                interestsArray.put(interest);
            }
            params.put("userId", userId);
            params.put("interests", interestsArray);
            Log.d("SaveInterests", "Request params: " + params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SaveInterests", "JSON Exception: " + e.getMessage());
        }

        String saveInterestsUrl = "http://10.0.2.2:3000/user/interests";
        Log.d("SaveInterests", "Sending request to: " + saveInterestsUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, saveInterestsUrl,
                response -> {
                    Log.d("SaveInterests", "Response received: " + response);
                    Intent intent = new Intent(InterestsActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Log.e("SaveInterests", "Failed to save interests: " + error.toString());
                    Toast.makeText(InterestsActivity.this, "Failed to save interests", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("userId", userId);
                    jsonBody.put("interests", new JSONArray(selectedInterests));
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException e) {
                    Log.e("SaveInterests", "JSON exception", e);
                    return null;
                } catch (UnsupportedEncodingException uee) {
                    Log.e("SaveInterests", "Encoding problem", uee);
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}
