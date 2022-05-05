package com.example.demo.ticket;

import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.example.demo.ticket.TicketJob.*;

/**
 * @author dinghuang123@gmail.com
 * @since 2022/4/22
 */
public class TicketSubThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketSubThread.class);

    private UserInfo userInfo;
    private String date;
    private CountDownLatch countDownLatch;

    public TicketSubThread(UserInfo userInfo, String date, CountDownLatch countDownLatch) {
        this.userInfo = userInfo;
        this.date = date;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        String message = null;
        try {
            //休眠一段时间防止对方封禁
            Response ticketResponse = getTicketResponse(this.userInfo, this.date, 0);
            message = ticketResponse.getBody().asString();
            Map<Object, Object> objects = ticketResponse.jsonPath().getMap("sch");
            List<Map<Object, Object>> tickets = new LinkedList<>();
            objects.forEach((k, v) -> {
                if (v instanceof HashMap) {
                    Map<Object, Object> hashMap = (Map<Object, Object>) v;
                    hashMap.forEach((k1, v1) -> {
                        if (v1 instanceof HashMap) {
                            Map<Object, Object> hashMap1 = (Map<Object, Object>) v1;
                            hashMap1.forEach((k2, v2) -> {
                                if (v2 instanceof HashMap) {
                                    Map<Object, Object> hashMap2 = (Map<Object, Object>) v2;
                                    if ("1".equals(hashMap2.get("y_state"))) {
                                        //说明有号
                                        tickets.add(hashMap2);
                                    }
                                }
                            });
                        }
                    });
                }
            });
            if (tickets.isEmpty()) {
                LOGGER.info("用户：" + this.userInfo.getUserName() + ",对应的医生：" + this.userInfo.getDocId() + "没有放号，日期范围:" + this.date);
            } else {
                userInfo.getTickets().addAll(tickets);
                sendEmail(getDate2(new Date()) + ":" + "用户：" + this.userInfo.getUserName() + ",对应的医生：" + this.userInfo.getDocId() + "已放号，日期范围:" + this.date);
            }
        } catch (Exception e) {

            if (e instanceof SSLException) {
                LOGGER.error("获取挂号信息失败", e);
            } else if (e instanceof JsonPathException) {
                LOGGER.error("获取挂号信息失败", e);
                if (message != null) {
                    LOGGER.error("获取挂号信息失败,返回内容{}", message);
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException) {

                }
            } else {
                LOGGER.error("获取挂号信息失败", e);
                sendEmail("程序错误：" + e.getMessage());
            }

        } finally {
            this.countDownLatch.countDown();
        }
    }
}
