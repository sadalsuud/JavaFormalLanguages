package grammar;

import java.util.*;

public class Clean {

    private Clean() {}

    public static void normalize(Grammar g) {
        List<Character> productiveAxioms = new ArrayList<>();
        while (g.getAxioms().size() > 0 && productiveAxioms.size() != g.getAxioms().size()) {
            int tmpSize;
            do {
                removeNonAccessibles(g, productiveAxioms);
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
        //refactorOneRuleAxiom(g);
    }

    private static void removeNonAccessibles(Grammar g, List<Character> productiveAxioms) {
        Axioms axioms = g.getAxiomsCopy();
        List<Character> accessiblesAxioms = new ArrayList<>();
        accessiblesAxioms.add(axioms.keySet().iterator().next());
        for (Map.Entry<Character, Rules> axiom : axioms.entrySet()) {
            for (Rule rule : axiom.getValue()) {
                rule.stream()
                        .filter(c -> c != axiom.getKey() && !Character.isLowerCase(c) && c != '&' && !accessiblesAxioms.contains(c))
                        .forEach(accessiblesAxioms::add);
            }
        }

        axioms.keySet().stream().filter(axiomName -> !accessiblesAxioms.contains(axiomName)).forEach(axiomName -> {
            g.getAxioms().remove(axiomName);
            if (productiveAxioms.contains(axiomName)) {
                productiveAxioms.remove(axiomName);
            }
            if (g.getAxiomsWithEpsilon().contains(axiomName)) {
                g.getAxiomsWithEpsilon().remove(axiomName);
            }
        });
    }

    private static boolean isAxiomProductive(Grammar g, Character axiomName, List<Character> productiveAxioms) {
        Rules rules = g.getAxioms().get(axiomName);
        for (Rule rule : rules) {
            int nbCharProductive = 0;
            for (Character c : rule) {
                if (Character.isLowerCase(c) || c == '&' || productiveAxioms.contains(c)) {
                    ++nbCharProductive;
                }
            }

            if (nbCharProductive == rule.size()) {
                return true;
            }
        }

        return false;
    }

    private static void removeNonProductiveRules(Grammar g, List<Character> productiveAxioms) {
        for (Map.Entry<Character, Rules> entry : g.getAxioms().entrySet()) {
            for (Rule rule : g.getAxiomRulesCopy(entry.getKey())) {
                for (Character c : rule) {
                    if (!Character.isLowerCase(c) && c != '&' && !productiveAxioms.contains(c)) {
                        entry.getValue().remove(rule);
                        break;
                    }
                }
            }
        }
    }

    private static void removeEmptyAxioms(Grammar g) {
        Axioms axioms = g.getAxiomsCopy();
        axioms.entrySet().stream().filter(entry -> entry.getValue().size() == 0).forEach(entry -> {
            g.getAxioms().remove(entry.getKey());
            if (g.getAxiomsWithEpsilon().contains(entry.getKey())) {
                g.getAxiomsWithEpsilon().remove(entry.getKey());
            }
        });
    }

    private static void refactorOneRuleAxiom(Grammar g) {
        Axioms axioms = g.getAxiomsCopy();
        axioms.entrySet().stream().filter(axiom -> axiom.getValue().size() == 1 && axiom.getKey() != g.getStartAxiom()).forEach(axiom -> {
            g.getAxioms().remove(axiom.getKey());
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

    private static void refactorKeyKeyEpsilonAxiom(Grammar g) {
        g.getAxiomsCopy().entrySet().stream()
                .filter(axiom -> axiom.getValue().size() == 2)
                .filter(axiom -> axiom.getValue().get(0).size() == 1)
                .filter(axiom -> axiom.getValue().get(0).get(0) == axiom.getKey())
                .filter(axiom -> axiom.getValue().get(1).size() == 1)
                .filter(axiom -> axiom.getValue().get(1).get(0) == '&')
                .forEach(axiom -> g.getCallingAxioms(axiom.getKey()).stream()
                                .filter(callingAxiom -> callingAxiom != axiom.getKey())
                                .forEach(callingAxiom -> {
                                    Rule keyRule = new Rule(Collections.singletonList(axiom.getKey()));
                                    Rule epsilonRule = new Rule(Collections.singletonList('&'));

                                    g.getAxioms().get(callingAxiom).remove(keyRule);
                                    g.getAxioms().get(callingAxiom).add(epsilonRule);
                                    g.getAxioms().remove(axiom.getKey());
                                    g.getAxiomsWithEpsilon().remove(axiom.getKey());
                                })
                );
    }

    private static void removeEpsilon(Grammar g) {
        Rule epsilonRule = new Rule(Collections.singletonList('&'));

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

                            for (int i = 0; i < combinations - 1; ++i) {
                                String binaryCombination = String.format("%" + occurrences + "s", Integer.toBinaryString(i)).replace(' ', '0');
                                final int[] binaryPosition = {0};

                                Rule newRule = new Rule();
                                rule.stream().filter(c -> c != axiomWithEpsilon || binaryCombination.charAt(binaryPosition[0]++) == '1').forEach(newRule::add);
                                if (newRule.size() == 0) {
                                    newRule.add('&');
                                    if (!g.getAxiomsWithEpsilon().contains(callingAxiom)) {
                                        g.getAxiomsWithEpsilon().add(callingAxiom);
                                    }
                                }
                                g.getAxioms().get(callingAxiom).add(newRule);
                            }
                        }
                    });
                });
                g.getAxioms().get(axiomWithEpsilon).remove(epsilonRule);
                g.getAxiomsWithEpsilon().remove(axiomWithEpsilon);
            });
        }
    }
}
