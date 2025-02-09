package com.orders.OrderService;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrdersHttpAgent {

    /*public static void getHoldings(){
        String url = "https://kite.zerodha.com/oms/portfolio/holdings";

        List<Header> headers11 = new ArrayList<>();
        headers11.add(new BasicHeader("Authorization", WebViewLogin.getKiteTokenHeader()));

        get(url, headers11);
    }*/

    public int placeOrder(String encToken, String quantity, String tradingSymbol) {

        String authorizationHeader = "enctoken " + encToken;

        int code=0;
        try{
            List<Header> headers11 = new ArrayList<>();
            headers11.add(new BasicHeader("Authorization", authorizationHeader));

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("variety", "regular"));
            nameValuePairs.add(new BasicNameValuePair("exchange", "NSE"));
            nameValuePairs.add(new BasicNameValuePair("tradingsymbol", tradingSymbol));
            nameValuePairs.add(new BasicNameValuePair("transaction_type",  "SELL"));
            nameValuePairs.add(new BasicNameValuePair("order_type", "MARKET"));
            nameValuePairs.add(new BasicNameValuePair("quantity", quantity));
            nameValuePairs.add(new BasicNameValuePair("price", "0"));
            nameValuePairs.add(new BasicNameValuePair("product", "MIS"));
            nameValuePairs.add(new BasicNameValuePair("validity", "DAY"));
            nameValuePairs.add(new BasicNameValuePair("disclosed_quantity", "0"));
            nameValuePairs.add(new BasicNameValuePair("trigger_price", "0"));
            nameValuePairs.add(new BasicNameValuePair("squareoff", "0"));
            nameValuePairs.add(new BasicNameValuePair("stoploss", "0"));
            nameValuePairs.add(new BasicNameValuePair("trailing_stoploss", "0"));
            nameValuePairs.add(new BasicNameValuePair("user_id", "MD3059"));
            nameValuePairs.add(new BasicNameValuePair("tag", "quick"));
             code = postAuth("https://kite.zerodha.com/oms/orders/regular", nameValuePairs, headers11,tradingSymbol,quantity);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return code;
    }

    public int postAuth(String url, List<NameValuePair> params, List<Header> headers, String name, String quantity) {
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
        int statusCode = 0;
        try {
            HttpPost httpPost = new HttpPost(url);
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params);
            httpPost.setEntity(urlEncodedFormEntity);
            for (Header header : headers) {
                httpPost.addHeader(header);
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            String data = EntityUtils.toString(httpResponse.getEntity());
            System.out.println(httpResponse.getStatusLine());
            System.out.println(data);
            ResponseData responseData = new ResponseData();
            responseData.setResponseCode(statusCode);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
            String formatedTime = LocalTime.now().format(formatter);
            responseData.setResponseTime(formatedTime);
            responseData.setReposeMsg(data);
            responseData.setSymbol(name);
            responseData.setQuantity(quantity);
            StartupRunner.responseDataList.add(responseData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return statusCode;
    }

}
