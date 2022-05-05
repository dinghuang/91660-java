package com.example.demo.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.demo.ticket.TicketJob.sendEmail;

/**
 * @author dinghuang123@gmail.com
 * @since 2022/4/22
 */
public class TicketThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketThread.class);

    private UserInfo userInfo;
    private List<String> dateList;
    private CountDownLatch countDownLatch;
    private ExecutorService executorService;

    public TicketThread(UserInfo userInfo, List<String> dateList, CountDownLatch countDownLatch, ExecutorService executorService) {
        this.userInfo = userInfo;
        this.dateList = dateList;
        this.countDownLatch = countDownLatch;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            CountDownLatch countDownLatch1 = new CountDownLatch(this.dateList.size());
            for (int i = 0; i < this.dateList.size(); i++) {
                this.executorService.submit(new TicketSubThread(this.userInfo, this.dateList.get(i), countDownLatch1));
            }
            countDownLatch1.await();
            if (this.userInfo.getTickets().isEmpty()) {
                LOGGER.info("用户：" + this.userInfo.getUserName() + ",对应的医生：" + this.userInfo.getDocId() + "没有放号");
            }
        } catch (Exception e) {
            LOGGER.error("获取挂号信息失败", e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                LOGGER.error("线程休眠失败", interruptedException);
            }
            sendEmail("程序错误：" + e.getMessage());
        } finally {
            this.countDownLatch.countDown();
        }
    }
}
