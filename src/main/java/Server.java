import org.json.JSONObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    private static final int PORT = 8989;

    public static void main(String[] args) throws IOException {

        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));

        System.out.println("Сервер запущен");

        try (ServerSocket serverSocket = new ServerSocket(PORT);) { // стартуем сервер один(!) раз
            while (true) { // в цикле(!) принимаем подключения

                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                ) {

                    out.println("Сервер запущен");
                    String word = in.readLine();

                    ArrayList<PageEntry> response = engine.search(word);

                    List<String> responseAsJson = getResponseAsJsonFormat(response);

                    out.println(responseAsJson);
                }
            }
        } catch (
                IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }

    }

    private static List<String> getResponseAsJsonFormat(ArrayList<PageEntry> response) {
        List<String> responseAsJson = new ArrayList<>();

        for (PageEntry pageEntry: response) {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("pdfName", pageEntry.getPdfName());
            jsonResponse.put("page", pageEntry.getPage());
            jsonResponse.put("count", pageEntry.getCount());
            responseAsJson.add(jsonResponse.toString());
        }
        return responseAsJson;
    }
}
