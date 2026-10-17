package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.linear.IntegerSmithNormalForm.Decomposition;
import java.math.BigInteger;
import java.util.*;

/** Solve simultaneously for an integral inverse G and witnesses GF ~ id, FG ~ id. */
public final class SimplicialChainInverseSolver {
    public static final int MAX_EQUATIONS=IntegerSmithNormalForm.MAX_DIMENSION;
    public static final int MAX_UNKNOWNS=IntegerSmithNormalForm.MAX_DIMENSION;
    private SimplicialChainInverseSolver() {}

    private static final class System {
        final SimplicialChainMap map;
        final Computation work;
        final int degrees,rows,columns;
        final int[] s,t,g,h,k;
        final IntegerMatrix coefficients;
        final IntegerVector rhs;
        System(SimplicialChainMap map,Computation work) {
            this.map=Objects.requireNonNull(map); this.work=work; degrees=map.chainMatrices().size();
            s=new int[degrees+1]; t=new int[degrees+1]; g=new int[degrees]; h=new int[degrees]; k=new int[degrees];
            int equations=0,unknowns=0;
            for(int d=0;d<degrees;d++) {
                s[d]=map.chainMatrices().get(d).columns(); t[d]=map.chainMatrices().get(d).rows();
                equations+=s[d]*s[d]+t[d]*t[d]+(d==0?0:s[d-1]*t[d]);
                if(equations>MAX_EQUATIONS) throw limit("equations",MAX_EQUATIONS);
            }
            for(int d=0;d<degrees;d++) { g[d]=unknowns; unknowns+=s[d]*t[d]; }
            for(int d=0;d<degrees;d++) { h[d]=unknowns; unknowns+=s[d+1]*s[d]; }
            for(int d=0;d<degrees;d++) { k[d]=unknowns; unknowns+=t[d+1]*t[d]; }
            if(unknowns>MAX_UNKNOWNS) throw limit("unknown coefficients",MAX_UNKNOWNS);
            rows=equations; columns=unknowns; work.use((long)rows*columns+rows);
            BigInteger[][] entries=new BigInteger[rows][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
            BigInteger[] values=new BigInteger[rows]; Arrays.fill(values,BigInteger.ZERO); int row=0;
            // d_source G_d - G_(d-1) d_target = 0.
            for(int d=1;d<degrees;d++) {
                IntegerMatrix ds=map.source().boundaryMatrix(BigInteger.valueOf(d),work),dt=map.target().boundaryMatrix(BigInteger.valueOf(d),work);
                for(int r=0;r<s[d-1];r++) for(int c=0;c<t[d];c++) {
                    work.use((long)s[d]+t[d-1]);
                    for(int a=0;a<s[d];a++) entries[row][g[d]+a*t[d]+c]=ds.get(r,a);
                    for(int b=0;b<t[d-1];b++) entries[row][g[d-1]+r*t[d-1]+b]=dt.get(b,c).negate();
                    row++;
                }
            }
            row=identityEquations(entries,values,row,true);
            identityEquations(entries,values,row,false);
            coefficients=new IntegerMatrix(rows,columns,entries); rhs=new IntegerVector(values);
        }
        private int identityEquations(BigInteger[][] entries,BigInteger[] values,int row,boolean source) {
            int[] ranks=source?s:t,offsets=source?h:k;
            RelativeSimplicialComplex pair=source?map.source():map.target();
            for(int d=0;d<degrees;d++) {
                IntegerMatrix next=pair.boundaryMatrix(BigInteger.valueOf(d+1),work),boundary=pair.boundaryMatrix(BigInteger.valueOf(d),work),f=map.chainMatrices().get(d);
                for(int r=0;r<ranks[d];r++) for(int c=0;c<ranks[d];c++) {
                    values[row]=r==c?BigInteger.ONE:BigInteger.ZERO;
                    int middle=source?t[d]:s[d]; work.use((long)middle+ranks[d+1]+(d==0?0:ranks[d-1]));
                    // GF + dH + Hd = id_source, or FG + dK + Kd = id_target.
                    for(int a=0;a<middle;a++) entries[row][g[d]+(source?r*t[d]+a:a*t[d]+c)]=source?f.get(a,c):f.get(r,a);
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
            IntegerVector result=solve(); if(result==null) throw MathFailure.undefined("This chain map has no integral inverse up to chain homotopy"); return result;
        }
        private IntegerMatrix decode(IntegerVector vector,int offset,int rows,int columns) {
            work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns];
            for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) entries[r][c]=vector.get(offset+r*columns+c);
            return new IntegerMatrix(rows,columns,entries);
        }
        SimplicialChainMap inverse(IntegerVector vector) {
            List<IntegerMatrix> matrices=new ArrayList<>();
            for(int d=0;d<degrees;d++) matrices.add(decode(vector,g[d],s[d],t[d]));
            return new SimplicialChainMap(new SimplicialChainMap.Data(map.target(),map.source(),matrices),work);
        }
        SimplicialChainHomotopy witness(IntegerVector vector,SimplicialChainMap composite,boolean source) {
            int[] ranks=source?s:t,offsets=source?h:k; List<IntegerMatrix> matrices=new ArrayList<>();
            for(int d=0;d<composite.chainMatrices().size();d++) matrices.add(decode(vector,offsets[d],ranks[d+1],ranks[d]));
            SimplicialChainMap identity=SimplicialChainMap.identity(composite.source(),work);
            return new SimplicialChainHomotopy(new SimplicialChainHomotopy.Data(composite,identity,matrices),work);
        }
    }
    private static MathFailure limit(String what,int maximum) {
        return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Integral inverse solving allows at most "+maximum+" total "+what);
    }
    public static boolean isHomotopyEquivalence(SimplicialChainMap map) { return new System(map,new Computation()).solve()!=null; }
    public static SimplicialChainMap inverse(SimplicialChainMap map) { return inverse(map,new Computation()); }
    static SimplicialChainMap inverse(SimplicialChainMap map,Computation work) {
        System system=new System(map,work); return system.inverse(system.requireSolution());
    }
    /** Exactly [GF -> id_source, FG -> id_target] for the same solved G, or undefined. */
    public static List<SimplicialChainHomotopy> inverseHomotopies(SimplicialChainMap map) {
        Computation work=new Computation(); System system=new System(map,work); IntegerVector solution=system.requireSolution();
        SimplicialChainMap inverse=system.inverse(solution);
        return Collections.unmodifiableList(Arrays.asList(system.witness(solution,inverse.compose(map,work),true),system.witness(solution,map.compose(inverse,work),false)));
    }
    static SimplicialChainEquivalence equivalence(SimplicialChainMap map,Computation work) {
        System system=new System(map,work); IntegerVector solution=system.requireSolution(); SimplicialChainMap inverse=system.inverse(solution);
        SimplicialChainHomotopy source=system.witness(solution,inverse.compose(map,work),true),target=system.witness(solution,map.compose(inverse,work),false);
        return new SimplicialChainEquivalence(new SimplicialChainEquivalence.Data(map,inverse,source,target),work);
    }
}
