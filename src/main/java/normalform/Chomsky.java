package normalform;

import exception.ChomskyException;
import exception.GrammarException;
import grammar.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Chomsky {

    private Chomsky() {}

    public static void normalize(Grammar g) throws ChomskyException, GrammarException {
        if (g.hasEpsilons() && !g.hasEpsilonsOnlyInStart()) {
            throw new ChomskyException("Grammar is not epsilon free");
        }

        refactorOneProductionRule(g);
        Clean.normalize(g);

        refactorTerminalsInNonNormalizedRules(g);

        while (!isNormalized(g)) {
            Rule smallestRule = getSmallestNonNormalizedRule(g);
            Rule newNormalizedRule = new Rule(Arrays.asList(smallestRule.get(0), smallestRule.get(1)));
            Character newNormalizedAxiomName = g.getFirstFreeAxiomName();

            g.getAxioms().values().stream().forEach(rules -> rules.stream()
                    .filter(rule -> !Chomsky.isRuleNormalized(rule))
                    .filter(rule -> rule.containSubRule(newNormalizedRule))
                    .forEach(rule -> rule.replace(newNormalizedRule, new Rule(Collections.singletonList(newNormalizedAxiomName)))));
            g.getAxioms().put(newNormalizedAxiomName, new Rules(Collections.singletonList(newNormalizedRule)));
        }
    }

    private static void refactorOneProductionRule(Grammar g) {
        g.getAxiomsCopy().entrySet().stream().forEach(axiom ->
                        axiom.getValue().stream().filter(rule -> rule.size() == 1 && Character.isUpperCase(rule.get(0))).forEach(rule -> {
                            g.getAxioms().get(axiom.getKey()).remove(rule);
                            g.getAxioms().get(axiom.getKey()).addAll(g.getAxiomRulesCopy(rule.get(0)));
                        })
        );
    }

    private static void refactorTerminalsInNonNormalizedRules(Grammar g) throws GrammarException {
        Map<Character, Character> modified = new HashMap<>();
        for (Map.Entry<Character, Rules> axiom : g.getAxiomsCopy().entrySet()) {
            for (int i = 0; i < axiom.getValue().size(); ++i) {
                if (isRuleNormalized(axiom.getValue().get(i))) {
                    continue;
                }

                for (int j = 0; j < axiom.getValue().get(i).size(); ++j) {
                    if (!Character.isLowerCase(axiom.getValue().get(i).get(j))) {
                        continue;
                    }

                    if (!modified.containsKey(axiom.getValue().get(i).get(j))) {
                        Character newAxiomName = g.getFirstFreeAxiomName();
                        modified.put(axiom.getValue().get(i).get(j), newAxiomName);
                        g.getAxioms().put(newAxiomName, new Rules(Collections.singletonList(new Rule(Collections.singletonList(axiom.getValue().get(i).get(j))))));
                    }
                    g.getAxioms().get(axiom.getKey()).get(i).set(j, modified.get(axiom.getValue().get(i).get(j)));
                }
            }
        }
    }

    private static Rule getSmallestNonNormalizedRule(Grammar g) {
        Rule smallestRule = null;
        for (Rules rules : g.getAxiomsCopy().values()) {
            for (Rule rule : rules) {
                if (!isRuleNormalized(rule) && (smallestRule == null || smallestRule.size() > rule.size())) {
                    smallestRule = rule;
                    if (rule.size() == 3) {
                        return smallestRule;
                    }
                }
            }
        }

        return smallestRule;
    }

    public static boolean isNormalized(Grammar g) {
        if (g.hasEpsilons() && !g.hasEpsilonsOnlyInStart()) {
            return false;
        }

        for (Rules rules : g.getAxioms().values()) {
            for (Rule rule : rules) {
                if (!isRuleNormalized(rule)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isRuleNormalized(Rule rule) {
        return rule.size() == 1 || (rule.size() == 2 && Character.isUpperCase(rule.get(0)) && Character.isUpperCase(rule.get(1)));
    }
}
