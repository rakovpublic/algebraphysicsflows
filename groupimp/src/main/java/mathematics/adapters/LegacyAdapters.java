package mathematics.adapters;

import algebra.IAlgebraItem;
import algebra.imp.MathTool;
import mathematics.core.*;
import operations.IAbsOperation;
import operations.simple.ITransferOperation;
import operations.simple.IUnsafeOperation;
import operations.simple.IOperation;
import operations.simple.ICustomResultOperation;
import operations.flat.IUnsafeFlatOperation;
import operations.flat.IFlatOperation;
import java.io.Serializable;
import java.util.*;

/** Keeps legacy signatures intact; checked bodies enforce mathematical input/output domains. */
public final class LegacyAdapters {
    private LegacyAdapters() { }
    public static <A,B> void install(UnaryOperation<A,B> operation) {
        install(operation.metadata().id,operation);
    }
    public static <A,B> void install(String name,UnaryOperation<A,B> operation) {
        operation.source().algebra().addAlgebraTransfer(name,new UnaryBridge<>(operation));
    }
    public static <A,B,C> void install(BinaryOperation<A,B,C> operation) {
        install(operation.metadata().id,operation);
    }
    @SuppressWarnings("unchecked")
    public static <A,B,C> void install(String name,BinaryOperation<A,B,C> operation) {
        operation.first().algebra().addUnsafeOperation(name,new BinaryBridge<>(operation));
        // Domain identity, not erased Java classes, justifies these narrowed adapters.
        if(operation.first()==(Object)operation.second()) {
            if(operation.first()==(Object)operation.target())
                operation.first().algebra().addOperation(name,new ClosedBinaryBridge<>((BinaryOperation<A,A,A>)(Object)operation));
            else operation.first().algebra().addCustomResultOperation(name,new SameInputBridge<>((BinaryOperation<A,A,C>)(Object)operation));
        }
    }
    public static <A,B,C> void install(FlatOperation<A,B,C> operation) {
        install(operation.metadata().id,operation);
    }
    @SuppressWarnings("unchecked")
    public static <A,B,C> void install(String name,FlatOperation<A,B,C> operation) {
        operation.first().algebra().addUnsafeOperationFlat(name,new FlatBridge<>(operation));
        if(operation.first()==(Object)operation.second() && operation.first()==(Object)operation.target())
            operation.first().algebra().addFlatOperation(name,new ClosedFlatBridge<>((FlatOperation<A,A,A>)(Object)operation));
    }
    public static MathTool installCatalog(OperationCatalog catalog) {
        MathTool tool=new MathTool("universal-mathematics");
        for(Domain<?> domain : catalog.domains().values()) tool.addAlgebra(domain.algebra());
        for(DescribedOperation operation : catalog.operations().values()) {
            if(operation instanceof UnaryOperation) install((UnaryOperation<?,?>)operation);
            else if(operation instanceof BinaryOperation) install((BinaryOperation<?,?,?>)operation);
            else if(operation instanceof FlatOperation) install((FlatOperation<?,?,?>)operation);
            else throw new IllegalArgumentException("No adapter for " + operation.getClass().getName());
        }
        return tool;
    }
    private abstract static class Descriptor implements IAbsOperation,Serializable {
        private static final long serialVersionUID=1L;
        private final DescribedOperation operation;
        Descriptor(DescribedOperation operation) { this.operation=operation; }
        public String getDescription() { return operation.getDescription(); }
        public String getAlgebraName() { return operation.getAlgebraName(); }
        public Class<?> getResultBaseClass() { return operation.getResultBaseClass(); }
        public Class<?> getSecondElementClass() { return operation.getSecondElementClass(); }
    }
    private static final class UnaryBridge<A,B> extends Descriptor implements ITransferOperation<A> {
        private static final long serialVersionUID=1L;
        private final UnaryOperation<A,B> operation;
        UnaryBridge(UnaryOperation<A,B> operation) { super(operation); this.operation=operation; }
        @SuppressWarnings("unchecked")
        public <V> IAlgebraItem<V> performOperation(A input) { return (IAlgebraItem<V>)operation.target().item(operation.apply(input)); }
    }
    private static final class BinaryBridge<A,B,C> extends Descriptor implements IUnsafeOperation<A> {
        private static final long serialVersionUID=1L;
        private final BinaryOperation<A,B,C> operation;
        BinaryBridge(BinaryOperation<A,B,C> operation) { super(operation); this.operation=operation; }
        @SuppressWarnings("unchecked")
        public <K,V> IAlgebraItem<K> performOperation(A first,V second) {
            return (IAlgebraItem<K>)operation.target().item(operation.apply(first,operation.second().require(second)));
        }
    }
    private static final class ClosedBinaryBridge<T> extends Descriptor implements IOperation<T> {
        private static final long serialVersionUID=1L;
        private final BinaryOperation<T,T,T> operation;
        ClosedBinaryBridge(BinaryOperation<T,T,T> operation) { super(operation); this.operation=operation; }
        public T performOperation(T first,T second) { return operation.apply(first,second); }
    }
    private static final class SameInputBridge<A,C> extends Descriptor implements ICustomResultOperation<A> {
        private static final long serialVersionUID=1L;
        private final BinaryOperation<A,A,C> operation;
        SameInputBridge(BinaryOperation<A,A,C> operation) { super(operation); this.operation=operation; }
        @SuppressWarnings("unchecked")
        public <V> IAlgebraItem<V> performOperation(A first,A second) {
            return (IAlgebraItem<V>)operation.target().item(operation.apply(first,second));
        }
    }
    private static final class ClosedFlatBridge<T> extends Descriptor implements IFlatOperation<T> {
        private static final long serialVersionUID=1L;
        private final FlatOperation<T,T,T> operation;
        ClosedFlatBridge(FlatOperation<T,T,T> operation) { super(operation); this.operation=operation; }
        public List<IAlgebraItem<T>> performOperation(T first,T second) {
            List<IAlgebraItem<T>> result=new ArrayList<>();
            for(T value : operation.apply(first,second)) result.add(operation.target().item(value));
            return result;
        }
    }
    private static final class FlatBridge<A,B,C> extends Descriptor implements IUnsafeFlatOperation<A> {
        private static final long serialVersionUID=1L;
        private final FlatOperation<A,B,C> operation;
        FlatBridge(FlatOperation<A,B,C> operation) { super(operation); this.operation=operation; }
        @SuppressWarnings("unchecked")
        public <K,V> List<IAlgebraItem<K>> performOperation(A first,V second) {
            List<IAlgebraItem<K>> result=new ArrayList<>();
            for(C value : operation.apply(first,operation.second().require(second))) result.add((IAlgebraItem<K>)operation.target().item(value));
            return result;
        }
    }
}
