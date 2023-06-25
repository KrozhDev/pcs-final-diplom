import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class BooleanSearchEngine implements SearchEngine {

    private final Map<String, List<PageEntry>> indexingMap = new HashMap<>();
    private final List<String> stopWords;

    public BooleanSearchEngine(File pdfsDir) {
        try {
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

            ReaderImpl readerImpl = new ReaderImpl("stop-ru.txt");
            this.stopWords = readerImpl.getWords();

        } catch (IOException e) {
            System.err.println("In catch IOException: " + e.getClass());
            throw new RuntimeException();
        }
    }

    private void addWordsToIndexingMap(File pdfFile, int page, Map<String, Integer> freqs) {
        for (String word : freqs.keySet()) {
            PageEntry pageEntry = new PageEntry(pdfFile.getName(), page, freqs.get(word));

            indexingMap.compute(word, (k, v) -> {
                List<PageEntry> pageEntries = new ArrayList<>();
                if (v == null) {
                    v = pageEntries;
                }
                v.add(pageEntry);
                return v;
            });
        }
    }

    private Map<String, Integer> getFreqMapFromArrayOfWords(String[] words) {
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
    }
}
