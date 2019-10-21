package com.lynch;


/**
 * Created by lynch on 2019-10-20. <br>
 **/
public class TxManagerMain {
    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start("localhost", 8080);
    }
}
