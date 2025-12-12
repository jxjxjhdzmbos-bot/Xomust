package com.xomust.game.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.xomust.game.R;
import com.xomust.game.models.User;
import com.xomust.game.utils.FirebaseManager;

import java.io.ByteArrayOutputStream;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseManager firebaseManager;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> signInLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView currentImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseManager = FirebaseManager.getInstance();
        progressBar = findViewById(R.id.progressBar);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup activity result launchers
        signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
            }
        );

        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (currentImageView != null) {
                        currentImageView.setImageURI(selectedImageUri);
                    }
                }
            }
        );

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseManager.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseManager.getCurrentUserId();
                        checkUserProfile(userId, account.getEmail());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserProfile(String userId, String email) {
        firebaseManager.getUsersRef().child(userId).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // User already has a profile
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // New user, show username dialog
                    showUsernameDialog(userId, email);
                }
            }
        });
    }

    private void showUsernameDialog(String userId, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_username, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        ImageView ivProfileImage = dialogView.findViewById(R.id.ivProfileImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnContinue = dialogView.findViewById(R.id.btnContinue);
        ProgressBar dialogProgressBar = dialogView.findViewById(R.id.progressBar);

        selectedImageUri = null;
        currentImageView = ivProfileImage;

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnContinue.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            dialogProgressBar.setVisibility(View.VISIBLE);
            btnContinue.setEnabled(false);

            // Check if username is unique
            firebaseManager.getUsersRef().orderByChild("username").equalTo(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            dialogProgressBar.setVisibility(View.GONE);
                            btnContinue.setEnabled(true);
                            Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            saveUserProfile(userId, email, username, ivProfileImage, dialog, dialogProgressBar, btnContinue);
                        }
                    }
                });
        });

        dialog.show();
    }

    private void saveUserProfile(String userId, String email, String username, 
                                 ImageView ivProfileImage, AlertDialog dialog, 
                                 ProgressBar dialogProgressBar, Button btnContinue) {
        if (selectedImageUri != null) {
            // Upload image to Firebase Storage
            firebaseManager.getProfileImagesRef().child(userId + ".jpg")
                .putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUserToDatabase(userId, email, username, uri.toString(), dialog, dialogProgressBar);
                    });
                })
                .addOnFailureListener(e -> {
                    dialogProgressBar.setVisibility(View.GONE);
                    btnContinue.setEnabled(true);
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
        } else {
            saveUserToDatabase(userId, email, username, null, dialog, dialogProgressBar);
        }
    }

    private void saveUserToDatabase(String userId, String email, String username, 
                                    String imageUrl, AlertDialog dialog, ProgressBar dialogProgressBar) {
        User user = new User(userId, email, username, imageUrl);
        firebaseManager.getUsersRef().child(userId).setValue(user)
            .addOnSuccessListener(aVoid -> {
                dialogProgressBar.setVisibility(View.GONE);
                dialog.dismiss();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                dialogProgressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            });
    }
}
