import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;



public class BooleanSearchEngine implements SearchEngine {

    public static Map<String, ArrayList<PageEntry>> indexingMap = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
//        File folder = new File("/path/to/folder");
        File[] pdfFiles = pdfsDir.listFiles();
        for (File pdfFile : pdfFiles) {
            if (pdfFile.isFile()) {
//                System.out.println(pdfFile.getName());
                var pdfDocument = new PdfDocument(new PdfReader(pdfFile));
//                System.out.println(pdfDocument.getNumberOfPages());
                for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
                    var text = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(page));
                    var words = text.split("\\P{IsAlphabetic}+");

                    Map<String, Integer> freqs = getFreqMapFromArrayOfWords(words);

                    addWordsToIndexingMap(pdfFile, page, freqs);
                }
            }
        }

        // прочтите тут все pdf и сохраните нужные данные,
        // тк во время поиска сервер не должен уже читать файлы
    }

    private static void addWordsToIndexingMap(File pdfFile, int page, Map<String, Integer> freqs) {
        for (String word: freqs.keySet()) {
            PageEntry pageEntry = new PageEntry(pdfFile.getName(), page, freqs.get(word));

            indexingMap.compute(word, (k, v) -> {
                ArrayList<PageEntry> pageEntries = new ArrayList<>();
                if (v==null) {
                    v = pageEntries;
                }
                v.add(pageEntry);
                return v;
            });
        }
    }

    private static Map<String, Integer> getFreqMapFromArrayOfWords(String[] words) {
        Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
        for (var word : words) { // перебираем слова
            if (word.isEmpty()) {
                continue;
            }
            word = word.toLowerCase();
            freqs.put(word, freqs.getOrDefault(word, 0) + 1);
        }
        return freqs;
    }

    @Override
    public ArrayList<PageEntry> search(String word) {

        ArrayList<PageEntry> response = indexingMap.get(word);
        response.sort(PageEntry::compareTo);



        return response;
    }

    public ArrayList<PageEntry> searchByManyWords(String words) {

        try {
            Reader reader = new Reader("stop-ru.txt");
            List<String> stopWords = reader.getWords();


            List<String> separatedWords = Arrays.asList(words.split(" "));

            for (String word: separatedWords) {
                if (stopWords.contains(word)) {
                    separatedWords.remove(word);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ArrayList<PageEntry> response = indexingMap.get(words);
        response.sort(PageEntry::compareTo);

        return response;
    }
}
