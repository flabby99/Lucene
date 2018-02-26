package common.analyzers;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilterFactory;
import org.apache.lucene.analysis.en.EnglishPossessiveFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;

import java.io.IOException;
import java.nio.file.Paths;


//Build a custom analyzer using a Tokenizer factory
//Multiple TokenFilterFactories and CharFilterFactories
//See https://lucene.apache.org/core/7_2_1/core/org/apache/lucene/analysis/package-summary.html
//For an explanation of the differences between these three
public class TestAnalyzer {
  static public Analyzer BuildAnalyzer(String dir) throws IOException {
    Analyzer ana = CustomAnalyzer.builder(Paths.get(dir))
        .withTokenizer(StandardTokenizerFactory.class)
        .addTokenFilter(StandardFilterFactory.class)
        .addTokenFilter(LowerCaseFilterFactory.class)
        .addTokenFilter(EnglishMinimalStemFilterFactory.class)
        .addTokenFilter(EnglishPossessiveFilterFactory.class)
        //To change the stopwords, pass in "words", "stopwords.txt",
        //Where stopwords.txt is the list to use
        //Also need to use "format", "wordset"
        //When not specified the English stop words set is used
        .addTokenFilter(StopFilterFactory.class,
            "ignoreCase", "true", "words", "stopwords.txt", "format", "snowball")
        .build();
    return ana;
  }
}
