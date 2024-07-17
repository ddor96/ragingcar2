package com.example.coincollectioncargame;

public class Score implements Comparable<Score> {
    private int score;
    private double latitude;
    private double longitude;

    public Score() {
    }

    public Score(int score, double latitude, double longitude) {
        this.score = score;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getScore() {
        return score;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int compareTo(Score other) {
        return Integer.compare(other.score, this.score); // Sorting scores
    }
}
