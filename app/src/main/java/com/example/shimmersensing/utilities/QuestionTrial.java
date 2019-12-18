package com.example.shimmersensing.utilities;

import java.io.Serializable;

public class QuestionTrial implements Serializable {
    private String sheet_name;
    private String nome_domanda;
    private int range;

    public QuestionTrial(String sheet_name, String nome_domanda, int range) {
        this.sheet_name = sheet_name;
        this.nome_domanda = nome_domanda;
        this.range = range;
    }

    public String getSheet_name() {
        return sheet_name;
    }

    public String getNome_domanda() {
        return nome_domanda;
    }

    public int getRange() {
        return range;
    }
}
