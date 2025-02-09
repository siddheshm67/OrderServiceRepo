package com.orders.OrderService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class HistoricalDataFetcher {

    private static final String API_URL = "https://api.kite.trade/orders/regular";

    public StockData fetchStockData(LocalDate date, int stockToken, String encToken) {
        StockData stockData = new StockData();
        String enctoken = encToken;
        int token = stockToken;
        String timeframe = "day";
        LocalDate sdt = date;
        LocalDate edt = date;

        String url = String.format("https://kite.zerodha.com/oms/instruments/historical/%d/%s", token, timeframe);

        // Build query parameters
        Map<String, String> params = new HashMap<>();
        params.put("oi", "1");
        params.put("from", sdt.toString());
        params.put("to", edt.toString());

        // Add headers
        String authorizationHeader = "enctoken " + enctoken;

        try {
            // Append query parameters to URL
            StringBuilder urlWithParams = new StringBuilder(url);
            urlWithParams.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlWithParams.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            // Remove the trailing "&"
            urlWithParams.deleteCharAt(urlWithParams.length() - 1);

            String stringUrl = urlWithParams.toString();
            String testUrl = "https://kite.zerodha.com/oms/instruments/events/5633?from=2025-02-07&to=2025-02-09";
            // Create connection
            URL requestUrl = new URL(stringUrl);
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
                String responseString = response.toString();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseString);
                JsonNode candlesNode = rootNode.path("data").path("candles");

                if (!candlesNode.isEmpty()) {
                    for (JsonNode candle : candlesNode) {
                        stockData.setOpen(candle.get(1).asDouble());
                        stockData.setHigh(candle.get(2).asDouble());
                        stockData.setLow(candle.get(3).asDouble());
                        stockData.setClose(candle.get(4).asDouble());
                    }
                }
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockData;
    }

    public StockData fetchStockDataUsingOkhttp(LocalDate date, int stockToken, String encToken) {
        StockData stockData = new StockData();
        String timeframe = "day";

        String url = String.format("https://kite.zerodha.com/oms/instruments/historical/%d/%s", stockToken, timeframe);

        // Build query parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("oi", "1");
        urlBuilder.addQueryParameter("from", date.toString());
        urlBuilder.addQueryParameter("to", date.toString());

        String finalUrl = urlBuilder.build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(finalUrl)
                .addHeader("Authorization", "enctoken " + encToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseString);
                JsonNode candlesNode = rootNode.path("data").path("candles");

                if (!candlesNode.isEmpty()) {
                    for (JsonNode candle : candlesNode) {
                        stockData.setOpen(candle.get(1).asDouble());
                        stockData.setHigh(candle.get(2).asDouble());
                        stockData.setLow(candle.get(3).asDouble());
                        stockData.setClose(candle.get(4).asDouble());
                    }
                }
            } else {
                System.out.println("GET request failed. Response Code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stockData;
    }

    public StockData fetchStockDataOptimized(LocalDate date, int stockToken, String encToken) {
        StockData stockData = new StockData();
        String url = String.format("https://kite.zerodha.com/oms/instruments/historical/%d/day", stockToken);

        HttpUrl finalUrl = HttpUrl.parse(url).newBuilder()
                .addQueryParameter("oi", "1")
                .addQueryParameter("from", date.toString())
                .addQueryParameter("to", date.toString())
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(finalUrl)
                .addHeader("Authorization", "enctoken " + encToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonNode candlesNode = new ObjectMapper().readTree(response.body().string())
                        .path("data").path("candles");

                if (!candlesNode.isEmpty()) {
                    JsonNode lastCandle = candlesNode.get(candlesNode.size() - 1);
                    stockData.setOpen(lastCandle.get(1).asDouble());
                    stockData.setHigh(lastCandle.get(2).asDouble());
                    stockData.setLow(lastCandle.get(3).asDouble());
                    stockData.setClose(lastCandle.get(4).asDouble());
                }
            } else {
                System.err.println("GET request failed. Response Code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stockData;
    }


}

