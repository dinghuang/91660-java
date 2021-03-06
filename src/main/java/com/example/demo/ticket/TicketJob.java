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
        //todo ????????????????????????
        getTicketJob();
    }

    private void getTicketJob() throws InterruptedException {
        if (noTicket) {
            LOGGER.info("????????????JOB");
            if (first) {
                beforeTask();
                first = false;
                // ????????????
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
            LOGGER.info("????????????JOB??????");
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
        //token????????????????????????
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
                        sendEmail("?????????" + user.getTickets().size() + "?????????????????????....");
                        LOGGER.info("?????????" + user.getTickets().size() + "?????????????????????....");
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
                            //todo ??????addressId???????????????????????????
                            // ??????select???????????????????????????????????????????????????0??????
                            provinceSelect.selectByIndex(1);
                            Select citySelect = new Select(user.getWebDriver().findElement(By.id("useraddress_city")));
                            // ??????select???????????????????????????????????????????????????0??????
                            citySelect.selectByIndex(2);
                            Select areaSelect = new Select(user.getWebDriver().findElement(By.id("useraddress_area")));
                            // ??????select???????????????????????????????????????????????????0??????
                            areaSelect.selectByIndex(5);
                            user.getWebDriver().findElement(By.id("check_yuyue_rule")).click();
                            user.getWebDriver().findElement(By.id("useraddress_detail")).sendKeys(user.getAddress());
                            user.getWebDriver().findElement(By.id("submitbtn")).click();
                            sendEmail("????????????????????????????????????");
                            LOGGER.info("???????????????????????????????????????");
                            this.noTicket = false;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("????????????", e);
                    sendEmail("???????????????" + e.getMessage());
                } finally {
                    user.setTickets(Collections.synchronizedList(new LinkedList<>()));
                }
            }
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                LOGGER.error("????????????,????????????", e);
                sendEmail("???????????????" + e.getMessage());
                return;
            } else {
                LOGGER.error("????????????", e);
                sendEmail("???????????????" + e.getMessage());
                Thread.sleep(3000);
            }
        }
    }

    public static void sendEmail(String message) {
        emailExecutorService.submit(new Thread(() -> {
            try {
                LOGGER.info("??????????????????????????????{}", message);
                Properties props = new Properties();
                // ??????debug??????
                props.setProperty("mail.debug", "true");
                // ?????????????????????????????????
                props.setProperty("mail.smtp.auth", "true");
                // ??????????????????????????????
                props.setProperty("mail.host", "smtp.qq.com");
                // ????????????????????????
                props.setProperty("mail.transport.protocol", "smtp");
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.socketFactory", sf);
                Session session = Session.getInstance(props);
                Message msg = new MimeMessage(session);
                msg.setSubject("????????????");
                msg.setText(message);
                msg.setFrom(new InternetAddress("111@qq.com"));
                Transport transport = session.getTransport();
                //todo ???????????????????????????smtp???clientCode
                transport.connect("smtp.qq.com", "111@qq.com", "1123213213");
                transport.sendMessage(msg, new Address[]{new InternetAddress("111@qq.com"), new InternetAddress("111@qq.com"), new InternetAddress("111@qq.com")});
                transport.close();
            } catch (Exception e) {
                LOGGER.error("??????????????????", e);
            }
        }));
    }

    public static Response getTicketResponse(UserInfo userInfo, String date, int i) throws Exception {
        Response ticketResponse = request(() -> given()
                //???????????????????????????
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("User-Agent", CommonUtils.randomUserAgent())
                .header("x-forwarded-for", CommonUtils.getRandomIp())
                .formParam("docid", userInfo.getDocId())
                .formParam("date", date)
                .formParam("days", "6")
                .when()
                //????????????
                .post("https://www.91160.com/doctors/ajaxgetclass.html").then()
                //???????????????
                .extract().response(), userInfo);
        if (ticketResponse.body().asString().contains("status")) {
            if (ticketResponse.jsonPath().getInt("status") == 403) {
                LOGGER.warn("??????????????????IP?????????????????????2????????????");
                Thread.sleep(2000);
                i++;
                if (i >= 50) {
                    throw new IllegalArgumentException("??????????????????IP????????????");
                } else {
                    return getTicketResponse(userInfo, date, i);
                }
            } else if (ticketResponse.jsonPath().getInt("status") == 502) {
                LOGGER.warn("?????????????????????");
                Thread.sleep(2000);
                i++;
                if (i >= 50) {
                    throw new IllegalArgumentException("?????????????????????");
                } else {
                    return getTicketResponse(userInfo, date, i);
                }
            }
        }
        if (ticketResponse.body().asString().contains("???????????????????????????")) {
            LOGGER.warn("?????????????????????????????????");
            Thread.sleep(2000);
            i++;
            if (i >= 50) {
                throw new IllegalArgumentException("??????????????????IP????????????");
            } else {
                return getTicketResponse(userInfo, date, i);
            }
        }
        try {
            if (ticketResponse.jsonPath().getInt("code") != 1) {
                throw new IllegalArgumentException("???????????????" + ticketResponse.body().asString());
            }
        } catch (Exception e) {
            LOGGER.error("????????????", e);
            sendEmail("????????????:" + ticketResponse.body().asString());
        }

        return ticketResponse;
    }

    private static Response request(LoginFunction loginFunction, UserInfo userInfo) throws Exception {
        Response response = loginFunction.execute();
        if (response.body().asString().contains("https://www.91160.com/user/login.html") && response.body().asString().contains("???????????????????????????????????????")) {
            //?????????????????????
            LOGGER.info("??????????????????????????????....");
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
        // ???????????????????????????
        userInfo.getWebDriver().manage().window().maximize();
        Object o = null;
        try {
            o = userInfo.getWebDriver().findElement(By.className("t-logined"));
        } catch (Exception e) {
        }
        if (o == null) {
            // ??????id???????????????
            userInfo.getWebDriver().findElement(By.id("_username")).sendKeys(userInfo.getUserName());
            userInfo.getWebDriver().findElement(By.id("_loginPass")).sendKeys(userInfo.getPassword());
            userInfo.getWebDriver().findElement(By.name("loginUser")).click();
            String value = null;
            try {
                value = userInfo.getWebDriver().manage().getCookieNamed("access_hash").getValue();
            } catch (Exception e) {
            }
            if (value == null) {
                throw new RuntimeException("????????????");
            } else {
                LOGGER.info("????????????");
            }
            Filter filter = (requestSpec, responseSpec, ctx) -> {
                for (Cookie cookie : userInfo.getWebDriver().manage().getCookies()) {
                    requestSpec.cookie(cookie.getName(), cookie.getValue());
                }
                return ctx.next(requestSpec, responseSpec);
            };
            //token????????????????????????
            RestAssured.filters(filter);
        }
    }

}
