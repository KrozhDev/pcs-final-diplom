import java.io.*;
import java.net.Socket;


public class Main {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    public static void main(String[] args) {

        try (Socket clientSocket = new Socket(HOST,PORT);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));)
        {
            String response = in.readLine();
            System.out.println(response);
            out.println("игра");
            response = in.readLine();
            System.out.println(response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}