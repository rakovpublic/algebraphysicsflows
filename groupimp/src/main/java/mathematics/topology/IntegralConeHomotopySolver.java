package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.linear.IntegerSmithNormalForm.Decomposition;
import java.math.BigInteger;
import java.util.*;

/** Bounded integral solving of all cone homotopy equations, including cross-component witnesses. */
public final class IntegralConeHomotopySolver {
    public static final int MAX_EQUATIONS=IntegerSmithNormalForm.MAX_DIMENSION;
    public static final int MAX_UNKNOWNS=IntegerSmithNormalForm.MAX_DIMENSION;
    private IntegralConeHomotopySolver() {}

    private static final class LinearSystem {
        final IntegralChainConeMap from,to;
        final Computation work;
        final int degrees,equations,unknowns;
        final int[] sourceRanks,targetRanks,offsets;
        final IntegerMatrix coefficients;
        final IntegerVector rhs;
        LinearSystem(IntegralChainConeMap from,IntegralChainConeMap to,Computation work) {
            this.from=Objects.requireNonNull(from); this.to=Objects.requireNonNull(to); this.work=work;
            if(!from.source().equals(to.source()) || !from.target().equals(to.target()))
                throw MathFailure.undefined("Cone homotopy solving requires identical full source and target defining maps");
            degrees=Math.max(from.source().dimension(),from.target().dimension())+1;
            sourceRanks=new int[degrees+1]; targetRanks=new int[degrees+1]; offsets=new int[degrees+1];
            int rows=0,columns=0;
            for(int k=0;k<degrees;k++) {
                BigInteger degree=BigInteger.valueOf(k); sourceRanks[k]=from.source().rank(degree); targetRanks[k]=from.target().rank(degree);
                rows+=sourceRanks[k]*targetRanks[k];
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
                IntegerMatrix targetBoundary=from.target().boundary(degree.add(BigInteger.ONE),work),
                        sourceBoundary=from.source().boundary(degree,work),f=from.matrix(degree,work),g=to.matrix(degree,work);
                for(int r=0;r<targetRanks[k];r++) for(int c=0;c<sourceRanks[k];c++) {
                    work.use(1L+targetRanks[k+1]+(k==0?0:sourceRanks[k-1])); values[equation]=g.get(r,c).subtract(f.get(r,c));
                    // (D_target K_k)[r,c] uses the row-major coordinates of K_k.
                    for(int a=0;a<targetRanks[k+1];a++) entries[equation][offsets[k]+a*sourceRanks[k]+c]=targetBoundary.get(r,a);
                    // (K_(k-1) D_source)[r,c] couples the adjacent witness degree.
                    if(k>0) for(int b=0;b<sourceRanks[k-1];b++) entries[equation][offsets[k-1]+r*sourceRanks[k-1]+b]=sourceBoundary.get(b,c);
                    equation++;
                }
            }
            coefficients=new IntegerMatrix(rows,columns,entries); rhs=new IntegerVector(values);
        }
        private static MathFailure limit(String what,int maximum) {
            return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Integral cone homotopy solving allows at most "+maximum+" total "+what);
        }
        /** Integral Smith coordinates with free coordinates zero; null records a divisibility obstruction. */
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
        IntegralConeHomotopy witness(IntegerVector values,IntegralChainConeMap start,IntegralChainConeMap end) {
            List<IntegerMatrix> matrices=new ArrayList<>(); work.use(unknowns);
            for(int k=0;k<degrees;k++) {
                BigInteger[][] entries=new BigInteger[targetRanks[k+1]][sourceRanks[k]];
                for(int r=0;r<targetRanks[k+1];r++) for(int c=0;c<sourceRanks[k];c++) entries[r][c]=values.get(offsets[k]+r*sourceRanks[k]+c);
                matrices.add(new IntegerMatrix(targetRanks[k+1],sourceRanks[k],entries));
            }
            return new IntegralConeHomotopy(new IntegralConeHomotopy.Data(start,end,matrices),work);
        }
    }
    public static boolean areHomotopic(IntegralChainConeMap from,IntegralChainConeMap to) {
        LinearSystem system=new LinearSystem(from,to,new Computation()); return system.coordinates(system.work.decompose(system.coefficients))!=null;
    }
    private static IntegralConeHomotopy between(LinearSystem system) {
        Decomposition smith=system.work.decompose(system.coefficients); IntegerVector coordinates=system.coordinates(smith);
        if(coordinates==null) throw MathFailure.undefined("These cone maps admit no integral chain homotopy");
        return system.witness(system.work.apply(smith.right(),coordinates),system.from,system.to);
    }
    public static IntegralConeHomotopy between(IntegralChainConeMap from,IntegralChainConeMap to) { return between(new LinearSystem(from,to,new Computation())); }
    /** One particular witness followed by a full integral basis of zero-to-zero homogeneous witnesses. */
    public static List<IntegralConeHomotopy> solutionGenerators(IntegralChainConeMap from,IntegralChainConeMap to) {
        LinearSystem system=new LinearSystem(from,to,new Computation()); Decomposition smith=system.work.decompose(system.coefficients); IntegerVector coordinates=system.coordinates(smith);
        if(coordinates==null) return Collections.emptyList();
        List<IntegralConeHomotopy> result=new ArrayList<>(); result.add(system.witness(system.work.apply(smith.right(),coordinates),from,to));
        if(smith.rank()<system.unknowns) {
            IntegralChainConeMap zero=IntegralChainConeMap.zero(from.source(),from.target(),system.work);
            for(int c=smith.rank();c<system.unknowns;c++) {
                system.work.use(system.unknowns); result.add(system.witness(smith.right().column(c),zero,zero));
            }
        }
        return Collections.unmodifiableList(result);
    }
    private static LinearSystem contractionSystem(IntegralChainMappingCone cone) {
        Computation work=new Computation();
        return new LinearSystem(IntegralChainConeMap.zero(cone,cone,work),IntegralChainConeMap.identity(cone,work),work);
    }
    public static boolean isContractible(IntegralChainMappingCone cone) {
        LinearSystem system=contractionSystem(cone); return system.coordinates(system.work.decompose(system.coefficients))!=null;
    }
    public static IntegralConeHomotopy contract(IntegralChainMappingCone cone) { return between(contractionSystem(cone)); }
}
