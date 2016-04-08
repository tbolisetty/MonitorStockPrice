package com.example;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by tejas on 4/7/2016.
 */
public class StockDetails {

    @SerializedName(value= "Symbol", alternate = {"symbol"})
    private String stockSymbol;
    @SerializedName("Name")
    private String name;
    @SerializedName("Date")
    private Date date;
    @SerializedName("Open")
    private float open;
    @SerializedName(value = "High",alternate = {"DaysHigh"})
    private float high;
    @SerializedName(value = "Low", alternate = {"DaysLow"})
    private float low;
    @SerializedName(value = "Close", alternate = {"LastTradePriceOnly"})
    private float currentPrice;
    @SerializedName("Adj_Close")
    private float adjClose;
    @SerializedName("Volume")
    private double volume;


    public StockDetails(String stockSymbol, Date date, float open, float high, float low, float currentPrice, float adjClose, double volume) {

        this.stockSymbol = stockSymbol;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.currentPrice = currentPrice;
        this.adjClose = adjClose;
        this.volume = volume;
    }

    public float getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(float adjClose) {
        this.adjClose = adjClose;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }


}
