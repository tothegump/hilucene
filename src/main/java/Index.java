import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.joda.time.LocalTime;

import java.io.*;

/**
 * Created by tothegump on 17/02/2017.
 */
public class Index {
    public static void main(String[] args) throws IOException {
        System.out.printf("hello");
        LocalTime currentTime = new LocalTime();
        System.out.println("The current local time is: " + currentTime);

        String indexDir = "/Users/tothegump/stupid/learn/intellijtest/tmpzjm/index";
        String dataDir = "/Users/tothegump/stupid/learn/intellijtest/tmpzjm/data";
        long start = System.currentTimeMillis();
        Index indexer = new Index(indexDir);
        int numIndexed;

        try {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
        } finally {
            indexer.close();
        }
        long end = System.currentTimeMillis();
        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }

    private IndexWriter writer;

    public Index(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30),
        true, IndexWriter.MaxFieldLength.UNLIMITED);
    }

    public void close() throws IOException {
        writer.close();
    }

    public int index(String dataDir, FileFilter filter) throws IOException {
        File[] files = new File(dataDir).listFiles();
        for (File f: files) {
            if (!f.isDirectory() &&
                    !f.isHidden() &&
                    f.exists() &&
                    f.canRead() &&
                    (filter == null || filter.accept(f))) {
                indexFile(f);
            }
        }
        return writer.numDocs();
    }

    private void indexFile(File f) throws IOException {
        System.out.println("Indexing " + f.getCanonicalFile());
        Document doc = getDocument(f);
        writer.addDocument(doc);
    }

    protected Document getDocument(File f) throws IOException {
        Document doc = new Document();
        doc.add(new Field("contents", new FileReader(f)));
        doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }

    private static class TextFilesFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(".txt");
        }
    }
}
