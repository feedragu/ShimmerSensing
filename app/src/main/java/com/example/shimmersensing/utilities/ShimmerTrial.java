package com.example.shimmersensing.utilities;

import java.io.Serializable;
import java.util.ArrayList;

public class ShimmerTrial implements Serializable {

    private String trialName, mode;
    private String trialDuration;
    private ArrayList<QuestionTrial> domande;

    public ShimmerTrial(String trialName, String trialDuration, String mode, ArrayList<QuestionTrial> domande) {
        this.trialName = trialName;
        this.trialDuration = trialDuration;
        this.mode = mode;
        this.domande = domande;
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

    public ArrayList<QuestionTrial> getN_domande() {
        return domande;
    }
}
