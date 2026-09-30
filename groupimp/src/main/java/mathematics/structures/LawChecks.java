package mathematics.structures;

import mathematics.core.*;
import mathematics.foundations.FiniteSet;
import java.util.*;

/** Finite counterexample search. Reports are empirical tests, never general proofs. */
public final class LawChecks {
    private LawChecks() { }
    public static final class Report {
        public final MathStatus.Epistemic status;
        public final List<?> counterexample;
        public final long cases;
        private Report(List<?> counterexample,long cases) {
            this.counterexample=Collections.unmodifiableList(new ArrayList<>(counterexample)); this.cases=cases;
            status=counterexample.isEmpty()?MathStatus.Epistemic.EMPIRICALLY_TESTED:MathStatus.Epistemic.COUNTEREXAMPLE_FOUND;
        }
    }
    public static <T> Report associativity(FiniteSet<T> sample,Functions.Binary<T,T,T> operation) {
        long cases=0;
        for(T a : sample.members()) for(T b : sample.members()) for(T c : sample.members()) {
            cases++;
            if(!Objects.equals(operation.apply(operation.apply(a,b),c),operation.apply(a,operation.apply(b,c)))) return new Report(Arrays.asList(a,b,c),cases);
        }
        return new Report(Collections.emptyList(),cases);
    }
    public static <T> Report commutativity(FiniteSet<T> sample,Functions.Binary<T,T,T> operation) {
        long cases=0;
        for(T a : sample.members()) for(T b : sample.members()) {
            cases++; if(!Objects.equals(operation.apply(a,b),operation.apply(b,a))) return new Report(Arrays.asList(a,b),cases);
        }
        return new Report(Collections.emptyList(),cases);
    }
    public static <T> Report identity(FiniteSet<T> sample,T identity,Functions.Binary<T,T,T> operation) {
        long cases=0;
        for(T a : sample.members()) {
            cases++; if(!Objects.equals(operation.apply(a,identity),a) || !Objects.equals(operation.apply(identity,a),a)) return new Report(Collections.singletonList(a),cases);
        }
        return new Report(Collections.emptyList(),cases);
    }
}
