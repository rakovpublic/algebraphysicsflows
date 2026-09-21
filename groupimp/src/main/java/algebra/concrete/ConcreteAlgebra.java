package algebra.concrete;

import algebra.imp.Algebra;
import algebra.imp.MathTool;
import mathematics.foundations.Unit;
import operations.IAbsOperation;
import operations.OperationMembers;
import operations.simple.*;
import operations.flat.*;
import java.io.Serializable;
import java.util.*;

/** Builds the existing Algebra and registers implementations of its existing operation interfaces. */
public abstract class ConcreteAlgebra<T> implements Serializable {
    private static final long serialVersionUID=1L;
    private final Algebra<T> algebra;
    private final Algebra<Unit> unit;
    private final Map<String,OperationRegistration> operations=new LinkedHashMap<>();
    private final Map<String,Algebra<?>> carriers=new LinkedHashMap<>();
    private final List<String> laws=new ArrayList<>();
    private boolean operationsInstalled;

    protected ConcreteAlgebra(Algebra<T> algebra,Algebra<Unit> unit) {
        this.algebra=Objects.requireNonNull(algebra); this.unit=Objects.requireNonNull(unit);
        carriers.put(algebra.getAlgebraName(),algebra);
    }
    protected static <T> Algebra<T> carrier(String name,Class<T> type,String description,operations.OperationBodies.Predicate<T> rule) {
        return AlgebraFactories.carrier(name,type,description,rule);
    }
    public final Algebra<T> algebra() { return algebra; }
    public final Algebra<Unit> unit() { return unit; }
    public final Map<String,OperationRegistration> operations() { return Collections.unmodifiableMap(operations); }
    public final Map<String,Algebra<?>> carriers() { return Collections.unmodifiableMap(carriers); }
    /** Law descriptions are documentation, never executable proof claims. */
    public final List<String> laws() { return Collections.unmodifiableList(laws); }
    protected final void law(String statement) { laws.add(statement); }
    private void include(Algebra<?> value) {
        Algebra<?> previous=carriers.putIfAbsent(value.getAlgebraName(),value);
        if(previous!=null && previous!=value) throw new IllegalArgumentException("Conflicting algebra "+value.getAlgebraName());
    }
    private void remember(String name,Algebra<?> first,Algebra<?> second,Algebra<?> result,boolean flat,boolean partial,IAbsOperation operation) {
        String id=algebra.getAlgebraName()+"."+name;
        String alias=first==algebra?name:id;
        OperationRegistration registration=new OperationRegistration(id,alias,first,second,result,flat,partial,operation);
        if(operations.putIfAbsent(name,registration)!=null) throw new IllegalArgumentException("Duplicate operation "+name);
        include(first); if(second!=null) include(second); include(result);
    }
    protected final void closed(String name,boolean partial,operations.OperationBodies.Binary<T,T,T> body) {
        remember(name,algebra,algebra,algebra,false,partial,new operations.simple.ClosedOperation<>(name,algebra,body));
    }
    @SuppressWarnings("unchecked")
    protected final <A,B> void unary(String name,Algebra<A> first,Algebra<B> result,boolean partial,operations.OperationBodies.Unary<A,B> body) {
        IAbsOperation operation=first==(Object)result
                ?new OneOperandOperation<>(name,first,(operations.OperationBodies.Unary<A,A>)(Object)body)
                :new TransferOperation<>(name,first,result,body);
        remember(name,first,null,result,false,partial,operation);
    }
    @SuppressWarnings("unchecked")
    protected final <A,B,C> void binary(String name,Algebra<A> first,Algebra<B> second,Algebra<C> result,boolean partial,operations.OperationBodies.Binary<A,B,C> body) {
        IAbsOperation operation;
        if(first==(Object)second)
            operation=new operations.simple.CustomResultOperation<>(name,first,result,(operations.OperationBodies.Binary<A,A,C>)(Object)body);
        else if(first==(Object)result)
            operation=new CustomMemberOperation<>(name,first,second,(operations.OperationBodies.Binary<A,B,A>)(Object)body);
        else if(second==(Object)result)
            operation=new SecondResultOperation<>(name,first,second,(operations.OperationBodies.Binary<A,B,B>)(Object)body);
        else operation=new operations.simple.MixedOperation<>(name,first,second,result,body);
        remember(name,first,second,result,false,partial,operation);
    }
    @SuppressWarnings("unchecked")
    protected final <A,B,C> void flat(String name,Algebra<A> first,Algebra<B> second,Algebra<C> result,boolean partial,operations.OperationBodies.Binary<A,B,List<C>> body) {
        IAbsOperation operation;
        if(first==(Object)second && first==(Object)result)
            operation=new operations.flat.ClosedFlatOperation<>(name,first,(operations.OperationBodies.Binary<A,A,List<A>>)(Object)body);
        else if(first==(Object)second)
            operation=new CustomResultFlatOperation<>(name,first,result,(operations.OperationBodies.Binary<A,A,List<C>>)(Object)body);
        else if(first==(Object)result)
            operation=new CustomMemberFlatOperation<>(name,first,second,(operations.OperationBodies.Binary<A,B,List<A>>)(Object)body);
        else if(second==(Object)result)
            operation=new SecondResultFlatOperation<>(name,first,second,(operations.OperationBodies.Binary<A,B,List<B>>)(Object)body);
        else operation=new operations.flat.MixedFlatOperation<>(name,first,second,result,body);
        remember(name,first,second,result,true,partial,operation);
    }
    protected final void constant(String name,T value) {
        OperationMembers.require(algebra,value);
        unary(name,unit,algebra,false,ignored -> value);
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    public final void register(MathTool tool) {
        for(Algebra<?> carrier : carriers.values()) {
            Algebra<?> existing=tool.getAlgebra(carrier.getAlgebraName());
            if(existing!=null && existing!=carrier) throw new IllegalArgumentException("MathTool already contains a different "+carrier.getAlgebraName()+" algebra");
        }
        for(Algebra<?> carrier : carriers.values()) tool.addAlgebra(carrier);
        if(operationsInstalled) return;
        for(OperationRegistration entry : operations.values()) {
            Algebra first=entry.first; IAbsOperation op=entry.operation;
            if(op instanceof IOperation) first.addOperation(entry.alias,(IOperation)op);
            else if(op instanceof IOneOperandOperation) first.addOneOperandOperation(entry.alias,(IOneOperandOperation)op);
            else if(op instanceof ITransferOperation) first.addAlgebraTransfer(entry.alias,(ITransferOperation)op);
            else if(op instanceof ICustomResultOperation) first.addCustomResultOperation(entry.alias,(ICustomResultOperation)op);
            else if(op instanceof ICustomMemberOperation) first.addCustomMemberOperation(entry.alias,(ICustomMemberOperation)op);
            else if(op instanceof ILeftProjectionOperation) first.addLeftProjectionOperation(entry.alias,(ILeftProjectionOperation)op);
            else if(op instanceof IUnsafeOperation) first.addUnsafeOperation(entry.alias,(IUnsafeOperation)op);
            else if(op instanceof IFlatOperation) first.addFlatOperation(entry.alias,(IFlatOperation)op);
            else if(op instanceof ICustomMemberFlatOperation) first.addCustomMemberFlatOperation(entry.alias,(ICustomMemberFlatOperation)op);
            else if(op instanceof ICustomResultFlatOperation) first.addCustomResultFlatOperation(entry.alias,(ICustomResultFlatOperation)op);
            else if(op instanceof ILeftProjectionFlatOperation) first.addLeftProjectionFlatOperation(entry.alias,(ILeftProjectionFlatOperation)op);
            else if(op instanceof IUnsafeFlatOperation) first.addUnsafeOperationFlat(entry.alias,(IUnsafeFlatOperation)op);
            else throw new IllegalArgumentException("Unsupported legacy operation interface");
        }
        operationsInstalled=true;
    }
}
