package algebra.imp;

import algebra.IAlgebraItem;
import algebra.IOperationInvoke;
import exceptions.NotMemberException;
import operations.simple.ICustomOperation;
import operations.simple.ITransferOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 31.03.2017.
 */
public abstract class AbstractAlgebra<T> implements IAlgebraItem<T> {
    private static final Logger logger = LogManager.getLogger(AbstractAlgebra.class);

    @Override
    public IAlgebraItem<T> performOperation(String operationName, T sElement) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasOperation(operationName)){
            if(algebra.validate(sElement)){
                return new AlgebraItemDecorator<T>(algebra,this.getCurrentItem(),sElement,algebra.getOperation(operationName));
            }
            NotMemberException ex=new NotMemberException("Element"+sElement.toString()+" is not member of this algebra.Check validation rules of algebra "+algebra.toString());
            logger.error("Invalid element",ex);
            throw ex;
        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public IAlgebraItem<T> performOperations(List<IOperationInvoke<T>> iOperationInvokes) {
        return null;
    }

    @Override
    public <K>IAlgebraItem<K> performCustomOperation(String operationName, T sElement) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasCustomOperation(operationName)){
            ICustomOperation<T> op=algebra.getCustomOperation(operationName);
            if(algebra.validate(sElement)){
                return op.performOperation(perform().getResult(),sElement);
            }
            NotMemberException ex=new NotMemberException("Element"+sElement.toString()+" is not member of this algebra.Check validation rules of algebra "+algebra.toString());
            logger.error("Invalid element",ex);
            throw ex;
        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public <K>IAlgebraItem<K> performAlgebraTransfer(String operationName) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasAlgebraTransfer(operationName)){
            ITransferOperation<T> op=algebra.getTransferOperation(operationName);
            return op.performOperation(perform().getResult());

        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public <K>List<IAlgebraItem<K>> performCustomFlatOperation(String operationName, T sElement) {
        return null;
    }

    @Override
    public<K>List<IAlgebraItem<K>> performAlgebraFlatTransfer(String operationName) {
        return null;
    }

    @Override
    public List<IAlgebraItem<T>> performFlatOperation(String operationName, T sElement) {
        return null;
    }
}
