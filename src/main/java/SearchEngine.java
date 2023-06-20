import java.util.List;

public interface SearchEngine {
    List<PageEntry> search(String word);

    List<PageEntry> searchByManyWords(String sentence);
}
