import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReaderImpl implements Reader {

    private BufferedReader reader;
    private List<String> words = new ArrayList<>();

    public ReaderImpl(String fileName) throws IOException {
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) {
                words.add(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getWords() {
        return words;
    }

}
