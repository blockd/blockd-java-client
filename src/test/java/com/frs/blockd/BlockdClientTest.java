package com.frs.blockd;

import org.testng.annotations.Test;

import java.util.List;

@Test
public class BlockdClientTest {


    @Test
    public void testHelloGoodbye() throws Exception {

        SimpleClient clientOne = new SimpleClient("localhost", 11311);
        clientOne.connect();
        clientOne.quit();
    }

    @Test
    public void testWisdom() throws Exception {

        SimpleClient clientOne = new SimpleClient("localhost", 11311);
        clientOne.connect();
        String wisdom = clientOne.wisdom();
        assert(wisdom != null && wisdom.length() > 0 );
    }

    @Test
    public void testRelease() throws Exception {

        SimpleClient clientOne = new SimpleClient("localhost", 11311);
        clientOne.connect();
        clientOne.acquire("HelloWorld");
        clientOne.release("HelloWorld");
    }

    @Test(expectedExceptions = {Exception.class})
    public void testAquireLockTimeout() throws Exception {

        SimpleClient clientOne = new SimpleClient("localhost", 11311);
        SimpleClient clientTwo = new SimpleClient("localhost", 11311);
        clientOne.connect();
        clientTwo.connect();
        clientOne.acquire("HelloWorld");
        clientTwo.acquire("HelloWorld", 1000);
    }


    @Test
    public void testReleaseAll() throws Exception {

        SimpleClient client = new SimpleClient("localhost", 11311);
        client.connect();
        client.acquire("HelloWorld1");
        client.acquire("HelloWorld2");
        client.acquire("HelloWorld3");
        client.releaseAll();
    }

    @Test
    public void testShow() throws Exception {

        SimpleClient client = new SimpleClient("localhost", 11311);
        client.connect();
        client.acquire("HelloWorld1");
        client.acquire("HelloWorld2");
        client.acquire("HelloWorld3");
        List<String> lockIds = client.show();
        assert(lockIds.size() == 3);
    }

    @Test
    public void testReleaseNothing() throws Exception {

        SimpleClient client = new SimpleClient("localhost", 11311);
        client.connect();
        client.releaseAll();
    }

    @Test(expectedExceptions = {Exception.class})
    public void testReleaseNonLockedId() throws Exception {

        SimpleClient client = new SimpleClient("localhost", 11311);
        client.connect();
        client.release("NeverLocked");
    }


}
