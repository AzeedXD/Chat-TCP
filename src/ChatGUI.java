import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import com.formdev.flatlaf.FlatDarkLaf;

public class ChatGUI {
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private Client client;
    private String username;

    public ChatGUI() {
        username = JOptionPane.showInputDialog(null, "Insert your username:", "Login", JOptionPane.PLAIN_MESSAGE);

        if (username == null || username.trim().isEmpty()) {
            System.exit(0);
        }

        prepareGUI();

        try {
            Socket socket = new Socket("localhost", 1234);
            client = new Client(socket, username, this);
            client.listenForMessage();
            displayMessage("SYSTEM:Connected as " + username);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Cannont Connect (localhost:1234)");
            System.exit(0);
        }
    }

    private void prepareGUI() {
        frame = new JFrame("Group Chat - " + username);
        messageArea = new JTextArea(20, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        inputField = new JTextField(30);
        JButton sendButton = new JButton("Send");


        inputField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            client.sendMessage(msg);
            displayMessage("You: " + msg);
            inputField.setText("");
        }
    }

    public void displayMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(msg + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            new ChatGUI();
        });
    }
}