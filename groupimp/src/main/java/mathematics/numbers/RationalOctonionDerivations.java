package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.util.*;

/** Exact inner derivations of O(Q), with an su(3)-type stabilizer slice fixing the selected unit l=e4. */
public final class RationalOctonionDerivations {
    private RationalOctonionDerivations(){}
    private static RationalOctonion basis(int n){Rational[] c=new Rational[8];Arrays.fill(c,Rational.ZERO);c[n]=Rational.ONE;return new RationalOctonion(c);}
    private static RationalMatrix operator(RationalOctonion a,boolean left){
        Rational[][] m=new Rational[8][8];for(int c=0;c<8;c++){
            RationalOctonion e=basis(c),v=left?a.multiply(e):e.multiply(a);
            for(int r=0;r<8;r++)m[r][c]=v.coordinate(r);
        }return new RationalMatrix(m);
    }
    private static RationalMatrix difference(RationalMatrix a,RationalMatrix b){
        Rational[][] m=new Rational[a.rows()][a.columns()];for(int r=0;r<a.rows();r++)for(int c=0;c<a.columns();c++)m[r][c]=a.get(r,c).subtract(b.get(r,c));return new RationalMatrix(m);
    }
    private static RationalMatrix sum(RationalMatrix a,RationalMatrix b){
        Rational[][] m=new Rational[a.rows()][a.columns()];for(int r=0;r<a.rows();r++)for(int c=0;c<a.columns();c++)m[r][c]=a.get(r,c).add(b.get(r,c));return new RationalMatrix(m);
    }
    private static RationalMatrix derivation(RationalOctonion a,RationalOctonion b){
        RationalMatrix la=operator(a,true),lb=operator(b,true),ra=operator(a,false),rb=operator(b,false);
        return sum(sum(difference(la.multiply(lb),lb.multiply(la)),difference(la.multiply(rb),rb.multiply(la))),difference(ra.multiply(rb),rb.multiply(ra)));
    }
    /** D_ab=[L_a,L_b]+[L_a,R_b]+[R_a,R_b], with exact 8x8 rational matrices. */
    public static RationalMatrix innerDerivation(int a,int b){
        if(a<1||a>7||b<1||b>7)throw new IllegalArgumentException("Imaginary octonion basis indices must be 1..7");
        return derivation(basis(a),basis(b));
    }
    /** Returns eight linearly independent G2 derivations that annihilate l=e4. */
    public static List<RationalMatrix> su3StabilizerCandidates(){
        List<RationalMatrix> basis=new ArrayList<>();for(int a=1;a<=7;a++)for(int b=a+1;b<=7;b++)basis.add(innerDerivation(a,b));
        Rational[][] action=new Rational[8][basis.size()];RationalVector l=new RationalVector(Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ONE,Rational.ZERO,Rational.ZERO,Rational.ZERO);
        for(int k=0;k<basis.size();k++){RationalVector image=basis.get(k).multiply(l);for(int r=0;r<8;r++)action[r][k]=image.get(r);}
        List<Rational[]> kernel=nullspace(action);List<RationalMatrix> selected=new ArrayList<>();int previousRank=0;
        for(Rational[] coefficients:kernel){RationalMatrix candidate=linearCombination(basis,coefficients);List<RationalMatrix> trial=new ArrayList<>(selected);trial.add(candidate);int rank=flatten(trial).rank();if(rank>previousRank){selected.add(candidate);previousRank=rank;if(previousRank==8)break;}}
        if(selected.size()!=8)throw new IllegalStateException("Expected an 8-dimensional stabilizer of l; computed rank="+selected.size());
        return Collections.unmodifiableList(selected);
    }
    private static List<Rational[]> nullspace(Rational[][] input){
        int rows=input.length,cols=input[0].length;Rational[][] a=new Rational[rows][cols];for(int r=0;r<rows;r++)a[r]=input[r].clone();
        List<Integer> pivots=new ArrayList<>();int pivotRow=0;
        for(int c=0;c<cols&&pivotRow<rows;c++){
            int p=pivotRow;while(p<rows&&a[p][c].signum()==0)p++;if(p==rows)continue;
            Rational[] tmp=a[pivotRow];a[pivotRow]=a[p];a[p]=tmp;Rational scale=Rational.ONE.divide(a[pivotRow][c]);for(int j=c;j<cols;j++)a[pivotRow][j]=a[pivotRow][j].multiply(scale);
            for(int r=0;r<rows;r++)if(r!=pivotRow&&a[r][c].signum()!=0){Rational factor=a[r][c];for(int j=c;j<cols;j++)a[r][j]=a[r][j].subtract(factor.multiply(a[pivotRow][j]));}
            pivots.add(c);pivotRow++;
        }
        List<Rational[]> result=new ArrayList<>();for(int free=0;free<cols;free++)if(!pivots.contains(free)){
            Rational[] vector=new Rational[cols];Arrays.fill(vector,Rational.ZERO);vector[free]=Rational.ONE;
            for(int r=0;r<pivots.size();r++)vector[pivots.get(r)]=a[r][free].negate();result.add(vector);
        }return result;
    }
    private static RationalMatrix linearCombination(List<RationalMatrix> basis,Rational[] coefficients){
        Rational[][] entries=new Rational[8][8];for(int r=0;r<8;r++)for(int c=0;c<8;c++){Rational v=Rational.ZERO;for(int k=0;k<basis.size();k++)v=v.add(coefficients[k].multiply(basis.get(k).get(r,c)));entries[r][c]=v;}return new RationalMatrix(entries);
    }
    private static RationalMatrix flatten(List<RationalMatrix> matrices){
        Rational[][] rows=new Rational[64][matrices.size()];for(int k=0;k<matrices.size();k++)for(int r=0;r<8;r++)for(int c=0;c<8;c++)rows[r*8+c][k]=matrices.get(k).get(r,c);return new RationalMatrix(rows);
    }
    /** Three exact adjoint su(2) brackets on H(Q), represented on its 4-dimensional coefficient space. */
    public static List<RationalMatrix> quaternionAdjointGenerators(){
        RationalQuaternion[] units={RationalQuaternion.I,RationalQuaternion.J,RationalQuaternion.K};List<RationalMatrix> out=new ArrayList<>();
        for(RationalQuaternion u:units){Rational[][] m=new Rational[4][4];for(int c=0;c<4;c++){
            Rational[] v={Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO};v[c]=Rational.ONE;
            RationalQuaternion x=new RationalQuaternion(v[0],v[1],v[2],v[3]),bracket=u.multiply(x).subtract(x.multiply(u));
            Rational[] values={bracket.real,bracket.i,bracket.j,bracket.k};for(int r=0;r<4;r++)m[r][c]=values[r];
        }out.add(new RationalMatrix(m));}return Collections.unmodifiableList(out);
    }
    /** One exact u(1) phase generator on the selected Q(i) real two-plane. */
    public static RationalMatrix complexPhaseGenerator(){
        return new RationalMatrix(new Rational[][]{{Rational.ZERO,Rational.of(-1)},{Rational.ONE,Rational.ZERO}});
    }
}
