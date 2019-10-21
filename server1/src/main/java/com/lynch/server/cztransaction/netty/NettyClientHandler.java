package com.lynch.server.cztransaction.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lynch.server.cztransaction.transactional.CzTransaction;
import com.lynch.server.cztransaction.transactional.CzTransactionManager;
import com.lynch.server.cztransaction.transactional.TransactionType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接收数据：" + msg.toString());

        JSONObject jsonObject = JSON.parseObject((String) msg);

        String groupId = jsonObject.getString("groupId");
        String command = jsonObject.getString("command");
        System.out.println("接收command：" + command);

        //对事务进行操作

        //通知子事务去执行

        CzTransaction czTransaction = CzTransactionManager.getCzTransaction(groupId);
        if (command.equals("commit")) {
            czTransaction.setTransactionType(TransactionType.commit);
        } else {
            czTransaction.setTransactionType(TransactionType.rollback);

        }

        czTransaction.getTask().singalTask();

    }

    public synchronized Object call(JSONObject data) throws Exception {
        return null;
    }
}
