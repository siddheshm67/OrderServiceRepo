package com.orders.OrderService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

@Service
public class OrderService {

    @Autowired
    private OrdersHttpAgent httpAgent;

    @Autowired
    private HistoricalDataFetcher historicalDataFetcher;

    int capital = 10000;

    public void tradeStock(int token, String name, String enctoken) throws Exception {
        StockData stockData = historicalDataFetcher.fetchStockData(LocalDate.of(2025,02,03), token, enctoken);
        double close = stockData.getClose();
        int responseCode = 0;
        if (close<capital){
            double quantity = capital / close;
            responseCode = httpAgent.placeOrder(enctoken, String.valueOf((int)quantity), name);
        }else {
            System.out.println("Stock price is grater then Capital...!!!");
            ResponseData responseData = new ResponseData();
            responseData.setResponseCode(000);
            responseData.setQuantity("0");
            responseData.setReposeMsg("Stock price is grater then Capital.");
            responseData.setResponseTime("00.00.00.00");
            responseData.setSymbol(name);
            StartupRunner.invalidResponseDataList.add(responseData);
        }
        System.out.println("status code: "+responseCode);
    }

    public void getTradedOrders(String enctoken) throws IOException {
        String authorizationHeader = "enctoken " + enctoken;
        String url = String.format("https://kite.zerodha.com/oms/orders");
        URL requestUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", authorizationHeader);
        // Read response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(response.toString(), Object.class);
            System.out.println(json);
        }
    }

    /*public void enterTrade(String tradingsymbol, int quantity, String authorizationHeader) throws Exception {
        String requestUrl = "https://kite.zerodha.com/oms/nudge/orders"; // Replace with actual URL
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", authorizationHeader);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // JSON body
        String jsonInputString = "[{" +
                "\"exchange\":\"NSE\"," +
                "\"tradingsymbol\":\"" + tradingsymbol + "\"," +
                "\"transaction_type\":\"SELL\"," +
                "\"variety\":\"regular\"," +
                "\"product\":\"MIS\"," +
                "\"order_type\":\"LIMIT\"," +
                "\"quantity\":" + quantity + "," +
                "\"price\":11641," +
                "\"context\":\"order_window.PLACE\"}]";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(response.toString(), Object.class);
            System.out.println(json);

            System.out.println("Data has been written");
        } else {
            System.out.println("GET request failed. Response Code: " + responseCode);
        }
    }*/
}
