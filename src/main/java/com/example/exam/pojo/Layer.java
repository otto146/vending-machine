package com.example.exam.pojo;

public class Layer {
    public int getIndex() {
        return index;
    }

    public Layer() {
    }

    public Layer(int index, int weight) {
        this.index = index;
        this.weight = weight;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    private int index;
    private int weight;
}
