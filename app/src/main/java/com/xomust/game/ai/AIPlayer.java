package com.xomust.game.ai;

import com.xomust.game.models.GameBoard;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    
    public enum Difficulty {
        EASY,    // Random moves
        MEDIUM,  // Blocks opponent and tries to win
        HARD     // Minimax algorithm
    }

    private Difficulty difficulty;
    private char aiSymbol;
    private char opponentSymbol;
    private Random random;

    public AIPlayer(Difficulty difficulty, char aiSymbol) {
        this.difficulty = difficulty;
        this.aiSymbol = aiSymbol;
        this.opponentSymbol = (aiSymbol == GameBoard.PLAYER_X) ? GameBoard.PLAYER_O : GameBoard.PLAYER_X;
        this.random = new Random();
    }

    public int[] getMove(GameBoard board) {
        switch (difficulty) {
            case EASY:
                return getRandomMove(board);
            case MEDIUM:
                return getMediumMove(board);
            case HARD:
                return getHardMove(board);
            default:
                return getRandomMove(board);
        }
    }

    private int[] getRandomMove(GameBoard board) {
        List<int[]> availableMoves = getAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return null;
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    private int[] getMediumMove(GameBoard board) {
        // First, try to win
        int[] winMove = findWinningMove(board, aiSymbol);
        if (winMove != null) {
            return winMove;
        }

        // Second, block opponent from winning
        int[] blockMove = findWinningMove(board, opponentSymbol);
        if (blockMove != null) {
            return blockMove;
        }

        // Third, take center if available
        if (board.getCell(1, 1) == GameBoard.EMPTY) {
            return new int[]{1, 1};
        }

        // Fourth, take a corner
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        List<int[]> availableCorners = new ArrayList<>();
        for (int[] corner : corners) {
            if (board.getCell(corner[0], corner[1]) == GameBoard.EMPTY) {
                availableCorners.add(corner);
            }
        }
        if (!availableCorners.isEmpty()) {
            return availableCorners.get(random.nextInt(availableCorners.size()));
        }

        // Otherwise, random move
        return getRandomMove(board);
    }

    private int[] getHardMove(GameBoard board) {
        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < GameBoard.SIZE; i++) {
            for (int j = 0; j < GameBoard.SIZE; j++) {
                if (board.getCell(i, j) == GameBoard.EMPTY) {
                    board.setCell(i, j, aiSymbol);
                    int score = minimax(board, 0, false);
                    board.setCell(i, j, GameBoard.EMPTY);

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{i, j};
                    }
                }
            }
        }

        return bestMove;
    }

    private int minimax(GameBoard board, int depth, boolean isMaximizing) {
        char winner = board.checkWinner();
        
        if (winner == aiSymbol) {
            return 10 - depth;
        } else if (winner == opponentSymbol) {
            return depth - 10;
        } else if (board.isFull()) {
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < GameBoard.SIZE; i++) {
                for (int j = 0; j < GameBoard.SIZE; j++) {
                    if (board.getCell(i, j) == GameBoard.EMPTY) {
                        board.setCell(i, j, aiSymbol);
                        int score = minimax(board, depth + 1, false);
                        board.setCell(i, j, GameBoard.EMPTY);
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < GameBoard.SIZE; i++) {
                for (int j = 0; j < GameBoard.SIZE; j++) {
                    if (board.getCell(i, j) == GameBoard.EMPTY) {
                        board.setCell(i, j, opponentSymbol);
                        int score = minimax(board, depth + 1, true);
                        board.setCell(i, j, GameBoard.EMPTY);
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    private int[] findWinningMove(GameBoard board, char player) {
        for (int i = 0; i < GameBoard.SIZE; i++) {
            for (int j = 0; j < GameBoard.SIZE; j++) {
                if (board.getCell(i, j) == GameBoard.EMPTY) {
                    board.setCell(i, j, player);
                    boolean isWin = board.checkWinner() == player;
                    board.setCell(i, j, GameBoard.EMPTY);
                    if (isWin) {
                        return new int[]{i, j};
                    }
                }
            }
        }
        return null;
    }

    private List<int[]> getAvailableMoves(GameBoard board) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < GameBoard.SIZE; i++) {
            for (int j = 0; j < GameBoard.SIZE; j++) {
                if (board.getCell(i, j) == GameBoard.EMPTY) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }
}
