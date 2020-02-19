package com.example.shimmersensing.utilities;

import java.io.Serializable;

public class QuestionTrial implements Serializable {
    private String nome_domanda;
    private int range;

    public QuestionTrial(String nome_domanda, int range) {
        this.nome_domanda = nome_domanda;
        this.range = range;
    }


    public String getNome_domanda() {
        return nome_domanda;
    }

    public int getRange() {
        return range;
    }
}
