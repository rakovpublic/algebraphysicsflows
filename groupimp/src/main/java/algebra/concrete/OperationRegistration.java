package algebra.concrete;

import algebra.imp.Algebra;
import operations.IAbsOperation;
import java.io.Serializable;

/** Read-only catalog entry for an operation already implemented by a legacy interface. */
public final class OperationRegistration implements Serializable {
    private static final long serialVersionUID=1L;
    public final String id,alias;
    public final Algebra<?> first,second,result;
    public final boolean flat,partial;
    public final IAbsOperation operation;
    OperationRegistration(String id,String alias,Algebra<?> first,Algebra<?> second,Algebra<?> result,
                          boolean flat,boolean partial,IAbsOperation operation) {
        this.id=id; this.alias=alias; this.first=first; this.second=second; this.result=result;
        this.flat=flat; this.partial=partial; this.operation=operation;
    }
}

