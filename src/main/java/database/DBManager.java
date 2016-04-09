package database;

import data.Stock;
import data.StockDetails;
import data.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class DBManager {

    public static Connection connection;
    public static ResultSet rs;

    public DBManager() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    final static String dbEndPoint = "monitorstockpriceinstance.ctoveuujovpy.us-east-1.rds.amazonaws.com";
    final static String url = "jdbc:mysql://" + dbEndPoint + ":3306/MonitorStockPriceDB";
    final static String username = "MonitorClient";
    final static String password = "12345";

    public ArrayList<StockDetails> getCompanyHistory(String stockSymbol, Date startDate, Date endDate) {
        String sqlQuery = "select * from stock_details where stock_symbol=? and date between ? and ?";
        ArrayList<StockDetails> stockDetails = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, stockSymbol);
            preparedStatement.setDate((2), new java.sql.Date(startDate.getTime()));
            preparedStatement.setDate((3), new java.sql.Date(endDate.getTime()));
            rs = preparedStatement.executeQuery();
            Date temp = new Date();
            while (rs.next()) {
                StockDetails sd = stockDetailMapper(rs);
                if (sd.getDate().before(temp)) {
                    temp = sd.getDate();
                }
                stockDetails.add(sd);
            }

            Collections.sort(stockDetails, new Comparator<StockDetails>() {
                @Override
                public int compare(StockDetails o1, StockDetails o2) {
                    if (o1.getDate().before(o2.getDate())) {
                        return -1;
                    } else if (o1.getDate().after(o2.getDate())) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            return stockDetails;
        } catch (Exception e) {
            return null;
        }
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


    public boolean insertStock(StockDetails stock) {
        try {
            String sqlQuery = "Insert into stock_details(stock_symbol,date,high,low,current_price,volume) values(?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString((1), stock.getStockSymbol());
            preparedStatement.setDate((2), new java.sql.Date(stock.getDate().getTime()));
            preparedStatement.setFloat(3, stock.getHigh());
            preparedStatement.setFloat(4, stock.getLow());
            preparedStatement.setFloat(5, stock.getCurrentPrice());
            preparedStatement.setDouble(6, stock.getVolume());
            preparedStatement.execute();

            return true;
        } catch (Exception e) {
            return false;
        }
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

    public ArrayList<Stock> getCompaniesList(int userId) {
        String sqlQuery = "select s.stock_symbol,s.stock_name from stock as s inner join users_stock as us " +
                "on s.stock_symbol=us.stock_symbol where user_id=?";

/*
        sqlQuery = "select s.stock_symbol,s.stock_name,sd.current_price from stock as s inner join users_stock " +
                "as us on s.stock_symbol=us.stock_symbol inner join stock_details as sd on s.stock_symbol=sd.stock_symbol " +
                "where user_id=? and sd.date=?";*/
        ArrayList<Stock> list = new ArrayList<>();
        try {
//            Date date = new Date();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, userId);
//            preparedStatement.setDate((2), new java.sql.Date(date.getTime()));

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                list.add(new Stock(rs.getString("stock_name"), rs.getString("stock_symbol")));
//            list.add(rs.getString("stock_symbol"));
            }
            return list;
        } catch (Exception e) {
            return null;
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


    public boolean checkCompanyPresentInUsers(int userId, String stockSymbol) {
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
                return insertStock(stock);
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


}
