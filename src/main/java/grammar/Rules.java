package grammar;

import java.util.ArrayList;
import java.util.Collection;

public class Rules extends ArrayList<Rule> {

    public Rules() {
        super();
    }

    public Rules(Collection<Rule> c) {
        super(c);
    }

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
