package com.risiko.model;

public enum Player {
    NONE, RED, BLUE;

    public Player other() {
        if (this == RED) return BLUE;
        if (this == BLUE) return RED;
        return NONE;
    }
}