package algebra.imp;

import algebra.IAlgebraItem;
import operations.flat.*;
import operations.simple.*;
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
    private final HashMap<String, ICustomResultOperation<T>> customOperations;
    private final HashMap<String, ITransferOperation<T>> transferOperations;
    private final HashMap<String, IFlatOperation<T>>algebraFlatOperations;
    private final HashMap<String, ICustomResultFlatOperation<T>> customFlatOperations;
    private final HashMap<String, ITransferFlatOperation<T>> transferFlatOperations;
    private final HashMap<String, IUnsafeOperation<T>> unsafeOperations;
    private final HashMap<String, IUnsafeFlatOperation<T>> unsafeFlatOperations;
    private final HashMap<String,ICustomMemberOperation<T>> customMemberOperations;
    private final HashMap<String,ICustomMemberFlatOperation<T>> customMemberFlatOperations;


    public Class getParamClass() {
        return paramClass;
    }

    private final Class paramClass;

    private final String algebraName;


    public Algebra( String algebraName,Class paramClass) {
        this.paramClass=paramClass;
        this.validationRules = new LinkedList<IValidationRule<T>>();
        this.algebraOperations = new HashMap<String, IOperation<T>>();
        this.customOperations = new HashMap<String,ICustomResultOperation<T>>();
        this.transferOperations = new HashMap<String,ITransferOperation<T>>();
        this.algebraName = algebraName;
        transferFlatOperations = new HashMap<String,ITransferFlatOperation<T>>();
        customFlatOperations = new HashMap<String,ICustomResultFlatOperation<T>>();
        algebraFlatOperations = new HashMap<String,IFlatOperation<T>>();
        unsafeOperations = new HashMap<String,IUnsafeOperation<T>>();
        unsafeFlatOperations= new HashMap<String,IUnsafeFlatOperation<T>>();
        customMemberOperations= new HashMap<String,ICustomMemberOperation<T>>();
        customMemberFlatOperations=new HashMap<String,ICustomMemberFlatOperation<T>>();

    }
    public boolean addCustomMemberOperation(String name, ICustomMemberOperation<T> operation ){
        if(!customMemberOperations.containsKey(name)){
            customMemberOperations.put(name,operation);
            return true;
        }
        return false;

    }
    public boolean addCustomMemberFlatOperation(String name, ICustomMemberFlatOperation<T> operation ){
        if(!customMemberFlatOperations.containsKey(name)){
            customMemberFlatOperations.put(name,operation);
            return true;
        }
        return false;

    }
    public boolean addUnsafeOperation(String name, IUnsafeOperation<T> operation ){
        if(!unsafeOperations.containsKey(name)){
            unsafeOperations.put(name,operation);
            return true;
        }
        return false;

    }
    public boolean addUnsafeOperationFlat(String name, IUnsafeFlatOperation<T> operation ){
        if(!unsafeFlatOperations.containsKey(name)){
            unsafeFlatOperations.put(name,operation);
            return true;
        }
        return false;

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
    public boolean addCustomResultOperation(String name, ICustomResultOperation<T> customOperation){
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

    public boolean addCustomResultFlatOperation(String name, ICustomResultFlatOperation<T> customFlatOperation){
        if(!customFlatOperations.containsKey(name)){
            customFlatOperations.put(name,customFlatOperation);
            return true;
        }
        return false;
    }
    public boolean addAlgebraFlatTransfer(String name, ITransferFlatOperation<T> transferFlatOperation){
        if(!transferFlatOperations.containsKey(name)){
            transferFlatOperations.put(name,transferFlatOperation);
            return true;
        }
        return false;
    }
    public boolean hasCustomMemberOperation(String name){
        return customMemberOperations.containsKey(name);
    }
    public boolean hasCustomMemberFlatOperation(String name){
        return customMemberFlatOperations.containsKey(name);
    }
    public boolean hasUnsafeOperation(String name){
        return unsafeOperations.containsKey(name);
    }
    public boolean hasUnsafeFlatOperation(String name){
        return unsafeFlatOperations.containsKey(name);
    }
    public boolean hasOperation(String name){
        return algebraOperations.containsKey(name);
    }
    public boolean hasCustomResultOperation(String name){
        return customOperations.containsKey(name);
    }
    public boolean hasAlgebraTransfer(String name){
        return transferOperations.containsKey(name);
    }
    public boolean hasFlatOperation(String name){
        return algebraFlatOperations.containsKey(name);
    }
    public boolean hasCustomResultFlatOperation(String name){
        return customFlatOperations.containsKey(name);
    }
    public boolean hasAlgebraFlatTransfer(String name){
        return transferFlatOperations.containsKey(name);
    }
    public ICustomMemberOperation<T> getCustomMemberOperation(String name){
        return this.customMemberOperations.get(name);
    }
    public ICustomMemberFlatOperation<T> getCustomMemberFlatOperation(String name){
        return  this.customMemberFlatOperations.get(name);
    }
    public IAlgebraItem<T> buildAlgebraItem(T value){
        return new AlgebraItem<T>(this,value);
    }
    public IUnsafeOperation<T> getUnsafeOperation(String name){
        return unsafeOperations.get(name);
    }
    public IUnsafeFlatOperation<T> getUnsafeFlatOperation(String name){
        return unsafeFlatOperations.get(name);
    }
    public IOperation<T> getOperation(String name){
        return algebraOperations.get(name);
    }
    public ICustomResultOperation<T> getCustomResultOperation(String name){
        return customOperations.get(name);
    }
    public ITransferOperation<T> getTransferOperation(String name){
        return transferOperations.get(name);
    }
    public IFlatOperation<T> getFlatOperation(String name){
        return algebraFlatOperations.get(name);
    }
    public ICustomResultFlatOperation<T> getCustomResultFlatOperation(String name){
        return customFlatOperations.get(name);
    }
    public ITransferFlatOperation<T> getTransferFlatOperation(String name){
        return transferFlatOperations.get(name);
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
