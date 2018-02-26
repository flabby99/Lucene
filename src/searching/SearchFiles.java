/*
 * SEAN MARTIN
 * This code is based on the Apache Lucene demo Indexing file
 * Found at https://lucene.apache.org/core/7_2_1/demo/src-html/org/apache/lucene/demo/SearchFiles.html
 */
package searching;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

//For similarities
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

/* My files */
import common.Util;
import common.analyzers.TestAnalyzer;

/** Simple command-line based search demo. */
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage =
      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-result dir] [-index dir] [-field f] [-repeat n] " +
          "[-queries file] [-query string] [-raw] [-paging hitsPerPage] [-bm25]" +
          "bm25 flag is used to set the scorer, if it is off, use tf-idf" +
          "\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

    String index = "index";
    String field = "contents";
    String outlocation = "results/results.txt";
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    boolean use_bm25 = false;
    String queryString = null;
    int hitsPerPage = 10;
    int numresults = 20;
    String run_id = "STANDARD";

    for(int i = 0;i < args.length;i++) {
      if ("-index".equals(args[i])) {
        index = args[i+1];
        i++;
      } else if ("-field".equals(args[i])) {
        field = args[i + 1];
        i++;
      } else if ("-result".equals(args[i])) {
        outlocation = args[i + 1];
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = args[i+1];
        i++;
      } else if ("-query".equals(args[i])) {
        queryString = args[i+1];
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-raw".equals(args[i])) {
        raw = true;
      }
        else if ("-bm25".equals(args[i])) {
        use_bm25 = true;
      } else if ("-paging".equals(args[i])) {
        hitsPerPage = Integer.parseInt(args[i+1]);
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least 1 hit per page.");
          System.exit(1);
        }
        i++;
      }
    }

    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);

    if(use_bm25) searcher.setSimilarity(new BM25Similarity());
    else searcher.setSimilarity(new ClassicSimilarity());
    Analyzer analyzer = new EnglishAnalyzer();
    Analyzer custom = TestAnalyzer.BuildAnalyzer("res");

    BufferedReader in;
    BufferedWriter out = Files.newBufferedWriter(Paths.get(outlocation));
    String line;
    int query_count = 1;
    if (queries != null) {
      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
      //Throw away the first I
      in.readLine();
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
    MultiFieldQueryParser parser = new MultiFieldQueryParser(
            new String[] {"Words", "Title"}, analyzer);
    parser.setDefaultOperator(QueryParser.Operator.OR);
    while (true) {
      if (queries == null && queryString == null) {                        // prompt the user
        System.out.println("Enter query: ");
      }
      if(queries == null) {
        line = getLine(queryString, in);
      }
      else {
        line = getCranQuery(queryString, in);
      }

      if (line == null || line.length() == 0) {
        break;
      }

      line = QueryParser.escape(line);
      Query query = parser.parse(line);
      System.out.println("Searching for: " + query.toString());

      if (repeat > 0) {                           // repeat & time as benchmark
        Date start = new Date();
        for (int i = 0; i < repeat; i++) {
          searcher.search(query, 100);
        }
        Date end = new Date();
        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
      }
      if(queries == null) {
        doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
      }

      else {
        doCranSearch(out, searcher, query, numresults, query_count, run_id);
      }

      if (queryString != null) {
        break;
      }
      ++query_count;
    }
    reader.close();
    out.close();
    in.close();
  }

  private static String getCranQuery(String queryString, BufferedReader in) throws IOException{
    StringBuilder stringBuilder = new StringBuilder();
    String line = in.readLine();
    if(line == null) return null;
    while(true) {
      line = in.readLine();
      if (line == null || Util.firstTwo(line).equals(".I")) {
        break;
      }
      stringBuilder.append(line + " ");
    }
    return stringBuilder.toString().trim();
  }

  private static String getLine(String queryString, BufferedReader in) throws IOException {
    String line = queryString != null ? queryString : in.readLine();
    line = line.trim();
    return line;
  }

  /**
   * This takes in a query - searches it in the index and adds a line to a file
   * The line added is in the format:
   * query_id iter doc_id rank sim run_id
   * *** Q0 *** *** *** NAME
   * iter and rank are required but they are not used
   *
   */
  private static String doCranSearch(BufferedWriter writer, IndexSearcher searcher, Query query,
                                     int numresults, int query_id, String run_id) throws IOException {
    StringBuilder stringBuilder;
    TopDocs results = searcher.search(query, numresults);
    ScoreDoc[] hits = results.scoreDocs;
    int rank = 0;
    for(ScoreDoc hit : hits) {
      ++rank;
      stringBuilder = new StringBuilder(query_id + " Q0 ");
      stringBuilder.append(hit.doc).append(" ").append(rank).append(" ");
      stringBuilder.append(hit.score).append(" ").append(run_id);
      System.out.println(stringBuilder.toString());
      writer.write(stringBuilder.toString());
      writer.newLine();
    }
    return null;
  }


  /**
   * This demonstrates a typical paging search scenario, where the search engine presents
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   *
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   *
   */
  private static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query,
                                     int hitsPerPage, boolean raw, boolean interactive) throws IOException {

    // Collect enough docs to show 5 pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;

    int numTotalHits = Math.toIntExact(results.totalHits);
    System.out.println(numTotalHits + " total matching documents");

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);

    while (true) {
      if (end > hits.length) {
        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
        System.out.println("Collect more (y/n) ?");
        String line = in.readLine();
        if (line.length() == 0 || line.charAt(0) == 'n') {
          break;
        }

        hits = searcher.search(query, numTotalHits).scoreDocs;
      }

      end = Math.min(hits.length, start + hitsPerPage);

      for (int i = start; i < end; i++) {
        if (raw) {                              // output raw format
          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
          continue;
        }

        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("Instance_ID");
        if (path != null) {
          System.out.println((i+1) + ". " + path);
          String title = doc.get("Title");
          if (title != null) {
            System.out.println("   Title: " + doc.get("Title"));
          }
        } else {
          System.out.println((i+1) + ". " + "No path for this document");
        }

      }

      if (!interactive || end == 0) {
        break;
      }

      if (numTotalHits >= end) {
        boolean quit = false;
        while (true) {
          System.out.print("Press ");
          if (start - hitsPerPage >= 0) {
            System.out.print("(p)revious page, ");
          }
          if (start + hitsPerPage < numTotalHits) {
            System.out.print("(n)ext page, ");
          }
          System.out.println("(q)uit or enter number to jump to a page.");

          String line = in.readLine();
          if (line.length() == 0 || line.charAt(0)=='q') {
            quit = true;
            break;
          }
          if (line.charAt(0) == 'p') {
            start = Math.max(0, start - hitsPerPage);
            break;
          } else if (line.charAt(0) == 'n') {
            if (start + hitsPerPage < numTotalHits) {
              start+=hitsPerPage;
            }
            break;
          } else {
            int page = Integer.parseInt(line);
            if ((page - 1) * hitsPerPage < numTotalHits) {
              start = (page - 1) * hitsPerPage;
              break;
            } else {
              System.out.println("No such page");
            }
          }
        }
        if (quit) break;
        end = Math.min(numTotalHits, start + hitsPerPage);
      }
    }
  }
}
