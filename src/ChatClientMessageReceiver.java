import java.io.DataInputStream;
import java.io.IOException;

public class ChatClientMessageReceiver implements Runnable {
    private final ChatClientMessageSenderThread messageSender;
    private DataInputStream receiveMessagesFromServerStream;
    private boolean active = false;

    public ChatClientMessageReceiver(final ChatClientMessageSenderThread client) {
        this.messageSender = client;
        try {
            receiveMessagesFromServerStream = new DataInputStream(client.getSocket().getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            client.close();
        }
    }

    public void close() {
        try {
            if (receiveMessagesFromServerStream != null)
                receiveMessagesFromServerStream.close();
        } catch (IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void run() {
        active = true;
        while (isActive()) {
            try {
                messageSender.handleMessage(receiveMessagesFromServerStream.readUTF());
            } catch (IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                messageSender.close();
                setActive(false);
            }
        }
    }
}
