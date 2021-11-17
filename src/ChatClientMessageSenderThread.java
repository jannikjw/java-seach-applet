public interface ChatClientMessageSenderThread {
    void handleMessage(final String msg);
    void close();
    java.net.Socket getSocket();
}
