package com.example.exam.pojo;

public class RecognitionItem {
    private String goodsId;
    private int num;

    public RecognitionItem(String goodsId, int num) {
        this.goodsId = goodsId;
        this.num = num;
    }

    public RecognitionItem() {
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
