package com.petermarshall;

public class BetPlacedUniBet {
    private double oddsOffered;
    private double stake;
    private boolean betSuccessful;

    public BetPlacedUniBet(double oddsOffered, double stake, boolean betSuccessful) {
        this.oddsOffered = oddsOffered;
        this.stake = stake;
        this.betSuccessful = betSuccessful;
    }

    public double getOddsOffered() {
        return oddsOffered;
    }

    public double getStake() {
        return stake;
    }

    public boolean isBetSuccessful() {
        return betSuccessful;
    }

    void setOddsOffered(double oddsOffered) {
        this.oddsOffered = oddsOffered;
    }

    void setStake(double stake) {
        this.stake = stake;
    }

    void setBetSuccessful(boolean betSuccessful) {
        this.betSuccessful = betSuccessful;
    }
}
