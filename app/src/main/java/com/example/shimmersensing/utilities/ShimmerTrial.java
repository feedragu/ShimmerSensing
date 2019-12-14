package com.example.shimmersensing.utilities;

public class ShimmerTrial {

    private String trialName, mode;
    private String n_domande, trialDuration;

    public ShimmerTrial(String trialName, String trialDuration, String mode, String n_domande) {
        this.trialName = trialName;
        this.trialDuration = trialDuration;
        this.mode = mode;
        this.n_domande = n_domande;
    }

    public String getTrialName() {
        return trialName;
    }

    public String getTrialDuration() {
        return trialDuration;
    }

    public String getMode() {
        return mode;
    }

    public String getN_domande() {
        return n_domande;
    }
}
