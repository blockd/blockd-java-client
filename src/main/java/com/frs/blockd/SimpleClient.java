package com.frs.blockd;

import org.json.JSONArray;
import org.json.JSONObject;

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
        JSONObject obj = readResponse();
        if ( "IMUSTBLOCKYOU".equals(obj.getString("status")) == false ) {
            throw new Exception("Invalid connect response.");
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

        JSONObject request = new JSONObject();
        request.put("command", "QUIT");
        request.put("nonce", System.currentTimeMillis());
        sendCommand(request);
        JSONObject response = readResponse();
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    /**
     * Calls blockd's WISDOM command.
     *
     * @throws Exception
     */
    @Override
    public String wisdom() throws Exception {

        JSONObject request = new JSONObject();
        request.put("command", "WISDOM");
        request.put("nonce", System.currentTimeMillis());
        sendCommand(request);
        JSONObject response = readResponse();
        return ( response.getString("quote"));
    }

    /**
     * Calls blockd's SHOW command.
     *
     * @return A list of the current lock identifiers.
     * @throws Exception
     */
    @Override
    public List<String> show() throws Exception {

        List<String> lockIds = new ArrayList<String>();
        JSONObject request = new JSONObject();
        request.put("command", "SHOW");
        request.put("nonce", System.currentTimeMillis());
        sendCommand(request);
        JSONObject response = readResponse();
        JSONArray locks = response.getJSONArray("locks");
        StringBuffer buff = new StringBuffer();
        for ( int i = 0; i < locks.length(); i++ ) {
            JSONObject lock = locks.getJSONObject(i);
            lockIds.add(lock.getString("lockId"));

        }
        return (lockIds);
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

        JSONObject request = new JSONObject();
        request.put("command", "ACQUIRE");
        request.put("lockId", lockId);
        request.put("nonce", System.currentTimeMillis());
        sendCommand(request);
        JSONObject response = readResponse();
        return ( response.getString("status"));
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

        JSONObject request = new JSONObject();
        request.put("command", "ACQUIRE");
        request.put("lockId", lockId);
        request.put("nonce", System.currentTimeMillis());
        request.put("timeout", timeout);
        request.put("mode", "W");
        sendCommand(request);
        JSONObject response = readResponse();
        if ( response.getString("status").equals("LOCKPENDING") ) {
            while (inputStream.available() == 0) {
                Thread.sleep(100);
            }
            response = readResponse();
            if ( response.getString("status").equals("ACQUIRETIMEOUT") ) {
                throw new Exception("ACQUIRETIMEOUT for " + lockId);
            }
        }
        return ( response.getString("status") );
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
    public String acquire(String lockId, int timeout, String mode) throws Exception {

        JSONObject request = new JSONObject();
        request.put("command", "ACQUIRE");
        request.put("lockId", lockId);
        request.put("nonce", System.currentTimeMillis());
        request.put("timeout", timeout);
        request.put("mode", mode);
        sendCommand(request);
        JSONObject response = readResponse();
        if ( response.getString("status").equals("LOCKPENDING") ) {
            while (inputStream.available() == 0) {
                Thread.sleep(100);
            }
            response = readResponse();
            if ( response.getString("status").equals("ACQUIRETIMEOUT") ) {
                throw new Exception("ACQUIRETIMEOUT for " + lockId);
            }
        }
        return ( response.getString("status") );
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

        JSONObject request = new JSONObject();
        request.put("command", "RELEASE");
        request.put("lockId", lockId);
        request.put("nonce", System.currentTimeMillis());
        sendCommand(request);
        JSONObject response = readResponse();
        if ("NOLOCKTORELEASE".equals(response.getString("status")) ) {
            throw new Exception("NOLOCKTORELEASE for " + lockId);
        }
        return (response.getString("status"));
    }

    /**
     * Calls blockd's command to release all locked items.
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<String> releaseAll() throws Exception {

        List<String> lockIds = new ArrayList<String>();
        JSONObject request = new JSONObject();
        request.put("command", "RELEASEALL");
        request.put("nonce", System.currentTimeMillis());
        sendCommand(request);
        JSONObject response = readResponse();
        if ( "NOLOCKSTORELEASEALL".equals(response.getString("status")) == false ) {
            lockIds.add(response.getString("lockId"));
            while ( inputStream.available() > 0 ) {
                response = readResponse();
                lockIds.add(response.getString("lockId"));
            }
        }
        return (lockIds);
    }

    /**
     * This private method is a convenience method to print to
     * the socket.
     *
     * @param cmd The string to be printed.
     * @throws Exception
     */
    private synchronized void sendCommand(JSONObject cmd) throws Exception {

        System.out.println("SEND: " + cmd.toString());
        outputStream.write(cmd.toString().getBytes());
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
    private synchronized JSONObject readResponse() throws Exception {

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
        String result = new String(buffer, 0, bytesRead);
        System.out.println("RECV: " + result);
        JSONObject response = new JSONObject(result);
        return ( response );
    }


}
