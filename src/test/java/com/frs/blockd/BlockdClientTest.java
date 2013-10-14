package com.frs.blockd;

import org.testng.annotations.Test;

@Test
public class BlockdClientTest {

    @Test
    public void rockyTest() throws Exception {

        BlockdClient client = new BlockdClient("localhost", 11311);
        client.connect();
        System.out.println(client.wisdom());
        System.out.println(client.aquire("APOLLO"));
        System.out.println(client.aquire("MICK", 1000));
        System.out.println(client.aquire("ROCKY", 2000, 'R'));
        Thread.sleep(1000);
        System.out.println(client.show());
        System.out.println(client.release("ROCKY"));
        System.out.println(client.release("APOLLO"));
        System.out.println(client.release("MICK"));
        client.close();
    }

}
