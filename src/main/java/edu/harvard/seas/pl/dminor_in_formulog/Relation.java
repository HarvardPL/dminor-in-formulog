package edu.harvard.seas.pl.dminor_in_formulog;

public enum Relation {
	FUNC_SIG, LABELED_PURE, TYPE_INDICATOR_FUNC, TYPE_ALIAS;

	public String toString() {
		return super.toString().toLowerCase();
	}
}
