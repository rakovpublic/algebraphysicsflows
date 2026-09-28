package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.linear.IntegerSmithNormalForm.Decomposition;
import java.math.BigInteger;
import java.util.*;

/** Solve the simultaneous integral equations dH + Hd = to - from, not just induced-map equality. */
public final class SimplicialChainHomotopySolver {
    public static final int MAX_EQUATIONS=IntegerSmithNormalForm.MAX_DIMENSION;
    public static final int MAX_UNKNOWNS=IntegerSmithNormalForm.MAX_DIMENSION;
    private SimplicialChainHomotopySolver() {}

    private static final class LinearSystem {
        final SimplicialChainMap from,to;
        final Computation work=new Computation();
        final int degrees,equations,unknowns;
        final int[] sourceRanks,targetRanks,offsets;
        final IntegerMatrix coefficients;
        final IntegerVector rhs;
        LinearSystem(SimplicialChainMap from,SimplicialChainMap to) {
            this.from=Objects.requireNonNull(from); this.to=Objects.requireNonNull(to);
            if(!from.source().equals(to.source()) || !from.target().equals(to.target()))
                throw MathFailure.undefined("Homotopy solving requires identical full labelled source and target pairs");
            degrees=from.chainMatrices().size(); sourceRanks=new int[degrees+1]; targetRanks=new int[degrees+1]; offsets=new int[degrees+1];
            int rows=0,columns=0;
            for(int k=0;k<degrees;k++) {
                IntegerMatrix f=from.chainMatrices().get(k); sourceRanks[k]=f.columns(); targetRanks[k]=f.rows();
                rows+=f.rows()*f.columns();
                if(rows>MAX_EQUATIONS) throw limit("equations",MAX_EQUATIONS);
            }
            for(int k=0;k<degrees;k++) {
                offsets[k]=columns; columns+=targetRanks[k+1]*sourceRanks[k];
                if(columns>MAX_UNKNOWNS) throw limit("unknown coefficients",MAX_UNKNOWNS);
            }
            offsets[degrees]=columns; equations=rows; unknowns=columns;
            work.use((long)rows*columns+rows); BigInteger[][] entries=new BigInteger[rows][columns];
            for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
            BigInteger[] values=new BigInteger[rows]; int equation=0;
            for(int k=0;k<degrees;k++) {
                BigInteger degree=BigInteger.valueOf(k);
                IntegerMatrix targetBoundary=from.target().boundaryMatrix(degree.add(BigInteger.ONE),work),
                        sourceBoundary=from.source().boundaryMatrix(degree,work),f=from.chainMatrices().get(k),g=to.chainMatrices().get(k);
                for(int r=0;r<targetRanks[k];r++) for(int c=0;c<sourceRanks[k];c++) {
                    work.use(1L+targetRanks[k+1]+(k==0?0:sourceRanks[k-1])); values[equation]=g.get(r,c).subtract(f.get(r,c));
                    // (d_target H_k)[r,c] = sum_a d_target[r,a] H_k[a,c].
                    for(int a=0;a<targetRanks[k+1];a++) entries[equation][offsets[k]+a*sourceRanks[k]+c]=targetBoundary.get(r,a);
                    // (H_(k-1) d_source)[r,c] = sum_b H_(k-1)[r,b] d_source[b,c].
                    if(k>0) for(int b=0;b<sourceRanks[k-1];b++) entries[equation][offsets[k-1]+r*sourceRanks[k-1]+b]=sourceBoundary.get(b,c);
                    equation++;
                }
            }
            coefficients=new IntegerMatrix(rows,columns,entries); rhs=new IntegerVector(values);
        }
        private static MathFailure limit(String what,int maximum) {
            return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Integral homotopy solving allows at most "+maximum+" total "+what);
        }
        /** Smith coordinates with all free coordinates zero, or null for an integral obstruction. */
        IntegerVector coordinates(Decomposition smith) {
            IntegerVector transformed=work.apply(smith.left(),rhs); work.use((long)unknowns+equations);
            BigInteger[] result=new BigInteger[unknowns]; Arrays.fill(result,BigInteger.ZERO);
            for(int i=0;i<equations;i++) {
                if(i<smith.rank()) {
                    BigInteger[] qr=transformed.get(i).divideAndRemainder(smith.diagonal().get(i,i));
                    if(qr[1].signum()!=0) return null; result[i]=qr[0];
                } else if(transformed.get(i).signum()!=0) return null;
            }
            return new IntegerVector(result);
        }
        SimplicialChainHomotopy witness(IntegerVector values,SimplicialChainMap start,SimplicialChainMap end) {
            List<IntegerMatrix> matrices=new ArrayList<>(); work.use(unknowns);
            for(int k=0;k<degrees;k++) {
                BigInteger[][] entries=new BigInteger[targetRanks[k+1]][sourceRanks[k]];
                for(int r=0;r<targetRanks[k+1];r++) for(int c=0;c<sourceRanks[k];c++) entries[r][c]=values.get(offsets[k]+r*sourceRanks[k]+c);
                matrices.add(new IntegerMatrix(targetRanks[k+1],sourceRanks[k],entries));
            }
            return new SimplicialChainHomotopy(new SimplicialChainHomotopy.Data(start,end,matrices),work);
        }
    }
    public static boolean areHomotopic(SimplicialChainMap from,SimplicialChainMap to) {
        LinearSystem system=new LinearSystem(from,to); return system.coordinates(system.work.decompose(system.coefficients))!=null;
    }
    public static SimplicialChainHomotopy between(SimplicialChainMap from,SimplicialChainMap to) {
        LinearSystem system=new LinearSystem(from,to); Decomposition smith=system.work.decompose(system.coefficients); IntegerVector coordinates=system.coordinates(smith);
        if(coordinates==null) throw MathFailure.undefined("These chain maps admit no integral chain homotopy");
        return system.witness(system.work.apply(smith.right(),coordinates),from,to);
    }
    /** [one particular witness, a full integral basis of zero-to-zero homogeneous witnesses]. */
    public static List<SimplicialChainHomotopy> solutionGenerators(SimplicialChainMap from,SimplicialChainMap to) {
        LinearSystem system=new LinearSystem(from,to); Decomposition smith=system.work.decompose(system.coefficients); IntegerVector coordinates=system.coordinates(smith);
        if(coordinates==null) return Collections.emptyList();
        List<SimplicialChainHomotopy> result=new ArrayList<>(); result.add(system.witness(system.work.apply(smith.right(),coordinates),from,to));
        if(smith.rank()<system.unknowns) {
            SimplicialChainMap zero=SimplicialChainMap.zero(from.source(),from.target(),system.work);
            for(int c=smith.rank();c<system.unknowns;c++) {
                system.work.use(system.unknowns); result.add(system.witness(smith.right().column(c),zero,zero));
            }
        }
        return Collections.unmodifiableList(result);
    }
}
