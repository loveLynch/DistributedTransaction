package com.lynch.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
@RestController
@RequestMapping("server1")
public class DemoController {
    @Autowired
    private DemoService demoService;

    @RequestMapping(value = "test")
    public void test(){
        demoService.test();
    }
}
