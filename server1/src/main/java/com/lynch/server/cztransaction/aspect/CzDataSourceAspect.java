package com.lynch.server.cztransaction.aspect;

import com.lynch.server.cztransaction.connection.CzConnection;
import com.lynch.server.cztransaction.transactional.CzTransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.sql.Connection;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
@Aspect
public class CzDataSourceAspect {
    /**
     * 由切面获取自定义的Connection
     *
     * @param point
     * @return
     */
    @Around("execution(* javax.sql.DataSource.getConnection(..))")//ConnectionImpl
    public Connection around(ProceedingJoinPoint point) {
        try {
            //什么都不干，直接返回的是原来的ConnectionImpl
            //return  point.proceed();
            Connection connection = (Connection) point.proceed();
            return new CzConnection(connection, CzTransactionManager.getCurrent());

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
