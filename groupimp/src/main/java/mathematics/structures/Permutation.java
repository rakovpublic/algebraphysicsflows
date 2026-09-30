package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A bijection of the zero-based labels 0,...,degree-1, represented by their images. */
public final class Permutation implements Serializable {
    private static final long serialVersionUID=1L;
    private final int[] images;
    public Permutation(int... images) {
        this.images=Objects.requireNonNull(images).clone();
        boolean[] seen=new boolean[images.length];
        for(int value : this.images) {
            if(value<0 || value>=images.length || seen[value]) throw MathFailure.invalid("Permutation images must bijectively cover 0..degree-1");
            seen[value]=true;
        }
    }
    public static Permutation identity(int degree) {
        if(degree<0) throw MathFailure.invalid("Negative permutation degree");
        int[] images=new int[degree]; for(int i=0;i<degree;i++) images[i]=i;
        return new Permutation(images);
    }
    public int degree() { return images.length; }
    public int image(int point) {
        if(point<0 || point>=degree()) throw MathFailure.undefined("Point is outside the permutation's finite action");
        return images[point];
    }
    public BigInteger image(BigInteger point) { return BigInteger.valueOf(image(pointIndex(point))); }
    private int pointIndex(BigInteger point) {
        if(point.signum()<0 || point.compareTo(BigInteger.valueOf(degree()))>=0)
            throw MathFailure.undefined("Point is outside the permutation's finite action");
        return point.intValueExact();
    }
    /** this(inner(i)): the right operand acts first. */
    public Permutation compose(Permutation inner) {
        if(degree()!=inner.degree()) throw MathFailure.invalid("Permutation degrees differ");
        int[] result=new int[degree()]; for(int i=0;i<result.length;i++) result[i]=images[inner.images[i]];
        return new Permutation(result);
    }
    public Permutation inverse() {
        int[] result=new int[degree()]; for(int i=0;i<result.length;i++) result[images[i]]=i;
        return new Permutation(result);
    }
    private List<List<Integer>> cycles() {
        List<List<Integer>> result=new ArrayList<>(); boolean[] seen=new boolean[degree()];
        for(int start=0;start<degree();start++) if(!seen[start]) {
            List<Integer> cycle=new ArrayList<>(); int current=start;
            do { cycle.add(current); seen[current]=true; current=images[current]; } while(current!=start);
            result.add(cycle);
        }
        return result;
    }
    public Permutation power(BigInteger exponent) {
        Objects.requireNonNull(exponent); int[] result=new int[degree()];
        for(List<Integer> cycle : cycles()) {
            int shift=exponent.mod(BigInteger.valueOf(cycle.size())).intValue();
            for(int i=0;i<cycle.size();i++) result[cycle.get(i)]=cycle.get((i+shift)%cycle.size());
        }
        return new Permutation(result);
    }
    public BigInteger order() {
        BigInteger result=BigInteger.ONE;
        for(List<Integer> cycle : cycles()) {
            BigInteger length=BigInteger.valueOf(cycle.size()); result=result.divide(result.gcd(length)).multiply(length);
        }
        return result;
    }
    public int sign() { int sign=1; for(List<Integer> cycle : cycles()) if(cycle.size()%2==0) sign=-sign; return sign; }
    public int fixedPointCount() { int count=0; for(int i=0;i<degree();i++) if(images[i]==i) count++; return count; }
    public List<BigInteger> orbit(BigInteger point) {
        int start=pointIndex(point),current=start; List<BigInteger> result=new ArrayList<>();
        do { result.add(BigInteger.valueOf(current)); current=images[current]; } while(current!=start);
        return Collections.unmodifiableList(result);
    }
    /** Nontrivial disjoint cycles, each represented as a permutation of the same full carrier. */
    public List<Permutation> disjointCycles() {
        List<Permutation> result=new ArrayList<>();
        for(List<Integer> cycle : cycles()) if(cycle.size()>1) {
            int[] factor=identity(degree()).images.clone();
            for(int point : cycle) factor[point]=images[point];
            result.add(new Permutation(factor));
        }
        return Collections.unmodifiableList(result);
    }
    /** Finite enumeration in lexicographic image order; resource cap is separate from the group definition. */
    public static List<Permutation> all(int degree) {
        if(degree<0) throw MathFailure.invalid("Negative permutation degree");
        if(degree>8) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Permutation enumeration is limited to degree 8");
        int[] current=identity(degree).images.clone(); List<Permutation> result=new ArrayList<>();
        do { result.add(new Permutation(current)); } while(next(current));
        return Collections.unmodifiableList(result);
    }
    private static boolean next(int[] values) {
        int pivot=values.length-2;
        while(pivot>=0 && values[pivot]>=values[pivot+1]) pivot--;
        if(pivot<0) return false;
        int next=values.length-1; while(values[next]<=values[pivot]) next--;
        int value=values[pivot]; values[pivot]=values[next]; values[next]=value;
        for(int left=pivot+1,right=values.length-1;left<right;left++,right--) {
            value=values[left]; values[left]=values[right]; values[right]=value;
        }
        return true;
    }
    @Override public boolean equals(Object other) { return other instanceof Permutation && Arrays.equals(images,((Permutation)other).images); }
    @Override public int hashCode() { return Arrays.hashCode(images); }
    @Override public String toString() { return "Perm"+Arrays.toString(images); }
}
