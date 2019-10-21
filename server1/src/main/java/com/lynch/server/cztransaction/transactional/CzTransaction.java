package com.lynch.server.cztransaction.transactional;

/**
 * Created by lynch on 2019-10-20. <br>
 * 自定义的一个事务
 * 子事务，状态
 **/
public class CzTransaction {

    private String groupId; //事务组id
    private String transactionId; //事务id
    private TransactionType transactionType;// 事务类型
    private Task task; //任务绑定

    public CzTransaction(String groupId, String transactionId) {
        this.groupId = groupId;
        this.transactionId = transactionId;
        this.task = new Task();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
