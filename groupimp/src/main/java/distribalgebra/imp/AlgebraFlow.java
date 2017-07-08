package distribalgebra.imp;

import algebra.IAlgebraItem;
import algebra.IOperationInvoke;
import algebra.ISuperAlgebraInitializer;
import algebra.imp.Algebra;
import algebra.imp.AlgebraItem;
import algebra.imp.SuperAlgebra;
import distribalgebra.IAlgebraKeyValueFlow;
import distribalgebra.IAlgebraFlow;
import distribalgebra.IFlowInvoke;
import distribalgebra.InputFormat;
import operations.simple.ICustomOperation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public class AlgebraFlow<T> implements IAlgebraFlow<T> {
    private SuperAlgebra superAlgebra;
    private List<?extends IAlgebraItem> currentFlow;
    private Algebra<?> currentAlgebra;
    private List< IFlowInvoke<?>> currentInvokes;

    private AlgebraFlow(List<IAlgebraItem<T>> currentFlow, Algebra<T> currentAlgebra, SuperAlgebra superAlgebra) {
        this.superAlgebra = superAlgebra;
        this.currentFlow = currentFlow;
        this.currentAlgebra = currentAlgebra;
    }

    private AlgebraFlow(SuperAlgebra superAlgebra,List<?extends IAlgebraItem> currentFlow, Algebra<T> currentAlgebra, List<IFlowInvoke<?>> currentInvokes) {
        this.superAlgebra = superAlgebra;
        this.currentFlow = currentFlow;
        this.currentAlgebra = currentAlgebra;
        this.currentInvokes = currentInvokes;
    }

    public AlgebraFlow(InputFormat<T> inputFormat, ISuperAlgebraInitializer algebraInitializer, String startAlgebra) {
        superAlgebra=algebraInitializer.initialize();
        currentAlgebra=superAlgebra.getAlgebra(startAlgebra);
        currentInvokes= new LinkedList< IFlowInvoke<?>>();
        if(currentAlgebra.getParamClass().equals(inputFormat.getInputType())){
        IFlowInvoke<T> invoke=  new IFlowInvoke<T>() {

            @Override
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> flow=inputFormat.read((Algebra<T>)currentAlgebra);
                currentFlow=flow;
                return flow;
            }
        };
        currentInvokes.add(invoke);
        }else{
            //TODO: add exception throw and logging
        }


    }

    @Override
    public IAlgebraFlow<T> performOperation(String operation, T element) {
        if (currentAlgebra.hasOperation(operation)&&currentAlgebra.getParamClass().equals(element.getClass())){
            IFlowInvoke<T> invoke=  new IFlowInvoke<T>() {

                @Override
                public List<IAlgebraItem<T>> perform() {
                    List<IAlgebraItem<T>> flow = new ArrayList<IAlgebraItem<T>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.add(item.performOperation(operation,element));

                    }
                    currentFlow=flow;
                    return  flow;
                }
            };
            currentInvokes.add(invoke);
        }else {
            //TODO: add exception throw and logging
            return null;
        }
        return this;
    }

    @Override
    public IAlgebraFlow<T> performOperations(List<IOperationInvoke<T>> iOperationInvokes) {
        return null;
    }

    @Override
    public <K> IAlgebraFlow<K> performCustomOperation(String operationName, T sElement) {
        ICustomOperation<K> customOperation=null;
        if (currentAlgebra.hasCustomOperation(operationName)&&currentAlgebra.getParamClass().equals(sElement.getClass())){
            customOperation=( ICustomOperation<K>)currentAlgebra.getCustomOperation(operationName);
            IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

                @Override
                public List<IAlgebraItem<K>> perform() {
                    List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.add(item.performCustomOperation(operationName,sElement));
                    }
                    currentFlow=flow;
                    return  flow;
                }
            };
            currentInvokes.add(invoke);
        }else {
            //TODO: add exception throw and logging
            return null;
        }
        if(superAlgebra.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.superAlgebra,this.currentFlow,(Algebra<K>) superAlgebra.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }

    }

    @Override
    public <K> IAlgebraFlow<K> performAlgebraTransfer(String operationName) {
        return null;
    }

    @Override
    public <K, V> IAlgebraKeyValueFlow<K, V> splitToKeyValue(String operationName) {
        return null;
    }

    @Override
    public  List<String> collect() {
       List<String> result = new ArrayList<>();
        for(IFlowInvoke invoke:currentInvokes){
            currentFlow=invoke.perform();
        }
        for(IAlgebraItem item:currentFlow){
            result.add(item.perform().getResult().toString());
        }
        return result;
    }

    @Override
    public void write() {

    }

    @Override
    public <K> List<AlgebraItem<K>> collectAlgebraItems() {
        return null;
    }

    @Override
    public Class getCurrentAlgebraItemClass() {
        return null;
    }

    @Override
    public String getCurrentAlgebraName() {
        return null;
    }
}
