import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {
    public static CopyOnWriteArrayList<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();

            clientHandlers.add(this);
            broadcastMessage("SYSTEM: " + clientUsername + " entered in chat");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) break;
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        String timeStamp = LocalDateTime.now().format(formatter);
        String messageWithTime = "[" + timeStamp + "] " + messageToSend;

        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageWithTime);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SYSTEM: " + clientUsername + " left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}