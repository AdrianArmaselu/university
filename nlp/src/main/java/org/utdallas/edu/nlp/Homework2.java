package org.utdallas.edu.nlp;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import dnl.utils.text.table.TextTable;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
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
        // Parse individual words
        List<CoreLabel> tokens = tokenizer.tokenize();

        // Remove non-words and characters
        tokens = tokens.stream().filter(coreLabel -> StringUtils.isAlphanumeric(coreLabel.toString())).collect(Collectors.toList());

        // construct bigrams
        List<List<CoreLabel>> ngrams = CollectionUtils.getNGrams(tokens, 2, 2);

        // create a count of bigram items
        Map<String, Integer> bigramCountTable = new HashMap<>();
        ngrams.forEach(coreLabels -> {
            String key = coreLabels.get(0) + " " + coreLabels.get(1);
            Integer value = bigramCountTable.get(key);
            bigramCountTable.put(key, value != null ? value + 1 : 1);

        });

        // COUNTS
        System.out.println("Bigram Count Table For Sentence 1(No Smoothing)");
        Map<String, Map<String, Integer>> table = bigramCountsTable(sentence1, bigramCountTable, false);
        printBigramCountsTable(table);
        System.out.println();

        System.out.println("Bigram Count Table For Sentence 2(No Smoothing)");
        table = bigramCountsTable(sentence2, bigramCountTable, false);
        printBigramCountsTable(table);
        System.out.println();

        System.out.println("Bigram Count Table For Sentence 1(With add1 Smoothing)");
        table = bigramCountsTable(sentence1, bigramCountTable, true);
        printBigramCountsTable(table);
        System.out.println();

        System.out.println("Bigram Count Table For Sentence 2(With add1 Smoothing)");
        table = bigramCountsTable(sentence2, bigramCountTable, true);
        printBigramCountsTable(table);
        System.out.println();


        // PROBABILITIES
        System.out.println("Bigram Probability Table For Sentence 1(No Smoothing)");
        Map<String, Map<String, Double>>  table2 = bigramProbabilitiesTable(sentence1, bigramCountTable, false);
        printBigramProbabilitesTable(table2);
        System.out.println();

        System.out.println("Bigram Probability Table For Sentence 2(No Smoothing)");
        Map<String, Map<String, Double>> table3 = bigramProbabilitiesTable(sentence2, bigramCountTable, false);
        printBigramProbabilitesTable(table3);
        System.out.println();

        System.out.println("Bigram Probability Table For Sentence 1(With add1 Smoothing)");
        Map<String, Map<String, Double>> table4 = bigramProbabilitiesTable(sentence1, bigramCountTable, true);
        printBigramProbabilitesTable(table4);
        System.out.println();

        System.out.println("Bigram Probability Table For Sentence 2(With add1 Smoothing)");
        Map<String, Map<String, Double>> table5 = bigramProbabilitiesTable(sentence2, bigramCountTable, true);
        printBigramProbabilitesTable(table5);
        System.out.println();

        // Total Probabilities
        System.out.println("Total Probability For Sentence 1(No Smoothing)");
        System.out.println(totalProbability(table2, sentence1, tokens));
        System.out.println();

        System.out.println("Total Probability For Sentence 2(No Smoothing)");
        System.out.println(totalProbability(table3, sentence2, tokens));
        System.out.println();

        System.out.println("Total Probability For Sentence 1(With add1 Smoothing)");
        System.out.println(totalProbability(table4, sentence1, tokens));
        System.out.println();

        System.out.println("Total Probability For Sentence 2(With add1 Smoothing)");
        System.out.println(totalProbability(table5, sentence2, tokens));
        System.out.println();

    }

    private <T> boolean isWordform(T coreLabel) {
        return false;
    }

    public static void main(String[] args) {
        Arguments arguments = new Arguments();
        JCommander jCommander = new JCommander(arguments);
        jCommander.parse(args);

        Homework2 homework2 = new Homework2(arguments.corpusFilePath, arguments.sentence1, arguments.sentence2);
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

    private double totalProbability(Map<String, Map<String, Double>> probabilities, String sentence, List<CoreLabel> tokens){
        double totalProbability = 1;
        List<List<CoreLabel>> ngrams =ngram(sentence);
        totalProbability *= (double)tokens.stream().filter(coreLabel -> coreLabel.toString().contains(ngrams.get(0).get(1).toString())).count() / tokens.size();
        for(List<CoreLabel> component : ngrams){
            totalProbability *= probabilities.get(component.get(0).toString()).get(component.get(1).toString());
        }
        return totalProbability;
    }


    private Map<String, Map<String, Integer>> bigramCountsTable(String sentence, Map<String, Integer> bigramCountTable, boolean smoothing) {
        Map<String, Map<String, Integer>> table = new LinkedHashMap<>();
        List<CoreLabel> sentenceWords = tokenize(sentence);
        for (CoreLabel word1 : sentenceWords) {
            table.put(word1.toString(), new LinkedHashMap<>());
            for (CoreLabel word2 : sentenceWords) {
                String key = word1 + " " + word2;
                Integer value = bigramCountTable.compute(key, (s, integer) -> integer == null ? 0 : integer);
                if( smoothing) {
                    if(value == 0)
                        value = 1;
                    else value += 1;
                }

                table.get(word1.toString()).put(word2.toString(), value);
            }
        }
        return table;
    }

    public int sum (Map<String, Integer> bigramCountTable, String key){
        return (int) bigramCountTable.entrySet().stream().filter(stringIntegerEntry -> stringIntegerEntry.getKey().startsWith(key)).count();
    }

//     take the count of bigram xy and divide it by all bigrams that start with x;
    private Map<String, Map<String, Double>> bigramProbabilitiesTable(String sentence, Map<String, Integer> bigramCountTable, boolean smoothing) {
        Map<String, Map<String, Double>> table = new LinkedHashMap<>();
        List<CoreLabel> sentenceWords = tokenize(sentence);
        for (CoreLabel word1 : sentenceWords) {
            table.put(word1.toString(), new LinkedHashMap<>());
            for (CoreLabel word2 : sentenceWords) {
                String key = word1 + " " + word2;
                Integer value = bigramCountTable.compute(key, (s, integer) -> integer == null ? 0 : integer);
                if( smoothing) {
                    if(value == 0)
                        value = 1;
                    else value += 1;
                }
                table.get(word1.toString()).put(word2.toString(), Double.valueOf(new DecimalFormat("#.#####").format((double)value / (sum(bigramCountTable, word1.toString())))));
            }
        }
        return table;
    }

    private void printBigramCountsTable(Map<String, Map<String, Integer>> table) {
        LinkedHashSet<String> columnNames = new LinkedHashSet<>();
        columnNames.add("Table");
        columnNames.addAll(table.keySet());
        Object[] keys = table.keySet().toArray();
        Object[][] data = new Object[table.values().size()][];
        for(int i  = 0 ; i < table.keySet().size() ; i++){
            Object[] source = table.get(keys[i]).values().toArray();
            data[i] = new Object[table.get(keys[i]).values().size() + 1];
            data[i][0] = keys[i];
            System.arraycopy(source, 0, data[i], 1, table.get(keys[i]).values().size());
        }
        TextTable tt = new TextTable(toStringArray(columnNames), data);
        tt.printTable();
    }

    private void printBigramProbabilitesTable(Map<String, Map<String, Double>> table) {
        LinkedHashSet<String> columnNames = new LinkedHashSet<>();
        columnNames.add("Table");
        columnNames.addAll(table.keySet());
        Object[] keys = table.keySet().toArray();
        Object[][] data = new Object[table.values().size()][];
        for(int i  = 0 ; i < table.keySet().size() ; i++){
            Object[] source = table.get(keys[i]).values().toArray();
            data[i] = new Object[table.get(keys[i]).values().size() + 1];
            data[i][0] = keys[i];
            System.arraycopy(source, 0, data[i], 1, table.get(keys[i]).values().size());
        }
        TextTable tt = new TextTable(toStringArray(columnNames), data);
        tt.printTable();
    }

    String[] toStringArray(Collection<String> collection) {
        Object[] objects = collection.toArray();
        String[] strings = new String[collection.size()];
        for (int i = 0; i < objects.length; i++)
            strings[i] = objects[i].toString();
        return strings;
    }

    public static List<List<CoreLabel>> ngram(String content) {
        return CollectionUtils.getNGrams(tokenize(content), 2, 2);
    }

    public static List<CoreLabel> tokenize(String content) {
        return new PTBTokenizer<>(
                new StringReader(content),
                new CoreLabelTokenFactory(),
                ""
        ).tokenize();
    }

    private <T> double sentenceLikelihood(List<T> sentence, List<T> ngrams) {
        return 0.0;
    }

    public static class Arguments{
        @Parameter(names = "-f", description = "File Path of Corpus File", required = true)
        String corpusFilePath;
        @Parameter(names = "-s1", description = "Sentence1", required = true)
        String sentence1;
        @Parameter(names = "-s2", description = "Sentence2", required = true)
        String sentence2;
    }

}