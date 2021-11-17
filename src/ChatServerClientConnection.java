import java.io.*;
import java.net.Socket;

public class ChatServerClientConnection implements Runnable {
    private ChatServerConnectionManager connectionManager;
    private Socket socket;
    private DataInputStream receiveMessagesFromClientStream = null;
    private DataOutputStream sendMessagesToClientStream = null;
    private boolean isActive = false;

    public ChatServerClientConnection(final ChatServerConnectionManager serv, final Socket sock) {
        super();
        connectionManager = serv;
        socket = sock;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }

    public void sendToClient(final String msg) {
        try {
            sendMessagesToClientStream.writeUTF(msg);
            sendMessagesToClientStream.flush();
        } catch (IOException ioe) {
            System.out.println(getID() + " ERROR sending: " + ioe.getMessage());
            connectionManager.removeClient(getID());
            setActive(false);
        }
    }

    public int getID() {
        return socket.getPort();
    }

    public void run() {
        System.out.println("Server Thread " + getID() + " running.");
        try {
            receiveMessagesFromClientStream = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream())
            );
            sendMessagesToClientStream = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream())
            );
            setActive(true);
            while (isActive()) {
                    connectionManager.handleMessage(
                            getID(),
                            receiveMessagesFromClientStream.readUTF()
                    );
            }
        } catch (IOException ioe) {
            System.out.println(getID() + " ERROR reading: " + ioe.getMessage());
            connectionManager.removeClient(getID());
            setActive(false);
        }
    }

    public void close() throws IOException {
        if (socket != null)
            socket.close();
        if (receiveMessagesFromClientStream != null)
            receiveMessagesFromClientStream.close();
        if (sendMessagesToClientStream != null)
            sendMessagesToClientStream.close();
    }
}