package operations;

import algebra.imp.Algebra;
import java.io.Serializable;

/** Shared descriptors for native operations; Algebra remains the carrier and validator. */
public abstract class AbstractOperation implements IAbsOperation,Serializable {
    private static final long serialVersionUID=1L;
    private final Algebra<?> second,target;
    private final String description;
    protected AbstractOperation(String description,Algebra<?> source,Algebra<?> second,Algebra<?> target) {
        this.description=description; this.second=second; this.target=target;
    }
    public String getDescription() { return description; }
    public String getAlgebraName() { return target.getAlgebraName(); }
    public Class<?> getResultBaseClass() { return target.getParamClass(); }
    public Class<?> getSecondElementClass() { return second==null?Void.class:second.getParamClass(); }
}

