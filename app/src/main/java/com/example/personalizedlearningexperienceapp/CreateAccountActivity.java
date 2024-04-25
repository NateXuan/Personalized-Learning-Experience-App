package com.example.personalizedlearningexperienceapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import android.content.ContentValues;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextConfirmEmail, editTextPassword, editTextConfirmPassword, editTextPhoneNumber;
    private final String url = "http://10.0.2.2:3000/register";
    private static final int CAMERA_REQUEST_CODE = 101;
    private ImageView imageViewProfile;
    private RequestQueue requestQueue;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize the views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmEmail = findViewById(R.id.editTextConfirmEmail);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        requestQueue = Volley.newRequestQueue(this);

        // Initialize the ActivityResultLauncher
//        activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    Log.d("CreateAccountActivity", "Activity result received");
//                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
//                        Log.d("CreateAccountActivity", "Result OK and data is not null");
//                        Bundle extras = result.getData().getExtras();
//                        Bitmap imageBitmap = (Bitmap) extras.get("data");
//                        imageViewProfile.setImageBitmap(imageBitmap);
//
////                        uploadImage(imageBitmap);
//                    } else {
//                        Log.d("CreateAccountActivity", "Result not OK or data is null");
//                    }
//                });

//        imageViewProfile.setOnClickListener(v -> {
//            requestCamera();
//        });

        buttonCreateAccount.setOnClickListener(v -> {
            registerNewUser();
        });
    }

    private void requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Authorization
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openCamera();
//            } else {
//                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Log.d("CreateAccountActivity", "openCamera() called");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri photoURI = createImageUri();
            if (photoURI != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activityResultLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "Error while creating file URI for image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("CreateAccountActivity", "No activity to handle camera intent.");
        }
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void uploadImage(Bitmap bitmap) {
        // Convert Bitmap to ByteArrayOutputStream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Convert ByteArrayOutputStream to RequestBody
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "profile.jpg", RequestBody.create(MediaType.parse("image/jpeg"), byteArray))
                .addFormDataPart("username", editTextUsername.getText().toString())
                .addFormDataPart("email", editTextEmail.getText().toString())
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(postBodyImage)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String imageUrl = jsonResponse.optString("url");
                            Toast.makeText(CreateAccountActivity.this, "Image uploaded to: " + imageUrl, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateAccountActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(CreateAccountActivity.this, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void registerNewUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String confirmEmail = editTextConfirmEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String phone = editTextPhoneNumber.getText().toString().trim();

        Log.d("CreateAccount", "Preparing to send data: Username: " + username + ", Email: " + email + ", Password: " + password + ", Phone: " + phone);

        // Check if any field is empty
        if (username.isEmpty() || email.isEmpty() || confirmEmail.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email matches confirm email
        if (!email.equals(confirmEmail)) {
            Toast.makeText(this, "Emails do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if password matches confirm password
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

//        // Validate email format
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Validate password strength
//        if (password.length() < 8) {
//            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Check for at least one number
//        if (!password.matches(".*\\d.*")) {
//            Toast.makeText(this, "Password must include at least one number", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Check for at least one letter
//        if (!password.matches(".*[a-zA-Z].*")) {
//            Toast.makeText(this, "Password must include at least one letter", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Check for at least one special character
//        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
//            Toast.makeText(this, "Password must include at least one special character", Toast.LENGTH_SHORT).show();
//            return;
//        }

        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("email", email);
            params.put("password", password);
            params.put("phone_number", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    try {
                        int userId = response.getInt("userId");
                        Log.d("CreateAccount", "User registered with ID: " + userId);

                        getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("userId", userId)
                                .apply();

                        Intent intent = new Intent(CreateAccountActivity.this, InterestsActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        Log.e("CreateAccount", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(CreateAccountActivity.this, "Register failed!", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}
