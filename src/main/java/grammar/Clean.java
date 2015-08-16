package grammar;

import utils.SpecialChars;

import java.util.*;

/**
 * This class cleans a grammar by :
 * - removing non accessible axioms
 * - removing non productive axioms
 * - refactoring axioms matching pattern : X -> X | &
 * - removing epsilon productions
 */
public class Clean {

    private Clean() {}

    /**
     * clean a grammar
     *
     * @param g
     *          the grammar to clean
     */
    public static void normalize(Grammar g) {
        List<Character> productiveAxioms = new ArrayList<>();
        while (g.getAxioms().size() > 0 && productiveAxioms.size() != g.getAxioms().size()) {
            int tmpSize;
            do {
                removeNonAccessible(g, productiveAxioms);
                tmpSize = productiveAxioms.size();

                g.getAxioms().keySet().stream()
                        .filter(axiomName -> isAxiomProductive(g, axiomName, productiveAxioms) && !productiveAxioms.contains(axiomName))
                        .forEach(productiveAxioms::add);

            } while (tmpSize != productiveAxioms.size());

            removeNonProductiveRules(g, productiveAxioms);
            removeEmptyAxioms(g);
        }

        refactorKeyKeyEpsilonAxiom(g);
        removeEpsilon(g);

        // removed because it breaks the Chomsky compatibility
        // refactorOneRuleAxiom(g);
    }

    /**
     * Remove non accessible axioms
     *
     * @param g
     *          the grammar
     * @param productiveAxioms
     *          list of all productive axioms
     */
    private static void removeNonAccessible(Grammar g, List<Character> productiveAxioms) {
        Axioms axioms = g.getAxiomsCopy();
        List<Character> accessibleAxioms = new ArrayList<>();

        /**
         * List all accessible axioms
         */
        accessibleAxioms.add(axioms.keySet().iterator().next());
        for (Map.Entry<Character, Rules> axiom : axioms.entrySet()) {
            for (Rule rule : axiom.getValue()) {
                rule.stream()
                        .filter(c -> c != axiom.getKey() && !Character.isLowerCase(c) && c != SpecialChars.epsilon && !accessibleAxioms.contains(c))
                        .forEach(accessibleAxioms::add);
            }
        }

        /**
         * Remove all non accessible axioms and also
         * their presence in the list of productive axioms
         */
        axioms.keySet().stream().filter(axiomName -> !accessibleAxioms.contains(axiomName)).forEach(axiomName -> {
            g.removeAxiom(axiomName);
            if (productiveAxioms.contains(axiomName)) {
                productiveAxioms.remove(axiomName);
            }
        });
    }

    /**
     * Check if an axiom is productive
     *
     * @param g
     *          the grammar
     * @param axiomName
     *          the axiom to check
     * @param productiveAxioms
     *          the list of all productive axioms
     * @return
     *          true if the axiom is productive, false otherwise
     */
    private static boolean isAxiomProductive(Grammar g, Character axiomName, List<Character> productiveAxioms) {
        Rules rules = g.getAxioms().get(axiomName);
        for (Rule rule : rules) {
            int nbCharProductive = 0;
            for (Character c : rule) {
                if (Character.isLowerCase(c) || c == SpecialChars.epsilon || productiveAxioms.contains(c)) {
                    ++nbCharProductive;
                }
            }

            if (nbCharProductive == rule.size()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove all non productive rules
     *
     * @param g
     *          the grammar
     * @param productiveAxioms
     *          the list of all productive axioms
     */
    private static void removeNonProductiveRules(Grammar g, List<Character> productiveAxioms) {
        for (Map.Entry<Character, Rules> entry : g.getAxioms().entrySet()) {
            for (Rule rule : g.getAxiomRulesCopy(entry.getKey())) {
                for (Character c : rule) {
                    if (!Character.isLowerCase(c) && c != SpecialChars.epsilon && !productiveAxioms.contains(c)) {
                        entry.getValue().remove(rule);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Remove axioms with no rules
     * (this can happens when the removeNonProductiveRules function
     * has removed all rules of the axioms)
     *
     * @param g
     *          the grammar
     */
    private static void removeEmptyAxioms(Grammar g) {
        Axioms axioms = g.getAxiomsCopy();
        axioms.entrySet().stream().filter(entry -> entry.getValue().size() == 0).forEach(entry -> {
            g.removeAxiom(entry.getKey());
            if (g.getAxiomsWithEpsilon().contains(entry.getKey())) {
                g.getAxiomsWithEpsilon().remove(entry.getKey());
            }
        });
    }

    /**
     * Remove axioms containing one rule by refactoring them
     * Example :
     *      S -> A | B
     *      A -> aa
     *      B -> bb | b
     *   become
     *      S -> aa | B
     *      B -> bb | b
     *
     * @param g
     *          the grammar
     */
    @Deprecated
    private static void refactorOneRuleAxiom(Grammar g) {
        Axioms axioms = g.getAxiomsCopy();
        axioms.entrySet().stream().filter(axiom -> axiom.getValue().size() == 1 && axiom.getKey() != g.getStartAxiom()).forEach(axiom -> {
            g.removeAxiom(axiom.getKey());
            Axioms axioms2 = g.getAxiomsCopy();
            for (Map.Entry<Character, Rules> axiom2 : axioms2.entrySet()) {
                for (int i = 0; i < axiom2.getValue().size(); ++i) {
                    for (int j = 0; j < axiom2.getValue().get(i).size(); ++j) {
                        if (axiom.getKey() == axiom2.getValue().get(i).get(j)) {
                            g.getAxioms().get(axiom2.getKey()).get(i).remove(j);
                            for (int k = 0; k < axiom.getValue().get(0).size(); ++k) {
                                g.getAxioms().get(axiom2.getKey()).get(i).add(j + k, axiom.getValue().get(0).get(k));
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Refactor rules like : X -> X | & which are useless
     * Example :
     *      S -> A
     *      A -> A | &
     *    become
     *      S -> &
     *
     * @param g
     *          the grammar
     */
    private static void refactorKeyKeyEpsilonAxiom(Grammar g) {
        g.getAxiomsCopy().entrySet().stream()
                .filter(axiom -> axiom.getValue().size() == 2)
                .filter(axiom -> axiom.getValue().get(0).size() == 1)
                .filter(axiom -> axiom.getValue().get(0).get(0) == axiom.getKey())
                .filter(axiom -> axiom.getValue().get(1).size() == 1)
                .filter(axiom -> axiom.getValue().get(1).get(0) == SpecialChars.epsilon)
                .forEach(axiom -> g.getCallingAxioms(axiom.getKey()).stream()
                                .filter(callingAxiom -> callingAxiom != axiom.getKey())
                                .forEach(callingAxiom -> {
                                    Rule keyRule = new Rule(Collections.singletonList(axiom.getKey()));
                                    Rule epsilonRule = new Rule(Collections.singletonList(SpecialChars.epsilon));

                                    g.getAxioms().get(callingAxiom).remove(keyRule);
                                    g.getAxioms().get(callingAxiom).add(epsilonRule);
                                    g.removeAxiom(axiom.getKey());
                                })
                );
    }

    /**
     * Remove all epsilons productions
     * (except in the starting axiom)
     *
     * Example :
     *      S -> a | b | CdC | C
     *      C -> aC | c | &
     *    become
     *      S -> a | b | CdC | Cd | dC | d | C | &
     *      C -> cC | c
     *
     * @param g
     *          the grammar
     */
    private static void removeEpsilon(Grammar g) {
        Rule epsilonRule = new Rule(Collections.singletonList(SpecialChars.epsilon));

        while (g.hasEpsilons() && !g.hasEpsilonsOnlyInStart()) {
            g.getAxiomsWithEpsilonCopy().stream().filter(axiomWithEpsilon -> axiomWithEpsilon != g.getStartAxiom()).forEach(axiomWithEpsilon -> {
                List<Character> callingAxioms = g.getCallingAxioms(axiomWithEpsilon);
                callingAxioms.stream().forEach(callingAxiom -> {
                    g.getAxiomsCopy().get(callingAxiom).stream().filter(rule -> rule.contains(axiomWithEpsilon)).forEach(rule -> {
                        if (rule.size() == 1) {
                            if (!g.getAxiomsWithEpsilon().contains(callingAxiom)) {
                                g.getAxioms().get(callingAxiom).add(epsilonRule);
                                g.getAxiomsWithEpsilon().add(callingAxiom);
                            }
                        } else {
                            int occurrences = Collections.frequency(rule, axiomWithEpsilon);
                            int combinations = (int) Math.pow(2, occurrences);

                            /**
                             * The algorithm is quite simple
                             * Imagine you have the following grammar :
                             *      S -> a | b | CdC
                             *      C -> aC | c | &
                             * and you want to remove the epsilon production in C
                             * The grammar become :
                             *      S -> a | b | CdC | Cd | dC | d
                             *      C -> cC | c
                             *
                             * The difficulty is to generate
                             * CdC ; Cd ; dC ; d
                             *
                             * as you can see, there are 2^2 = 4 possibilities
                             * We iterate from 0 to 4 - 1 (since CdC is already present)
                             * and we convert i (which is the counter) to binary.
                             * we get : 00 ; 01 ; 10 ; 11
                             *
                             * After, we iterate from the initial rule (which is CdC) and :
                             * - if there is a 0, we remove the C
                             * - else, we keep the C
                             *
                             * for 1 : we have 01, so we obtain dC (the first C is removed, and the second is kept)
                             */
                            for (int i = 0; i < combinations - 1; ++i) {
                                String binaryCombination = String.format("%" + occurrences + "s", Integer.toBinaryString(i)).replace(' ', '0');
                                final int[] binaryPosition = {0};

                                Rule newRule = new Rule();
                                rule.stream().filter(c -> c != axiomWithEpsilon || binaryCombination.charAt(binaryPosition[0]++) == '1').forEach(newRule::add);
                                if (newRule.size() == 0) {
                                    newRule.add(SpecialChars.epsilon);
                                    if (!g.getAxiomsWithEpsilon().contains(callingAxiom)) {
                                        g.getAxiomsWithEpsilon().add(callingAxiom);
                                    }
                                }
                                g.getAxioms().get(callingAxiom).add(newRule);
                            }
                        }
                    });
                });

                /**
                 * The axiom is now epsilon free
                 * so we can remove it from the list of
                 * axioms containing epsilon
                 */
                g.getAxioms().get(axiomWithEpsilon).remove(epsilonRule);
                g.getAxiomsWithEpsilon().remove(axiomWithEpsilon);
            });
        }

        /**
         * Replace epsilon from the starting axiom
         * at the end (more beautiful)
         */
        if (g.hasEpsilonsOnlyInStart()) {
            g.getAxioms().get(g.getStartAxiom()).remove(epsilonRule);
            g.getAxioms().get(g.getStartAxiom()).add(epsilonRule);
        }
    }
}
