package mathematics.numbers;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerVector;
import mathematics.linear.RationalVector;
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
    private final int[][] cocycle;
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
        this(rank,defaultCocycle(rank),input);
    }
    public RationalTwistedLatticeAlgebra(int rank,int[][] cocycleMatrix,Map<Exponent,RationalQuaternion> input) {
        if(rank<1) throw new IllegalArgumentException("Lattice rank must be positive");
        this.rank=rank;this.cocycle=validateCocycle(rank,cocycleMatrix);TreeMap<Exponent,RationalQuaternion> copy=new TreeMap<>();
        for(Map.Entry<Exponent,RationalQuaternion> e:input.entrySet()){
            if(e.getKey().rank()!=rank)throw new IllegalArgumentException("Support exponent has wrong rank");
            put(copy,e.getKey(),Objects.requireNonNull(e.getValue()));
        }
        terms=Collections.unmodifiableNavigableMap(copy);
    }
    private static int[][] defaultCocycle(int rank){
        if(rank<1)throw new IllegalArgumentException("Lattice rank must be positive");int[][] m=new int[rank][rank];if(rank>2){m[1][2]=1;m[2][1]=1;}return m;
    }
    private static int[][] validateCocycle(int rank,int[][] input){
        if(input==null||input.length!=rank)throw new IllegalArgumentException("Cocycle matrix must be rank by rank");int[][] out=new int[rank][rank];
        for(int i=0;i<rank;i++){
            if(input[i]==null||input[i].length!=rank)throw new IllegalArgumentException("Cocycle matrix must be rank by rank");
            for(int j=0;j<rank;j++){
                if((input[i][j]&~1)!=0)throw new IllegalArgumentException("Cocycle entries must be 0 or 1 in F_2");
                if(i==j&&input[i][j]!=0)throw new IllegalArgumentException("Alternating cocycle matrix must have zero diagonal");
                out[i][j]=input[i][j];
            }
        }
        for(int i=0;i<rank;i++)for(int j=0;j<rank;j++)if(out[i][j]!=out[j][i])
            throw new IllegalArgumentException("Sign cocycle matrix must be antisymmetric over F_2 (symmetric entries with zero diagonal)");
        return out;
    }
    private static void put(Map<Exponent,RationalQuaternion> out,Exponent e,RationalQuaternion q){
        RationalQuaternion sum=out.containsKey(e)?out.get(e).add(q):q;
        if(sum.equals(RationalQuaternion.ZERO))out.remove(e);else out.put(e,sum);
    }
    public static RationalTwistedLatticeAlgebra zero(int rank){return new RationalTwistedLatticeAlgebra(rank,Collections.emptyMap());}
    public static RationalTwistedLatticeAlgebra zero(int rank,int[][] cocycle){return new RationalTwistedLatticeAlgebra(rank,cocycle,Collections.emptyMap());}
    public static RationalTwistedLatticeAlgebra basis(int rank,Exponent e,RationalQuaternion coefficient){return new RationalTwistedLatticeAlgebra(rank,Collections.singletonMap(e,coefficient));}
    public static RationalTwistedLatticeAlgebra basis(int rank,int[][] cocycle,Exponent e,RationalQuaternion coefficient){return new RationalTwistedLatticeAlgebra(rank,cocycle,Collections.singletonMap(e,coefficient));}
    public static RationalTwistedLatticeAlgebra fromQuaternion(int rank,RationalQuaternion q){return basis(rank,new Exponent(Collections.nCopies(rank,java.math.BigInteger.ZERO)),q);}
    public static RationalTwistedLatticeAlgebra fromQuaternion(int rank,int[][] cocycle,RationalQuaternion q){return basis(rank,cocycle,new Exponent(Collections.nCopies(rank,java.math.BigInteger.ZERO)),q);}
    public static RationalTwistedLatticeAlgebra groupBasis(IntegerVector exponent){
        if(exponent.dimension()<1)throw MathFailure.undefined("A lattice exponent must have positive runtime rank");
        return basis(exponent.dimension(),new Exponent(exponent.entries()),RationalQuaternion.ONE);
    }
    public int rank(){return rank;}
    public int[][] cocycleMatrix(){int[][] result=new int[rank][rank];for(int i=0;i<rank;i++)result[i]=cocycle[i].clone();return result;}
    public NavigableMap<Exponent,RationalQuaternion> terms(){return terms;}
    public boolean isZero(){return terms.isEmpty();}
    public Exponent valuation(){if(isZero())throw MathFailure.undefined("Zero has no lattice valuation");return terms.firstKey();}
    public RationalQuaternion leadingCoefficient(){if(isZero())throw MathFailure.undefined("Zero has no leading coefficient");return terms.get(terms.firstKey());}
    /**
     * Additive finite-quotient projection pi_(5->4) from the first five exponent parities
     * and H(Q) coefficient coordinates onto the 128 rational coordinates of DixonZ2(Q).
     * It uses packedIndex=4*parityClass+quaternionComponent in Dixon's
     * [Re(c0), Im(c0), ..., Re(c63), Im(c63)] rational-vector ordering.
     * The map is surjective for rank >= 5, but intentionally is not an algebra homomorphism:
     * an associative bulk algebra cannot map multiplicatively onto a non-associative algebra.
     * Coordinates beyond the first five do not affect this boundary projection.
     */
    public RationalDixonZ2 foldParityClassesToDixonBoundary(){
        if(rank<5)throw MathFailure.undefined("The Dixon boundary fold requires at least five lattice coordinates");
        Rational[] coordinates=new Rational[128];Arrays.fill(coordinates,Rational.ZERO);
        for(Map.Entry<Exponent,RationalQuaternion> term:terms.entrySet()){
            int parityClass=0;
            for(int axis=0;axis<5;axis++)if(term.getKey().coordinate(axis).testBit(0))parityClass|=1<<axis;
            RationalQuaternion q=term.getValue();Rational[] components={q.real,q.i,q.j,q.k};
            for(int component=0;component<4;component++){
                int coordinate=parityClass*4+component;
                coordinates[coordinate]=coordinates[coordinate].add(components[component]);
            }
        }
        return RationalDixonZ2.fromVector(new RationalVector(coordinates));
    }
    public RationalTwistedLatticeAlgebra add(RationalTwistedLatticeAlgebra b){sameRank(b);TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>(terms);for(Map.Entry<Exponent,RationalQuaternion> e:b.terms.entrySet())put(out,e.getKey(),e.getValue());return new RationalTwistedLatticeAlgebra(rank,cocycle,out);}
    public RationalTwistedLatticeAlgebra negate(){TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>();for(Map.Entry<Exponent,RationalQuaternion> e:terms.entrySet())out.put(e.getKey(),e.getValue().negate());return new RationalTwistedLatticeAlgebra(rank,cocycle,out);}
    public RationalTwistedLatticeAlgebra subtract(RationalTwistedLatticeAlgebra b){return add(b.negate());}
    /** A normalized bilinear sign 2-cocycle induced by an alternating F2 matrix. */
    public static int tau(Exponent g,Exponent h){
        return tau(g,h,defaultCocycle(g.rank()));
    }
    public static int tau(Exponent g,Exponent h,int[][] signMatrix){
        if(g.rank()!=h.rank())throw new IllegalArgumentException("Lattice ranks differ");
        int[][] matrix=validateCocycle(g.rank(),signMatrix);
        int parity=0;
        for(int i=0;i<g.rank();i++)for(int j=i+1;j<g.rank();j++)
            if(matrix[i][j]!=0&&g.coordinate(i).testBit(0)&&h.coordinate(j).testBit(0))parity^=1;
        return parity==0?1:-1;
    }
    private static RationalQuaternion sigma(Exponent g,RationalQuaternion q){
        // sigma_g=Ad(i)^(g[1]), an automorphism of H(Q). It is invariant under
        // the BSM map g[0] -> 2g[0], so that map remains an algebra endomorphism.
        return g.rank()>1&&g.coordinate(1).testBit(0)?new RationalQuaternion(q.real,q.i,q.j.negate(),q.k.negate()):q;
    }
    public RationalTwistedLatticeAlgebra multiply(RationalTwistedLatticeAlgebra b){
        sameRank(b);TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>();
        for(Map.Entry<Exponent,RationalQuaternion> x:terms.entrySet())for(Map.Entry<Exponent,RationalQuaternion> y:b.terms.entrySet()){
            Exponent exponent=x.getKey().add(y.getKey());RationalQuaternion coefficient=x.getValue().multiply(sigma(x.getKey(),y.getValue()));
            if(tau(x.getKey(),y.getKey(),cocycle)<0)coefficient=coefficient.negate();put(out,exponent,coefficient);
        }
        return new RationalTwistedLatticeAlgebra(rank,cocycle,out);
    }
    /** BSM-style non-surjective mode: g_0 -> 2g_0. Odd first-coordinate residue classes map to the even subgroup. */
    public RationalTwistedLatticeAlgebra darkMatterEndomorphism(){
        for(int j=0;j<rank;j++)if(cocycle[0][j]!=0)
            throw MathFailure.undefined("The first-coordinate doubling map preserves this algebra only when the cocycle is trivial on coordinate 0");
        TreeMap<Exponent,RationalQuaternion> out=new TreeMap<>();
        for(Map.Entry<Exponent,RationalQuaternion> e:terms.entrySet())put(out,e.getKey().doubleFirstCoordinate(),e.getValue());
        return new RationalTwistedLatticeAlgebra(rank,cocycle,out);
    }
    public boolean hasOddFirstCoordinateSupport(){for(Exponent e:terms.keySet())if(e.firstCoordinateOdd())return true;return false;}
    public RationalQuaternion toQuaternion(){
        if(terms.isEmpty())return RationalQuaternion.ZERO;
        if(terms.size()!=1||!terms.firstKey().equals(new Exponent(Collections.nCopies(rank,java.math.BigInteger.ZERO))))
            throw MathFailure.undefined("Lattice element is not supported only at the identity exponent");
        return terms.firstEntry().getValue();
    }
    public boolean toBoolean(){RationalQuaternion q=toQuaternion();if(q.equals(RationalQuaternion.ZERO))return false;if(q.equals(RationalQuaternion.ONE))return true;throw MathFailure.undefined("Only embedded zero and one map to Boolean");}
    private void sameRank(RationalTwistedLatticeAlgebra b){if(rank!=b.rank)throw new IllegalArgumentException("Lattice ranks differ");if(!Arrays.deepEquals(cocycle,b.cocycle))throw new IllegalArgumentException("Lattice sign cocycles differ");}
    @Override public boolean equals(Object o){return o instanceof RationalTwistedLatticeAlgebra&&rank==((RationalTwistedLatticeAlgebra)o).rank&&Arrays.deepEquals(cocycle,((RationalTwistedLatticeAlgebra)o).cocycle)&&terms.equals(((RationalTwistedLatticeAlgebra)o).terms);}
    @Override public int hashCode(){return Objects.hash(rank,Arrays.deepHashCode(cocycle),terms);}
    @Override public String toString(){return "H(Q)*_{sigma,tau}Z^"+rank+terms;}
}
