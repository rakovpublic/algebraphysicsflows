package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.util.*;

/**
 * Finite-support twisted skew group algebra H(Q)*_{sigma,tau} Z^N.
 * The lexicographic order is available for valuation queries. This class stores
 * finite supports; it does not pretend to store arbitrary Hahn series.
 */
public final class RationalTwistedLatticeAlgebra implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int rank;
    private final NavigableMap<Exponent,RationalQuaternion> terms;

    public static final class Exponent implements Comparable<Exponent>, Serializable {
        private static final long serialVersionUID = 1L;
        private final List<java.math.BigInteger> coordinates;
        public Exponent(long... coordinates) {
            List<java.math.BigInteger> copy=new ArrayList<>();
            for(long x:coordinates) copy.add(java.math.BigInteger.valueOf(x));
            this.coordinates=Collections.unmodifiableList(copy);
        }
        public Exponent(List<java.math.BigInteger> coordinates) {
            if(coordinates.isEmpty()) throw new IllegalArgumentException("Lattice rank must be positive");
            List<java.math.BigInteger> copy=new ArrayList<>();
            for(java.math.BigInteger x:coordinates) copy.add(Objects.requireNonNull(x));
            this.coordinates=Collections.unmodifiableList(copy);
        }
        public int rank(){return coordinates.size();}
        public java.math.BigInteger coordinate(int i){return coordinates.get(i);}
        public Exponent add(Exponent b){sameRank(b);List<java.math.BigInteger> c=new ArrayList<>();for(int i=0;i<rank();i++)c.add(coordinate(i).add(b.coordinate(i)));return new Exponent(c);}
        public Exponent doubleFirstCoordinate(){List<java.math.BigInteger> c=new ArrayList<>(coordinates);c.set(0,c.get(0).shiftLeft(1));return new Exponent(c);}
        public boolean firstCoordinateOdd(){return coordinate(0).testBit(0);}
        private void sameRank(Exponent b){if(rank()!=b.rank())throw new IllegalArgumentException("Lattice ranks differ");}
        @Override public int compareTo(Exponent b){sameRank(b);for(int i=0;i<rank();i++){int c=coordinate(i).compareTo(b.coordinate(i));if(c!=0)return c;}return 0;}
        @Override public boolean equals(Object o){return o instanceof Exponent&&coordinates.equals(((Exponent)o).coordinates);}
        @Override public int hashCode(){return coordinates.hashCode();}
        @Override public String toString(){return coordinates.toString();}
    }

    public RationalTwistedLatticeAlgebra(int rank, Map<Exponent,RationalQuaternion> input) {
        if(rank<1) throw new IllegalArgumentException("Lattice rank must be positive");
        this.rank=rank; TreeMap<Exponent,RationalQuaternion> copy=new TreeMap<>();
        for(Map.Entry<Exponent,RationalQuaternion> e:input.entrySet()){
            if(e.getKey().rank()!=rank)throw new IllegalArgumentException("Support exponent has wrong rank");
            put(copy,e.getKey(),Objects.requireNonNull(e.getValue()));
        }
        terms=Collections.unmodifiableNavigableMap(copy);
    }
    private static void put(Map<Exponent,RationalQuaternion> out,Exponent e,RationalQuaternion q){
        RationalQuaternion sum=out.containsKey(e)?out.get(e).add(q):q;
        if(sum.equals(RationalQuaternion.ZERO))out.remove(e);else out.put(e,sum);
    }
    public static RationalTwistedLatticeAlgebra zero(int rank){return new RationalTwistedLatticeAlgebra(rank,Collections.emptyMap());}
    public static RationalTwistedLatticeAlgebra basis(int rank,Exponent e,RationalQuaternion coefficient){return new RationalTwistedLatticeAlgebra(rank,Collections.singletonMap(e,coefficient));}
    public static RationalTwistedLatticeAlgebra fromQuaternion(int rank,RationalQuaternion q){return basis(rank,new Exponent(Collections.nCopies(rank,java.math.BigInteger.ZERO)),q);}
    public int rank(){return rank;}
    public NavigableMap<Exponent,RationalQuaternion> terms(){return terms;}
    public boolean isZero(){return terms.isEmpty();}
    public Exponent valuation(){if(isZero())throw MathFailure.undefined("Zero has no lattice valuation");return terms.firstKey();}
    public RationalQuaternion leadingCoefficient(){if(isZero())throw MathFailure.undefined("Zero has no leading coefficient");return terms.get(terms.firstKey());}
    public RationalTwistedLatticeAlgebra add(RationalTwistedLatticeAlgebra b){sameRank(b);TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>(terms);for(Map.Entry<Exponent,RationalQuaternion> e:b.terms.entrySet())put(out,e.getKey(),e.getValue());return new RationalTwistedLatticeAlgebra(rank,out);}
    public RationalTwistedLatticeAlgebra negate(){TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>();for(Map.Entry<Exponent,RationalQuaternion> e:terms.entrySet())out.put(e.getKey(),e.getValue().negate());return new RationalTwistedLatticeAlgebra(rank,out);}
    public RationalTwistedLatticeAlgebra subtract(RationalTwistedLatticeAlgebra b){return add(b.negate());}
    /** A normalized sign 2-cocycle on the last N-1 coordinates, with a nontrivial 1-2 interaction when rank >= 3. */
    public static int tau(Exponent g,Exponent h){
        if(g.rank()!=h.rank())throw new IllegalArgumentException("Lattice ranks differ");
        int parity=0;
        for(int i=1;i<g.rank();i++)for(int j=i+1;j<g.rank();j++)
            if(g.coordinate(i).testBit(0)&&h.coordinate(j).testBit(0))parity^=1;
        return parity==0?1:-1;
    }
    private static RationalQuaternion sigma(Exponent g,RationalQuaternion q){
        // sigma_g=Ad(i)^(g_1), an automorphism of H(Q). It is invariant under
        // the BSM map g_0 -> 2g_0, so that map remains an algebra endomorphism.
        return g.rank()>1&&g.coordinate(1).testBit(0)?new RationalQuaternion(q.real,q.i,q.j.negate(),q.k.negate()):q;
    }
    public RationalTwistedLatticeAlgebra multiply(RationalTwistedLatticeAlgebra b){
        sameRank(b);TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>();
        for(Map.Entry<Exponent,RationalQuaternion> x:terms.entrySet())for(Map.Entry<Exponent,RationalQuaternion> y:b.terms.entrySet()){
            Exponent exponent=x.getKey().add(y.getKey());RationalQuaternion coefficient=x.getValue().multiply(sigma(x.getKey(),y.getValue()));
            if(tau(x.getKey(),y.getKey())<0)coefficient=coefficient.negate();put(out,exponent,coefficient);
        }
        return new RationalTwistedLatticeAlgebra(rank,out);
    }
    /** BSM-style non-surjective mode: g_0 -> 2g_0. Odd first-coordinate residue classes map to the even subgroup. */
    public RationalTwistedLatticeAlgebra darkMatterEndomorphism(){
        TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>();
        for(Map.Entry<Exponent,RationalQuaternion> e:terms.entrySet())put(out,e.getKey().doubleFirstCoordinate(),e.getValue());
        return new RationalTwistedLatticeAlgebra(rank,out);
    }
    public boolean hasOddFirstCoordinateSupport(){for(Exponent e:terms.keySet())if(e.firstCoordinateOdd())return true;return false;}
    public RationalQuaternion toQuaternion(){
        if(terms.isEmpty())return RationalQuaternion.ZERO;
        if(terms.size()!=1||!terms.firstKey().equals(new Exponent(Collections.nCopies(rank,java.math.BigInteger.ZERO))))
            throw MathFailure.undefined("Lattice element is not supported only at the identity exponent");
        return terms.firstEntry().getValue();
    }
    public boolean toBoolean(){RationalQuaternion q=toQuaternion();if(q.equals(RationalQuaternion.ZERO))return false;if(q.equals(RationalQuaternion.ONE))return true;throw MathFailure.undefined("Only embedded zero and one map to Boolean");}
    private void sameRank(RationalTwistedLatticeAlgebra b){if(rank!=b.rank)throw new IllegalArgumentException("Lattice ranks differ");}
    @Override public boolean equals(Object o){return o instanceof RationalTwistedLatticeAlgebra&&rank==((RationalTwistedLatticeAlgebra)o).rank&&terms.equals(((RationalTwistedLatticeAlgebra)o).terms);}
    @Override public int hashCode(){return Objects.hash(rank,terms);}
    @Override public String toString(){return "H(Q)*_{sigma,tau}Z^"+rank+terms;}
}
