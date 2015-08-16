package grammar;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class stores a list of Rules
 */
public class Rules extends ArrayList<Rule> {

    /**
     * Default constructor
     */
    public Rules() {
        super();
    }

    /**
     * Default constructor with parameter
     *
     * @param c
     *          list of all rules to add
     */
    public Rules(Collection<Rule> c) {
        super(c);
    }

    /**
     * add a rule to the list
     *
     * @param r
     *          the rule to add
     * @return
     *          true if the rule was not already present and it was inserted, false otherwise
     */
    @Override
    public boolean add(Rule r) {
        return !contains(r) && super.add(r);
    }

    @Override
    public int hashCode() {
        final int[] res = {0};
        this.stream().forEach(rule -> res[0] += (rule.hashCode() * 2) + 7);

        return res[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Rules)) {
            return false;
        }

        Rules cmp = (Rules) o;
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
        stream().forEach(rule -> ret[0] += rule + " | ");
        return ret[0].substring(0, ret[0].length() - 3);
    }
}
