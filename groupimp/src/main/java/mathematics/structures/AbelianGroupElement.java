package mathematics.structures;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerVector;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An element with a retained presentation and canonical coordinates in that presentation's Smith basis. */
public final class AbelianGroupElement implements Serializable {
    private static final long serialVersionUID=1L;
    private final PresentedAbelianGroup group;
    private final IntegerVector coordinates;
    AbelianGroupElement(PresentedAbelianGroup group,IntegerVector coordinates) {
        this.group=Objects.requireNonNull(group); this.coordinates=group.normalizeSmith(coordinates);
    }
    public PresentedAbelianGroup group() { return group; }
    public IntegerVector smithCoordinates() { return coordinates; }
    public IntegerVector representative() { return group.lift(coordinates); }
    public AbelianGroupElement add(AbelianGroupElement other) {
        if(!group.equals(other.group)) throw MathFailure.undefined("Abelian element addition requires the same presentation and coordinate map");
        return group.fromSmith(coordinates.add(other.coordinates));
    }
    public AbelianGroupElement scale(BigInteger integer) { return group.fromSmith(coordinates.scale(integer)); }
    public boolean isZero() { for(BigInteger value : coordinates.entries()) if(value.signum()!=0) return false; return true; }
    public boolean isTorsion() {
        for(int i=0;i<coordinates.dimension();i++) if(group.modulus(i).signum()==0 && coordinates.get(i).signum()!=0) return false;
        return true;
    }
    public BigInteger order() {
        if(!isTorsion()) throw MathFailure.undefined("An element with nonzero free coordinates has infinite order");
        BigInteger result=BigInteger.ONE;
        for(int i=0;i<coordinates.dimension();i++) if(group.modulus(i).signum()!=0) {
            BigInteger order=group.modulus(i).divide(group.modulus(i).gcd(coordinates.get(i)));
            result=result.divide(result.gcd(order)).multiply(order);
        }
        return result;
    }
    public List<AbelianGroupElement> cyclicSubgroup() {
        int count=PresentedAbelianGroup.enumerationSize(order()); List<AbelianGroupElement> result=new ArrayList<>();
        AbelianGroupElement current=group.zero();
        for(int i=0;i<count;i++) { result.add(current); current=current.add(this); } return Collections.unmodifiableList(result);
    }
    /** All x with integer*x=this, in mixed-radix Smith-coordinate order, whenever the fiber is finite. */
    public List<AbelianGroupElement> multiplicationPreimages(BigInteger integer) {
        if(integer.signum()==0) return isZero()?group.elements():Collections.emptyList();
        int dimension=coordinates.dimension(); BigInteger[] base=new BigInteger[dimension],steps=new BigInteger[dimension],counts=new BigInteger[dimension];
        BigInteger cardinality=BigInteger.ONE;
        for(int i=0;i<dimension;i++) {
            BigInteger modulus=group.modulus(i),coordinate=coordinates.get(i);
            if(modulus.signum()==0) {
                BigInteger[] division=coordinate.divideAndRemainder(integer); if(division[1].signum()!=0) return Collections.emptyList();
                base[i]=division[0]; steps[i]=BigInteger.ZERO; counts[i]=BigInteger.ONE;
            } else {
                BigInteger gcd=integer.gcd(modulus); if(coordinate.mod(gcd).signum()!=0) return Collections.emptyList();
                counts[i]=gcd; steps[i]=modulus.divide(gcd);
                base[i]=steps[i].equals(BigInteger.ONE)?BigInteger.ZERO:
                        integer.divide(gcd).mod(steps[i]).modInverse(steps[i]).multiply(coordinate.divide(gcd)).mod(steps[i]);
                cardinality=cardinality.multiply(gcd);
            }
        }
        // Check solvability of every coordinate before imposing the output-size cap.
        int count=PresentedAbelianGroup.enumerationSize(cardinality); List<AbelianGroupElement> result=new ArrayList<>();
        for(int code=0;code<count;code++) {
            BigInteger remaining=BigInteger.valueOf(code); BigInteger[] values=new BigInteger[dimension];
            for(int i=0;i<dimension;i++) {
                BigInteger[] division=remaining.divideAndRemainder(counts[i]); remaining=division[0];
                values[i]=base[i].add(steps[i].multiply(division[1]));
            }
            result.add(group.fromSmith(new IntegerVector(values)));
        }
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof AbelianGroupElement)) return false; AbelianGroupElement element=(AbelianGroupElement)other;
        return group.equals(element.group) && coordinates.equals(element.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(group,coordinates); }
    @Override public String toString() { return "AbelianElement(group="+group+", smith="+coordinates+")"; }
}
