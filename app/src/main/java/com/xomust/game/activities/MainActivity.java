package com.xomust.game.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.xomust.game.R;
import com.xomust.game.ai.AIPlayer;
import com.xomust.game.models.GameRoom;
import com.xomust.game.models.User;
import com.xomust.game.utils.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        firebaseManager = FirebaseManager.getInstance();
        
        // Check if user is logged in
        if (!firebaseManager.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        setupUI();
    }

    private void setupUI() {
        // Load user info and display welcome message
        String userId = firebaseManager.getCurrentUserId();
        firebaseManager.getUsersRef().child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    findViewById(R.id.tvWelcome).setVisibility(View.VISIBLE);
                    ((android.widget.TextView) findViewById(R.id.tvWelcome))
                        .setText("Welcome, " + user.getUsername() + "!");
                }
            }
        });

        findViewById(R.id.btnPlayOffline).setOnClickListener(v -> showOfflineModeDialog());
        findViewById(R.id.btnPlayOnline).setOnClickListener(v -> showOnlineModeDialog());
        findViewById(R.id.btnAbout).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        });
        findViewById(R.id.btnSignOut).setOnClickListener(v -> signOut());
    }

    private void showOfflineModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_offline_mode, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnPvP).setOnClickListener(v -> {
            dialog.dismiss();
            startGame(false, null);
        });

        dialogView.findViewById(R.id.btnPvAI).setOnClickListener(v -> {
            dialog.dismiss();
            showDifficultyDialog();
        });

        dialog.show();
    }

    private void showDifficultyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_difficulty, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnEasy).setOnClickListener(v -> {
            dialog.dismiss();
            startGame(true, AIPlayer.Difficulty.EASY);
        });

        dialogView.findViewById(R.id.btnMedium).setOnClickListener(v -> {
            dialog.dismiss();
            startGame(true, AIPlayer.Difficulty.MEDIUM);
        });

        dialogView.findViewById(R.id.btnHard).setOnClickListener(v -> {
            dialog.dismiss();
            startGame(true, AIPlayer.Difficulty.HARD);
        });

        dialog.show();
    }

    private void showOnlineModeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_online_mode, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnCreateRoom).setOnClickListener(v -> {
            dialog.dismiss();
            showRoomNameDialog(true);
        });

        dialogView.findViewById(R.id.btnJoinRoom).setOnClickListener(v -> {
            dialog.dismiss();
            showRoomNameDialog(false);
        });

        dialog.show();
    }

    private void showRoomNameDialog(boolean isCreating) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_room_name, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etRoomName = dialogView.findViewById(R.id.etRoomName);
        
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            if (roomName.isEmpty()) {
                Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            
            if (isCreating) {
                createRoom(roomName);
            } else {
                joinRoom(roomName);
            }
        });

        dialog.show();
    }

    private void createRoom(String roomName) {
        String userId = firebaseManager.getCurrentUserId();
        
        firebaseManager.getUsersRef().child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    GameRoom room = new GameRoom(roomName, roomName, userId, user.getUsername());
                    
                    firebaseManager.getRoomsRef().child(roomName).setValue(room)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(MainActivity.this, OnlineGameActivity.class);
                            intent.putExtra("roomName", roomName);
                            intent.putExtra("isCreator", true);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, 
                                "Failed to create room: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                }
            }
        });
    }

    private void joinRoom(String roomName) {
        String userId = firebaseManager.getCurrentUserId();
        
        firebaseManager.getRoomsRef().child(roomName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (!snapshot.exists()) {
                    Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                GameRoom room = snapshot.getValue(GameRoom.class);
                if (room != null && room.getPlayer2Id() != null) {
                    Toast.makeText(this, "Room is full", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                firebaseManager.getUsersRef().child(userId).get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                        User user = userTask.getResult().getValue(User.class);
                        if (user != null) {
                            firebaseManager.getRoomsRef().child(roomName)
                                .child("player2Id").setValue(userId);
                            firebaseManager.getRoomsRef().child(roomName)
                                .child("player2Username").setValue(user.getUsername());
                            
                            Intent intent = new Intent(MainActivity.this, OnlineGameActivity.class);
                            intent.putExtra("roomName", roomName);
                            intent.putExtra("isCreator", false);
                            startActivity(intent);
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Failed to join room", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame(boolean vsAI, AIPlayer.Difficulty difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("vsAI", vsAI);
        if (difficulty != null) {
            intent.putExtra("difficulty", difficulty.name());
        }
        startActivity(intent);
    }

    private void signOut() {
        firebaseManager.getAuth().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
