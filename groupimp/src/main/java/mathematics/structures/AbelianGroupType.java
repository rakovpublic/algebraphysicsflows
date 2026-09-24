package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Isomorphism type Z^r plus finite cyclic invariant factors, with no chosen generators or elements. */
public final class AbelianGroupType implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_FACTORS=256, MAX_INPUT_FACTORS=1024;
    public static final AbelianGroupType ZERO=new AbelianGroupType(BigInteger.ZERO,Collections.emptyList());
    public static final AbelianGroupType Z=new AbelianGroupType(BigInteger.ONE,Collections.emptyList());
    private final BigInteger freeRank;
    private final List<BigInteger> factors;
    private static MathFailure limit() {
        return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Abelian group types allow 256 invariant factors and 1024 intermediate cyclic factors");
    }
    public AbelianGroupType(BigInteger freeRank,Collection<BigInteger> cyclicOrders) {
        if(Objects.requireNonNull(freeRank).signum()<0) throw MathFailure.invalid("Free rank must be nonnegative");
        this.freeRank=freeRank;
        if(cyclicOrders.size()>MAX_INPUT_FACTORS) throw limit();
        List<BigInteger> values=new ArrayList<>();
        for(BigInteger order : cyclicOrders) {
            if(Objects.requireNonNull(order).signum()<=0) throw MathFailure.invalid("Torsion cyclic orders must be positive");
            if(!order.equals(BigInteger.ONE)) values.add(order);
        }
        // Z/a + Z/b is Z/gcd(a,b) + Z/lcm(a,b), without requiring integer factorization.
        for(int i=0;i<values.size();i++) for(int j=i+1;j<values.size();j++) {
            BigInteger a=values.get(i),b=values.get(j),gcd=a.gcd(b);
            values.set(i,gcd); values.set(j,a.divide(gcd).multiply(b));
        }
        values.removeIf(BigInteger.ONE::equals);
        if(values.size()>MAX_FACTORS) throw limit();
        factors=Collections.unmodifiableList(values);
    }
    public static AbelianGroupType free(BigInteger rank) { return new AbelianGroupType(rank,Collections.emptyList()); }
    /** Z/nZ, with n=0 denoting Z and n=1 the trivial group. */
    public static AbelianGroupType cyclic(BigInteger order) {
        if(order.signum()<0) throw MathFailure.undefined("A cyclic order must be nonnegative");
        return order.signum()==0?Z:new AbelianGroupType(BigInteger.ZERO,Collections.singletonList(order));
    }
    public BigInteger freeRank() { return freeRank; }
    public List<BigInteger> invariantFactors() { return factors; }
    public BigInteger minimalGenerators() { return freeRank.add(BigInteger.valueOf(factors.size())); }
    public boolean isFinite() { return freeRank.signum()==0; }
    public boolean isTorsionFree() { return factors.isEmpty(); }
    public boolean isTrivial() { return isFinite() && isTorsionFree(); }
    public boolean isCyclic() { return isFinite()?factors.size()<=1:freeRank.equals(BigInteger.ONE) && factors.isEmpty(); }
    public BigInteger order() {
        if(!isFinite()) throw MathFailure.undefined("An infinite group has no finite order in N");
        BigInteger result=BigInteger.ONE; for(BigInteger factor : factors) result=result.multiply(factor); return result;
    }
    public BigInteger exponent() {
        if(!isFinite()) throw MathFailure.undefined("A group with a free summand has no finite exponent");
        return factors.isEmpty()?BigInteger.ONE:factors.get(factors.size()-1);
    }
    public AbelianGroupType torsionPart() { return new AbelianGroupType(BigInteger.ZERO,factors); }
    public AbelianGroupType freePart() { return free(freeRank); }
    public AbelianGroupType directSum(AbelianGroupType other) {
        List<BigInteger> result=new ArrayList<>(factors); result.addAll(other.factors);
        return new AbelianGroupType(freeRank.add(other.freeRank),result);
    }
    private static void copies(List<BigInteger> result,List<BigInteger> factors,BigInteger count) {
        if(count.signum()<0) throw MathFailure.undefined("A direct-sum multiplicity must be nonnegative");
        if(factors.isEmpty()) return;
        BigInteger size=count.multiply(BigInteger.valueOf(factors.size())).add(BigInteger.valueOf(result.size()));
        if(size.compareTo(BigInteger.valueOf(MAX_INPUT_FACTORS))>0) throw limit();
        for(int i=0;i<count.intValueExact();i++) result.addAll(factors);
    }
    public AbelianGroupType repeat(BigInteger count) {
        List<BigInteger> result=new ArrayList<>(); copies(result,factors,count);
        return new AbelianGroupType(freeRank.multiply(count),result);
    }
    private List<BigInteger> commonFactors(AbelianGroupType other) {
        List<BigInteger> result=new ArrayList<>();
        for(BigInteger a : factors) for(BigInteger b : other.factors) {
            BigInteger gcd=a.gcd(b);
            if(!gcd.equals(BigInteger.ONE)) { if(result.size()==MAX_INPUT_FACTORS) throw limit(); result.add(gcd); }
        }
        return result;
    }
    public AbelianGroupType tensorProduct(AbelianGroupType other) {
        List<BigInteger> result=commonFactors(other); copies(result,factors,other.freeRank); copies(result,other.factors,freeRank);
        return new AbelianGroupType(freeRank.multiply(other.freeRank),result);
    }
    /** Hom_Z(this, target), classified as an abelian group; individual homomorphisms are not enumerated. */
    public AbelianGroupType homGroup(AbelianGroupType target) {
        List<BigInteger> result=commonFactors(target); copies(result,target.factors,freeRank);
        return new AbelianGroupType(freeRank.multiply(target.freeRank),result);
    }
    public AbelianGroupType tor1(AbelianGroupType other) { return new AbelianGroupType(BigInteger.ZERO,commonFactors(other)); }
    /** Ext^1_Z(this, target); the first argument is contravariant. */
    public AbelianGroupType ext1(AbelianGroupType target) {
        List<BigInteger> result=commonFactors(target); copies(result,factors,target.freeRank);
        return new AbelianGroupType(BigInteger.ZERO,result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof AbelianGroupType)) return false; AbelianGroupType group=(AbelianGroupType)other;
        return freeRank.equals(group.freeRank) && factors.equals(group.factors);
    }
    @Override public int hashCode() { return Objects.hash(freeRank,factors); }
    @Override public String toString() { return "AbelianGroup(rank="+freeRank+", torsion="+factors+")"; }
}
