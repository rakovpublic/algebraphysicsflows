package algebra.imp;

import algebra.IAlgebraItem;
import exceptions.NotMemberException;
import operations.flat.*;
import operations.simple.ICustomMemberOperation;
import operations.simple.ICustomResultOperation;
import operations.simple.ITransferOperation;
import operations.simple.IUnsafeOperation;
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
    public <K>IAlgebraItem<K> performCustomResultOperation(String operationName, T sElement) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasCustomResultOperation(operationName)){
            ICustomResultOperation<T> op=algebra.getCustomResultOperation(operationName);
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
    public <K>List<IAlgebraItem<K>> performCustomResultFlatOperation(String operationName, T sElement) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasCustomResultFlatOperation(operationName)){
            ICustomResultFlatOperation<T> op=algebra.getCustomResultFlatOperation(operationName);
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
    public<V>List<IAlgebraItem<V>> performAlgebraFlatTransfer(String operationName) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasAlgebraFlatTransfer(operationName)){
            ITransferFlatOperation<T> op=algebra.getTransferFlatOperation(operationName);
            return (List<IAlgebraItem<V>>)op.performOperation(perform().getResult());

        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public List<IAlgebraItem<T>> performFlatOperation(String operationName, T sElement) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasFlatOperation(operationName)){
            IFlatOperation<T> op=algebra.getFlatOperation(operationName);
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
    public <K, V> IAlgebraItem<K> performUnsafeOperation(String operationName, V element) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasUnsafeOperation(operationName)){
            IUnsafeOperation<T> op=algebra.getUnsafeOperation(operationName);
            return op.<K,V>performOperation(perform().getResult(),element);
        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public <K, V> List<IAlgebraItem<K>> performUnsafeFlatOperation(String operationName, V element) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasUnsafeFlatOperation(operationName)){
            IUnsafeFlatOperation<T> op=algebra.getUnsafeFlatOperation(operationName);
            return op.<K,V>performOperation(perform().getResult(),element);
        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public <K> IAlgebraItem<T> performCustomMemberOperation(String operationName, K element) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasCustomMemberOperation(operationName)){
            ICustomMemberOperation<T> op=algebra.getCustomMemberOperation(operationName);

                return op.performOperation(perform().getResult(),element);

        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }

    @Override
    public <K> List<IAlgebraItem<T>> performCustomMemberFlatOperation(String operationName, K element) {
        Algebra<T> algebra=this.getAlgebra();
        if(this.getAlgebra().hasCustomMemberFlatOperation(operationName)){
            ICustomMemberFlatOperation<T> op=algebra.getCustomMemberFlatOperation(operationName);
                return op.performOperation(perform().getResult(),element);

        }
        UnsupportedOperationException ex= new UnsupportedOperationException("Operation"+operationName+" is not exists in this algebra.");
        logger.error("Invalid operation name.",ex);
        throw ex;
    }
}
