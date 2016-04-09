package com.monitorstock;

import com.google.gson.Gson;
import controller.CompanyController;
import data.Stock;
import data.StockDetails;
import spark.Request;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Main m= new Main();
        get("/",(req,res) -> m.render("/home.html"));
        get("/list/:id", (req, res) -> listCompanies(req));
        post("/stock/:id", (req, res) -> add(req));
        get("/history/:id/:company/:startDate/:endDate", (req, res) -> getHistory(req));
        delete("stock/:id/:company", (req, res) -> deleteStock(req));

    }

    public String render(String s) {
        try{

            URL url=getClass().getResource(s);
            Path path= Paths.get(url.toURI());
            return new String (Files.readAllBytes(path), Charset.defaultCharset());
        }catch (Exception e){
            System.out.println(e);;
        }
        return null;
    }


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
        String company = req.queryParams("stock");
        boolean added = controller.addNewCompany(userId, company);
        if (added) {
            return company + " Added";
        }
        return "Error";
    }
}
