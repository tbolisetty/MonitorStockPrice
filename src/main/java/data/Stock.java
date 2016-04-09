package data;

/**
 * Created by tejas on 4/7/2016.
 */
public class Stock {

    private String stockSymbol;
    private String stockName;

    public String getStockSymbol() {
        return stockSymbol;
    }

    public Stock(String stockSymbol, String stockName) {
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
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
