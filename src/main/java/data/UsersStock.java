package data;

/**
 * Created by tejas on 4/7/2016.
 */
public class UsersStock {

    private int userId;

    public UsersStock(int userId, String stockSymbol) {
        this.userId = userId;
        this.stockSymbol = stockSymbol;
    }

    private String stockSymbol;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}
