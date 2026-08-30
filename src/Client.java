import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private ChatGUI gui;

    public Client(Socket socket, String username, ChatGUI gui) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.gui = gui;

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }

    public void sendMessage(String message) {
        try {
            if (socket.isConnected() && message != null && !message.trim().isEmpty()) {
                bufferedWriter.write(username + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromServer;
            while (socket.isConnected()) {
                try {
                    msgFromServer = bufferedReader.readLine();
                    if (msgFromServer == null) break;
                    if (gui != null) {
                        gui.displayMessage(msgFromServer);
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedWriter, bufferedReader);
                    break;
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}