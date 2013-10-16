package com.frs.blockd;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * The ClusterClient is simply a wrapper around multiple BasicClient instances.
 * A very simple consistent hashing method is used to route requests to a particular
 * server.
 */
public class ClusterClient implements BlockdClient {

    private TreeMap<Integer, SimpleClient> nodes = new TreeMap<Integer, SimpleClient>();

    /**
     * Constructor.
     */
    public ClusterClient() {
        // No Op
    }

    /**
     * This method adds a blockd server to cluster of servers know to this client
     *
     * @param host
     * @param port
     */
    public void addNode(String host, int port) {

        SimpleClient client = new SimpleClient(host, port);
        nodes.put(computeHash(host + port), client);
    }

    /**
     * A very contrived has function, but one that should be easily
     * implemented in JS also.
     *
     * @param key
     * @return
     */
    private int computeHash(String key) {

        int hash = 7;
        for (int i = 0; i < key.length(); i++) {
            hash = hash * 31 + key.charAt(i);
        }
        hash = Math.abs(hash);
        return (hash);
    }

    /**
     * Determine which server will handle a request for a given
     * lockId.
     *
     * @param lockId
     * @return
     * @throws Exception
     */
    public int whichNode(String lockId) throws Exception {

        int lockKey = computeHash(lockId);
        int range = Integer.MAX_VALUE / nodes.size();
        int start = 0;
        int end = range;
        Iterator<Integer> keys = nodes.keySet().iterator();
        while (keys.hasNext()) {
            int key = keys.next();
            if (lockKey >= start && lockKey < end) {
                return (key);
            }
            start += range;
            end += range;
        }
        throw new Exception("Should never get here!");
    }

    @Override
    public String getHost() {

        return (null);
    }

    @Override
    public int getPort() {

        return (0);
    }

    /**
     * Connects to all the blockd servers added via <code>addNode()</code>
     *
     * @throws Exception
     */
    @Override
    public void connect() throws Exception {

        Iterator<Integer> keys = nodes.keySet().iterator();
        while (keys.hasNext()) {
            BlockdClient client = nodes.get(keys.next());
            client.connect();
        }
    }

    /**
     * This method checks to see if the client is connected. In the case of
     * the ClusterClient, this will return true iif we're connected to
     * all specified nodes.
     *
     * @return
     * @throws Exception
     */
    @Override
    public boolean isConnected() throws Exception {

        boolean connected = false;
        Iterator<Integer> keys = nodes.keySet().iterator();
        while (keys.hasNext()) {
            BlockdClient client = nodes.get(keys.next());
            connected = connected && client.isConnected();
        }
        return (connected);
    }

    /**
     * This method implements the QUIT operation.
     *
     * @throws Exception
     */
    @Override
    public void quit() throws Exception {

        Iterator<Integer> keys = nodes.keySet().iterator();
        while (keys.hasNext()) {
            BlockdClient client = nodes.get(keys.next());
            client.quit();
        }
    }

    /**
     * This method implements the WISDOM operation
     *
     * @return
     * @throws Exception
     */
    @Override
    public String wisdom() throws Exception {

        BlockdClient client = nodes.get(nodes.keySet().iterator().next());
        return (client.wisdom());
    }

    /**
     * This method implements the SHOW operation.
     *
     * @return
     * @throws Exception
     */
    @Override
    public String show() throws Exception {

        StringBuilder buff = new StringBuilder();
        Iterator<Integer> keys = nodes.keySet().iterator();
        while (keys.hasNext()) {
            BlockdClient client = nodes.get(keys.next());
            buff.append(client.getHost());
            buff.append(":");
            buff.append(client.getPort());
            buff.append("-");
            buff.append(client.show());
            buff.append("\n");
        }
        return (buff.toString());
    }

    /**
     * This method implements the ACQUIRE operation.
     *
     * @param lockId
     * @return
     * @throws Exception
     */
    @Override
    public String acquire(String lockId) throws Exception {

        BlockdClient client = nodes.get(whichNode(lockId));
        return client.acquire(lockId);
    }

    /**
     * This method implements the ACQUIRE operation.
     *
     * @param lockId
     * @param timeout
     * @return
     * @throws Exception
     */
    @Override
    public String acquire(String lockId, int timeout) throws Exception {

        BlockdClient client = nodes.get(whichNode(lockId));
        return client.acquire(lockId, timeout);
    }

    /**
     * This method implements the ACQUIRE operation.
     *
     * @param lockId
     * @param timeout
     * @param mode
     * @return
     * @throws Exception
     */
    @Override
    public String acquire(String lockId, int timeout, char mode) throws Exception {

        BlockdClient client = nodes.get(whichNode(lockId));
        return client.acquire(lockId, timeout, mode);
    }

    /**
     * This method implements the RELEASE operation.
     *
     * @param lockId
     * @return
     * @throws Exception
     */
    @Override
    public String release(String lockId) throws Exception {

        BlockdClient client = nodes.get(whichNode(lockId));
        return client.release(lockId);
    }

    /**
     * This method implements the RELEASEALL operation.
     *
     * @return
     * @throws Exception
     */
    @Override
    public String releaseAll() throws Exception {

        StringBuffer buff = new StringBuffer();

        Iterator<Integer> keys = nodes.keySet().iterator();
        while (keys.hasNext()) {
            BlockdClient client = nodes.get(keys.next());
            buff.append(client.releaseAll());
            buff.append("\n");
        }
        return (buff.toString());
    }
}
