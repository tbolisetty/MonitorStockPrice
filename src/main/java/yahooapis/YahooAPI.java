package yahooapis;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import data.StockDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tejas on 4/8/2016.
 */
public class YahooAPI {

    final static String baseUrl = "http://query.yahooapis.com/v1/public/yql?q=";
    final static String endUrl = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

    public StockDetails getStockDetails(String companyName) {
        try {
            String getCurrentQuoteQuery = "select * from yahoo.finance.quote where symbol in ('" + companyName + "')";
            JsonElement jsonElement = getYahooQueryResponse(getCurrentQuoteQuery);
            JsonElement quotesObject = jsonElement.getAsJsonObject().get("query").getAsJsonObject().get("results").getAsJsonObject().get("quote");
            Gson gson = new Gson();
            StockDetails stockDetails = gson.fromJson(quotesObject.toString(), StockDetails.class);
            Date date = new Date();
            stockDetails.setDate(date);
            return stockDetails;
        } catch (Exception e) {
            return null;
        }
    }


    private JsonElement getYahooQueryResponse(String query) throws IOException {
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


    public ArrayList<StockDetails> getYahooHistoryData(String stockSymbol, Date startDate, Date endDate) throws IOException, SQLException {

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


        return stockDetails;

    }


}
