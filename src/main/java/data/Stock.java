package data;

/**
 * Created by tejas on 4/7/2016.
 */
public class Stock {

    private String stockSymbol;
    private String stockName;
    private float currentPrice;

    public Stock(String stockName, String stockSymbol) {
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
    }

    public Stock(String stockName, String stockSymbol, float currentPrice) {
        this.currentPrice = currentPrice;
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;
    }

    public float getCurrentPrice() {

        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }
}
