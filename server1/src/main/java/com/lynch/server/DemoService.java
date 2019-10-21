package com.lynch.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by lynch on 2019-10-20. <br>
 **/
@Service
public class DemoService {
    @Autowired
    private DemoDao demoDao;

    @Transactional
//    @CzTransactional(isStart = true) //分布式事务的开始，true
    public void test() {
        demoDao.insert("server1");
        HttpClient.get("http://localhost:8082/server2/test");//server2
        int i = 100 / 0;
    }
}
