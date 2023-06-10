import java.util.Objects;

public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    private int count;

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }

    public String getPdfName() {
        return pdfName;
    }

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int compareTo(PageEntry o) {
        if (o.getCount() - count == 0) {
            if (o.getPage() - page == 0) {
                return o.getPdfName().compareTo(pdfName);
            } else return page - o.getPage();
        } else return o.getCount() - count;
    }

    @Override
    public String toString(){
        return this.pdfName + " " + this.page + " " + this.count;
    }

    @Override
    public boolean equals(Object o) {
        PageEntry pageEntry = (PageEntry) o;
        return  ( pageEntry.getPdfName().equals(this.getPdfName()) && pageEntry.getPage() == this.getPage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pdfName, page);
    }
}
