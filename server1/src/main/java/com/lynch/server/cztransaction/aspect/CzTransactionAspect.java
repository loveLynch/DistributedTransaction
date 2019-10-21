package com.lynch.server.cztransaction.aspect;

import com.lynch.server.cztransaction.annotation.CzTransactional;
import com.lynch.server.cztransaction.transactional.CzTransaction;
import com.lynch.server.cztransaction.transactional.CzTransactionManager;
import com.lynch.server.cztransaction.transactional.TransactionType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
@Aspect
public class CzTransactionAspect implements Ordered {

    @Around("@annotation(com.lynch.server.cztransaction.annotation.CzTransactional)")
    public void invoke(ProceedingJoinPoint point) {
        //第一个事务，创建事务组
        //判断是否为第一个事务
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        CzTransactional czTransactional = method.getAnnotation(CzTransactional.class);

        String groupId = "";
        if (czTransactional.isStart()) {
            //创建事务组
            groupId = CzTransactionManager.createCzTransactionGroup();
        }


        //添加事务到事务组
        CzTransaction czTransaction = CzTransactionManager.createCzTransaction(groupId);

        try {
            point.proceed();//spring原来的逻辑
            //成功则提交
            czTransaction.setTransactionType(TransactionType.commit);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            //否则回滚
            czTransaction.setTransactionType(TransactionType.rollback);

        }

        CzTransactionManager.addCzTransaction(groupId, czTransaction,czTransactional.isEnd());


    }


    /**
     * 设置优先级
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 10000;
    }
}
