package com.xomust.game.models;

public class GameRoom {
    private String roomId;
    private String roomName;
    private String player1Id;
    private String player1Username;
    private String player2Id;
    private String player2Username;
    private String currentTurnId;
    private String boardState;  // 9 characters representing the board
    private String winnerId;
    private boolean isGameOver;
    private long lastMoveTime;

    public GameRoom() {
        // Required empty constructor for Firebase
    }

    public GameRoom(String roomId, String roomName, String player1Id, String player1Username) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.player1Id = player1Id;
        this.player1Username = player1Username;
        this.player2Id = null;
        this.player2Username = null;
        this.currentTurnId = player1Id;
        this.boardState = "         "; // 9 empty spaces
        this.winnerId = null;
        this.isGameOver = false;
        this.lastMoveTime = System.currentTimeMillis();
    }

    // Getters and setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public String getCurrentTurnId() {
        return currentTurnId;
    }

    public void setCurrentTurnId(String currentTurnId) {
        this.currentTurnId = currentTurnId;
    }

    public String getBoardState() {
        return boardState;
    }

    public void setBoardState(String boardState) {
        this.boardState = boardState;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }
}
