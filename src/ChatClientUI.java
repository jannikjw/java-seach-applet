import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClientUI extends Frame implements ChatClientMessageSenderThread {
    private Socket socket = null;
    private DataOutputStream streamOut = null;
    private ChatClientMessageReceiver messageReceiver = null;
    private TextArea displayTextArea = new TextArea();
    private TextField inputTextField = new TextField();
    private Button
            sendButton = new Button("Send"),
            connectButton = new Button("Connect"),
            quitButton = new Button("Bye");
    private String serverName = "localhost";
    private int serverPort = 1024;

    public ChatClientUI() {
        Panel keys = new Panel();
        keys.setLayout(new GridLayout(1, 2));
        keys.add(quitButton);
        keys.add(connectButton);
        Panel south = new Panel();
        south.setLayout(new BorderLayout());
        south.add("West", keys);
        south.add("Center", inputTextField);
        south.add("East", sendButton);
        Label title = new Label("Simple Chat Client Applet", Label.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 14));
        setLayout(new BorderLayout());
        add("North", title);
        add("Center", displayTextArea);
        add("South", south);
        quitButton.setEnabled(false);
        sendButton.setEnabled(false);
        pack();
        setVisible(true);

        quitButton.addActionListener(actionEvent -> {
            inputTextField.setText(".bye");
            send();
            quitButton.setEnabled(false);
            sendButton.setEnabled(false);
            connectButton.setEnabled(true);
        });

        connectButton.addActionListener(actionEvent -> {
            connect(serverName, serverPort);
        });

        sendButton.addActionListener(actionEvent -> {
            send();
            inputTextField.requestFocus();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public Socket getSocket() {
        return socket;
    }

    public void connect(String serverName, int serverPort) {
        println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            println("Connected: " + socket);
            open();
            sendButton.setEnabled(true);
            connectButton.setEnabled(false);
            quitButton.setEnabled(true);
        } catch (UnknownHostException uhe) {
            println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            println("Unexpected exception: " + ioe.getMessage());
        }
    }

    private void send() {
        try {
            streamOut.writeUTF(inputTextField.getText());
            streamOut.flush();
            inputTextField.setText("");
        } catch (IOException ioe) {
            println("Sending error: " + ioe.getMessage());
            close();
        }
    }

    public void handleMessage(final String msg) {
        if (msg.equals(".bye")) {
            println("Connection closed!");
            close();
        } else
            println(msg);
    }

    public void open() {
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            messageReceiver = new ChatClientMessageReceiver(this);
            new Thread(messageReceiver).start();
        } catch (IOException ioe) {
            println("Error opening output stream: " + ioe);
        }
    }

    public void close() {
        try {
            if (streamOut != null)
                streamOut.close();
            if (socket != null)
                socket.close();
        } catch (IOException ioe) {
            println("Error closing ...");
        }
        messageReceiver.close();
        messageReceiver.setActive(false);
    }

    private void println(final String msg) {
        displayTextArea.append(msg + "\n");
    }

    private static void createWindow() {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Test");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(true);
            JTextArea textArea = new JTextArea(15, 50);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setFont(Font.getFont(Font.SANS_SERIF));
            JScrollPane scroller = new JScrollPane(textArea);
            scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            JPanel inputpanel = new JPanel();
            inputpanel.setLayout(new FlowLayout());
            JTextField input = new JTextField(20);
            JButton button = new JButton("Enter");
            DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            panel.add(scroller);
            inputpanel.add(input);
            inputpanel.add(button);
            panel.add(inputpanel);
            frame.setContentPane(panel);
            frame.pack();
            frame.setVisible(true);
        });
    }

    public static void main(final String[] args) {
        final ChatClientUI chatGUI = new ChatClientUI();
    }

}