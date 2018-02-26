/*
 * SEAN MARTIN
 * This code is based on the Apache Lucene demo Indexing file
 * Found at https://lucene.apache.org/core/7_2_1/demo/src-html/org/apache/lucene/demo/IndexFiles.html
 */
package indexing;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.lang.StringBuilder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import common.Util;

/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */

public class IndexFiles {

  private IndexFiles() {}

  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String usage = "java org.apache.lucene.demo.IndexFiles"
                 + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                 + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                 + "in INDEX_PATH that can be searched with SearchFiles";
    String indexPath = "index";
    String docsPath = null;
    boolean create = true;
    for(int i=0;i<args.length;i++) {
      if ("-index".equals(args[i])) {
        indexPath = args[i+1];
        i++;
      } else if ("-docs".equals(args[i])) {
        docsPath = args[i+1];
        i++;
      } else if ("-update".equals(args[i])) {
        create = false;
      }
    }

    if (docsPath == null) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }

    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir)) {
      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }

    Date start = new Date();
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      if (create) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      // Optional: for better indexing performance, if you
      // are indexing many documents, increase the RAM
      // buffer.  But if you do this, increase the max heap
      // size to the JVM (eg add -Xmx512m or -Xmx1g):
      //
      // iwc.setRAMBufferSizeMB(256.0);

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexCranfield(writer, docDir);

      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here.  This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      // writer.forceMerge(1);

      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  /** Reads through the cranfield collection and indexes it */
  private static void indexCranfield(IndexWriter writer, Path file) {
    String line;
    StringBuilder stringBuilder = new StringBuilder();
    int doc_index = 1;
    Document doc = new Document();
    try {
       // Create a buffered Reader
       BufferedReader bufferedReader =
          Files.newBufferedReader(file, StandardCharsets.UTF_8);
       //Read until end of file
       while((line = bufferedReader.readLine()) != null) {
         switch (Util.firstTwo(line)) {
           case ".I" :
            if (doc_index != 1) {
              doc.add(new TextField("Words", stringBuilder.toString(), Field.Store.YES));
              stringBuilder.setLength(0);
              indexDocument(writer, doc);
            }
            doc = new Document();
            doc.add(new IntPoint("Instance_ID", doc_index));
            doc.add(new StoredField("Instance_ID", doc_index));
            ++doc_index;
            break;

          case ".T" :
            stringBuilder.setLength(0);
            break;

          case ".A" :
            doc.add(new TextField("Title", stringBuilder.toString(), Field.Store.YES));
            stringBuilder.setLength(0);
            break;

          case ".B" :
            doc.add(new TextField("Author", stringBuilder.toString(), Field.Store.YES));
            stringBuilder.setLength(0);
            break;

          case ".W" :
            doc.add(new StringField("Bibliographic", stringBuilder.toString(), Field.Store.YES));
            stringBuilder.setLength(0);
            break;

          default:
            stringBuilder.append(line).append(" ");
            break;
         }
       }

       //index the last file
       doc.add(new TextField("Words", stringBuilder.toString(), Field.Store.YES));
       indexDocument(writer, doc);

       //close file.
       bufferedReader.close();
    }
    catch(IOException ex) {
       ex.printStackTrace();
    }
  }

  /** Indexes a single document using writer*/
  private static void indexDocument(IndexWriter writer, Document doc) throws IOException  {
    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
      // New index, so we just add the document (no old document can be there):
      System.out.println("adding " + doc.get("Title"));
      writer.addDocument(doc);
    } else {
      // Existing index (an old copy of this document may have been indexed) so
      // we use updateDocument instead to replace the old one matching the exact
      // path, if present:
      System.out.println("updating " + doc.get("Title"));
      writer.updateDocument(new Term("Instance_ID", doc.get("Instance_ID")), doc);
    }
  }
}
