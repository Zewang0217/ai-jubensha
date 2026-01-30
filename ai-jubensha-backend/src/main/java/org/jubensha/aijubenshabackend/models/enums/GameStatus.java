package org.jubensha.aijubenshabackend.models.enums;

public enum GameStatus {
    CREATED,
    STARTED,
    PAUSED,
    ENDED,
    CANCELED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}