package com.xomust.game.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xomust.game.R;
import com.xomust.game.ai.AIPlayer;
import com.xomust.game.models.GameBoard;

public class GameActivity extends AppCompatActivity {

    private GameBoard gameBoard;
    private Button[][] buttons;
    private TextView tvGameStatus;
    private TextView tvPlayerInfo;
    private Button btnPlayAgain;
    private Button btnBackToMenu;
    
    private boolean isVsAI;
    private AIPlayer aiPlayer;
    private boolean isPlayerXTurn = true;
    private boolean gameEnded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameBoard = new GameBoard();
        buttons = new Button[3][3];

        tvGameStatus = findViewById(R.id.tvGameStatus);
        tvPlayerInfo = findViewById(R.id.tvPlayerInfo);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        isVsAI = getIntent().getBooleanExtra("vsAI", false);
        
        if (isVsAI) {
            String difficultyStr = getIntent().getStringExtra("difficulty");
            AIPlayer.Difficulty difficulty = AIPlayer.Difficulty.valueOf(difficultyStr);
            aiPlayer = new AIPlayer(difficulty, GameBoard.PLAYER_O);
            tvPlayerInfo.setText("You are X, AI is O");
        } else {
            tvPlayerInfo.setText("Player X vs Player O");
        }

        setupBoard();
        updateStatus();

        btnPlayAgain.setOnClickListener(v -> resetGame());
        btnBackToMenu.setOnClickListener(v -> finish());
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

    private void onCellClicked(int row, int col) {
        if (gameEnded || gameBoard.getCell(row, col) != GameBoard.EMPTY) {
            return;
        }

        if (isVsAI && !isPlayerXTurn) {
            return; // It's AI's turn
        }

        makeMove(row, col);
    }

    private void makeMove(int row, int col) {
        if (gameBoard.makeMove(row, col)) {
            updateButton(row, col, gameBoard.getCurrentPlayer());
            
            if (checkGameEnd()) {
                return;
            }

            gameBoard.switchPlayer();
            isPlayerXTurn = !isPlayerXTurn;
            updateStatus();

            if (isVsAI && !isPlayerXTurn && !gameEnded) {
                // AI's turn
                new Handler().postDelayed(this::makeAIMove, 500);
            }
        }
    }

    private void makeAIMove() {
        if (gameEnded) return;

        int[] move = aiPlayer.getMove(gameBoard);
        if (move != null) {
            makeMove(move[0], move[1]);
        }
    }

    private void updateButton(int row, int col, char player) {
        buttons[row][col].setText(String.valueOf(player));
        buttons[row][col].setTextColor(player == GameBoard.PLAYER_X ? 
            getResources().getColor(R.color.player_x) : 
            getResources().getColor(R.color.player_o));
    }

    private boolean checkGameEnd() {
        char winner = gameBoard.checkWinner();
        
        if (winner != GameBoard.EMPTY) {
            gameEnded = true;
            if (isVsAI) {
                if (winner == GameBoard.PLAYER_X) {
                    tvGameStatus.setText(R.string.you_won);
                    tvGameStatus.setTextColor(getResources().getColor(R.color.player_x));
                } else {
                    tvGameStatus.setText(R.string.you_lost);
                    tvGameStatus.setTextColor(getResources().getColor(R.color.player_o));
                }
            } else {
                tvGameStatus.setText("Player " + winner + " Wins!");
                tvGameStatus.setTextColor(winner == GameBoard.PLAYER_X ? 
                    getResources().getColor(R.color.player_x) : 
                    getResources().getColor(R.color.player_o));
            }
            btnPlayAgain.setVisibility(View.VISIBLE);
            return true;
        }
        
        if (gameBoard.isFull()) {
            gameEnded = true;
            tvGameStatus.setText(R.string.draw);
            tvGameStatus.setTextColor(Color.GRAY);
            btnPlayAgain.setVisibility(View.VISIBLE);
            return true;
        }
        
        return false;
    }

    private void updateStatus() {
        if (!gameEnded) {
            if (isVsAI) {
                if (isPlayerXTurn) {
                    tvGameStatus.setText(R.string.your_turn);
                } else {
                    tvGameStatus.setText("AI's Turn");
                }
            } else {
                tvGameStatus.setText("Player " + (isPlayerXTurn ? "X" : "O") + "'s Turn");
            }
            tvGameStatus.setTextColor(getResources().getColor(R.color.primary));
        }
    }

    private void resetGame() {
        gameBoard.reset();
        isPlayerXTurn = true;
        gameEnded = false;
        btnPlayAgain.setVisibility(View.GONE);
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }
        
        updateStatus();
    }
}
