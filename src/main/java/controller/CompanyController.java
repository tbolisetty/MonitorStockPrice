package controller;

import data.Stock;
import data.StockDetails;
import database.DBManager;
import yahooapis.YahooAPI;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tejas on 4/8/2016.
 */
public class CompanyController {

    private DBManager db;
    private YahooAPI yahooAPI;

    public CompanyController() {
        db = new DBManager();
        yahooAPI = new YahooAPI();
    }

    public boolean deleteCompany(int userId, String stockSymbol) {
// check for user has company listed
        if (db.checkCompanyPresentInUsers(userId, stockSymbol)) {
            //delete the company from users table for that user
            return db.deleteCompanyFromUser(userId, stockSymbol);
        }
        return false;
    }

    public ArrayList<Stock> listCompanies(int userId) {
        // TODO:get data from DB and return
        //check if user exists in the user table
        if (db.checkUserExists(userId)) {
            ArrayList<Stock> stocks=db.getCompaniesList(userId);
            for(Stock stock:stocks){
                // call yahoo api to get updated price
                StockDetails sdetail=yahooAPI.getStockDetails(stock.getStockSymbol());
                //insert into db
                db.insertInStockDetails(sdetail);
                stock.setCurrentPrice(sdetail.getCurrentPrice());
            }
            return stocks;
        }
        return null;
//        throw new NotImplementedException();
    }

    public ArrayList<Stock> refresh(int userId) {
        //get list of all companies for user
        ArrayList<Stock> list = listCompanies(userId);
        ArrayList<Stock> updatedList = new ArrayList<>();

        //fetch current price for all companies from yahoo
        for (Stock stock : list) {
            StockDetails sd = yahooAPI.getStockDetails(stock.getStockSymbol());
            db.insertInStockDetails(sd);
            updatedList.add(new Stock(stock.getStockName(), sd.getStockSymbol(), sd.getCurrentPrice()));
        }
        //update the record in stock_details in db
        // return the new record
        return updatedList;
    }

    public ArrayList<StockDetails> companyHistory(int userId, String company, Date startDate, Date endDate) {
        try {
            //check if user has company
            if (db.checkCompanyPresentInUsers(userId, company)) {
                //get company data from db
                ArrayList<StockDetails> sd = db.getCompanyHistory(company, startDate, endDate);
                //fetch all the previous data before temp from date startDate mentioned
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                String dateInString = sd.get(0).getDate().toString();
                Date s = sdf.parse(dateInString);

                if (s.after(startDate)) {
                    //fetch all data from yahoo
                    ArrayList<StockDetails> stockDetails = yahooAPI.getYahooHistoryData(company, startDate, endDate);
                    for (StockDetails stock : stockDetails) {
                        db.insertStock(stock);
                    }
                    sd.addAll(stockDetails);
                }
                return sd;
            }
        } catch (Exception e) {

        }
        return null;
    }

    public boolean addNewCompany(int userId, String company){
        // get company data from yahoo api
        try {
            StockDetails sd = yahooAPI.getStockDetails(company);
            //check if stock is present in stock table
            if (!db.checkCompanyPresentInStock(sd)) {
                //insert into stock table
                db.insertCompanyInStock(sd);
            }
            //check if present in users table
            if (!db.checkCompanyPresentInUsers(userId, sd.getStockSymbol())) {
                //insert into user_stock table
                db.insertCompanyInUsers(userId, company);
            }else {
                db.insertInStockDetails(sd);
                return false;
            }
            //insert into stock_details table
            return db.insertInStockDetails(sd);


        } catch (Exception e) {
            return false;
        }
    }
}