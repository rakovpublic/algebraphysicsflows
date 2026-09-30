package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A finite cocone, validated and searched as a cone on the opposite diagram. */
public final class FiniteCocone implements Serializable {
    private static final long serialVersionUID=1L;
    public final FiniteFunctor diagram;
    public final BigInteger vertex;
    private final FiniteCone dual;

    public FiniteCocone(FiniteFunctor diagram,BigInteger vertex,Map<BigInteger,BigInteger> legs) {
        this(new FiniteCone(diagram.opposite(),vertex,legs));
    }
    private FiniteCocone(FiniteCone dual) {
        this.dual=Objects.requireNonNull(dual); this.diagram=dual.diagram.opposite(); this.vertex=dual.vertex;
    }
    public static FiniteCocone fromOpposite(FiniteCone cone) { return new FiniteCocone(cone); }
    public FiniteCone opposite() { return dual; }
    public static FiniteCocone fromTransformation(FiniteNaturalTransformation legs,BigInteger vertex) {
        if(!legs.target.equals(FiniteFunctor.constant(legs.source.source,legs.source.target,vertex)))
            throw MathFailure.undefined("A cocone transformation must end at the constant diagram on the supplied vertex");
        return new FiniteCocone(FiniteCone.fromTransformation(legs.opposite(),vertex));
    }
    public Map<BigInteger,BigInteger> legMap() { return dual.legMap(); }
    public BigInteger leg(BigInteger object) { return dual.leg(object); }
    public List<BigInteger> legValues() { return dual.legValues(); }
    public FiniteNaturalTransformation asTransformation() { return dual.asTransformation().opposite(); }
    public FiniteCocone reindex(FiniteFunctor before) { return new FiniteCocone(dual.reindex(before.opposite())); }
    public FiniteCocone map(FiniteFunctor after) { return new FiniteCocone(dual.map(after.opposite())); }
    /** All commuting arrows from this vertex to the other vertex. */
    public List<BigInteger> mediatorsTo(FiniteCocone other) { return dual.mediatorsFrom(other.dual); }
    public boolean isColimit() { return dual.isLimit(); }
    /** Require the entire colimit property and return the unique arrow out to the other cocone. */
    public BigInteger descend(FiniteCocone other) { return dual.lift(other.dual); }
    public static FiniteCocone colimit(FiniteFunctor diagram) { return new FiniteCocone(FiniteCone.limit(diagram.opposite())); }
    public static List<FiniteCocone> at(FiniteFunctor diagram,BigInteger vertex) {
        List<FiniteCocone> result=new ArrayList<>();
        for(FiniteCone cone : FiniteCone.at(diagram.opposite(),vertex)) result.add(new FiniteCocone(cone));
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) { return other instanceof FiniteCocone && dual.equals(((FiniteCocone)other).dual); }
    @Override public int hashCode() { return dual.hashCode(); }
    @Override public String toString() { return "Cocone(diagram="+diagram+", vertex="+vertex+", legs="+legMap()+")"; }
}
