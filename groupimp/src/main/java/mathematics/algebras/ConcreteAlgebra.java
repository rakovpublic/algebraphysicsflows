package mathematics.algebras;

import algebra.imp.Algebra;
import algebra.imp.MathTool;
import mathematics.adapters.LegacyAdapters;
import mathematics.core.*;
import mathematics.foundations.Unit;
import java.io.Serializable;
import java.util.*;
import static mathematics.core.MathStatus.Computation.EXACT;

/** A concrete legacy Algebra plus checked operations and explicit law declarations. */
public abstract class ConcreteAlgebra<T> implements Serializable {
    private static final long serialVersionUID=1L;
    private final Domain<T> domain;
    private final Domain<Unit> unit;
    private final Map<String,DescribedOperation> operations=new LinkedHashMap<>();
    private final Map<String,Domain<?>> carriers=new LinkedHashMap<>();
    private final List<Structure.Law> laws=new ArrayList<>();
    private boolean operationsInstalled;

    protected ConcreteAlgebra(Domain<T> domain,Domain<Unit> unit) {
        this.domain=Objects.requireNonNull(domain); this.unit=Objects.requireNonNull(unit);
        carriers.put(domain.metadata().id,domain);
    }
    public final Domain<T> domain() { return domain; }
    public final Domain<Unit> unit() { return unit; }
    public final Algebra<T> algebra() { return domain.algebra(); }
    public final Map<String,DescribedOperation> operations() { return Collections.unmodifiableMap(operations); }
    public final Map<String,Domain<?>> carriers() { return Collections.unmodifiableMap(carriers); }
    protected final Metadata metadata(String name) {
        return Metadata.builder(domain.metadata().id+"."+name,name,"Concrete "+domain+" operation: "+name)
                .provenance(Provenance.unreviewed("docs/CONCRETE_ALGEBRAS.md")).build();
    }
    private <O extends DescribedOperation> O remember(String name,O operation) {
        if(operations.putIfAbsent(name,operation)!=null) throw new IllegalArgumentException("Duplicate operation alias: "+name);
        for(Domain<?> operand : operation.signature().operands) include(operand);
        include(operation.signature().result);
        return operation;
    }
    private void include(Domain<?> value) {
        Domain<?> existing=carriers.putIfAbsent(value.metadata().id,value);
        if(existing!=null && existing!=value) throw new IllegalArgumentException("Conflicting carrier "+value);
    }
    protected final void law(String statement) {
        laws.add(new Structure.Law(statement,MathStatus.Epistemic.DEFINED,"Declared law; empirical tests are linked in the coverage registry; no formal proof claimed"));
    }
    protected final BinaryOperation<T,T,T> closed(String name,boolean partial,Functions.Binary<T,T,T> body) {
        return binary(name,domain,domain,domain,partial,body);
    }
    protected final <A,B,C> BinaryOperation<A,B,C> binary(String name,Domain<A> first,Domain<B> second,Domain<C> result,boolean partial,Functions.Binary<A,B,C> body) {
        return remember(name,new BinaryOperation<>(metadata(name),first,second,result,EXACT,partial,body));
    }
    protected final <A,B> UnaryOperation<A,B> unary(String name,Domain<A> source,Domain<B> result,boolean partial,Functions.Unary<A,B> body) {
        return remember(name,new UnaryOperation<>(metadata(name),source,result,EXACT,partial,body));
    }
    protected final <A,B,C> FlatOperation<A,B,C> flat(String name,Domain<A> first,Domain<B> second,Domain<C> result,boolean partial,Functions.Binary<A,B,List<C>> body) {
        return remember(name,new FlatOperation<>(metadata(name),first,second,result,EXACT,partial,body));
    }
    protected final void constant(String name,T value) {
        domain.require(value);
        unary(name,unit,domain,false,ignored -> value);
    }
    public final Structure structure() {
        return new Structure(metadata("structure"),carriers,operations,laws);
    }
    /** Same-carrier operations use performOperation; mixed operations use performUnsafeOperation. */
    public final void register(MathTool tool) {
        // Check every carrier before mutating the supplied tool.
        for(Domain<?> carrier : carriers.values()) {
            Algebra<?> existing=tool.getAlgebra(carrier.metadata().id);
            if(existing!=null && existing!=carrier.algebra()) throw new IllegalArgumentException("MathTool already contains a different "+carrier+" algebra");
        }
        for(Domain<?> carrier : carriers.values()) tool.addAlgebra(carrier.algebra());
        if(operationsInstalled) return;
        for(Map.Entry<String,DescribedOperation> entry : operations.entrySet()) {
            DescribedOperation op=entry.getValue();
            // Constants and opposite-order actions live on another carrier: qualify their aliases.
            String alias=op.signature().operands.get(0)==domain ? entry.getKey() : op.metadata().id;
            if(op instanceof UnaryOperation) LegacyAdapters.install(alias,(UnaryOperation<?,?>)op);
            else if(op instanceof BinaryOperation) LegacyAdapters.install(alias,(BinaryOperation<?,?,?>)op);
            else if(op instanceof FlatOperation) LegacyAdapters.install(alias,(FlatOperation<?,?,?>)op);
        }
        operationsInstalled=true;
    }
}
