package stageunimib.federicoragusa816623.shimmersensing.utilities;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class ShimmerTrial implements Serializable, Cloneable {

    private String trialName, mode, url_icon, descritpion;
    private String trialDuration;
    private ArrayList<QuestionTrial> domande;

    public ShimmerTrial(String trialName, String trialDuration, String mode, String url_icon, ArrayList<QuestionTrial> domande, String descritpion) {
        this.trialName = trialName;
        this.trialDuration = trialDuration;
        this.mode = mode;
        this.url_icon = url_icon;
        this.domande = domande;
        this.descritpion = descritpion;
    }

    public String getDescritpion() {
        return descritpion;
    }


    public ArrayList<QuestionTrial> getDomande() {
        return domande;
    }

    public String getUrl_icon() {
        return url_icon;
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

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
