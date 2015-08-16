package grammar;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class stores a list of Character
 * which represent a rule
 */
public class Rule extends ArrayList<Character> {

    /**
     * Default constructor
     */
    public Rule() {
        super();
    }

    /**
     * Default constructor with parameter
     *
     * @param c
     *          list of all characters to add
     */
    public Rule(Collection<Character> c) {
        super(c);
    }

    /**
     * Check if a rule contains a subrule
     * Examples :
     *      ABCD contains BC
     *      ABCD does not contains AC
     *
     * @param subRule
     *          the rule to search
     * @return
     *          true if the subrule is contained in rule, false otherwise
     */
    public boolean containSubRule(Rule subRule) {
        int position = 0;
        for (Character c : this) {
            if (c == subRule.get(position)) {
                if (++position == subRule.size()) {
                    return true;
                }
            } else {
                position = 0;
            }
        }

        return false;
    }

    /**
     * Get the index of a subrule in a rule
     *
     * @param subRule
     *          the rule to search
     * @return
     *          the position of the first occurence, -1 otherwise
     */
    public int indexOfSubRule(Rule subRule) {
        int position = 0;
        for (int i = 0; i < size(); ++i) {
            if (get(i) == subRule.get(position)) {
                if (++position == subRule.size()) {
                    return i - position + 1;
                }
            } else {
                position = 0;
            }
        }
        return -1;
    }

    /**
     * Replace a subrule in a rule
     * Example : replace AA by BB in BDAA -> BDBB
     *
     * @param subRule
     *          the pattern to replace
     * @param replacement
     *          the replacement
     */
    public void replace(Rule subRule, Rule replacement) {
        int index = indexOfSubRule(subRule);
        this.removeRange(index, index + subRule.size());
        this.addAll(index, replacement);
    }

    @Override
    public int hashCode() {
        final int[] res = {0};
        this.stream().forEach(c -> res[0] += (c * 4) + 11);

        return res[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Rule)) {
            return false;
        }

        Rule cmp = (Rule) o;
        if (this.size() != cmp.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); ++i) {
            if (this.get(i) != cmp.get(i)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        final String[] ret = {""};
        stream().forEach(c -> ret[0] += c);
        return ret[0];
    }
}
