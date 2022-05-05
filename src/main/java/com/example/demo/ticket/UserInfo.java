package com.example.demo.ticket;

import org.openqa.selenium.WebDriver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author dinghuang123@gmail.com
 * @since 2022/4/20
 */
public class UserInfo {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 医生id
     */
    private String docId;

    /**
     * 地址
     */
    private String address;

    /**
     * 地址id
     */
    private String addressId;

    /**
     * 医院id
     */
    private String unitId;

    /**
     * 医院部门id
     */
    private String depId;

    private WebDriver webDriver;


    public WebDriver getWebDriver() {
        return webDriver;
    }

    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    private volatile List<Map<Object, Object>> tickets = Collections.synchronizedList(new LinkedList<>());

    public List<Map<Object, Object>> getTickets() {
        return tickets;
    }

    public void setTickets(List<Map<Object, Object>> tickets) {
        this.tickets = tickets;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }
}
