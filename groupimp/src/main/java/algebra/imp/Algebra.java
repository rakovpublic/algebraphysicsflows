package algebra.imp;

import algebra.IAlgebraItem;
import operations.IAbsOperation;
import operations.flat.*;
import operations.simple.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.ArrayUtils;
import rules.IValidationRule;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public final class Algebra<T> implements Serializable {
    private static final Logger logger = LogManager.getLogger(Algebra.class);
    private final List<IValidationRule<T>> validationRules;
    private final HashMap<String, IOperation<T>> algebraOperations;
    private final HashMap<String, ICustomResultOperation<T>> customOperations;
    private final HashMap<String, ITransferOperation<T>> transferOperations;
    private final HashMap<String, IFlatOperation<T>> algebraFlatOperations;
    private final HashMap<String, ICustomResultFlatOperation<T>> customFlatOperations;
    private final HashMap<String, ITransferFlatOperation<T>> transferFlatOperations;
    private final HashMap<String, List<IUnsafeOperation<T>>> unsafeOperations;
    private final HashMap<String, List<IUnsafeFlatOperation<T>>> unsafeFlatOperations;
    private final HashMap<String, List<ICustomMemberOperation<T>>> customMemberOperations;
    private final HashMap<String, List<ICustomMemberFlatOperation<T>>> customMemberFlatOperations;


    public Class getParamClass() {
        return paramClass;
    }

    private final Class paramClass;

    private final String algebraName;

    private final String description;


    public Algebra(String algebraName, Class paramClass,String description) {
        this.description=description;
        this.paramClass = paramClass;
        this.validationRules = new LinkedList<IValidationRule<T>>();
        this.algebraOperations = new HashMap<>();
        this.customOperations = new HashMap<>();
        this.transferOperations = new HashMap<>();
        this.algebraName = algebraName;
        transferFlatOperations = new HashMap<>();
        customFlatOperations = new HashMap<>();
        algebraFlatOperations = new HashMap<>();
        unsafeOperations = new HashMap<>();
        unsafeFlatOperations = new HashMap<>();
        customMemberOperations = new HashMap<>();
        customMemberFlatOperations = new HashMap<>();

    }

    /**
     * add custom member operation to algebra
     *
     * @param name      operation name
     * @param operation
     * @return true if added successful, false if custom member operation with such name already exists
     * @see ICustomMemberOperation
     */
    public boolean addCustomMemberOperation(String name, ICustomMemberOperation<T> operation) {
        if (!customMemberOperations.containsKey(name)) {
            customMemberOperations.get(name).add(operation);
        }else {
            List<ICustomMemberOperation<T>> operations=new LinkedList<>();
            operations.add(operation);
            customMemberOperations.put(name,operations);
        }
        return true;
    }

    /**
     * add custom member flat operation to algebra
     *
     * @param name      operation name
     * @param operation
     * @return true if added successful, false if custom member flat operation with such name already exists
     * @see ICustomMemberFlatOperation
     */
    public boolean addCustomMemberFlatOperation(String name, ICustomMemberFlatOperation<T> operation) {
        if (!customMemberFlatOperations.containsKey(name)) {
            customMemberFlatOperations.get(name).add(operation);
        }else {
            List<ICustomMemberFlatOperation<T>> operations=new LinkedList<>();
            operations.add(operation);
            customMemberFlatOperations.put(name,operations);
        }
        return true;
    }

    /**
     * add unsafe operation to algebra
     *
     * @param name      operation name
     * @param operation
     * @return true if added successful, false if unsafe operation with such name already exists
     * @see IUnsafeOperation
     */
    public boolean addUnsafeOperation(String name, IUnsafeOperation<T> operation) {
        if (!unsafeOperations.containsKey(name)) {
            unsafeOperations.get(name).add(operation);
        }else {
            List<IUnsafeOperation<T>> operations=new LinkedList<>();
            operations.add(operation);
            unsafeOperations.put(name,operations);
        }
        return true;
    }

    /**
     * add unsafe flat operation to algebra
     *
     * @param name      operation name
     * @param operation
     * @return true if added successful, false if unsafe flat operation with such name already exists
     * @see IUnsafeFlatOperation
     */
    public boolean addUnsafeOperationFlat(String name, IUnsafeFlatOperation<T> operation) {
        if (!unsafeFlatOperations.containsKey(name)) {
            unsafeFlatOperations.get(name).add(operation);
        }else {
            List<IUnsafeFlatOperation<T>> operations=new LinkedList<>();
            operations.add(operation);
            unsafeFlatOperations.put(name,operations);
        }
        return true;
    }

    /**
     * add operation to algebra
     *
     * @param name      operation name
     * @param operation
     * @return true if added successful, false if operation with such name already exists
     * @see IOperation
     */
    public boolean addOperation(String name, IOperation<T> operation) {
        algebraOperations.put(name,operation);
        return true;
    }

    /**
     * add validation rule which validate values on build
     *
     * @param rule
     * @see IValidationRule
     */
    public void addValidationRule(IValidationRule<T> rule) {
        validationRules.add(rule);
    }

    /**
     * add custom result operation to algebra
     *
     * @param name            operation name
     * @param customOperation
     * @return true if added successful, false if result member operation with such name already exists
     * @see ICustomResultOperation
     */
    public boolean addCustomResultOperation(String name, ICustomResultOperation<T> customOperation) {
        customOperations.put(name,customOperation);
        return true;
    }

    /**
     * add transfer operation to algebra
     *
     * @param name              operation name
     * @param transferOperation
     * @return true if added successful, false if transfer operation with such name already exists
     * @see ITransferOperation
     */
    public boolean addAlgebraTransfer(String name, ITransferOperation<T> transferOperation) {
        transferOperations.put(name,transferOperation);
        return true;
    }

    /**
     * add flat operation to algebra
     *
     * @param name          operation name
     * @param flatOperation
     * @return true if added successful, false if flat operation with such name already exists
     * @see IFlatOperation
     */
    public boolean addFlatOperation(String name, IFlatOperation<T> flatOperation) {
        algebraFlatOperations.put(name,flatOperation);
        return true;
    }

    /**
     * add custom result flat operation to algebra
     *
     * @param name                operation name
     * @param customFlatOperation
     * @return true if added successful, false if custom result flat operation with such name already exists
     * @see ICustomResultFlatOperation
     */
    public boolean addCustomResultFlatOperation(String name, ICustomResultFlatOperation<T> customFlatOperation) {
        customFlatOperations.put(name,customFlatOperation);
        return true;
    }

    /**
     * add flat transfer operation to algebra
     *
     * @param name                  operation name
     * @param transferFlatOperation
     * @return true if added successful, false if flat transfer operation with such name already exists
     * @see ITransferFlatOperation
     */
    public boolean addAlgebraFlatTransfer(String name, ITransferFlatOperation<T> transferFlatOperation) {
        transferFlatOperations.put(name,transferFlatOperation);
        return true;
    }

    /**
     * check if custom member operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see ICustomMemberOperation
     */
    public boolean hasCustomMemberOperation(String name) {
        return customMemberOperations.containsKey(name);
    }

    /**
     * check if custom member flat operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see ICustomMemberFlatOperation
     */
    public boolean hasCustomMemberFlatOperation(String name) {
        return customMemberFlatOperations.containsKey(name);
    }

    /**
     * check if unsafe operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see IUnsafeOperation
     */
    public boolean hasUnsafeOperation(String name) {
        return unsafeOperations.containsKey(name);
    }

    /**
     * check if unsafe flat operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see IUnsafeFlatOperation
     */
    public boolean hasUnsafeFlatOperation(String name) {
        return unsafeFlatOperations.containsKey(name);
    }

    /**
     * check if operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see IOperation
     */
    public boolean hasOperation(String name) {
        return algebraOperations.containsKey(name);
    }

    /**
     * check if custom result operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see ICustomResultOperation
     */
    public boolean hasCustomResultOperation(String name) {
        return customOperations.containsKey(name);
    }

    /**
     * check if transfer operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see ITransferOperation
     */
    public boolean hasAlgebraTransfer(String name) {
        return transferOperations.containsKey(name);
    }

    /**
     * check if custom member operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see IFlatOperation
     */
    public boolean hasFlatOperation(String name) {
        return algebraFlatOperations.containsKey(name);
    }

    /**
     * check if custom member operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see ICustomResultFlatOperation
     */
    public boolean hasCustomResultFlatOperation(String name) {
        return customFlatOperations.containsKey(name);
    }

    /**
     * check if custom member operation exists
     *
     * @param name operation name
     * @return true if exists
     * @see ITransferFlatOperation
     */
    public boolean hasAlgebraFlatTransfer(String name) {
        return transferFlatOperations.containsKey(name);
    }

    /**
     * get custom member operation instance
     *
     * @param name operation name
     * @return
     * @see ICustomMemberOperation
     */
    public ICustomMemberOperation<T> getCustomMemberOperation(String name) {
        return this.customMemberOperations.get(name).get(0);
    }

    /**
     * get custom member flat operation instance
     *
     * @param name operation name
     * @return
     * @see ICustomMemberFlatOperation
     */
    public ICustomMemberFlatOperation<T> getCustomMemberFlatOperation(String name) {
        return this.customMemberFlatOperations.get(name).get(0);
    }

    /**
     * build algebra item
     *
     * @param value
     * @return algebra item parametrized T
     * @see IAlgebraItem
     */
    public IAlgebraItem<T> buildAlgebraItem(T value) {
        if (validate(value)) {
            return new AlgebraItem<T>(this, value);
        } else {
            return null;
        }
    }

    /**
     * get unsafe operation instance
     *
     * @param name operation name
     * @return
     * @see IUnsafeOperation
     */
    public IUnsafeOperation<T> getUnsafeOperation(String name) {
        return unsafeOperations.get(name).get(0);
    }

    /**
     * get unsafe flat operation instance
     *
     * @param name operation name
     * @return
     * @see IUnsafeFlatOperation
     */
    public IUnsafeFlatOperation<T> getUnsafeFlatOperation(String name) {
        return unsafeFlatOperations.get(name).get(0);
    }

    /**
     * get  operation instance
     *
     * @param name operation name
     * @return
     * @see IOperation
     */
    public IOperation<T> getOperation(String name) {
        return algebraOperations.get(name);
    }

    /**
     * get custom result operation instance
     *
     * @param name operation name
     * @return
     * @see ICustomResultOperation
     */
    public ICustomResultOperation<T> getCustomResultOperation(String name) {
        return customOperations.get(name);
    }

    /**
     * get transfer operation instance
     *
     * @param name operation name
     * @return
     * @see ITransferOperation
     */
    public ITransferOperation<T> getTransferOperation(String name) {
        return transferOperations.get(name);
    }

    /**
     * get flat operation instance
     *
     * @param name operation name
     * @return
     * @see IFlatOperation
     */
    public IFlatOperation<T> getFlatOperation(String name) {
        return algebraFlatOperations.get(name);
    }

    /**
     * get custom result flat operation instance
     *
     * @param name operation name
     * @return
     * @see ICustomResultFlatOperation
     */
    public ICustomResultFlatOperation<T> getCustomResultFlatOperation(String name) {
        return customFlatOperations.get(name);
    }

    /**
     * get transfer flat operation instance
     *
     * @param name operation name
     * @return
     * @see ITransferFlatOperation
     */
    public ITransferFlatOperation<T> getTransferFlatOperation(String name) {
        return transferFlatOperations.get(name);
    }

    /**
     * validate element value on matching algebra
     *
     * @param value
     * @return
     * @see IValidationRule
     */
    boolean validate(T value) {
        for (IValidationRule<T> rule : validationRules) {
            if (!rule.validate(value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return algebra name
     */
    public String getAlgebraName() {
        return algebraName;
    }
/**
 * @return algebra short description
 * */
    public String getDescription() {
        return description;
    }
    /**
     * @return full algebra description(with methods) as json
     * */
    /**
     *
     *     private final HashMap<String, IOperation<T>> algebraOperations;
     private final HashMap<String, ICustomResultOperation<T>> customOperations;
     private final HashMap<String, ITransferOperation<T>> transferOperations;
     private final HashMap<String, IFlatOperation<T>> algebraFlatOperations;
     private final HashMap<String, ICustomResultFlatOperation<T>> customFlatOperations;
     private final HashMap<String, ITransferFlatOperation<T>> transferFlatOperations;
     private final HashMap<String, IUnsafeOperation<T>> unsafeOperations;
     private final HashMap<String, IUnsafeFlatOperation<T>> unsafeFlatOperations;
     private final HashMap<String, ICustomMemberOperation<T>> customMemberOperations;
     private final HashMap<String, ICustomMemberFlatOperation<T>> customMemberFlatOperations;


     * */
    public String generateDescription(){

        StringBuilder sb = new StringBuilder();
        sb.append("{\"algebraName\":\"");
        sb.append(this.getAlgebraName());
        sb.append("\",\"algebraDescription\":\"");
        sb.append(getDescription());
        sb.append("\"");
        //TODO: Make description
    /*   if(algebraOperations.size()>0){
           sb.append(",\"simpleOperations\":");
           sb.append(getOperationsDescription(algebraOperations));
       }if(customOperations.size()>0){
            sb.append(",\"customOperations\":");
            sb.append(getOperationsDescription(customOperations));
        }if(transferOperations.size()>0){
            sb.append(",\"transferOperations\":");
            sb.append(getOperationsDescription(transferOperations));
        }if(customMemberOperations.size()>0){
            sb.append(",\"customMemberOperations\":");
            sb.append(getOperationsDescription(customMemberOperations));
        }if(unsafeOperations.size()>0){
            sb.append(",\"unsafeOperations\":");
            sb.append(getOperationsDescription(unsafeOperations));
        }if(algebraFlatOperations.size()>0){
            sb.append(",\"simpleFlatOperations\":");
            sb.append(getOperationsDescription(algebraFlatOperations));
        }if(customFlatOperations.size()>0){
            sb.append(",\"customFlatOperations\":");
            sb.append(getOperationsDescription(customFlatOperations));
        }if(transferFlatOperations.size()>0){
            sb.append(",\"transferFlatOperations\":");
            sb.append(getOperationsDescription(transferFlatOperations));
        }if(unsafeFlatOperations.size()>0){
            sb.append(",\"unsafeFlatOperations\":");
            sb.append(getOperationsDescription(unsafeFlatOperations));
        }if(customMemberFlatOperations.size()>0){
            sb.append(",\"customMemberFlatOperations\":");
            sb.append(getOperationsDescription(customMemberFlatOperations));
        }*/
        sb.append("}");
        return sb.toString();
    }
    private <K extends IAbsOperation> String getOperationsDescription(HashMap<String,K> op){
        StringBuilder sb = new StringBuilder();
        Set<String> keys=op.keySet();
        if(keys.size()>0){
            sb.append("[");
            for(String name:op.keySet()){
                sb.append("\"operationName\":\"");
                sb.append(name);
                sb.append("\",\"operationDescription\":\"");
                sb.append(op.get(name).getDescription());
                sb.append("\",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("]");
        }
        return sb.toString();
    }


    /**
     * get unsafe operation instance
     *
     * @param name operation name
     * @return
     * @see IUnsafeOperation
     */
    public IUnsafeOperation<T> getUnsafeOperationWithParam(String name,Class<?> clazz) {
        for(IUnsafeOperation operation:this.unsafeOperations.get(name)){
            if(operation.getSecondElementClass()==clazz){
                return operation;
            }
        }
        return null;
    }

    /**
     * get unsafe flat operation instance
     *
     * @param name operation name
     * @return
     * @see IUnsafeFlatOperation
     */
    public IUnsafeFlatOperation<T> getUnsafeFlatOperationWithParam(String name,Class<?> clazz) {
        for(IUnsafeFlatOperation operation:this.unsafeFlatOperations.get(name)){
            if(operation.getSecondElementClass()==clazz){
                return operation;
            }
        }
        return null;
    }
    /**
     * get custom member operation instance
     *
     * @param name operation name
     * @return
     * @see ICustomMemberOperation
     */
    public ICustomMemberOperation<T> getCustomMemberOperationWithParam(String name,Class<?> clazz) {
        for(ICustomMemberOperation operation:this.customMemberOperations.get(name)){
            if(operation.getSecondElementClass()==clazz){
                return operation;
            }
        }
        return null;
    }

    /**
     * get custom member flat operation instance
     *
     * @param name operation name
     * @return
     * @see ICustomMemberFlatOperation
     */
    public ICustomMemberFlatOperation<T> getCustomMemberFlatOperationWithParam(String name,Class<?> clazz) {
        for(ICustomMemberFlatOperation operation:this.customMemberFlatOperations.get(name)){
            if(operation.getSecondElementClass()==clazz){
                return operation;
            }
        }
        return null;
    }

}
