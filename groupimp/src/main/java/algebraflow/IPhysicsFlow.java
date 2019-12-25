package algebraflow;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.PhysicsItem;
import cluster.IPart;

import java.util.HashMap;
import java.util.List;

public interface IPhysicsFlow {
List<HashMap<Algebra<?>,Integer>> getAllPossibleResultSetDescriptions();

    /**
     * perform operation for each element in flow with two elements of type T and return result type T
     *
     * @param operation operation name
     * @param element   second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.simple.IOperation
     */
    public IPhysicsFlow performPhysicsOperation(String operation, PhysicsItem<?> element);

    /**
     * perform custom result operation for each element in flow with two elements of type T and return result type K
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomResultOperation
     */
    public  IPhysicsFlow performCustomResultPhysicsOperation(String operationName, PhysicsItem<?> sElement);

    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     *
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ITransferOperation
     */
    public  IPhysicsFlow performAlgebraPhysicsTransfer(String operationName);

    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.IUnsafeOperation
     */
    public  IPhysicsFlow performAlgebraPhysicsUnsafe(String operationName, PhysicsItem<?> element);

    /**
     * perform unsafe operation for each element in flow with two elements of type T and return result type T
     *
     * @param operation operation name
     * @param element   second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.flat.IFlatOperation
     */
    public IPhysicsFlow performFlatPhysicsOperation(String operation, PhysicsItem<?> element);

    /**
     * perform operation for each element in flow with two elements of type T and return result type K
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomResultFlatOperation
     */
    public  IPhysicsFlow performFlatCustomResultPhysicsOperation(String operationName, PhysicsItem<?> sElement);

    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     *
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ITransferFlatOperation
     */
    public  IPhysicsFlow performFlatAlgebraPhysicsTransfer(String operationName);

    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.IUnsafeFlatOperation
     */
    public IPhysicsFlow performFlatAlgebraPhysicsUnsafe(String operationName, PhysicsItem<?> element);

    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomMemberOperation
     */
    public  IPhysicsFlow performCustomMemberPhysicsOperation(String operationName, PhysicsItem<?> sElement);

    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomMemberFlatOperation
     */
    public  IPhysicsFlow performFlatCustomMemberPhysicsOperation(String operationName,PhysicsItem<?> sElement);

    /**
     * collect items from flow and return it as list of strings, toString method used
     *
     * @return List of items in string view
     */
    public List<String> collect();

    /**
     * writes items to storage
     *
     * @param writer
     * @see IWriter
     */
    public <K> void write(IWriter<K> writer);

    /**
     * collects elements from flow as list of IAlgebraItems
     *
     * @return list of IAlgebraItems
     * @see IAlgebraItem
     */
    public <K> List<PhysicsItem<K>> collectAlgebraItems();

    /**
     * @return class which used for current AlgebraItem  parametrization
     */
    public Class getCurrentAlgebraItemClass();

    /**
     * @return current algebra name
     */
    public String getCurrentAlgebraName();


    /**
     * Set part of input for worker.
     *
     * @param part part of input
     * @see IPart
     */
    public <T> void setInput(IPart<T> part);
}
