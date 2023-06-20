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
        File[] pdfFiles = pdfsDir.listFiles();
        for (File pdfFile : pdfFiles) {
            if (pdfFile.isFile()) {
                var pdfDocument = new PdfDocument(new PdfReader(pdfFile));
                for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
                    var text = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(page));
                    var words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = getFreqMapFromArrayOfWords(words);
                    addWordsToIndexingMap(pdfFile, page, freqs);
                }
            }
        }
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
        Map<String, Integer> freqs = new HashMap<>();
        for (var word : words) {
            if (word.isEmpty()) {
                continue;
            }
            word = word.toLowerCase();
            freqs.put(word, freqs.getOrDefault(word, 0) + 1);
        }
        return freqs;
    }

    @Override
    public List<PageEntry> search(String sentence) {

        try {
            ReaderImpl readerImpl = new ReaderImpl("stop-ru.txt");
            List<String> stopWords = readerImpl.getWords();
            List<String> words = new ArrayList<>(Arrays.asList(sentence.split(" ")));
            List<String> wordsToDelete = new ArrayList<>();
            for (String word : words) {
                if (stopWords.contains(word)) {
                    wordsToDelete.add(word);
                }
            }

            Iterator<String> wordIterator = words.iterator();
            while (wordIterator.hasNext()) {
                String nextWord = wordIterator.next();
                if (stopWords.contains(nextWord)) {
                    wordIterator.remove();
                }
            }

            Map<PageEntry, Integer> pageEntries = new HashMap<>();


            for (String word : words) {
                List<PageEntry> nextWordPageEntries = indexingMap.get(word);
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
    }
}
