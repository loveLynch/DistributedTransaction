package com.lynch.server;

import org.apache.ibatis.annotations.Mapper;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
@Mapper
public interface DemoDao {
    void insert(String name);
}
