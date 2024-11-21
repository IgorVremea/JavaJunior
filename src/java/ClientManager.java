import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable{
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String name;
    private static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket){
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            broadcastMessage("Server: " + name + " has connected to chat");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
            try {
                if(socket != null) socket.close();
                if(bufferedReader != null) bufferedReader.close();
                if(bufferedWriter != null) bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    private void broadcastMessage(String messageToSend){
        for(ClientManager client : clients){
            try{
                if(!client.name.equals(name)) {
                    client.bufferedWriter.write(messageToSend);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    public String getName(){
        return name;
    }
    public void removeClient(){
        clients.remove(this);
        broadcastMessage("Server: " + name + " left the chat");
    }
}
