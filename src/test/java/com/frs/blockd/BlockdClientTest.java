package com.frs.blockd;

import org.testng.annotations.Test;

@Test
public class BlockdClientTest {

    @Test
    public void rockyTest() throws Exception {

        SimpleClient client = new SimpleClient("localhost", 11311);
        client.connect();
        System.out.println(client.wisdom());
        System.out.println(client.acquire("APOLLO"));
        System.out.println(client.acquire("MICK", 1000));
        System.out.println(client.acquire("ROCKY", 2000, 'R'));
        Thread.sleep(1000);
        System.out.println(client.show());
        System.out.println(client.release("ROCKY"));
        System.out.println(client.release("APOLLO"));
        System.out.println(client.release("MICK"));
        client.quit();
    }

}
