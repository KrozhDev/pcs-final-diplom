import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


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
        for (String word : freqs.keySet()) {
            PageEntry pageEntry = new PageEntry(pdfFile.getName(), page, freqs.get(word));

            indexingMap.compute(word, (k, v) -> {
                ArrayList<PageEntry> pageEntries = new ArrayList<>();
                if (v == null) {
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
    public List<PageEntry> search(String word) {

        ArrayList<PageEntry> response = indexingMap.get(word);
        response.sort(PageEntry::compareTo);

        return response;
    }

    @Override
    public List<PageEntry> searchByManyWords(String sentence) {

        try {
            Reader reader = new Reader("stop-ru.txt");
            List<String> stopWords = reader.getWords();

            List<String> words = new ArrayList<>(Arrays.asList(sentence.split(" ")));
//            for (String s : words.split(" ")) {
//                separatedWords.add(s);
//            }


            List<String> wordsToDelete = new ArrayList<>();
            for (String word : words) {
                if (stopWords.contains(word)) {
                    wordsToDelete.add(word);
                }
            }

            Iterator<String> wordIterator = words.iterator();//создаем итератор
            while (wordIterator.hasNext()) {//до тех пор, пока в списке есть элементы

                String nextWord = wordIterator.next();//получаем следующий элемент
                if (stopWords.contains(nextWord)) {
                    wordIterator.remove();
                }
            }

            Map<PageEntry, Integer> pageEntries = new HashMap<>();

            //ключ пейдж ентри файл страница
            //значение это мой каунт

            for (String word : words) {
                List<PageEntry> nextWordPageEntries = search(word);
                for (PageEntry pageEntry : nextWordPageEntries) {
                    pageEntries.compute(pageEntry, (k, v) -> {
                        if (v == null) {
                            v = k.getCount();
                        } else {
                            v += k.getCount();
                        }
                        return v;
                    });
                }
            }

            return pageEntries.entrySet().stream()
                    .map(e -> new PageEntry(e.getKey().getPdfName(), e.getKey().getPage(), e.getValue()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        ArrayList<PageEntry> response = indexingMap.get(words);
//        response.sort(PageEntry::compareTo);
//
//        return response;
    }
}
