package com.example.demo.ticket;

import com.sun.mail.util.MailSSLSocketFactory;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.security.sasl.AuthenticationException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;

/**
 * @author dinghuang123@gmail.com
 * @since 2022/4/22
 */
@Configuration
@EnableScheduling
public class TicketJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketJob.class);
    private boolean first = true;
    private boolean noTicket = true;
    private List<UserInfo> userInfoList = new LinkedList<>();
    private static ExecutorService executorService = new ThreadPoolExecutor(10, 10, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(40), new ThreadPoolExecutor.DiscardOldestPolicy());
    private static ExecutorService emailExecutorService = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(40), new ThreadPoolExecutor.DiscardOldestPolicy());
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDWuY4Gff8FO3BAKetyvNgGrdZM9CMNoe45SzHMXxAPWw6E2idaEjqe5uJFjVx55JW+5LUSGO1H5MdTcgGEfh62ink/cNjRGJpR25iVDImJlLi2izNs9zrQukncnpj6NGjZu/2z7XXfJb4XBwlrmR823hpCumSD1WiMl1FMfbVorQIDAQAB";

    @Autowired
    private WebDriverConfiguration webDriverConfiguration;


    @Scheduled(cron = "0/20 * * * * ?")
    public void configureTasks() throws InterruptedException {
        //todo 这边自己调整频率
        getTicketJob();
    }

    private void getTicketJob() throws InterruptedException {
        if (noTicket) {
            LOGGER.info("开始抢票JOB");
            if (first) {
                beforeTask();
                first = false;
                // 配置用户
                UserInfo userInfo = new UserInfo();
                userInfo.setPassword("123");
                userInfo.setUserName("123");
                userInfo.setAddress("xxx");
                userInfo.setAddressId("111");
                userInfo.setDepId("11");
                userInfo.setDocId("11");
                userInfo.setUnitId("1111");
                userInfo.setWebDriver(webDriverConfiguration.getWebDriver());
                this.userInfoList.add(userInfo);
                UserInfo userInfo2 = new UserInfo();
                userInfo2.setPassword("123");
                userInfo2.setUserName("123");
                userInfo2.setAddress("xxx");
                userInfo2.setAddressId("11");
                userInfo2.setDepId("111");
                userInfo2.setDocId("11111");
                userInfo2.setWebDriver(webDriverConfiguration.getWebDriver());
                this.userInfoList.add(userInfo2);
            }
            getTicket(userInfoList);
            LOGGER.info("开始抢票JOB结束");
        }
    }

    private void beforeTask() {
        Filter filter = (requestSpec, responseSpec, ctx) -> {
            requestSpec
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .header("Pragma", "no-cache")
                    .header("Referer", "https://www.91160.com")
                    .header("Origin", "https://www.91160.com")
                    .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Sec-Fetch-Site", "same-origin");
            return ctx.next(requestSpec, responseSpec);
        };
        //token设置到全局请求中
        RestAssured.filters(filter);
    }

    public void getTicket(List<UserInfo> userInfoList) throws InterruptedException {
        try {
            List<String> dateList = new LinkedList<>();
            Date date = new Date();
            dateList.add(getDate(date));
            for (int i = 1; i < 4; i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.WEEK_OF_YEAR, i);
                dateList.add(getDate(new Date(calendar.getTimeInMillis())));
            }
            CountDownLatch countDownLatch = new CountDownLatch(userInfoList.size());
            for (UserInfo user : userInfoList) {
                executorService.submit(new TicketThread(user, dateList, countDownLatch, executorService));
            }
            countDownLatch.await();
            for (UserInfo user : userInfoList) {
                try {
                    if (!user.getTickets().isEmpty()) {
                        sendEmail("搜索到" + user.getTickets().size() + "个号，开始抢号....");
                        LOGGER.info("搜索到" + user.getTickets().size() + "个号，开始抢号....");
                        login(user);
                        for (Map<Object, Object> ticket : user.getTickets()) {
                            String url = "https://www.91160.com/guahao/ystep1/uid-" + user.getUnitId() + "/depid-" + user.getDepId()
                                    + "/schid-" + ticket.get("schedule_id") + ".html";
                            user.getWebDriver().get(url);
                            WebElement webElement = user.getWebDriver().findElement(By.id("delts")).findElement(By.xpath("li"));
                            Actions action = new Actions(user.getWebDriver());
                            action.moveToElement(webElement).perform();
                            action.click().perform();
                            Select provinceSelect = new Select(user.getWebDriver().findElement(By.id("useraddress_province")));
                            //todo 通过addressId自动反过来选择下拉
                            // 通过select索引定位选择下拉框元素，注意索引从0开始
                            provinceSelect.selectByIndex(1);
                            Select citySelect = new Select(user.getWebDriver().findElement(By.id("useraddress_city")));
                            // 通过select索引定位选择下拉框元素，注意索引从0开始
                            citySelect.selectByIndex(2);
                            Select areaSelect = new Select(user.getWebDriver().findElement(By.id("useraddress_area")));
                            // 通过select索引定位选择下拉框元素，注意索引从0开始
                            areaSelect.selectByIndex(5);
                            user.getWebDriver().findElement(By.id("check_yuyue_rule")).click();
                            user.getWebDriver().findElement(By.id("useraddress_detail")).sendKeys(user.getAddress());
                            user.getWebDriver().findElement(By.id("submitbtn")).click();
                            sendEmail("预约成功，请留意短信通知");
                            LOGGER.info("预约成功，请留意短信通知！");
                            this.noTicket = false;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("程序错误", e);
                    sendEmail("程序错误：" + e.getMessage());
                } finally {
                    user.setTickets(Collections.synchronizedList(new LinkedList<>()));
                }
            }
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                LOGGER.error("认证失败,退出程序", e);
                sendEmail("登录失败：" + e.getMessage());
                return;
            } else {
                LOGGER.error("程序错误", e);
                sendEmail("程序错误：" + e.getMessage());
                Thread.sleep(3000);
            }
        }
    }

    public static void sendEmail(String message) {
        emailExecutorService.submit(new Thread(() -> {
            try {
                LOGGER.info("开始发送邮件，内容：{}", message);
                Properties props = new Properties();
                // 开启debug调试
                props.setProperty("mail.debug", "true");
                // 发送服务器需要身份验证
                props.setProperty("mail.smtp.auth", "true");
                // 设置邮件服务器主机名
                props.setProperty("mail.host", "smtp.qq.com");
                // 发送邮件协议名称
                props.setProperty("mail.transport.protocol", "smtp");
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.socketFactory", sf);
                Session session = Session.getInstance(props);
                Message msg = new MimeMessage(session);
                msg.setSubject("抢号信息");
                msg.setText(message);
                msg.setFrom(new InternetAddress("111@qq.com"));
                Transport transport = session.getTransport();
                //todo 这里设置自己邮箱的smtp的clientCode
                transport.connect("smtp.qq.com", "111@qq.com", "1123213213");
                transport.sendMessage(msg, new Address[]{new InternetAddress("111@qq.com"), new InternetAddress("111@qq.com"), new InternetAddress("111@qq.com")});
                transport.close();
            } catch (Exception e) {
                LOGGER.error("发送邮件失败", e);
            }
        }));
    }

    public static Response getTicketResponse(UserInfo userInfo, String date, int i) throws Exception {
        Response ticketResponse = request(() -> given()
                //打印请求的所有数据
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("User-Agent", CommonUtils.randomUserAgent())
                .header("x-forwarded-for", CommonUtils.getRandomIp())
                .formParam("docid", userInfo.getDocId())
                .formParam("date", date)
                .formParam("days", "6")
                .when()
                //请求路径
                .post("https://www.91160.com/doctors/ajaxgetclass.html").then()
                //状态码断言
                .extract().response(), userInfo);
        if (ticketResponse.body().asString().contains("status")) {
            if (ticketResponse.jsonPath().getInt("status") == 403) {
                LOGGER.warn("查询太频繁，IP已被封禁，尝试2秒后继续");
                Thread.sleep(2000);
                i++;
                if (i >= 50) {
                    throw new IllegalArgumentException("查询太频繁，IP已被封禁");
                } else {
                    return getTicketResponse(userInfo, date, i);
                }
            } else if (ticketResponse.jsonPath().getInt("status") == 502) {
                LOGGER.warn("服务器网关超时");
                Thread.sleep(2000);
                i++;
                if (i >= 50) {
                    throw new IllegalArgumentException("服务器网关超时");
                } else {
                    return getTicketResponse(userInfo, date, i);
                }
            }
        }
        if (ticketResponse.body().asString().contains("知道创宇云防御节点")) {
            LOGGER.warn("查询太频繁，被防御拦截");
            Thread.sleep(2000);
            i++;
            if (i >= 50) {
                throw new IllegalArgumentException("查询太频繁，IP已被封禁");
            } else {
                return getTicketResponse(userInfo, date, i);
            }
        }
        try {
            if (ticketResponse.jsonPath().getInt("code") != 1) {
                throw new IllegalArgumentException("未知错误：" + ticketResponse.body().asString());
            }
        } catch (Exception e) {
            LOGGER.error("未知错误", e);
            sendEmail("未知错误:" + ticketResponse.body().asString());
        }

        return ticketResponse;
    }

    private static Response request(LoginFunction loginFunction, UserInfo userInfo) throws Exception {
        Response response = loginFunction.execute();
        if (response.body().asString().contains("https://www.91160.com/user/login.html") && response.body().asString().contains("您没有登录，不能访问该页面")) {
            //说明要重新登录
            LOGGER.info("需要重新登录，登录中....");
            login(userInfo);
            return loginFunction.execute();
        } else {
            return response;
        }
    }

    public static String getDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static String getDate2(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static void login(UserInfo userInfo) {
        userInfo.getWebDriver().get("https://user.91160.com/login.html");
        // 将浏览器窗口最大化
        userInfo.getWebDriver().manage().window().maximize();
        Object o = null;
        try {
            o = userInfo.getWebDriver().findElement(By.className("t-logined"));
        } catch (Exception e) {
        }
        if (o == null) {
            // 根据id属性值定位
            userInfo.getWebDriver().findElement(By.id("_username")).sendKeys(userInfo.getUserName());
            userInfo.getWebDriver().findElement(By.id("_loginPass")).sendKeys(userInfo.getPassword());
            userInfo.getWebDriver().findElement(By.name("loginUser")).click();
            String value = null;
            try {
                value = userInfo.getWebDriver().manage().getCookieNamed("access_hash").getValue();
            } catch (Exception e) {
            }
            if (value == null) {
                throw new RuntimeException("登录失败");
            } else {
                LOGGER.info("登录成功");
            }
            Filter filter = (requestSpec, responseSpec, ctx) -> {
                for (Cookie cookie : userInfo.getWebDriver().manage().getCookies()) {
                    requestSpec.cookie(cookie.getName(), cookie.getValue());
                }
                return ctx.next(requestSpec, responseSpec);
            };
            //token设置到全局请求中
            RestAssured.filters(filter);
        }
    }

}
