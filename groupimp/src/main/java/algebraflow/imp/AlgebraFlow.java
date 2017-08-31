package algebraflow.imp;

import algebra.IAlgebraItem;
import algebra.IMathToolInitializer;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebraflow.IFlowInvoke;
import algebraflow.IWriter;
import algebraflow.InputFormat;
import exceptions.AlgebraNotExistsException;
import exceptions.NotMemberException;
import exceptions.UnsupportedOperationException;
import operations.flat.ICustomMemberFlatOperation;
import operations.flat.ICustomResultFlatOperation;
import operations.flat.ITransferFlatOperation;
import operations.flat.IUnsafeFlatOperation;
import operations.simple.ICustomMemberOperation;
import operations.simple.ICustomResultOperation;
import operations.simple.ITransferOperation;
import operations.simple.IUnsafeOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public class AlgebraFlow<T> implements IAlgebraFlow<T> {
    private static final Logger logger = LogManager.getLogger(AlgebraFlow.class);
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

    public AlgebraFlow(InputFormat<T> inputFormat, IMathToolInitializer algebraInitializer, String startAlgebra) {
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
            logger.error("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+inputFormat.getInputType());
        }


    }
    /**
     * perform operation for each element in flow with two elements of type T and return result type T
     * @param operation operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.simple.IOperation
     * */

    @Override
    public IAlgebraFlow<T> performOperation(String operation, T element) {
        if(!currentAlgebra.getParamClass().equals(element.getClass())){
            NotMemberException exception= new NotMemberException("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+element.getClass());
            logger.error("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+element.getClass(),exception);
            throw exception;
        }
        if (currentAlgebra.hasOperation(operation)){
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
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operation+"operation type simple");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operation+"operation type simple",exception);
            throw exception;
        }
        return this;
    }

    /**
     * perform custom result operation for each element in flow with two elements of type T and return result type K
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomResultOperation
     * */

    @Override
    public <K> IAlgebraFlow<K> performCustomResultOperation(String operationName, T sElement) {
        ICustomResultOperation<K> customOperation=null;
        if(!currentAlgebra.getParamClass().equals(sElement.getClass())){
            NotMemberException exception= new NotMemberException("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+sElement.getClass());
            logger.error("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+sElement.getClass(),exception);
            throw exception;
        }
        if (currentAlgebra.hasCustomResultOperation(operationName)){
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
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type customresult");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type customresult",exception);
            throw exception;
        }
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }

    }
    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ITransferOperation
     * */
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
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type transfer");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName,exception);
            throw exception;
        }
        if(mathTool.hasAlgebra(transferOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(transferOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+transferOperation.getAlgebraName()+" in math tool: "+mathTool.getName()+"operation type transfer");
            logger.error("Cannot find algebra"+transferOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }

    }
    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     * @param operationName operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.IUnsafeOperation
     * */
    @Override
    public <K, V> IAlgebraFlow<K> performAlgebraUnsafe(String operationName, V element) {
        IUnsafeOperation<K> customOperation=null;
        if (currentAlgebra.hasUnsafeOperation(operationName)){
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
        }else{
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type unsafe");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type unsafe",exception);
            throw exception;
        }
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }
    }
    /**
     * perform unsafe operation for each element in flow with two elements of type T and return result type T
     * @param operation operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.flat.IFlatOperation
     * */
    @Override
    public IAlgebraFlow<T> performFlatOperation(String operation, T element) {
        if(currentAlgebra.getParamClass().equals(element.getClass())){
            NotMemberException exception= new NotMemberException("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+element.getClass());
            logger.error("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+element.getClass(),exception);
            throw exception;
        }
        if (currentAlgebra.hasFlatOperation(operation)){
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
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operation+"operation type simpleflat");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operation+"operation type simpleflat",exception);
            throw exception;
        }
        return this;
    }
    /**
     * perform operation for each element in flow with two elements of type T and return result type K
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomResultFlatOperation
     * */
    @Override
    public <K> IAlgebraFlow<K> performFlatCustomResultOperation(String operationName, T sElement) {
        ICustomResultFlatOperation<K> customOperation=null;
        if(!currentAlgebra.getParamClass().equals(sElement.getClass())){
            NotMemberException exception= new NotMemberException("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+sElement.getClass());
            logger.error("Incorrect param type expected:"+currentAlgebra.getParamClass()+"found:"+sElement.getClass(),exception);
            throw exception;
        }
        if (currentAlgebra.hasCustomResultFlatOperation(operationName)){
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
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type customresultflat");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type customresultflat",exception);
            throw exception;
        }
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }

    }
    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ITransferFlatOperation
     * */
    @Override
    public <K> IAlgebraFlow<K> performFlatAlgebraTransfer(String operationName) {
        ITransferFlatOperation<K> transferOperation =null;
        if (currentAlgebra.hasAlgebraTransfer(operationName)){
            transferOperation=(ITransferFlatOperation<K>)currentAlgebra.getTransferFlatOperation(operationName);
            IFlowInvoke<K> invoke=  new IFlowInvoke<K>() {

                @Override
                public List<IAlgebraItem<K>> perform() {
                    List<IAlgebraItem<K>> flow = new ArrayList<IAlgebraItem<K>>();
                    for(IAlgebraItem<T> item:currentFlow){
                        flow.addAll(item.performAlgebraFlatTransfer(operationName));
                    }
                    currentFlow=flow;
                    return  flow;
                }
            };
            currentInvokes.add(invoke);
        }else {
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type transferflat");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type transferflat",exception);
            throw exception;
        }
        if(mathTool.hasAlgebra(transferOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(transferOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+transferOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+transferOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }
    }
    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     * @param operationName operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.IUnsafeFlatOperation
     * */
    @Override
    public <K, V> IAlgebraFlow<K> performFlatAlgebraUnsafe(String operationName, V element) {
        IUnsafeFlatOperation<K> customOperation=null;
        if (currentAlgebra.hasUnsafeFlatOperation(operationName)){
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
        }else {
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type unsafeflat");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type unsafeflat",exception);
            throw exception;
        }
        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<K>(this.mathTool,this.currentFlow,(Algebra<K>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }
    }
    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomMemberOperation
     * */
    @Override
    public <K> IAlgebraFlow<T> performCustomMemberOperation(String operationName, K sElement) {
        ICustomMemberOperation<K> customOperation=null;
        if(currentAlgebra.hasCustomMemberOperation(operationName)){
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
        }else {
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type custommeber");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type custommeber",exception);
            throw exception;
        }

        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<T>(this.mathTool,this.currentFlow,(Algebra<T>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }
    }
    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomMemberFlatOperation
     * */
    @Override
    public <K> IAlgebraFlow<T> performFlatCustomMemberOperation(String operationName, K sElement) {
        ICustomMemberFlatOperation<K> customOperation=null;
        if(currentAlgebra.hasCustomMemberFlatOperation(operationName)){
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
        }else {
            UnsupportedOperationException exception= new UnsupportedOperationException("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type custommeberflat");
            logger.error("Algebra "+currentAlgebra.getAlgebraName()+" has not operation"+operationName+"operation type custommeberflat",exception);
            throw exception;
        }

        if(mathTool.hasAlgebra(customOperation.getAlgebraName())){
            return new AlgebraFlow<T>(this.mathTool,this.currentFlow,(Algebra<T>) mathTool.getAlgebra(customOperation.getAlgebraName()),this.currentInvokes);

        }else {
            AlgebraNotExistsException  exception= new AlgebraNotExistsException("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            logger.error("Cannot find algebra"+customOperation.getAlgebraName()+" in math tool: "+mathTool.getName());
            throw exception;
        }
    }
    /**
     *collect items from flow and return it as list of strings, toString method used
     *@return List of items in string view
     * */

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
    /**
     *writes items to storage
     * @param writer
     *@see IWriter
     * */
    @Override
    public<K> void write(IWriter<K> writer) {
        writer.write(this.collectAlgebraItems());

    }
    /**
     *collects elements from flow as list of IAlgebraItems
     * @return list of IAlgebraItems
     * @see IAlgebraItem
     * */
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
    /**
     *@return class which used for current AlgebraItem  parametrization
     * */

    @Override
    public Class getCurrentAlgebraItemClass() {
        return this.currentAlgebra.getParamClass();
    }
    /**
     *@return current algebra name
     * */
    @Override
    public String getCurrentAlgebraName() {
        return this.currentAlgebra.getAlgebraName();
    }
}
