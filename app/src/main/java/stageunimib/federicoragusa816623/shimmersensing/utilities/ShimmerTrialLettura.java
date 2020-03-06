package stageunimib.federicoragusa816623.shimmersensing.utilities;

import java.util.ArrayList;

public class ShimmerTrialLettura extends ShimmerTrial {

    private String url_file_text;

    public String getUrl_file_text() {
        return url_file_text;
    }

    public ShimmerTrialLettura(String trialName, String trialDuration, String mode, String url_icon, ArrayList<QuestionTrial> domande, String description, String url_file_text) {
        super(trialName, trialDuration, mode, url_icon, domande, description);
        this.url_file_text = url_file_text;
    }
}
