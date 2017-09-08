package org.utdallas.edu.nlp;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

// mention nlp libraries in the project
// use names together for the sentences

public class Homework2 {

    private PTBTokenizer<CoreLabel> tokenizer;

    private String corpus;
    private String sentence1;
    private String sentence2;
    private Map<String, Integer> ngramTable;


    public Homework2(String corpusPath, String sentence1, String sentence2) {
        corpus = loadCorpus(corpusPath);
        this.sentence1 = sentence1;
        this.sentence2 = sentence2;
        tokenizer = new PTBTokenizer<>(
                new StringReader(corpus),
                new CoreLabelTokenFactory(),
                ""
        );
        ngramTable = new HashMap<>();
    }

    private String loadCorpus(String corpusPath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(corpusPath);
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(inputStream);
        }
        return null;
    }

    public void run() {
        List<CoreLabel> tokens = tokenizer.tokenize();
        tokens = tokens.stream().filter(coreLabel -> StringUtils.isAlphanumeric(coreLabel.toString())).collect(Collectors.toList());
        System.out.println(tokens);
        List<List<CoreLabel>> ngrams = CollectionUtils.getNGrams(tokens, 2, 2);
        System.out.println(ngrams);

        ngrams.stream().map(coreLabels -> coreLabels.get(0) + " " + coreLabels.get(1))
    }

    private <T> boolean isWordform(T coreLabel){
    }

    public static void main(String[] args) {
        Homework2 homework2 = new Homework2(args[0], args[1], args[2]);
        homework2.run();
    }

    private static void closeResource(Closeable closeableResource) {
        if (closeableResource != null)
            try {
                closeableResource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private <T> List<List<Integer>> bigramCountsTable(List<T> sentence, List<List<T>> ngrams) {

    }

    private <T> double sentenceLikelihood(List<T> sentence, List<T> ngrams) {
        return s;
    }

}