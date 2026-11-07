package aethereal.ui;
import aethereal.model.SearchMatch;
import aethereal.model.Translation;


public final class SearchResultRow {
    public final SearchMatch searchResult;
    public final Translation qualifier;

    public final SearchTypeBadge typeLabel;

    public SearchResultRow(SearchMatch class794Var, Translation class254Var, SearchTypeBadge class791Var) {
        this.searchResult = class794Var;
        this.qualifier = class254Var;
        this.typeLabel = class791Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "searchResult=" + this.searchResult + ", " + "qualifier=" + this.qualifier + ", " + "typeLabel=" + this.typeLabel + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.searchResult, this.qualifier, this.typeLabel);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SearchResultRow)) return false;
        SearchResultRow o = (SearchResultRow) obj;
        return java.util.Objects.equals(this.searchResult, o.searchResult) && java.util.Objects.equals(this.qualifier, o.qualifier) && java.util.Objects.equals(this.typeLabel, o.typeLabel);
    }
public SearchMatch searchResult() {
        return this.searchResult;
    }

    public Translation qualifier() {
        return this.qualifier;
    }

    public SearchTypeBadge typeLabel() {
        return this.typeLabel;
    }
}
