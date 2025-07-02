package com.example.exam.pojo;

public class Stock {
    private String goodsId;
    private int layer;
    private int num;

    public String getGoodsId() {
        return goodsId;
    }

    public Stock() {
    }

    public Stock(String goodsId, int layer, int num) {
        this.goodsId = goodsId;
        this.layer = layer;
        this.num = num;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }


}
