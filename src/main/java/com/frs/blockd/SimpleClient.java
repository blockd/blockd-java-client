package com.frs.blockd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a simple client for the Blockd Server.
 * See https://github.com/blockd/blockd-server/wiki/Protocol
 */
public class SimpleClient implements BlockdClient {

    private ArrayList<BlockdListener> listeners = new ArrayList<BlockdListener>();
    private Socket socket;
    private OutputStream outputStream;
    private BufferedInputStream inputStream;
    private String host;
    private int port;

    /**
     * Constructor. Sets the host and port, but won't connect to the server
     * until <code>connect()</code> is called.
     *
     * @param host blockd host name or IP address
     * @param port blockd listen port
     */
    public SimpleClient(String host, int port) {

        this.host = host;
        this.port = port;
    }

    /**
     * This method returns the host name given in the constructor.
     *
     * @return The host to which this client communicates.
     */
    public String getHost() {

        return (host);
    }

    /**
     * This method returns the port number given in the constructor.
     *
     * @return The host port to which the client communicates.
     */
    public int getPort() {

        return (port);
    }

    /**
     * This method connects to the blockd server. This must be called
     * before any commands are sent to the server.
     *
     * @throws Exception On a connection/network exception.
     */
    @Override
    public void connect() throws Exception {

        socket = new Socket(host, port);
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());
        String ehlo = readResponse();
        if ("IMUSTBLOCKYOU".equals(ehlo) == false) {
            throw new Exception("Invalid registration response.");
        }
    }

    /**
     * Checks that the socket is connected to the server and that the
     * input and output streams are non-null.
     *
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {

        return (socket != null && socket.isConnected() && inputStream != null && outputStream != null);
    }

    /**
     * Closes the connection to the blockd server. Any locks acquired
     * should be released. You can call <code>connect()</code> again to
     * reconnect to the server.
     *
     * @throws Exception
     */
    @Override
    public void quit() throws Exception {

        sendCommand("QUIT");
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    /**
     * Calls blockd's WISDOM command.
     *
     * @return Wisdom
     * @throws Exception
     */
    @Override
    public String wisdom() throws Exception {

        sendCommand("WISDOM");
        return (readResponse());
    }

    /**
     * Calls blockd's SHOW command.
     *
     * @return A list of the current lock identifiers.
     * @throws Exception
     */
    @Override
    public String show() throws Exception {

        sendCommand("SHOW");
        return (readResponse());
    }

    /**
     * Calls blockd's ACQUIRE command to lock the given lock identifier.
     *
     * @param lockId The id to lock
     * @return
     * @throws Exception
     */
    @Override
    public String acquire(String lockId) throws Exception {

        sendCommand(String.format("ACQUIRE %s", lockId));
        String response = readResponse();
        if ( response.startsWith("LOCKPENDING") ) {
            while (inputStream.available() == 0) {
                Thread.sleep(100);
            }
            response = readResponse();
            if ( response.startsWith("ACQUIRETIMEOUT") ) {
                throw new Exception("ACQUIRETIMEOUT for " + lockId);
            }
        }
        return ( response );
    }

    /**
     * Calls blockd's ACQUIRE command to lock the given lock identifier
     * with the specified timeout. If the lock is not available, this
     * method will block until <code>timeout</code> is reached, or the
     * lock becomes available.
     *
     * @param lockId  The identifier to lock
     * @param timeout The amount of time to wait for a lock.
     * @return
     * @throws Exception
     */
    @Override
    public String acquire(String lockId, int timeout) throws Exception {

        sendCommand(String.format("ACQUIRE %s %d W", lockId, timeout));
        String response = readResponse();
        if ( response.startsWith("LOCKPENDING") ) {
            while (inputStream.available() == 0) {
                Thread.sleep(100);
            }
            response = readResponse();
            if ( response.startsWith("ACQUIRETIMEOUT") ) {
                throw new Exception("ACQUIRETIMEOUT for " + lockId);
            }
        }
        return ( response );
    }

    /**
     * Calls blockd's ACQUIRE command to lock the given lock identifier
     * with the specified timeout.
     *
     * @param lockId  The identifier to lock
     * @param timeout The amount of time to wait for a lock.
     * @param mode    'W'rite or 'R'ead mode
     * @return
     * @throws Exception
     */
    @Override
    public String acquire(String lockId, int timeout, char mode) throws Exception {

        sendCommand(String.format("ACQUIRE %s %d %s", lockId, timeout, mode));
        String response = readResponse();
        if ( response.startsWith("LOCKPENDING") ) {
            while (inputStream.available() == 0) {
                Thread.sleep(100);
            }
            response = readResponse();
            if ( response.startsWith("ACQUIRETIMEOUT") ) {
                throw new Exception("ACQUIRETIMEOUT for " + lockId);
            }
        }
        return ( response );
    }

    /**
     * Releases the specified lock identifier.
     *
     * @param lockId The lock identifier to release
     * @return
     * @throws Exception
     */
    @Override
    public String release(String lockId) throws Exception {

        sendCommand(String.format("RELEASE %s", lockId));
        String response = readResponse();
        if (response.startsWith("NOLOCKTORELEASE") ) {
            throw new Exception("NOLOCKTORELEASE for " + lockId);
        }
        return (response);
    }

    /**
     * Calls blockd's command to release all locked items.
     *
     * @return
     * @throws Exception
     */
    @Override
    public String releaseAll() throws Exception {

        sendCommand("RELEASEALL");
        // TODO: Server will send a RELEASED XYZ for each
        // lock, so get them all.
        return (readResponse());
    }

    /**
     * This private method is a convenience method to print to
     * the socket.
     *
     * @param cmd The string to be printed.
     * @throws Exception
     */
    private synchronized void sendCommand(String cmd) throws Exception {

        outputStream.write(cmd.getBytes());
        outputStream.write('\n');
        outputStream.flush();
    }

    /**
     * This private method is a convenience method to read from
     * the socket, up to 1024 characters.
     *
     * @return The string that was read.
     * @throws IOException
     */
    private synchronized String readResponse() throws IOException {

        int maxBytes = 1024;
        byte[] buffer = new byte[maxBytes];
        int curbyte = 0;
        int bytesRead = 0;
        while (true) {
            curbyte = inputStream.read();
            if (curbyte < 0 || curbyte == '\n') {
                break;
            }
            if (curbyte != '\r') {
                buffer[bytesRead++] = (byte) curbyte;
            }
            if (bytesRead >= maxBytes) {
                throw new IOException("readResponse: Line length exceeded "
                                              + maxBytes + " bytes!");
            }
        }
        return new String(buffer, 0, bytesRead);
    }

    /**
     * This method adds a <code>BlockdListener</code> to this client
     * for notification of async command results.
     *
     * @param listener  The listener to add.
     */
    @Override
    public void addBlockdListener(BlockdListener listener) {

        if ( !listeners.contains(listener) ) {
            listeners.add(listener);
        }
    }

    /**
     * This method removes a previously added <code>BlockdListener</code>
     * from this client.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeBlockdListener(BlockdListener listener) {

        listeners.remove(listener);
    }

    /**
     * This method returns a list of the currently reigstered
     * <code>BlockdListener</code> listeners.
     *
     * @return  The list of current listeners.
     */
    @Override
    public List<BlockdListener> getListeners() {

        return ( listeners );
    }
}
