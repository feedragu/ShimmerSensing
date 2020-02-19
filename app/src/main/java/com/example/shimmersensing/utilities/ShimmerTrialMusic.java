package com.example.shimmersensing.utilities;

import java.util.ArrayList;

public class ShimmerTrialMusic extends ShimmerTrial {

    private String url_file_audio;

    public String getUrl_file_audio() {
        return url_file_audio;
    }

    public ShimmerTrialMusic(String trialName, String trialDuration, String mode, String url_icon, ArrayList<QuestionTrial> domande, String url_file_audio) {
        super(trialName, trialDuration, mode, url_icon, domande);
        this.url_file_audio=url_file_audio;
    }
}
