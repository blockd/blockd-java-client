package com.frs.blockd;

import java.util.List;

/**
 * The base interface for blockd clients.
 */
public interface BlockdClient {

    /**
     * The blockd server host name
     *
     * @return
     */
    public String getHost();

    /**
     * The blockd server port
     *
     * @return
     */
    public int getPort();

    /**
     * Connect to the blockd server
     *
     * @throws Exception
     */
    public void connect() throws Exception;

    /**
     * Are we connected to a server?
     *
     * @return
     * @throws Exception
     */
    public boolean isConnected() throws Exception;

    /**
     * Implements the QUIT operation.
     *
     * @throws Exception
     */
    public void quit() throws Exception;

    /**
     * Implements the WISDOM operation.
     *
     */
    public String wisdom() throws Exception;

    /**
     * Implements SHOW operation.
     *
     * @return
     * @throws Exception
     */
    public List<String> show() throws Exception;

    /**
     * Implements the ACQUIRE [lockId] operation.
     *
     * @param lockId
     * @return
     * @throws Exception
     */
    public String acquire(String lockId) throws Exception;

    /**
     * Implements the ACQUIRE [lockId] [timeout] operation.
     *
     * @param lockId
     * @param timeout
     * @return
     * @throws Exception
     */
    public String acquire(String lockId, int timeout) throws Exception;

    /**
     * Implements the ACQUIRE [lockId] [timeout] [mode] operation.
     *
     * @param lockId
     * @param timeout
     * @param mode
     * @return
     * @throws Exception
     */
    public String acquire(String lockId, int timeout, String mode) throws Exception;

    /**
     * Implements the RELEASE [lockId] operation.
     *
     * @param lockId
     * @return
     * @throws Exception
     */
    public String release(String lockId) throws Exception;

    /**
     * Implements the RELEASEALL operation.
     *
     * @return
     * @throws Exception
     */
    public List<String> releaseAll() throws Exception;

}
