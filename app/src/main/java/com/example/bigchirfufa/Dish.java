package com.example.bigchirfufa;

public class Dish {
    String title;
    String time;
    String price;
    String weight;
    String image;
    String text;
    Integer count;

    Dish(String title, String time, String price, String weight, String image,String text) {
        this.title = title;
        this.image = image;
        this.price = price;
        this.time = time;
        this.weight = weight;
        this.text = text;
        count = 0;
    }
}



