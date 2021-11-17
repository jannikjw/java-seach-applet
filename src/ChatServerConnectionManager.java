import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServerConnectionManager implements Runnable {
    private final Map<Integer, ChatServerClientConnection> clientConnections = new HashMap<>();
    private ServerSocket incomingConnectionRequestHandler;
    private Thread acceptClientConnectionsThread;

    public ChatServerConnectionManager(final int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            incomingConnectionRequestHandler = new ServerSocket(port);
            System.out.println("Server started: " + incomingConnectionRequestHandler);
            start();
        } catch (IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while (acceptClientConnectionsThread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addIncomingClientConnection(incomingConnectionRequestHandler.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    public void start() {
        if (acceptClientConnectionsThread == null) {
            acceptClientConnectionsThread = new Thread(this);
            acceptClientConnectionsThread.start();
        }
    }

    public void stop() {
        if (acceptClientConnectionsThread != null) {
            acceptClientConnectionsThread.stop();
            acceptClientConnectionsThread = null;
        }
    }

    public synchronized void handleMessage(final int clientConnectionID, final String chatMessage) {
        if (chatMessage.equals(".bye") && clientConnections.containsKey(clientConnectionID)) {
            clientConnections.get(clientConnectionID).sendToClient(".bye");
            removeClient(clientConnectionID);
        } else {
            final String msg = clientConnectionID + ": " + chatMessage;
            for (ChatServerClientConnection c: clientConnections.values())
                c.sendToClient(msg);
        }
    }

    public synchronized void removeClient(final int clientConnectionID) {
        if (clientConnections.containsKey(clientConnectionID)) {
            final ChatServerClientConnection toTerminate = clientConnections.get(clientConnectionID);
            System.out.println("Removing client acceptClientConnectionsThread " + clientConnectionID);
            clientConnections.remove(toTerminate);
            try {
                toTerminate.close();
            } catch (IOException ioe) {
                System.out.println("Error closing acceptClientConnectionsThread: " + ioe);
            }
            toTerminate.setActive(false);
        }
    }

    private void addIncomingClientConnection(final Socket socket) {
        System.out.println("Client accepted: " + socket);
        final ChatServerClientConnection client;
        client = new ChatServerClientConnection(this, socket);
        clientConnections.put(client.getID(), client);
        new Thread(client).start();
    }

    public static void main(final String[] args) {
        ChatServerConnectionManager chatServer = null;
        if (args.length != 1)
            System.out.println("Usage: java ChatServerConnectionManager port");
        else
            chatServer = new ChatServerConnectionManager(Integer.parseInt(args[0]));
    }
}
