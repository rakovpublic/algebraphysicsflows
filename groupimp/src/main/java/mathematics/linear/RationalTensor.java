package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;

/** Dense exact coordinate tensors with row-major entries; contraction uses the standard coordinate pairing. */
public final class RationalTensor implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_ORDER=32,MAX_ENTRIES=1000000;
    private final int[] shape;
    private final Rational[] entries;

    public RationalTensor(int[] shape,Rational... entries) {
        int size=entryCount(shape);
        if(entries.length!=size) throw MathFailure.invalid("Tensor entry count does not match its shape");
        this.shape=shape.clone(); this.entries=entries.clone();
        for(Rational entry : this.entries) Objects.requireNonNull(entry);
    }
    private static int entryCount(int[] shape) {
        if(shape.length>MAX_ORDER) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Tensor order exceeds the dense representation limit");
        boolean empty=false;
        for(int dimension : shape) {
            if(dimension<0) throw MathFailure.invalid("Tensor axis dimensions must be nonnegative");
            if(dimension==0) empty=true;
        }
        if(empty) return 0;
        long size=1;
        for(int dimension : shape) {
            size*=dimension;
            if(size>MAX_ENTRIES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Tensor materialization exceeds the entry limit");
        }
        return (int)size;
    }
    public int order() { return shape.length; }
    public int size() { return entries.length; }
    public List<Integer> shape() {
        List<Integer> result=new ArrayList<>(); for(int dimension : shape) result.add(dimension);
        return Collections.unmodifiableList(result);
    }
    public List<Rational> entries() { return Collections.unmodifiableList(Arrays.asList(entries)); }
    public Rational get(int... indices) {
        if(indices.length!=order()) throw MathFailure.undefined("One coordinate is required per tensor axis");
        int offset=0;
        for(int i=0;i<order();i++) {
            if(indices[i]<0 || indices[i]>=shape[i]) throw MathFailure.undefined("Tensor coordinate is outside its axis");
            offset=offset*shape[i]+indices[i];
        }
        return entries[offset];
    }
    private void sameShape(RationalTensor other) {
        if(!Arrays.equals(shape,other.shape)) throw MathFailure.undefined("Tensor shapes must agree");
    }
    public RationalTensor add(RationalTensor other) {
        sameShape(other); Rational[] values=new Rational[size()];
        for(int i=0;i<size();i++) values[i]=entries[i].add(other.entries[i]);
        return new RationalTensor(shape,values);
    }
    public RationalTensor scale(Rational scalar) {
        Rational[] values=new Rational[size()]; for(int i=0;i<size();i++) values[i]=entries[i].multiply(scalar);
        return new RationalTensor(shape,values);
    }
    public RationalTensor hadamard(RationalTensor other) {
        sameShape(other); Rational[] values=new Rational[size()];
        for(int i=0;i<size();i++) values[i]=entries[i].multiply(other.entries[i]);
        return new RationalTensor(shape,values);
    }
    public Rational dot(RationalTensor other) {
        sameShape(other); Rational result=Rational.ZERO;
        for(int i=0;i<size();i++) result=result.add(entries[i].multiply(other.entries[i]));
        return result;
    }
    public RationalTensor tensorProduct(RationalTensor other) {
        int[] dimensions=Arrays.copyOf(shape,order()+other.order());
        System.arraycopy(other.shape,0,dimensions,order(),other.order());
        Rational[] values=new Rational[entryCount(dimensions)]; int offset=0;
        for(Rational first : entries) for(Rational second : other.entries) values[offset++]=first.multiply(second);
        return new RationalTensor(dimensions,values);
    }
    private void axis(int index) { if(index<0 || index>=order()) throw MathFailure.undefined("Tensor axis is outside its order"); }
    private static int[] coordinates(int offset,int[] shape) {
        int[] result=new int[shape.length];
        for(int i=shape.length-1;i>=0;i--) { result[i]=offset%shape[i]; offset/=shape[i]; }
        return result;
    }
    public RationalTensor swapAxes(int first,int second) {
        axis(first); axis(second); int[] dimensions=shape.clone();
        dimensions[first]=shape[second]; dimensions[second]=shape[first];
        Rational[] values=new Rational[size()];
        for(int offset=0;offset<size();offset++) {
            int[] indices=coordinates(offset,dimensions); int swap=indices[first]; indices[first]=indices[second]; indices[second]=swap;
            values[offset]=get(indices);
        }
        return new RationalTensor(dimensions,values);
    }
    /** Sum over equal coordinates of two distinct equal-sized axes; other axes keep their order. */
    public RationalTensor contract(int first,int second) {
        axis(first); axis(second);
        if(first==second || shape[first]!=shape[second]) throw MathFailure.undefined("Contraction needs distinct axes of the same dimension");
        int[] dimensions=new int[order()-2]; int next=0;
        for(int i=0;i<order();i++) if(i!=first && i!=second) dimensions[next++]=shape[i];
        Rational[] values=new Rational[entryCount(dimensions)];
        for(int offset=0;offset<values.length;offset++) {
            int[] remaining=coordinates(offset,dimensions),indices=new int[order()]; next=0;
            for(int i=0;i<order();i++) if(i!=first && i!=second) indices[i]=remaining[next++];
            Rational sum=Rational.ZERO;
            for(int k=0;k<shape[first];k++) { indices[first]=k; indices[second]=k; sum=sum.add(get(indices)); }
            values[offset]=sum;
        }
        return new RationalTensor(dimensions,values);
    }
    public static RationalTensor scalar(Rational value) { return new RationalTensor(new int[0],value); }
    public Rational scalar() { if(order()!=0) throw MathFailure.undefined("A scalar tensor must have order zero"); return entries[0]; }
    public static RationalTensor fromVector(RationalVector vector) {
        int[] shape={vector.dimension()}; Rational[] values=new Rational[entryCount(shape)];
        for(int i=0;i<values.length;i++) values[i]=vector.get(i);
        return new RationalTensor(shape,values);
    }
    public RationalVector toVector() { if(order()!=1) throw MathFailure.undefined("A vector tensor must have order one"); return new RationalVector(entries); }
    public static RationalTensor fromMatrix(RationalMatrix matrix) {
        int[] shape={matrix.rows(),matrix.columns()}; Rational[] values=new Rational[entryCount(shape)]; int offset=0;
        for(int r=0;r<shape[0];r++) for(int c=0;c<shape[1];c++) values[offset++]=matrix.get(r,c);
        return new RationalTensor(shape,values);
    }
    public RationalMatrix toMatrix() {
        if(order()!=2 || shape[0]==0 || shape[1]==0) throw MathFailure.undefined("The matrix carrier requires order two and positive dimensions");
        Rational[][] values=new Rational[shape[0]][shape[1]];
        for(int r=0;r<shape[0];r++) for(int c=0;c<shape[1];c++) values[r][c]=get(r,c);
        return new RationalMatrix(values);
    }
    @Override public boolean equals(Object other) {
        return other instanceof RationalTensor && Arrays.equals(shape,((RationalTensor)other).shape) && Arrays.equals(entries,((RationalTensor)other).entries);
    }
    @Override public int hashCode() { return 31*Arrays.hashCode(shape)+Arrays.hashCode(entries); }
    @Override public String toString() { return "Tensor(shape="+Arrays.toString(shape)+", entries="+Arrays.toString(entries)+")"; }
}
