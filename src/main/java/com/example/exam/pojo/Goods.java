package com.example.exam.pojo;

public class Goods {
    private String id;
    private int weight;
    private double packageTolerance;

    public Goods(String id, int weight, double packageTolerance) {
        this.id = id;
        this.weight = weight;
        this.packageTolerance = packageTolerance;
    }

    public Goods() {
    }


    public String getId() {
        return id;
    }

    public double getPackageTolerance() {
        return packageTolerance;
    }

    public int getWeight() {
        return weight;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setPackageTolerance(double packageTolerance){
        this.packageTolerance = packageTolerance;
    }

}
