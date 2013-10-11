package com.frs.blockd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This is a simple client for the Blockd Server.
 * See https://github.com/blockd/blockd-server/wiki/Protocol
 *
 */
public class BlockdClient {

    private Socket socket;
    private OutputStream outputStream;
    private BufferedInputStream inputStream;
    private String host;
    private int port;

    /**
     * Constructor. Sets the host and port, but won't connect to the server
     * until <code>connect()</code> is called.
     *
     * @param host  blockd host name or IP address
     * @param port  blockd listen port
     */
    public BlockdClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * This method connects to the blockd server. This must be called
     * before any commands are sent to the server.
     *
     * @throws Exception    On a connection/network exception.
     */
    public void connect() throws Exception {
        socket = new Socket(host, port);
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());
        String ehlo = readLine();
        if ( "IMUSTBLOCKYOU".equals(ehlo) == false ) {
            throw new Exception("Invalid registration response.");
        }
    }

    /**
     * Checks that the socket is connected to the server and that the
     * input and output streams are non-null.
     *
     * @return  true if connected, false otherwise.
     */
    public boolean isConnected() {
        return ( socket.isConnected() && inputStream != null && outputStream != null );
    }

    /**
     * Closes the connection to the blockd server. Any locks acquired
     * should be released. You can call <code>connect()</code> again to
     * reconnect to the server.
     *
     * @throws Exception
     */
    public void close() throws Exception {
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    /**
     * Calls blockd's WISDOM command.
     *
     * @return  Wisdom
     * @throws Exception
     */
    public String wisdom() throws Exception {
        printLine("WISDOM");
        return (readLine());
    }

    /**
     * Calls blockd's SHOW command.
     *
     * @return A list of the current lock identifiers.
     * @throws Exception
     */
    public String show() throws Exception {
        printLine("SHOW");
        return (readLine());
    }

    /**
     * Calls blockd's ACQUIRE command to lock the give lock identifier.
     *
     * @param lockId    The id to lock
     *
     * @return
     * @throws Exception
     */
    public String aquire(String lockId) throws Exception {
        printLine(String.format("ACQUIRE %s", lockId));
        return (readLine());
    }

    /**
     * Calls blockd's ACQUIRE command to lock the given lock identifier
     * with the specified timeout.
     *
     * @param lockId    The identifier to lock
     * @param timeout   The amount of time to wait for a lock.
     *
     * @return
     * @throws Exception
     */
    public String aquire(String lockId, int timeout) throws Exception {
        printLine(String.format("ACQUIRE %s %d W", lockId, timeout));
        return (readLine());
    }

    /**
     * Calls blockd's ACQUIRE command to lock the given lock identifier
     * with the specified timeout.
     *
     * @param lockId    The identifier to lock
     * @param timeout   The amount of time to wait for a lock.
     * @param mode      'W'rite or 'R'ead mode
     *
     * @return
     * @throws Exception
     */
    public String aquire(String lockId, int timeout, char mode) throws Exception {
        printLine(String.format("ACQUIRE %s %d %s", lockId, timeout, mode));
        return (readLine());
    }

    /**
     * Releases the specified lock identifier.
     *
     * @param lockId The lock identifier to release
     *
     * @return
     * @throws Exception
     */
    public String release(String lockId) throws Exception {
        printLine(String.format("RELEASE %s", lockId));
        return (readLine());
    }

    /**
     * Calls blockd's command to release all locked items.
     *
     * @return
     * @throws Exception
     */
    public String releaseAll() throws Exception {
        printLine("RELEASEALL");
        return (readLine());
    }

    /**
     * This private method is a convenience method to print to
     * the socket.
     *
     * @param cmd   The string to be printed.
     *
     * @throws Exception
     */
    private void printLine(String cmd) throws Exception {
        outputStream.write(cmd.getBytes());
        outputStream.write('\n');
        outputStream.flush();
    }

    /**
     * This private method is a convenience method to read from
     * the socket, up to 1024 characters.
     *
     * @return  The string that was read.
     *
     * @throws IOException
     */
    private String readLine() throws IOException {
        int maxBytes = 1024;
        byte[] buffer = new byte[maxBytes];
        int curbyte = 0;
        int bytesRead = 0;
        while (true) {
            curbyte = inputStream.read();
            if (curbyte < 0 || curbyte == '\n')
                break;
            if (curbyte != '\r')
                buffer[bytesRead++] = (byte) curbyte;
            if (bytesRead >= maxBytes)
                throw new IOException("readLine: Line length exceeded "
                                              + maxBytes + " bytes!");
        }
        return new String(buffer, 0, bytesRead);
    }

}
