package algorithm;

import exception.CYKException;
import grammar.Grammar;
import grammar.Rule;
import grammar.Rules;
import normalform.Chomsky;

import java.util.Map;

/*
2ème ligne	    3ème ligne 	    4ème ligne	    5ème ligne

00 - 01		    00 - 11		    00 - 21		    00 - 31
		        10 - 02		    10 - 12		    10 - 22
			                    20 - 03		    20 - 13
			    			                    30 - 04

01 - 02		    01 - 12		    01 - 22
		        11 - 03		    11 - 13
			                    21 - 04


02 - 03		    02 - 13
		        12 - 04



03 - 04
 */
public class CYK {
    private CYK() {}

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

        for (int i = 0; i < wordLength; ++i) {
            for (Map.Entry<Character, Rules> axiom : g.getAxioms().entrySet()) {
                for (Rule rule : axiom.getValue()) {
                    if (rule.size() == 1 && rule.get(0) == word.charAt(i) && !vector[0][i].contains(axiom.getKey().toString())) {
                        vector[0][i] += axiom.getKey().toString();
                    }
                }
            }

            if (vector[0][i].length() == 0) {
                return false;
            }
        }

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
        return vector[wordLength - 1][0].contains(Character.toString(g.getStartAxiom()));
    }

    private static void initVector(String[][] vector, int wordLength) {
        for (int i = 0; i < wordLength; ++i) {
            for (int j = 0; j < wordLength; ++j) {
                vector[i][j] = "";
            }
        }
    }
}