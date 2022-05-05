package com.example.demo.ticket;

import com.google.common.collect.Lists;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dinghuang123@gmail.com
 * @since 2022/4/22
 */
@Component
public class WebDriverConfiguration {

    private ChromeDriver webDriver;

    public WebDriver getWebDriver() {
        if (webDriver == null) {
            System.setProperty("webdriver.chrome.driver", "/root/ticket/chromedriver");
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("disable-infobars");
            chromeOptions.addArguments("--disable-blink-features");
            chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
            //设置ExperimentalOption
            List<String> excludeSwitches = Lists.newArrayList("enable-automation");
            chromeOptions.setExperimentalOption("excludeSwitches", excludeSwitches);
            chromeOptions.setExperimentalOption("useAutomationExtension", false);
            chromeOptions.addArguments("user-agent=" + CommonUtils.randomUserAgent());
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            //修改window.navigator.webdirver=undefined，防机器人识别机制
            this.webDriver = new ChromeDriver(chromeOptions);
            this.webDriver.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})", "");
            return this.webDriver;
        } else {
            return this.webDriver;
        }

    }

}
