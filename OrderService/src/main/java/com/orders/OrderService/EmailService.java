package com.orders.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHtmlEmail(List<ResponseData> responseDataList) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("siddheshmadiwale@gmail.com");
        helper.setTo("madiwalesiddhesh@gmail.com");
        helper.setSubject("Trade Report");

        // Build HTML Table
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        htmlContent.append("<tr><th>Symbol</th><th>Quantity</th><th>Response Time</th><th>Response Code</th><th>Response Message</th></tr>");

        for (ResponseData data : responseDataList) {
            String mainMessage = null;
            if (data.getQuantity().equalsIgnoreCase("0")){
                mainMessage = data.getReposeMsg();
            }else {
                String[] responseArr = data.getReposeMsg().split(",");
                String[] msgArr = responseArr[1].split(":");
                mainMessage = msgArr[1];
            }
            htmlContent.append("<tr>")
                    .append("<td>").append(data.getSymbol()).append("</td>")
                    .append("<td>").append(data.getQuantity()).append("</td>")
                    .append("<td>").append(data.getResponseTime()).append("</td>")
                    .append("<td>").append(data.getResponseCode()).append("</td>")
                    .append("<td>").append(mainMessage).append("</td>")
                    .append("</tr>");
        }
        htmlContent.append("</table>");

        helper.setText(htmlContent.toString(), true);

        mailSender.send(message);

        System.out.println("Email sent successfully!");
    }
}

