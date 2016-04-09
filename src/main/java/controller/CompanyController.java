package controller;

import data.StockDetails;
import data.UsersStock;
import database.DBManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import yahooapis.YahooAPI;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tejas on 4/8/2016.
 */
public class CompanyController {

    private DBManager db;

    public CompanyController() {
        db = new DBManager();
    }

    public boolean deleteCompany(int userId, String stockSymbol){
// check for user has company listed
        if(db.checkCompanyPresentInUsers(userId,stockSymbol)){

        }
        return false;
    }
    public ArrayList<StockDetails> getCompanies(int userId) {
        // TODO:get data from DB and return
        //check if user exists in the user table
        if (db.checkUserExists(userId)) {
            return db.getCompaniesList(userId);
        }
        return null;
//        throw new NotImplementedException();
    }

    public boolean addNewCompany(int userId, String company) throws IOException {
        // get company data from yahoo api
        try {
            StockDetails sd = YahooAPI.getStockDetails(company);
            //check if stock is present in stock table
            if (!db.checkCompanyPresentInStock(sd)) {
                //insert into stock table
                db.insertCompanyInStock(sd);
            }
            //check if present in users table
            if (!db.checkCompanyPresentInUsers(sd.getStockSymbol(),userId)) {
                //insert into user_stock table
                db.insertCompanyInUsers(userId, company);
            }
            //insert into stock_details table
            return db.insertInStockDetails(sd);


        } catch (Exception e) {
            return false;
        }
        // save details
//        db.saveStockDetails(sd);
    }
}