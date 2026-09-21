package mathematics.foundations;

import mathematics.core.*;
import static mathematics.core.MathStatus.Membership.*;

public final class FoundationDomains {
    private FoundationDomains() { }
    public static final Domain<Unit> UNIT=new Domain<>(Metadata.of("Unit","Singleton carrier for nullary operations"),Unit.class,value -> MEMBER);
    public static <T> Domain<T> empty(String id, Class<T> representation) { return new Domain<>(Metadata.of(id,"Empty carrier"),representation,value -> NOT_MEMBER); }
    @SuppressWarnings("unchecked")
    public static <A,B> Domain<Pair<A,B>> product(String id, Domain<A> first, Domain<B> second) {
        Class<Pair<A,B>> representation=(Class<Pair<A,B>>)(Class<?>)Pair.class;
        return new Domain<>(Metadata.of(id,"Product of " + first + " and " + second),representation,pair -> {
            MathStatus.Membership a=first.contains(pair.first), b=second.contains(pair.second);
            if (a == NOT_MEMBER || b == NOT_MEMBER) return NOT_MEMBER;
            return a == MEMBER && b == MEMBER ? MEMBER : UNKNOWN;
        });
    }
    @SuppressWarnings("unchecked")
    public static <A,B> Domain<MathFunction<A,B>> functions(String id, Domain<A> source, Domain<B> target) {
        Class<MathFunction<A,B>> representation=(Class<MathFunction<A,B>>)(Class<?>)MathFunction.class;
        return new Domain<>(Metadata.of(id,"Declared executable functions from " + source + " to " + target + "; totality is an implementation obligation"),representation,
                f -> f.source()==source && f.target()==target ? MEMBER : NOT_MEMBER);
    }
}
