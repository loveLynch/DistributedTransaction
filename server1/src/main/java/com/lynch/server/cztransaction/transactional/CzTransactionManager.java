package com.lynch.server.cztransaction.transactional;

import com.alibaba.fastjson.JSONObject;
import com.lynch.server.cztransaction.netty.NettyClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lynch on 2019-10-20. <br>
 * 事务管理者
 **/

public class CzTransactionManager {

    private static NettyClient nettyClient;

    private static Map<String, CzTransaction> map = new HashMap<>();//为了测试简单，先简单写为1对1

    private static ThreadLocal<CzTransaction> current = new ThreadLocal<>();

    //静态属性注入，set方法注入
    @Autowired
    public void setNettyClient(NettyClient nettyClient) {
        CzTransactionManager.nettyClient = nettyClient;
    }

    /**
     * 创建事务组
     *
     * @return
     */
    public static String createCzTransactionGroup() {
        String groupId = UUID.randomUUID().toString();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupId", groupId);
        jsonObject.put("command", "create");
        nettyClient.send(jsonObject);
        return groupId;
    }

    /**
     * 创建事务
     *
     * @param groupId
     * @return
     */
    public static CzTransaction createCzTransaction(String groupId) {
        String transactionId = UUID.randomUUID().toString();
        CzTransaction czTransaction = new CzTransaction(groupId, transactionId);
        current.set(czTransaction);
        map.put(groupId, czTransaction);
        return czTransaction;

    }

    /**
     * 添加事务到事务组
     *
     * @param groupId
     * @param czTransaction
     */
    public static void addCzTransaction(String groupId, CzTransaction czTransaction, Boolean isEnd) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupId", czTransaction.getGroupId());
        jsonObject.put("transactionId", czTransaction.getTransactionId());
        jsonObject.put("command", "add");
        jsonObject.put("isEnd", isEnd);

        nettyClient.send(jsonObject);

        System.out.println("添加事务");


    }


    /**
     * 根据groupId 获取事务
     *
     * @param groupId
     * @return
     */
    public static CzTransaction getCzTransaction(String groupId) {
        return map.get(groupId);
    }

    /**
     * 获取同一个线程的事务对象
     *
     * @return
     */
    public static CzTransaction getCurrent() {
        return current.get();
    }

}
