package database;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import data.Stock;
import data.StockDetails;
import data.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class DBManager {

    final static String baseUrl = "http://query.yahooapis.com/v1/public/yql?q=";
    final static String endUrl = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

    public static Connection connection;
    public static ResultSet rs;

    final static String dbEndPoint = "monitorstockpriceinstance.ctoveuujovpy.us-east-1.rds.amazonaws.com";
    final static String url = "jdbc:mysql://" + dbEndPoint + ":3306/MonitorStockPriceDB";
    final static String username = "MonitorClient";
    final static String password = "12345";

    public static void main(String args[]) throws IOException {
//    System.out.println("hiii");
        try {


            ArrayList<String> stockList = new ArrayList<>();
            stockList.add("YHOO");
            stockList.add("AAPL");
            stockList.add("GOOG");
            stockList.add("MSFT");
            // String a= String.join(",",stockList);


//            getStockQuote();
            connectDB();


            int userId = 1;
            ArrayList<String> companyList = listCompanies(userId);
            String stockSymbol = stockList.get(1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            String dateInString = "2016-03-03";
            Date startDate = sdf.parse(dateInString);
            Date endDate = new Date();
            companyHistory(userId, stockSymbol, startDate, endDate);
            deleteCompany(1, "YHOO");
            addCompany(1, stockList);
            System.out.println(connection.isClosed());
            ArrayList<User> users = showUsers();
            for (User u : users) {
                System.out.println(u.getId() + " " + u.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void connectDB() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);

    }

    private static ArrayList<StockDetails> companyHistory(int userId, String stockSymbol, Date startDate, Date endDate) throws SQLException, IOException {
        ArrayList<StockDetails> stockDetails = new ArrayList<>();
        // check if user is present in user table
        String sqlQuery = "select * from user where id=" + userId;
        ArrayList<String> list = new ArrayList<>();
        ResultSet rs = connection.prepareStatement(sqlQuery).executeQuery();
        if (rs.next()) {
            //check if stock is present in users stock table
            sqlQuery = "select * from users_stock where user_id=? and stock_symbol=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, stockSymbol);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                //get all data between two dates selected
                sqlQuery = "select * from stock_details where stock_symbol=? and date between ? and ?";
                preparedStatement = connection.prepareStatement(sqlQuery);
                preparedStatement.setString(1, stockSymbol);
                preparedStatement.setDate((2), new java.sql.Date(startDate.getTime()));
                preparedStatement.setDate((3), new java.sql.Date(endDate.getTime()));
                rs = preparedStatement.executeQuery();
                Date temp = new Date();
                while (rs.next()) {
                    StockDetails sd = new StockDetails();
                    sd.setStockSymbol(rs.getString("stock_symbol"));
                    sd.setDate(rs.getDate("date"));
                    sd.setOpen(rs.getFloat("open"));
                    sd.setHigh(rs.getFloat("high"));
                    sd.setLow(rs.getFloat("low"));
                    sd.setCurrentPrice(rs.getFloat("current_price"));
                    sd.setVolume(rs.getDouble("volume"));
                    sd.setAdjClose(rs.getFloat("adj_close"));
                    if (sd.getDate().before(temp)) {
                        temp = sd.getDate();
                    }
                    stockDetails.add(sd);

                }

                //fetch all the previous data before temp from date startDate mentioned
                if (temp.after(startDate)) {
                    //fetch all data from yahoo
                    stockDetails.addAll(getYahooHistoryData(stockSymbol, startDate, endDate));

                }

                /*Collections.sort(stockDetails, new Comparator<StockDetails>() {
                    @Override
                    public int compare(StockDetails o1, StockDetails o2) {
                        if(o1.getDate().after(o2.getDate())){
                            return 1;
                        }else if(o1.getDate().before(o2.getDate())){
                            return -1;
                        }else{
                            return 0;
                        }
                    }
                });
                */
                //check if the data is within range or fetch new data from yahoo

            }
        }
        return stockDetails;
    }

    public StockDetails stockDetailMapper(ResultSet rs) throws SQLException {
        StockDetails sd = new StockDetails();
        sd.setStockSymbol(rs.getString("stock_symbol"));
        sd.setDate(rs.getDate("date"));
        sd.setOpen(rs.getFloat("open"));
        sd.setHigh(rs.getFloat("high"));
        sd.setLow(rs.getFloat("low"));
        sd.setCurrentPrice(rs.getFloat("current_price"));
        sd.setVolume(rs.getDouble("volume"));
        sd.setAdjClose(rs.getFloat("adj_close"));
        return sd;
    }

    private static ArrayList<StockDetails> getYahooHistoryData(String stockSymbol, Date startDate, Date endDate) throws IOException, SQLException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        String sDate = sdf.format(startDate);
        String eDate = sdf.format(endDate);

        String getHistoryQuery = "select * from yahoo.finance.historicaldata where symbol = '" + stockSymbol + "' and startDate = '" + sDate + "' and endDate = '" + eDate + "'";
        JsonElement jsonElement = getYahooQueryResponse(getHistoryQuery);
        JsonArray quotesObject = jsonElement.getAsJsonObject().get("query").getAsJsonObject().get("results").getAsJsonObject().get("quote").getAsJsonArray();
        Gson gson = new Gson();
        ArrayList<StockDetails> stockDetails = new ArrayList<>();

        for (JsonElement jElement : quotesObject) {
            StockDetails s = gson.fromJson(jElement.toString(), StockDetails.class);
            Date date = new Date();
            if (s.getDate() == null) {
                s.setDate(date);
            }
            stockDetails.add(s);
        }

        for (StockDetails stock : stockDetails) {

            String sqlQuery = "Insert into stock_details(stock_symbol,date,high,low,current_price,volume) values(?,?,?,?,?,?)"; // + stock.getStockSymbol() + "'," + stock.getDate() + ",'"
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString((1), stock.getStockSymbol());
            preparedStatement.setDate((2), new java.sql.Date(stock.getDate().getTime()));
            preparedStatement.setFloat(3, stock.getHigh());
            preparedStatement.setFloat(4, stock.getLow());
            preparedStatement.setFloat(5, stock.getCurrentPrice());
            preparedStatement.setDouble(6, stock.getVolume());
            preparedStatement.execute();

        }
        return stockDetails;

    }

    public boolean checkUserExists(int userId) {
        String sqlQuery = "select * from user where id=" + userId;
        try {
            ArrayList<String> list = new ArrayList<>();
            ResultSet rs = connection.prepareStatement(sqlQuery).executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public ArrayList<StockDetails> getCompaniesList(int userId) {
        String sqlQuery = "select stock_symbol from users_stock where user_id=?";
        ArrayList<StockDetails> list = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, userId);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                list.add(stockDetailMapper(rs));
//            list.add(rs.getString("stock_symbol"));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private static ArrayList<String> listCompanies(int userId) throws SQLException {
        //check if user exists in the user table;
        String sqlQuery = "select * from user where id=" + userId;
        ArrayList<String> list = new ArrayList<>();
        ResultSet rs = connection.prepareStatement(sqlQuery).executeQuery();
        if (rs.next()) {
            sqlQuery = "select stock_symbol from users_stock where user_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, userId);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("stock_symbol"));

            }
        } else {
            list = null;
        }
        return list;

    }


    public static void getStockQuote() throws IOException {
//        String getHistoryQuery = "select * from yahoo.finance.historicaldata where symbol = 'YHOO' and startDate = '2009-09-11' and endDate = '2010-03-10'";
        String getCurrentQuoteQuery = "select * from yahoo.finance.quote where symbol in ('YHOO','AAPL','GOOG','MSFT')";
//        String query= getHistoryQuery;
        String query = getCurrentQuoteQuery;
        JsonElement je = getYahooQueryResponse(query);

        JsonArray quotesObject = je.getAsJsonObject().get("query").getAsJsonObject().get("results").getAsJsonObject().get("quote").getAsJsonArray();
        Gson gson = new Gson();
        ArrayList<StockDetails> sda = new ArrayList<>();

        for (JsonElement jsonElement : quotesObject) {
            sda.add(gson.fromJson(jsonElement.toString(), StockDetails.class));
        }

    }

    public boolean checkCompanyPresentInStock(StockDetails stock) {
        String checkStock = "select * from stock where stock_symbol='" + stock.getStockSymbol() + "'";
        try {
            rs = queryDB(checkStock);
            //if not present in stock table store the stock name and symbol in the stock table
            if (!rs.next()) {
                return false;
            } else {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkCompanyPresentInUsers(String stockSymbol,int userId) throws SQLException {
        String checkStock = "select * from users_stock where stock_symbol='" + stockSymbol + "' and user_id="+ userId;
        rs = queryDB(checkStock);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean insertCompanyInUsers(int userId, String stockSymbol) throws SQLException {
        String sqlQuery = "Insert into users_stock(user_id,stock_symbol) values(?,?)"; //+ userId + "','" + stock.getStockSymbol() + "')";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setInt((1), userId);
        preparedStatement.setString((2), stockSymbol);
        return preparedStatement.execute();
    }

    public boolean insertInStockDetails(StockDetails stock) {
        String sqlQuery = "select * from stock_details where stock_symbol='" + stock.getStockSymbol() + "'and date='" + new java.sql.Date(stock.getDate().getTime()) + "'";
        try {
            // check if stock is already present, if yes update with current value
            rs = queryDB(sqlQuery);

            if (!rs.next()) {
                sqlQuery = "Insert into stock_details(stock_symbol,date,high,low,current_price,volume) values(?,?,?,?,?,?)"; // + stock.getStockSymbol() + "'," + stock.getDate() + ",'"
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                preparedStatement.setString((1), stock.getStockSymbol());
                preparedStatement.setDate((2), new java.sql.Date(stock.getDate().getTime()));
                preparedStatement.setFloat(3, stock.getHigh());
                preparedStatement.setFloat(4, stock.getLow());
                preparedStatement.setFloat(5, stock.getCurrentPrice());
                preparedStatement.setDouble(6, stock.getVolume());
                return preparedStatement.execute();
            } else {
                sqlQuery = "update stock_details set high=" + stock.getHigh() + ", low=" + stock.getLow() + ", current_price=" + stock.getCurrentPrice() + ", volume=" + stock.getVolume() +
                        "where stock_symbol='" + stock.getStockSymbol() + "' and date='" + new java.sql.Date(stock.getDate().getTime()) + "'";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                int result = preparedStatement.executeUpdate();
                if (result > 0) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean insertCompanyInStock(StockDetails stock) throws SQLException {
        String insertStock = "Insert into stock(stock_symbol,stock_name) values(?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertStock);
        preparedStatement.setString((1), stock.getStockSymbol());
        preparedStatement.setString((2), stock.getName());
        return preparedStatement.execute();

    }

    public static void addCompany(int userId, ArrayList<String> stockSymbol) throws IOException, ParseException, SQLException {
        //get data from yahoo for that stock
//       String getCurrentQuoteQuery = "select * from yahoo.finance.quote where symbol in ('YHOO','AAPL','GOOG','MSFT')";
        String stockLists = stockSymbol.stream()
                .map((s) -> "'" + s + "'")
                .collect(Collectors.joining(", "));
        System.out.println(stockLists);

        String a = String.join(",", stockSymbol);

        String getCurrentQuoteQuery = "select * from yahoo.finance.quote where symbol in (" + stockLists + ")";
        JsonElement jsonElement = getYahooQueryResponse(getCurrentQuoteQuery);
        JsonArray quotesObject = jsonElement.getAsJsonObject().get("query").getAsJsonObject().get("results").getAsJsonObject().get("quote").getAsJsonArray();
        Gson gson = new Gson();
        ArrayList<StockDetails> stockDetails = new ArrayList<>();

        for (JsonElement jElement : quotesObject) {
            StockDetails s = gson.fromJson(jElement.toString(), StockDetails.class);
            Date date = new Date();
            s.setDate(date);
            stockDetails.add(s);
        }
        ResultSet rs;
        String sqlQuery;
        //check if stock is present in stock table
        for (StockDetails stock : stockDetails) {
            String checkStock = "select * from stock where stock_symbol='" + stock.getStockSymbol() + "'";
            try {
                rs = queryDB(checkStock);
                //if not present in stock table store the stock name and symbol in the stock table
                if (!rs.next()) {
                    String insertStock = "Insert into stock(stock_symbol,stock_name) values(?,?)"; //"'" + stock.getStockSymbol() + "','" + stock.getName() + "')";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertStock);
                    preparedStatement.setString((1), stock.getStockSymbol());
                    preparedStatement.setString((2), stock.getName());
                    preparedStatement.execute();
                }
                //store stock details in the users table
                //already present in stock table, check for users table
                checkStock = "select * from users_stock where stock_symbol='" + stock.getStockSymbol() + "'";
                rs = queryDB(checkStock);
                if (!rs.next()) {
                    sqlQuery = "Insert into users_stock(user_id,stock_symbol) values(?,?)"; //+ userId + "','" + stock.getStockSymbol() + "')";
                    PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                    preparedStatement.setInt((1), userId);
                    preparedStatement.setString((2), stock.getStockSymbol());
                    preparedStatement.execute();
                }
                //update stock price in stock details table
                checkStock = "select * from stock_details where stock_symbol='" + stock.getStockSymbol() + "'and date='" + new java.sql.Date(stock.getDate().getTime()) + "'";
                rs = queryDB(checkStock);
                if (!rs.next()) {
                    sqlQuery = "Insert into stock_details(stock_symbol,date,high,low,current_price,volume) values(?,?,?,?,?,?)"; // + stock.getStockSymbol() + "'," + stock.getDate() + ",'"
                    PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                    preparedStatement.setString((1), stock.getStockSymbol());
                    preparedStatement.setDate((2), new java.sql.Date(stock.getDate().getTime()));
                    preparedStatement.setFloat(3, stock.getHigh());
                    preparedStatement.setFloat(4, stock.getLow());
                    preparedStatement.setFloat(5, stock.getCurrentPrice());
                    preparedStatement.setDouble(6, stock.getVolume());
                    preparedStatement.execute();
                }
                //update stock price in the stock details table or create a new record
                sqlQuery = "update stock_details set high=" + stock.getHigh() + ", low=" + stock.getLow() + ", current_price=" + stock.getCurrentPrice() + ", volume=" + stock.getVolume() +
                        "where stock_symbol='" + stock.getStockSymbol() + "' and date='" + new java.sql.Date(stock.getDate().getTime()) + "'";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


    }

    public boolean checkUserHasCompany(int userId, String stockSymbol) {
        String sqlQuery = "select * from users_stock where user_id=" + userId + " and stock_symbol='" + stockSymbol + "'";
        try {
            ResultSet rs = queryDB(sqlQuery);
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean deleteCompanyFromUser(int userId, String stockSymbol) {
        String sqlQuery = "delete from users_stock where user_id= ? and stock_symbol=?";// + userId + " and stock_symbol='" + stockSymbol + "'";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sqlQuery);
            pstmt.setInt(1, userId);
            pstmt.setString(2, stockSymbol);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void deleteCompany(int userId, String stockSymbol) throws SQLException {
        String sqlQuery = "select * from users_stock where user_id=" + userId + " and stock_symbol='" + stockSymbol + "'";
        ResultSet rs = queryDB(sqlQuery);
        if (rs.next()) {
            sqlQuery = "delete from users_stock where user_id= ? and stock_symbol=?";// + userId + " and stock_symbol='" + stockSymbol + "'";
            PreparedStatement pstmt = connection.prepareStatement(sqlQuery);
            pstmt.setInt(1, userId);
            pstmt.setString(2, stockSymbol);
            pstmt.executeUpdate();
        }
    }

    private static ResultSet queryDB(String sqlQuery) throws SQLException {
//        Class.forName("com.mysql.jdbc.Driver");
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            rs = stmt.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;

    }

    private static ArrayList<User> showUsers() throws SQLException {
        String sql = "select * from user";
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        ArrayList<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3)));
        }
        return users;
    }

    private static JsonElement getYahooQueryResponse(String query) throws IOException {
        String fullUrlStr = baseUrl + URLEncoder.encode(query, "UTF-8") + endUrl;
        URL fullUrl = new URL(fullUrlStr);
        HttpURLConnection conn =
                (HttpURLConnection) fullUrl.openConnection();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }
        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

//        JSONObject jsonObj = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(new JsonReader(rd));
        rd.close();
        conn.disconnect();
        return je;
    }


    public void saveStockDetails(StockDetails sd) {
    }
}
