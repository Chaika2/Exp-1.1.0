package aethereal.model;
import aethereal.type.MatchMode;


public final class SearchMatch implements Comparable<SearchMatch> {
    public final SearchNavTarget searchTarget;

    public final MatchMode matchType;

    public SearchMatch(SearchNavTarget class844Var, MatchMode class793Var) {
        this.searchTarget = class844Var;
        this.matchType = class793Var;
    }

    @Override
    public int compareTo(SearchMatch class794Var) {
        return Integer.compare(this.matchType.getPriority(), class794Var.matchType.getPriority());
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "searchTarget=" + this.searchTarget + ", " + "matchType=" + this.matchType + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.searchTarget, this.matchType);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SearchMatch)) return false;
        SearchMatch o = (SearchMatch) obj;
        return java.util.Objects.equals(this.searchTarget, o.searchTarget) && java.util.Objects.equals(this.matchType, o.matchType);
    }
public SearchNavTarget searchTarget() {
        return this.searchTarget;
    }

    public MatchMode matchType() {
        return this.matchType;
    }
}
