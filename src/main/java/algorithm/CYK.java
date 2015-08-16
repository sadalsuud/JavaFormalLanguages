package algorithm;

import exception.CYKException;
import grammar.Grammar;
import grammar.Rule;
import grammar.Rules;
import normalform.Chomsky;

import java.util.Map;

/**
 * This class performs the CYK (Cocke–Younger–Kasami)
 * algorithm to check if a word can be generated
 * by a Grammar
 *
 * TODO : get the parse tree
 */
public class CYK {

    private CYK() {}

    /**
     * Check if the given word can be generated
     * with the grammar
     *
     * Condition : the grammar must be in CNF (Chomsky Normal Form)
     *
     * @param g
     *          the grammar
     * @param word
     *          the word to check
     * @return
     *          true if the word can be generated, false otherwise
     * @throws CYKException
     *          thrown if the grammar is not in CNF
     */
    public static boolean isMember(Grammar g, String word) throws CYKException {
        if (!Chomsky.isNormalized(g)) {
            throw new CYKException("Grammar is not in CNF");
        }

        if (word.length() == 0) {
            return g.getAxiomsWithEpsilon().contains(g.getStartAxiom());
        }

        int wordLength = word.length();
        String[][] vector = new String[wordLength][wordLength];
        initVector(vector, wordLength);

        try {
            buildFirstLine(g, word, vector);
            buildVector(g, wordLength, vector);
            return vector[wordLength - 1][0].contains(Character.toString(g.getStartAxiom()));
        } catch (CYKException e) {
            return false;
        }
    }

    /**
     * Build the first line of the vector by
     * finding axioms which produce each characters
     *
     * @param g
     *          the grammar
     * @param word
     *          the word to check
     * @param vector
     *          the vector of the CYK algorithm
     * @throws CYKException
     *          if a non producible character is found
     */
    private static void buildFirstLine(Grammar g, String word, String[][] vector) throws CYKException {
        int wordLength = word.length();
        for (int i = 0; i < wordLength; ++i) {
            for (Map.Entry<Character, Rules> axiom : g.getAxioms().entrySet()) {
                for (Rule rule : axiom.getValue()) {
                    if (rule.size() == 1 && rule.get(0) == word.charAt(i) && !vector[0][i].contains(axiom.getKey().toString())) {
                        vector[0][i] += axiom.getKey().toString();
                    }
                }
            }

            if (vector[0][i].length() == 0) {
                throw new CYKException("The character " + word.charAt(i) + " is not in the grammar");
            }
        }
    }

    /**
     * Build the vector by following the CYK algorithm
     * The indices for characters "B" and "C"
     * are calculated following the following table
     *
     * 2nd line	    3rd line 	    4th line	    5th line      ...
     *
     * 00 - 01      00 - 11		    00 - 21		    00 - 31
     * 10 - 02	    10 - 12		    10 - 22
     * 20 - 03	    20 - 13
     * 30 - 04
     *
     * 01 - 02      01 - 12		    01 - 22
     * 11 - 03	    11 - 13
     * 21 - 04
     *
     * 02 - 03	    02 - 13
     * 12 - 04
     *
     * 03 - 04
     *
     * @param g
     *          the grammar
     * @param wordLength
     *          the length of the word
     * @param vector
     *          the vector of the CYK algorithm
     */
    private static void buildVector(Grammar g, int wordLength, String[][] vector) {
        for (int i = 1; i < wordLength; ++i) {
            for (int j = 0; j < wordLength - i; ++j) {
                for (int k = 0; k < i; ++k) {

                    for (Character B : vector[k][j].toCharArray()) {
                        for (Character C : vector[i - k - 1][j + k +1].toCharArray()) {

                            for (Map.Entry<Character, Rules> axiom : g.getAxioms().entrySet()) {
                                for (Rule rule : axiom.getValue()) {
                                    if (rule.size() == 2 && rule.get(0) == B && rule.get(1) == C && !vector[i][j].contains(axiom.getKey().toString())) {
                                        vector[i][j] += axiom.getKey().toString();
                                    }
                                }
                            }

                        }
                    }

                }
            }
        }
    }

    /**
     * Init the vector with 0 length strings
     * Avoid modifying null strings
     *
     * @param vector
     *          the vector to initialize
     * @param wordLength
     *          the length of the word
     */
    private static void initVector(String[][] vector, int wordLength) {
        for (int i = 0; i < wordLength; ++i) {
            for (int j = 0; j < wordLength; ++j) {
                vector[i][j] = "";
            }
        }
    }
}