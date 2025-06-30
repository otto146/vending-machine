package com.example.exam.pojo;

public class Goods {
    private String id;

    public Goods(String id, int weight) {
        this.id = id;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public Goods() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    private int weight;
}
