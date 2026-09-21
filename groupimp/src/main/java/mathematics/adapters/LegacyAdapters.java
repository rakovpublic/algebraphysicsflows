package mathematics.adapters;

import algebra.IAlgebraItem;
import algebra.imp.MathTool;
import mathematics.core.*;
import operations.IAbsOperation;
import operations.simple.ITransferOperation;
import operations.simple.IUnsafeOperation;
import operations.flat.IUnsafeFlatOperation;
import java.io.Serializable;
import java.util.*;

/** Keeps legacy signatures intact; checked bodies enforce mathematical input/output domains. */
public final class LegacyAdapters {
    private LegacyAdapters() { }
    public static <A,B> void install(UnaryOperation<A,B> operation) {
        operation.source().algebra().addAlgebraTransfer(operation.metadata().id,new UnaryBridge<>(operation));
    }
    public static <A,B,C> void install(BinaryOperation<A,B,C> operation) {
        operation.first().algebra().addUnsafeOperation(operation.metadata().id,new BinaryBridge<>(operation));
    }
    public static <A,B,C> void install(FlatOperation<A,B,C> operation) {
        operation.first().algebra().addUnsafeOperationFlat(operation.metadata().id,new FlatBridge<>(operation));
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
