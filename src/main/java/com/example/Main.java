package com.example;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spark.Request;
import spark.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by tejas on 4/5/2016.
 */
public class Main {
    public static void main(String[] args) {
        get("/list", (req, res) -> listAll(req));
        post("/stock", (req, res) -> add(req));
        get("/history/:id", (req, res) -> getHistory(req));
        delete("stock/:id", (req, res) -> deleteStock(req));
    }

    private static Object listAll(Request req) throws IOException {
        String baseUrl = "http://query.yahooapis.com/v1/public/yql?q=";
//        String query = "select * from upcoming.events where location='San Francisco' and search_text='dance'";
        String query = "select * from yahoo.finance.quote where symbol in ('YHOO','AAPL','GOOG','MSFT')";
        String endUrl="&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
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
        StringBuilder sb = new StringBuilder();
        String line;
        Gson gson = new Gson();
        while ((line = rd.readLine()) != null) {

            sb.append(line);
            JsonArray  jarry= new JsonParser().parse(sb.toString()).getAsJsonArray();
            for(int i=0;i<jarry.size();i++){
                JsonObject jobj= jarry.get(0).getAsJsonObject();
            }
        }

//        String json= gson.toJson(sb.toString());

        rd.close();

        conn.disconnect();
        return sb.toString();
//        InputStream is = fullUrl.openStream();

//        return "list";
    }

    private static Object getHistory(Request req) {
        return "history" + req.params("id");
    }

    private static Object deleteStock(Request req) {
        return "deleted"+ req.params("id");
    }


    private static String add(Request req) {
        return "Added";
    }
}
