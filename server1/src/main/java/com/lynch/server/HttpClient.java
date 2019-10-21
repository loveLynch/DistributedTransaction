package com.lynch.server;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
public class HttpClient {
    public static void get(String url) {
        //1.打开浏览器，发起请求
        CloseableHttpClient client = HttpClients.createDefault();
        //2.输入网址
        HttpGet httpGet = new HttpGet(url);
        //3.回车。拿到响应数据
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpGet);

            //4.解析响应
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = response.getEntity();
                System.out.println(EntityUtils.toString(httpEntity, "utf-8"));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
