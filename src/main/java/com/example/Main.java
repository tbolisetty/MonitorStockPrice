package com.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controller.CompanyController;
import data.Stock;
import data.StockDetails;
import spark.Request;
import spark.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static spark.Spark.*;

/**
 * Created by tejas on 4/5/2016.
 */
public class Main {
    static CompanyController controller;
    static Gson gson;

    public static void main(String[] args) {
        controller = new CompanyController();
        gson = new Gson();
        get("/list/:id", (req, res) -> listCompanies(req));
        post("/stock/:id/:company", (req, res) -> add(req));
        get("/history/:id/:company/:startDate/:endDate", (req, res) -> getHistory(req));
        delete("stock/:id/:company", (req, res) -> deleteStock(req));
    }


    // db instance
    // monitorstockpriceinstance.ctoveuujovpy.us-east-1.rds.amazonaws.com:3306
    private static Object listCompanies(Request req) throws IOException {
        ArrayList<Stock> stocks = controller.listCompanies(Integer.parseInt(req.params("id")));
        return gson.toJson(stocks);
    }

    private static Object getHistory(Request req) throws ParseException {
        int userId = Integer.parseInt(req.params("id"));
        String company = req.params("company");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        Date startDate = sdf.parse(req.params("startDate"));
        Date endDate = sdf.parse(req.params("endDate"));
        ArrayList<StockDetails> sdetails = controller.companyHistory(userId, company, startDate, endDate);

        return gson.toJson(sdetails);
    }

    private static Object deleteStock(Request req) {
        int userId = Integer.parseInt(req.params("id"));
        String company = req.params("company");
        controller.deleteCompany(userId, company);
        return "deleted" + req.params("company");
    }


    private static String add(Request req) {
        int userId = Integer.parseInt(req.params("id"));
        String company = req.params("company");
        boolean added = controller.addNewCompany(userId, company);
        if (added) {
            return req.params("company") + " Added";
        }
        return "Error";
    }
}
