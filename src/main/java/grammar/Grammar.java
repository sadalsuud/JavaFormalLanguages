package grammar;

import exception.BadFormattedGrammarException;
import exception.GrammarException;
import normalform.Chomsky;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Grammar {
    private Axioms axioms = new Axioms();
    private char startAxiom;
    private List<Character> axiomsWithEpsilon = new ArrayList<>();

    public Grammar(String file) throws IOException, BadFormattedGrammarException {
        parse(new BufferedReader(new FileReader(file)));
    }

    private void parse(BufferedReader br) throws IOException, BadFormattedGrammarException {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.replace(" ", "");
            String[] axiom = line.split(":");
            if (axiom.length != 2) {
                throw new BadFormattedGrammarException("Bad axiom : " + line);
            }

            if (axiom[0].length() != 1) {
                throw new BadFormattedGrammarException("Axiom name must be 1 character length");
            }

            char axiomName = axiom[0].charAt(0);
            if ((axiomName < 'A' || axiom[0].charAt(0) > 'Z') && axiomName != '&') {
                throw new BadFormattedGrammarException("Axiom name must be uppercase or epsilon (&)");
            }

            String[] elements = axiom[1].split("\\|");
            Rules rules = new Rules();
            for (String element : elements) {
                Rule r = new Rule();
                for (Character c : element.toCharArray()) {
                    if (!Character.isLetter(c) && c != '&') {
                        throw new BadFormattedGrammarException(c + " is not a character");
                    }

                    if (c == '&') {
                        if (axiomsWithEpsilon.contains(axiomName)) {
                            throw new BadFormattedGrammarException("Epsilon must appears at most once in an axiom");
                        }
                        axiomsWithEpsilon.add(axiomName);
                    }
                    r.add(c);
                }

                if (axiomsWithEpsilon.contains(axiomName) && r.size() > 1) {
                    throw new BadFormattedGrammarException("Epsilon must be the only character in a rule");
                }
                rules.add(r);
            }

            if (axioms.size() == 0) {
                startAxiom = axiomName;
            }

            axioms.put(axiomName, rules);
        }
    }

    public Axioms getAxioms() {
        return axioms;
    }

    public char getStartAxiom() {
        return startAxiom;
    }

    public List<Character> getAxiomsWithEpsilon() {
        return axiomsWithEpsilon;
    }

    public List<Character> getAxiomsWithEpsilonCopy() {
        return axiomsWithEpsilon.stream().collect(Collectors.toList());
    }

    public Axioms getAxiomsCopy() {
        Axioms axioms = new Axioms();
        for (Map.Entry<Character, Rules> axiom : this.axioms.entrySet()) {
            axioms.put(axiom.getKey(), getAxiomRulesCopy(axiom.getKey()));
        }

        return axioms;
    }

    public Rules getAxiomRulesCopy(Character axiomName) {
        Rules rules = new Rules();
        for (Rule rule : axioms.get(axiomName)) {
            Rule copy = rule.stream().collect(Collectors.toCollection(Rule::new));
            rules.add(copy);
        }
        return rules;
    }

    public List<Character> getCallingAxioms(Character axiomName) {
        List<Character> axiomNames =  new ArrayList<>();
        for (Map.Entry<Character, Rules> axiom : axioms.entrySet()) {
            for (Rule rule : axiom.getValue()) {
                rule.stream().filter(c -> c == axiomName && !axiomNames.contains(axiom.getKey())).forEach(c -> axiomNames.add(axiom.getKey()));
            }
        }

        return axiomNames;
    }

    public boolean hasEpsilons() {
        return axiomsWithEpsilon.size() != 0;
    }

    public boolean hasEpsilonsOnlyInStart() {
        return axiomsWithEpsilon.size() == 1 && axiomsWithEpsilon.get(0) == startAxiom;
    }

    public Character getFirstFreeAxiomName() throws GrammarException {
        for (char c = 'A'; c <= 'Z'; ++c) {
            if (axioms.get(c) == null) {
                return c;
            }
        }

        throw new GrammarException("No free axiom name");
    }

    @Override
    public String toString() {
        final String[] ret = {""};
        axioms.entrySet().stream().forEach(entry -> {
            ret[0] += entry.getKey() + " -> ";
            entry.getValue().stream().forEach(rule -> ret[0] += rule + " | ");
            ret[0] = ret[0].substring(0, ret[0].length() - 3);
            ret[0] += "\n";
        });

        return ret[0];
    }
}