package com.orders.OrderService;

import java.time.LocalTime;

public class ResponseData {

    private String symbol;
    private int responseCode;
    private String quantity;
    private String responseTime;
    private String reposeMsg;

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getReposeMsg() {
        return reposeMsg;
    }

    public void setReposeMsg(String reposeMsg) {
        this.reposeMsg = reposeMsg;
    }
}
