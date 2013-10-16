package com.frs.blockd;

import org.testng.annotations.Test;

@Test
public class BlockdClientTest {

    @Test
    public void rockyTest() throws Exception {

        SimpleClient client = new SimpleClient("localhost", 11311);
        client.connect();
        System.out.println(client.wisdom());
        System.out.println(client.acquire("APOLLO_CREED"));
        System.out.println(client.acquire("MICK_THE_TRAINER", 1000));
        System.out.println(client.acquire("ROCKY_BALBOA", 2000, 'R'));
        System.out.println(client.show());
        System.out.println(client.release("ROCKY_BALBOA"));
        System.out.println(client.release("APOLLO_CREED"));
        System.out.println(client.release("MICK_THE_TRAINER"));
        client.quit();
    }

}
