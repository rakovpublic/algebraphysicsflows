package algebra.imp;

import algebra.IAlgebraItem;
import operations.flat.ICustomFlatOperation;
import operations.flat.IFlatOperation;
import operations.flat.ISplitFlatOperation;
import operations.flat.ITransferFlatOperation;
import operations.simple.ICustomOperation;
import operations.simple.IOperation;
import operations.simple.ISplitOperation;
import operations.simple.ITransferOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rules.IValidationRule;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public final class Algebra<T> implements Serializable{
    private static final Logger logger = LogManager.getLogger(Algebra.class);
    private final List<IValidationRule<T>> validationRules;
    private final HashMap<String, IOperation<T>>algebraOperations;
    private final HashMap<String, ICustomOperation<T>> customOperations;
    private final HashMap<String, ITransferOperation<T>> transferOperations;
    private final HashMap<String, IFlatOperation<T>>algebraFlatOperations;
    private final HashMap<String, ICustomFlatOperation<T>> customFlatOperations;
    private final HashMap<String, ITransferFlatOperation<T>> transferFlatOperations;
    private final HashMap<String, ISplitOperation<T>> splitOperations;
    private final HashMap<String, ISplitFlatOperation<T>> splitFlatOperations;

    public Class getParamClass() {
        return paramClass;
    }

    private final Class paramClass;

    private final String algebraName;


    public Algebra( String algebraName,Class paramClass) {
        this.paramClass=paramClass;
        this.validationRules = new LinkedList<IValidationRule<T>>();
        this.algebraOperations = new HashMap<String, IOperation<T>>();
        this.customOperations = new HashMap<String,ICustomOperation<T>>();
        this.transferOperations = new HashMap<String,ITransferOperation<T>>();
        this.algebraName = algebraName;
        transferFlatOperations = new HashMap<String,ITransferFlatOperation<T>>();
        customFlatOperations = new HashMap<String,ICustomFlatOperation<T>>();
        algebraFlatOperations = new HashMap<String,IFlatOperation<T>>();
        splitFlatOperations = new HashMap<String,ISplitFlatOperation<T>>();
        splitOperations = new HashMap<String,ISplitOperation<T>>();
    }
    public boolean addOperation(String name, IOperation<T> operation ){
        if(!algebraOperations.containsKey(name)){
            algebraOperations.put(name,operation);
            return true;
        }
        return false;

    }
    public void addValidationRule(IValidationRule<T> rule){
        validationRules.add(rule);
    }
    public boolean addCustomOperation(String name,ICustomOperation<T> customOperation){
        if(!customOperations.containsKey(name)){
            customOperations.put(name,customOperation);
            return true;
        }
        return false;
    }
    public boolean addAlgebraTransfer(String name, ITransferOperation<T> transferOperation){
        if(!transferOperations.containsKey(name)){
            transferOperations.put(name,transferOperation);
            return true;
        }
        return false;
    }
    public boolean addFlatOperation(String name, IFlatOperation<T> flatOperation ){
        if(!algebraFlatOperations.containsKey(name)){
            algebraFlatOperations.put(name,flatOperation);
            return true;
        }
        return false;

    }

    public boolean addCustomFlatOperation(String name,ICustomFlatOperation<T> customFlatOperation){
        if(!customFlatOperations.containsKey(name)){
            customFlatOperations.put(name,customFlatOperation);
            return true;
        }
        return false;
    }
    public boolean addAlgebraFlatTransfer(String name, ITransferFlatOperation<T> transferFlatOperation){
        if(!splitOperations.containsKey(name)){
            transferFlatOperations.put(name,transferFlatOperation);
            return true;
        }
        return false;
    }
    public boolean addSplitOperation(String name,ISplitOperation<T> splitOperation){
        if(!splitOperations.containsKey(name)){
            splitOperations.put(name,splitOperation);
            return true;
        }
        return false;
    }
    public boolean addSplitFlatOperation(String name,ISplitFlatOperation<T> splitFlatOperation){
        if(!splitFlatOperations.containsKey(name)){
            splitFlatOperations.put(name,splitFlatOperation);
            return true;
        }
        return false;
    }
    public boolean hasSplitOperation(String name){
        return splitOperations.containsKey(name);
    }
    public boolean hasSplitFlatOperation(String name){
        return splitFlatOperations.containsKey(name);
    }

    public boolean hasOperation(String name){
        return algebraOperations.containsKey(name);
    }
    public boolean hasCustomOperation(String name){
        return customOperations.containsKey(name);
    }
    public boolean hasAlgebraTransfer(String name){
        return transferOperations.containsKey(name);
    }
    public boolean hasFlatOperation(String name){
        return algebraFlatOperations.containsKey(name);
    }
    public boolean hasCustomFlatOperation(String name){
        return customFlatOperations.containsKey(name);
    }
    public boolean hasAlgebraFlatTransfer(String name){
        return transferFlatOperations.containsKey(name);
    }
    public IAlgebraItem<T> buildAlgebraItem(T value){
        return new AlgebraItem<T>(this,value);
    }
    IOperation<T> getOperation(String name){
        return algebraOperations.get(name);
    }
    public ICustomOperation<T>  getCustomOperation(String name){
        return customOperations.get(name);
    }
    ITransferOperation<T> getTransferOperation(String name){
        return transferOperations.get(name);
    }
    IFlatOperation<T> getFlatOperation(String name){
        return algebraFlatOperations.get(name);
    }
    ICustomFlatOperation<T>  getCustomFlatOperation(String name){
        return customFlatOperations.get(name);
    }
    ITransferFlatOperation<T> getTransferFlatOperation(String name){
        return transferFlatOperations.get(name);
    }
    ISplitOperation<T>  getSplitOperation(String name){
        return splitOperations.get(name);
    }
    ISplitFlatOperation<T> getSplitFlatOperation(String name){
        return splitFlatOperations.get(name);
    }
    boolean validate(T value){
        for(IValidationRule<T>rule:validationRules){
            if(!rule.validate(value)){
                return false;
            }
        }
        return true;
    }

   public String getAlgebraName() {
        return algebraName;
    }
}