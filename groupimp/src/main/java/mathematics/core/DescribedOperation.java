package mathematics.core;

import operations.IAbsOperation;
import java.io.Serializable;

public interface DescribedOperation extends IAbsOperation, Serializable {
    Metadata metadata();
    Signature signature();
    MathStatus.Computation computation();
    default String getDescription() { return metadata().description; }
    default String getAlgebraName() { return signature().result.metadata().id; }
    default Class<?> getResultBaseClass() { return signature().result.representation(); }
    default Class<?> getSecondElementClass() { return signature().operands.size() == 2 ? signature().operands.get(1).representation() : Void.class; }
}
