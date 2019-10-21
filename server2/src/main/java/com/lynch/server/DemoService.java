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
    public void test() {
        demoDao.insert("server2");
    }
}
