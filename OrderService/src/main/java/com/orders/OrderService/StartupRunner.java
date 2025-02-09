package com.orders.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    public static List<ResponseData> responseDataList = new ArrayList<>();
    public static List<ResponseData> invalidResponseDataList = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        Long startTime = System.nanoTime();
        List<String> argsList = Arrays.stream(args).toList();
        String encToken = getENCToken();
        Map<Integer, String> stockDataMap = getStockData("datafile.csv");
        if (LocalDate.parse(stockDataMap.get(001)).equals(LocalDate.now())) {
            stockDataMap.remove(001);
            stockDataMap.forEach((token,name)->{
                try {
                    orderService.tradeStock(token,name,encToken);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Long endTime = System.nanoTime();
            System.out.println("timeTaken : " + (endTime-startTime));
            responseDataList.addAll(invalidResponseDataList);
            emailService.sendHtmlEmail(responseDataList);
            System.out.println(".........Done.......");
            System.exit(0);
        }else {
            System.out.println("file not updated !!!!!!!!!!!!!....... | Date: "+stockDataMap.get(001));
        }
    }

    private String getENCToken(){
        String firstLine = null;
        try (BufferedReader br = new BufferedReader(new FileReader("tokenfile.txt"))) {
            // Read the first line
            firstLine = br.readLine();
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return firstLine;
    }

    public Map<Integer, String> getStockData(String filePath) {
        Map<Integer, String> dataMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int key = Integer.parseInt(parts[0]);
                    String value = parts[1];
                    dataMap.put(key, value);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return dataMap;
    }
}
