//package com.example.demo.ticket;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//
///**
// * @author dinghuang123@gmail.com
// * @since 2022/4/22
// */
//public class HtmlTest {
//
//    public static void main(String[] args) throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader("/Users/dinghuang/Desktop/医院抓包/demo/src/main/resources/test.html"));
//        StringBuilder stringBuilder = new StringBuilder();
//        String line;
//        String ls = System.getProperty("line.separator");
//        while ((line = reader.readLine()) != null) {
//            stringBuilder.append(line);
//            stringBuilder.append(ls);
//        }
//        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//        reader.close();
//        String content = stringBuilder.toString();
//        Document ticketDocument = Jsoup.parse(content);
//        Element ticketElement = ticketDocument.getElementById("suborder");
//        Element ticketTipsElement = ticketDocument.getElementsByClass("tips_wrap").first();
//        String schData = ticketElement.getAllElements().get(1).val();
//        System.out.println(schData);
//        String mid = ticketTipsElement.getAllElements().get(1).val();
//        System.out.println(mid);
//        String detlid = ticketDocument.getElementById("detlid").val();
//        System.out.println(detlid);
//        String detlidRealtime = ticketDocument.getElementById("detlid_realtime").val();
//        System.out.println(detlidRealtime);
//        String levelCode = ticketDocument.getElementById("level_code").val();
//        System.out.println(levelCode);
//    }
//}
