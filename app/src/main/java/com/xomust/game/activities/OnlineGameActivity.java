package com.xomust.game.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.xomust.game.R;
import com.xomust.game.models.GameBoard;
import com.xomust.game.models.GameRoom;
import com.xomust.game.utils.FirebaseManager;

public class OnlineGameActivity extends AppCompatActivity {

    private GameBoard gameBoard;
    private Button[][] buttons;
    private TextView tvGameStatus;
    private TextView tvRoomName;
    private TextView tvPlayerInfo;
    private Button btnPlayAgain;
    private Button btnBackToMenu;
    private ProgressBar progressBar;

    private FirebaseManager firebaseManager;
    private DatabaseReference roomRef;
    private ValueEventListener roomListener;
    
    private String roomName;
    private String currentUserId;
    private boolean isPlayer1;
    private char mySymbol;
    private char opponentSymbol;
    private boolean gameEnded = false;
    private boolean opponentJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        firebaseManager = FirebaseManager.getInstance();
        currentUserId = firebaseManager.getCurrentUserId();
        
        roomName = getIntent().getStringExtra("roomName");
        boolean isCreator = getIntent().getBooleanExtra("isCreator", false);

        gameBoard = new GameBoard();
        buttons = new Button[3][3];

        tvGameStatus = findViewById(R.id.tvGameStatus);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvPlayerInfo = findViewById(R.id.tvPlayerInfo);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        progressBar = findViewById(R.id.progressBar);

        tvRoomName.setText("Room: " + roomName);
        
        setupBoard();
        setupFirebaseListener();

        btnPlayAgain.setOnClickListener(v -> resetGame());
        btnBackToMenu.setOnClickListener(v -> {
            leaveRoom();
            finish();
        });
    }

    private void setupBoard() {
        GridLayout gridLayout = findViewById(R.id.gameBoard);
        gridLayout.removeAllViews();

        int size = 300;
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button(this);
                button.setTextSize(32);
                button.setTextColor(Color.BLACK);
                button.setBackgroundColor(Color.WHITE);
                
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = size / 3;
                params.height = size / 3;
                params.setMargins(2, 2, 2, 2);
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                button.setLayoutParams(params);

                final int row = i;
                final int col = j;
                button.setOnClickListener(v -> onCellClicked(row, col));

                buttons[i][j] = button;
                gridLayout.addView(button);
            }
        }
    }

    private void setupFirebaseListener() {
        roomRef = firebaseManager.getRoomsRef().child(roomName);
        
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(OnlineGameActivity.this, "Room deleted", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                GameRoom room = snapshot.getValue(GameRoom.class);
                if (room != null) {
                    updateGameState(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineGameActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        };

        roomRef.addValueEventListener(roomListener);
    }

    private void updateGameState(GameRoom room) {
        // Determine player roles
        if (room.getPlayer1Id().equals(currentUserId)) {
            isPlayer1 = true;
            mySymbol = GameBoard.PLAYER_X;
            opponentSymbol = GameBoard.PLAYER_O;
        } else {
            isPlayer1 = false;
            mySymbol = GameBoard.PLAYER_O;
            opponentSymbol = GameBoard.PLAYER_X;
        }

        // Check if opponent joined
        if (room.getPlayer2Id() != null && !opponentJoined) {
            opponentJoined = true;
            progressBar.setVisibility(View.GONE);
            tvPlayerInfo.setText("You: " + mySymbol + " | Opponent: " + opponentSymbol);
        } else if (room.getPlayer2Id() == null) {
            progressBar.setVisibility(View.VISIBLE);
            tvGameStatus.setText(R.string.waiting_for_opponent);
            return;
        }

        // Update board
        String boardState = room.getBoardState();
        if (boardState != null && boardState.length() == 9) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int index = i * 3 + j;
                    char cell = boardState.charAt(index);
                    gameBoard.setCell(i, j, cell);
                    
                    if (cell != ' ') {
                        buttons[i][j].setText(String.valueOf(cell));
                        buttons[i][j].setTextColor(cell == GameBoard.PLAYER_X ? 
                            getResources().getColor(R.color.player_x) : 
                            getResources().getColor(R.color.player_o));
                    } else {
                        buttons[i][j].setText("");
                    }
                }
            }
        }

        // Update game status
        if (room.isGameOver()) {
            gameEnded = true;
            btnPlayAgain.setVisibility(View.VISIBLE);
            
            if (room.getWinnerId() != null) {
                if (room.getWinnerId().equals(currentUserId)) {
                    tvGameStatus.setText(R.string.you_won);
                    tvGameStatus.setTextColor(getResources().getColor(R.color.player_x));
                } else {
                    tvGameStatus.setText(R.string.you_lost);
                    tvGameStatus.setTextColor(getResources().getColor(R.color.player_o));
                }
            } else {
                tvGameStatus.setText(R.string.draw);
                tvGameStatus.setTextColor(Color.GRAY);
            }
        } else {
            boolean isMyTurn = room.getCurrentTurnId().equals(currentUserId);
            if (isMyTurn) {
                tvGameStatus.setText(R.string.your_turn);
            } else {
                tvGameStatus.setText(R.string.opponent_turn);
            }
            tvGameStatus.setTextColor(getResources().getColor(R.color.primary));
        }
    }

    private void onCellClicked(int row, int col) {
        if (gameEnded || !opponentJoined) {
            return;
        }

        if (gameBoard.getCell(row, col) != GameBoard.EMPTY) {
            return;
        }

        roomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                GameRoom room = task.getResult().getValue(GameRoom.class);
                if (room != null && room.getCurrentTurnId().equals(currentUserId)) {
                    makeMove(row, col, room);
                } else {
                    Toast.makeText(this, "Not your turn!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void makeMove(int row, int col, GameRoom room) {
        // Update board state
        StringBuilder boardState = new StringBuilder(room.getBoardState());
        int index = row * 3 + col;
        boardState.setCharAt(index, mySymbol);
        
        // Update local game board
        gameBoard.setCell(row, col, mySymbol);
        
        // Check for winner
        char winner = gameBoard.checkWinner();
        boolean isFull = gameBoard.isFull();
        
        String nextTurnId = isPlayer1 ? room.getPlayer2Id() : room.getPlayer1Id();
        
        // Update Firebase
        roomRef.child("boardState").setValue(boardState.toString());
        roomRef.child("currentTurnId").setValue(nextTurnId);
        roomRef.child("lastMoveTime").setValue(System.currentTimeMillis());
        
        if (winner != GameBoard.EMPTY) {
            roomRef.child("winnerId").setValue(currentUserId);
            roomRef.child("gameOver").setValue(true);
        } else if (isFull) {
            roomRef.child("winnerId").setValue(null);
            roomRef.child("gameOver").setValue(true);
        }
    }

    private void resetGame() {
        gameBoard.reset();
        gameEnded = false;
        btnPlayAgain.setVisibility(View.GONE);
        
        roomRef.child("boardState").setValue("         ");
        roomRef.child("currentTurnId").setValue(roomRef.child("player1Id").toString());
        roomRef.child("winnerId").setValue(null);
        roomRef.child("gameOver").setValue(false);
        roomRef.child("lastMoveTime").setValue(System.currentTimeMillis());
    }

    private void leaveRoom() {
        if (roomListener != null) {
            roomRef.removeEventListener(roomListener);
        }
        
        // If player leaves, delete the room or notify opponent
        if (isPlayer1) {
            roomRef.removeValue();
        } else {
            roomRef.child("player2Id").removeValue();
            roomRef.child("player2Username").removeValue();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }
}
