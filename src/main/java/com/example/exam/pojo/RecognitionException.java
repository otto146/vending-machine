package com.example.exam.pojo;

public class RecognitionException {
    private int layer;
    // 枚举值，自行发挥
    private ExceptionEnum exception;
    private int beginWeight;
    private int endWeight;

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public ExceptionEnum getException() {
        return exception;
    }

    public void setException(ExceptionEnum exception) {
        this.exception = exception;
    }

    public int getBeginWeight() {
        return beginWeight;
    }

    public void setBeginWeight(int beginWeight) {
        this.beginWeight = beginWeight;
    }

    public int getEndWeight() {
        return endWeight;
    }

    public void setEndWeight(int endWeight) {
        this.endWeight = endWeight;
    }

    public RecognitionException(int layer, ExceptionEnum exception, int beginWeight, int endWeight) {
        this.layer = layer;
        this.exception = exception;
        this.beginWeight = beginWeight;
        this.endWeight = endWeight;
    }

    public RecognitionException() {
    }
}
