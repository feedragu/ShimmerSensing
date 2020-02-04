package com.example.shimmersensing.utilities;

import java.util.ArrayList;

public class ShimmerTrialMusic extends ShimmerTrial {

    private String trackEncoded;

    public String getTrackEncoded() {
        return trackEncoded;
    }

    public ShimmerTrialMusic(String trialName, String trialDuration, String mode, ArrayList<QuestionTrial> domande, String trackEncoded) {
        super(trialName, trialDuration, mode, domande);
        this.trackEncoded=trackEncoded;
    }
}
