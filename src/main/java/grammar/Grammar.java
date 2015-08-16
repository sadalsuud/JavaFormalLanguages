package grammar;

import exception.BadFormattedGrammarException;
import exception.GrammarException;
import utils.SpecialChars;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class parses a Grammar in a file.
 *
 */
public class Grammar {
    /**
     * The grammar represented in list of axioms
     */
    private Axioms axioms = new Axioms();

    /**
     * The starting axiom
     */
    private char startAxiom;

    /**
     * Axioms containing epsilon
     */
    private List<Character> axiomsWithEpsilon = new ArrayList<>();

    /**
     * Default constructor
     *
     * @param file
     *              the filename to parse
     * @throws IOException
     * @throws BadFormattedGrammarException
     */
    public Grammar(String file) throws IOException, BadFormattedGrammarException {
        parse(new BufferedReader(new FileReader(file)));
    }

    /**
     * parse the file
     *
     * @param br
     *              the file buffer
     * @throws IOException
     * @throws BadFormattedGrammarException
     */
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
            if ((axiomName < 'A' || axiom[0].charAt(0) > 'Z') && axiomName != SpecialChars.epsilon) {
                throw new BadFormattedGrammarException("Axiom name must be uppercase or epsilon (&)");
            }

            String[] elements = axiom[1].split("\\|");
            Rules rules = new Rules();
            for (String element : elements) {
                Rule r = new Rule();
                for (Character c : element.toCharArray()) {
                    if (!Character.isLetter(c) && c != SpecialChars.epsilon) {
                        throw new BadFormattedGrammarException(c + " is not a character");
                    }

                    if (c == SpecialChars.epsilon) {
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

    /**
     * get all axioms
     *
     * @return
     *          all axioms
     */
    public Axioms getAxioms() {
        return axioms;
    }

    /**
     * Remove axiom and its presences in axioms with epsilon list
     *
     * @param axiomName
     *          the axiom to remove
     */
    public boolean removeAxiom(Character axiomName) {
        if (axioms.containsKey(axiomName)) {
            axioms.remove(axiomName);
            if (axiomsWithEpsilon.contains(axiomName)) {
                axiomsWithEpsilon.remove(axiomName);
            }
            return true;
        }
        return false;
    }

    /**
     * get a copy of all axioms
     *
     * @return
     *          a copy of all axioms
     */
    public Axioms getAxiomsCopy() {
        Axioms axioms = new Axioms();
        for (Map.Entry<Character, Rules> axiom : this.axioms.entrySet()) {
            axioms.put(axiom.getKey(), getAxiomRulesCopy(axiom.getKey()));
        }

        return axioms;
    }

    /**
     * get a copy of rules from a particular axiom
     *
     * @param axiomName
     *          the axiom that we want rules
     * @return
     *          a copy of rules
     */
    public Rules getAxiomRulesCopy(Character axiomName) {
        Rules rules = new Rules();
        for (Rule rule : axioms.get(axiomName)) {
            Rule copy = rule.stream().collect(Collectors.toCollection(Rule::new));
            rules.add(copy);
        }
        return rules;
    }

    /**
     * get the starting axiom
     *
     * @return
     *          the starting axiom
     */
    public Character getStartAxiom() {
        return startAxiom;
    }

    /**
     * get the list of axioms containing epsilon
     *
     * @return
     *          the axioms containing epsilon
     */
    public List<Character> getAxiomsWithEpsilon() {
        return axiomsWithEpsilon;
    }

    /**
     * get a copy of the list of axioms containing epsilong
     *
     * @return
     *          a copy of axioms containing epsilon
     */
    public List<Character> getAxiomsWithEpsilonCopy() {
        return axiomsWithEpsilon.stream().collect(Collectors.toList());
    }

    /**
     * Get all axioms names calling a certain axiom
     *
     * @param axiomName
     *          the axiom that we want calling axioms
     * @return
     *          the list of calling axioms
     */
    public List<Character> getCallingAxioms(Character axiomName) {
        List<Character> axiomNames =  new ArrayList<>();
        for (Map.Entry<Character, Rules> axiom : axioms.entrySet()) {
            for (Rule rule : axiom.getValue()) {
                rule.stream().filter(c -> c == axiomName && !axiomNames.contains(axiom.getKey())).forEach(c -> axiomNames.add(axiom.getKey()));
            }
        }

        return axiomNames;
    }

    /**
     * Check if the grammar contains some epsilons
     *
     * @return
     *          true if some epsilons are presents in grammar, false otherwise
     */
    public boolean hasEpsilons() {
        return axiomsWithEpsilon.size() > 0;
    }

    /**
     * Check if the grammar has only one epsilon
     * and this epsilon is in the starting axiom
     *
     * @return
     *          true if there is epsilon only in starting axiom
     */
    public boolean hasEpsilonsOnlyInStart() {
        return axiomsWithEpsilon.size() == 1 && axiomsWithEpsilon.get(0) == startAxiom;
    }

    /**
     * Get the first free available axiom name
     *
     * @return
     *          the first free axiom name
     * @throws GrammarException
     *          if all axiom names are in use
     */
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