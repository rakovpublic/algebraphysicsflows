package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.linear.IntegerSmithNormalForm.Decomposition;
import java.math.BigInteger;
import java.util.*;

/** Solve for an upper-triangular retained cone inverse G and unrestricted witnesses GF ~ id, FG ~ id. */
public final class IntegralConeInverseSolver {
    public static final int MAX_EQUATIONS=IntegerSmithNormalForm.MAX_DIMENSION;
    public static final int MAX_UNKNOWNS=IntegerSmithNormalForm.MAX_DIMENSION;
    private IntegralConeInverseSolver() {}

    private static final class System {
        final IntegralChainConeMap map;
        final Computation work;
        final int degrees,rows,columns;
        final int[] s,t,sourceTargetRanks,targetTargetRanks,h,k;
        final int[][][] g;
        final IntegerMatrix coefficients;
        final IntegerVector rhs;
        System(IntegralChainConeMap map,Computation work) {
            this.map=Objects.requireNonNull(map); this.work=work; degrees=Math.max(map.source().dimension(),map.target().dimension())+1;
            s=new int[degrees+1]; t=new int[degrees+1]; sourceTargetRanks=new int[degrees+1]; targetTargetRanks=new int[degrees+1];
            g=new int[degrees][][]; h=new int[degrees]; k=new int[degrees];
            int equations=0,unknowns=0;
            for(int d=0;d<degrees;d++) {
                BigInteger degree=BigInteger.valueOf(d); s[d]=map.source().rank(degree); t[d]=map.target().rank(degree);
                sourceTargetRanks[d]=map.source().target().basis(degree).size(); targetTargetRanks[d]=map.target().target().basis(degree).size();
                equations+=s[d]*s[d]+t[d]*t[d]+(d==0?0:s[d-1]*t[d]);
                if(equations>MAX_EQUATIONS) throw limit("equations",MAX_EQUATIONS);
            }
            // G : target cone -> source cone must have zero lower-left blocks.
            // Its diagonal blocks are the vertical maps; its upper-right block is minus the square witness.
            for(int d=0;d<degrees;d++) {
                work.use((long)s[d]*t[d]); g[d]=new int[s[d]][t[d]];
                for(int r=0;r<s[d];r++) for(int c=0;c<t[d];c++)
                    g[d][r][c]=(r<sourceTargetRanks[d] || c>=targetTargetRanks[d])?unknowns++:-1;
            }
            for(int d=0;d<degrees;d++) { h[d]=unknowns; unknowns+=s[d+1]*s[d]; }
            for(int d=0;d<degrees;d++) { k[d]=unknowns; unknowns+=t[d+1]*t[d]; }
            if(unknowns>MAX_UNKNOWNS) throw limit("unknown coefficients",MAX_UNKNOWNS);
            rows=equations; columns=unknowns; work.use((long)rows*columns+rows);
            BigInteger[][] entries=new BigInteger[rows][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
            BigInteger[] values=new BigInteger[rows]; Arrays.fill(values,BigInteger.ZERO); int row=0;
            // d_source G_d - G_(d-1) d_target = 0.
            for(int d=1;d<degrees;d++) {
                IntegerMatrix ds=map.source().boundary(BigInteger.valueOf(d),work),dt=map.target().boundary(BigInteger.valueOf(d),work);
                for(int r=0;r<s[d-1];r++) for(int c=0;c<t[d];c++) {
                    work.use((long)s[d]+t[d-1]);
                    for(int a=0;a<s[d];a++) put(entries,row,g[d][a][c],ds.get(r,a));
                    for(int b=0;b<t[d-1];b++) put(entries,row,g[d-1][r][b],dt.get(b,c).negate());
                    row++;
                }
            }
            row=identityEquations(entries,values,row,true);
            identityEquations(entries,values,row,false);
            coefficients=new IntegerMatrix(rows,columns,entries); rhs=new IntegerVector(values);
        }
        private int identityEquations(BigInteger[][] entries,BigInteger[] values,int row,boolean source) {
            int[] ranks=source?s:t,offsets=source?h:k;
            IntegralChainMappingCone pair=source?map.source():map.target();
            for(int d=0;d<degrees;d++) {
                IntegerMatrix next=pair.boundary(BigInteger.valueOf(d+1),work),boundary=pair.boundary(BigInteger.valueOf(d),work),f=map.matrix(BigInteger.valueOf(d),work);
                for(int r=0;r<ranks[d];r++) for(int c=0;c<ranks[d];c++) {
                    values[row]=r==c?BigInteger.ONE:BigInteger.ZERO;
                    int middle=source?t[d]:s[d]; work.use((long)middle+ranks[d+1]+(d==0?0:ranks[d-1]));
                    // GF + dH + Hd = id_source, or FG + dK + Kd = id_target.
                    for(int a=0;a<middle;a++) put(entries,row,source?g[d][r][a]:g[d][a][c],source?f.get(a,c):f.get(r,a));
                    for(int a=0;a<ranks[d+1];a++) entries[row][offsets[d]+a*ranks[d]+c]=next.get(r,a);
                    if(d>0) for(int b=0;b<ranks[d-1];b++) entries[row][offsets[d-1]+r*ranks[d-1]+b]=boundary.get(b,c);
                    row++;
                }
            }
            return row;
        }
        IntegerVector solve() {
            Decomposition smith=work.decompose(coefficients); IntegerVector transformed=work.apply(smith.left(),rhs);
            work.use((long)columns+rows); BigInteger[] coordinates=new BigInteger[columns]; Arrays.fill(coordinates,BigInteger.ZERO);
            for(int i=0;i<rows;i++) {
                if(i<smith.rank()) {
                    BigInteger[] quotient=transformed.get(i).divideAndRemainder(smith.diagonal().get(i,i));
                    if(quotient[1].signum()!=0) return null; coordinates[i]=quotient[0];
                } else if(transformed.get(i).signum()!=0) return null;
            }
            return work.apply(smith.right(),new IntegerVector(coordinates));
        }
        IntegerVector requireSolution() {
            IntegerVector result=solve(); if(result==null) throw MathFailure.undefined("This cone map has no inverse in the retained-square carrier up to integral cone homotopy"); return result;
        }
        private IntegerMatrix decode(IntegerVector vector,int offset,int rows,int columns) {
            work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns];
            for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) entries[r][c]=vector.get(offset+r*columns+c);
            return new IntegerMatrix(rows,columns,entries);
        }
        IntegralChainConeMap inverse(IntegerVector vector) {
            return IntegralChainConeMap.fromBlocks(map.target(),map.source(),(degree,row,column) -> {
                int coordinate=g[degree][row][column]; return coordinate<0?BigInteger.ZERO:vector.get(coordinate);
            },work);
        }
        IntegralConeHomotopy witness(IntegerVector vector,IntegralChainConeMap composite,boolean source) {
            int[] ranks=source?s:t,offsets=source?h:k; List<IntegerMatrix> matrices=new ArrayList<>();
            for(int d=0;d<=composite.source().dimension();d++) matrices.add(decode(vector,offsets[d],ranks[d+1],ranks[d]));
            IntegralChainConeMap identity=IntegralChainConeMap.identity(composite.source(),work);
            return new IntegralConeHomotopy(new IntegralConeHomotopy.Data(composite,identity,matrices),work);
        }
    }
    /** Every equation is linear in the allowed inverse coordinates and the two unrestricted witnesses. */
    private static void put(BigInteger[][] entries,int row,int coordinate,BigInteger value) {
        if(coordinate>=0) entries[row][coordinate]=entries[row][coordinate].add(value);
    }
    private static MathFailure limit(String what,int maximum) {
        return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Retained cone inverse solving allows at most "+maximum+" total "+what);
    }
    public static boolean hasRetainedInverse(IntegralChainConeMap map) { return new System(map,new Computation()).solve()!=null; }
    public static IntegralChainConeMap inverse(IntegralChainConeMap map) {
        System system=new System(map,new Computation()); return system.inverse(system.requireSolution());
    }
    /** Exactly [GF -> id_source, FG -> id_target] for the same solved retained G, or undefined. */
    public static List<IntegralConeHomotopy> inverseHomotopies(IntegralChainConeMap map) {
        Computation work=new Computation(); System system=new System(map,work); IntegerVector solution=system.requireSolution();
        IntegralChainConeMap inverse=system.inverse(solution);
        return Collections.unmodifiableList(Arrays.asList(system.witness(solution,inverse.compose(map,work),true),system.witness(solution,map.compose(inverse,work),false)));
    }
    static IntegralConeEquivalence equivalence(IntegralChainConeMap map,Computation work) {
        System system=new System(map,work); IntegerVector solution=system.requireSolution(); IntegralChainConeMap inverse=system.inverse(solution);
        IntegralConeHomotopy source=system.witness(solution,inverse.compose(map,work),true),target=system.witness(solution,map.compose(inverse,work),false);
        return new IntegralConeEquivalence(new IntegralConeEquivalence.Data(map,inverse,source,target),work);
    }
}
