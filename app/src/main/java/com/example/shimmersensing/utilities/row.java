package com.example.shimmersensing.utilities;

public class row {

    private int img;
    private String row_name;


    public String getRow_name() {
        return row_name;
    }

    public row(int img, String row_name) {
        this.img = img;
        this.row_name= row_name;
    }


    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
