package algebraflow.imp;

import algebra.IAlgebraItem;
import algebra.ISuperAlgebraInitializer;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebraflow.IFlowInvoke;
import algebraflow.IWriter;
import algebraflow.InputFormat;
import operations.flat.ICustomMemberFlatOperation;
import operations.flat.ICustomResultFlatOperation;
import operations.flat.IUnsafeFlatOperation;
import operations.simple.ICustomMemberOperation;
import operations.simple.ICustomResultOperation;
import operations.simple.ITransferOperation;
import operations.simple.IUnsafeOperation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public class AlgebraFlow<T> implements IAlgebraFlow<T> {
    private MathTool mathTool;
    private List<?extends IAlgebraItem> currentFlow;
    private Algebra<?> currentAlgebra;
    private List< IFlowInvoke<?>> currentInvokes;

    private AlgebraFlow(List<IAlgebraItem<T>> currentFlow, Algebra<T> currentAlgebra, MathTool mathTool) {
        this.mathTool = mathTool;
        this.currentFlow = currentFlow;
        this.currentAlgebra = currentAlgebra;
    }

    private AlgebraFlow(MathTool mathTool, List<?extends IAlgebraItem> currentFlow, Algebra<T> currentAlgebra, List<IFlowInvoke<?>> currentInvokes) {
        this.mathTool = mathTool;
        this.currentFlow = currentFlow;
        this.currentAlgebra = currentAlgebra;
        this.currentInvokes = currentInvokes;
    }

    public AlgebraFlow(InputFormat<T> inputFormat, ISuperAlgebraInitializer algebraInitializer, String startAlgebra) {
        mathTool =algebraInitializer.initialize();
        currentAlgebra= mathTool.getAlgebra(startAlgebra);
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
    public <K> IAlgebraFlow<K> performCustomResultOperation(String operationName, T sElement) {
        ICustomResultOperation<K> customOperation=null;
        if (currentAlgebra.hasCustomResultOperation(operationName)&&currentAlgebra.getParamClass().equals(sElement.getClass())){
            customOperation=(ICustomResultOperation<K>)currentAlgebra.getCustomResultOperation(operationName);
            IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

                @Override
                public List<IAlgebraItem<K>> perform() {
                    List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.add(item.performCustomResultOperation(operationName,sElement));
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
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }

    }

    @Override
    public <K> IAlgebraFlow<K> performAlgebraTransfer(String operationName) {

        ITransferOperation<K> transferOperation =null;
        if (currentAlgebra.hasAlgebraTransfer(operationName)){
            transferOperation=( ITransferOperation<K>)currentAlgebra.getTransferOperation(operationName);
            IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

                @Override
                public List<IAlgebraItem<K>> perform() {
                    List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.add(item.performAlgebraTransfer(operationName));
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
        if(mathTool.hasAlgebra(transferOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(transferOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }

    }

    @Override
    public <K, V> IAlgebraFlow<K> performAlgebraUnsafe(String operationName, V element) {
        IUnsafeOperation<K> customOperation=null;
            customOperation=(IUnsafeOperation<K>)currentAlgebra.getUnsafeOperation(operationName);
            IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

                @Override
                public List<IAlgebraItem<K>> perform() {
                    List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.add(item.performUnsafeOperation(operationName,element));
                    }
                    currentFlow=flow;
                    return  flow;
                }
            };
            currentInvokes.add(invoke);
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }
    }

    @Override
    public IAlgebraFlow<T> performFlatOperation(String operation, T element) {
        if (currentAlgebra.hasFlatOperation(operation)&&currentAlgebra.getParamClass().equals(element.getClass())){
            IFlowInvoke<T> invoke=  new IFlowInvoke<T>() {

                @Override
                public List<IAlgebraItem<T>> perform() {
                    List<IAlgebraItem<T>> flow = new ArrayList<IAlgebraItem<T>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.addAll(item.performFlatOperation(operation,element));

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
    public <K> IAlgebraFlow<K> performFlatCustomResultOperation(String operationName, T sElement) {
        ICustomResultFlatOperation<K> customOperation=null;
        if (currentAlgebra.hasCustomResultFlatOperation(operationName)&&currentAlgebra.getParamClass().equals(sElement.getClass())){
            customOperation=(ICustomResultFlatOperation<K>)currentAlgebra.getCustomResultFlatOperation(operationName);
            IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

                @Override
                public List<IAlgebraItem<K>> perform() {
                    List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.addAll(item.performCustomResultFlatOperation(operationName,sElement));
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
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }

    }

    @Override
    public <K> IAlgebraFlow<K> performFlatAlgebraTransfer(String operationName) {
        return null;
    }

    @Override
    public <K, V> IAlgebraFlow<K> performFlatAlgebraUnsafe(String operationName, V element) {
        IUnsafeFlatOperation<K> customOperation=null;
        customOperation=(IUnsafeFlatOperation<K>)currentAlgebra.getUnsafeFlatOperation(operationName);
        IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

            @Override
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                for(IAlgebraItem<T> item:currentFlow){
                    flow.addAll(item.performUnsafeFlatOperation(operationName,element));
                }
                currentFlow=flow;
                return  flow;
            }
        };
        currentInvokes.add(invoke);
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }
    }

    @Override
    public <K> IAlgebraFlow<T> performCustomMemberOperation(String operationName, K sElement) {
        ICustomMemberOperation<K> customOperation=null;
            customOperation=(ICustomMemberOperation<K>)currentAlgebra.getCustomMemberOperation(operationName);
            IFlowInvoke<T> invoke=  new IFlowInvoke<T>() {

                @Override
                public List<IAlgebraItem<T>> perform() {
                    List<IAlgebraItem<T>> flow = new ArrayList<IAlgebraItem<T>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.add(item.performCustomMemberOperation(operationName,sElement));
                    }
                    currentFlow=flow;
                    return  flow;
                }
            };
            currentInvokes.add(invoke);

        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<T>(this.mathTool,this.currentFlow,(Algebra<T>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }
    }

    @Override
    public <K> IAlgebraFlow<T> performFlatCustomMemberOperation(String operationName, K sElement) {
        ICustomMemberFlatOperation<K> customOperation=null;
        customOperation=(ICustomMemberFlatOperation<K>)currentAlgebra.getCustomMemberFlatOperation(operationName);
        IFlowInvoke<T> invoke=  new IFlowInvoke<T>() {

            @Override
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> flow = new ArrayList<IAlgebraItem<T>>();
                for(IAlgebraItem<T> item:currentFlow){
                    flow.addAll(item.performCustomMemberFlatOperation(operationName,sElement));
                }
                currentFlow=flow;
                return  flow;
            }
        };
        currentInvokes.add(invoke);

        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<T>(this.mathTool,this.currentFlow,(Algebra<T>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            //TODO: add exception throw and logging
            return null;
        }
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
    public<K> void write(IWriter<K> writer) {
        writer.write(this.collectAlgebraItems());

    }

    @Override
    public <K> List<IAlgebraItem<K>> collectAlgebraItems() {
        List<IAlgebraItem<K>> result = new ArrayList<>();
        for(IFlowInvoke invoke:currentInvokes){
            currentFlow=invoke.perform();
        }
        for(IAlgebraItem<K> item:currentFlow){
            result.add(item.perform());
        }
        return result;
    }


    @Override
    public Class getCurrentAlgebraItemClass() {
        return this.currentAlgebra.getParamClass();
    }

    @Override
    public String getCurrentAlgebraName() {
        return this.currentAlgebra.getAlgebraName();
    }
}
