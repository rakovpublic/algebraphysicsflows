package algebraflow.imp;

import algebra.IAlgebraItem;
import algebra.IMathToolInitializer;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebraflow.IFlowInvoke;
import algebraflow.IWriter;
import algebraflow.InputFormat;
import cluster.IPart;
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
    private final MathTool mathTool;
    private static final class FlowState implements java.io.Serializable {
        private static final long serialVersionUID=1L;
        private List<? extends IAlgebraItem> values;
    }
    private final FlowState flowState;
    private final Algebra<?> currentAlgebra;
    private List<IFlowInvoke<?>> currentInvokes;

    @Override
    public IAlgebraFlow<T> performOneOperandFlatOperation(String operationName) {
        if(!currentAlgebra.hasOneOperandFlatOperation(operationName)) throw new UnsupportedOperationException("No flat one-operand operation " + operationName);
        currentInvokes.add(new IFlowInvoke<T>() {
            public String getAlgebraName() { return currentAlgebra.getAlgebraName(); }
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> result=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) result.addAll(item.performOneOperandFlatOperation(operationName));
                flowState.values=result;
                return result;
            }
        });
        return this;
    }

    @Override
    public IAlgebraFlow<T> performOneOperandOperation(String operationName) {
        if(!currentAlgebra.hasOneOperandOperation(operationName))
            throw new UnsupportedOperationException("No one-operand operation " + operationName + " in " + currentAlgebra.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<T>() {
            public String getAlgebraName() { return currentAlgebra.getAlgebraName(); }
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> result=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) result.add(item.performOneOperandOperation(operationName));
                flowState.values=result;
                return result;
            }
        });
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> IAlgebraFlow<V> performLeftProjectionOperation(String operationName,V second) {
        operations.simple.ILeftProjectionOperation<T,V> operation=(operations.simple.ILeftProjectionOperation<T,V>)currentAlgebra.getLeftProjectionOperation(operationName,second.getClass());
        if(operation==null) throw new UnsupportedOperationException("No A x B -> B operation " + operationName + " for " + second.getClass().getName());
        Algebra<V> result=(Algebra<V>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<V>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<V>> perform() {
                List<IAlgebraItem<V>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.add(item.performLeftProjectionOperation(operationName,second));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<V>(mathTool,flowState,result,currentInvokes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> IAlgebraFlow<V> performLeftProjectionFlatOperation(String operationName,V second) {
        operations.flat.ILeftProjectionFlatOperation<T,V> operation=(operations.flat.ILeftProjectionFlatOperation<T,V>)currentAlgebra.getLeftProjectionFlatOperation(operationName,second.getClass());
        if(operation==null) throw new UnsupportedOperationException("No flat A x B -> B operation " + operationName + " for " + second.getClass().getName());
        Algebra<V> result=(Algebra<V>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<V>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<V>> perform() {
                List<IAlgebraItem<V>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.addAll(item.performLeftProjectionFlatOperation(operationName,second));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<V>(mathTool,flowState,result,currentInvokes);
    }

    private AlgebraFlow(MathTool mathTool, FlowState flowState, Algebra<T> currentAlgebra, List<IFlowInvoke<?>> currentInvokes) {
        this.mathTool = mathTool;
        this.flowState = flowState;
        this.currentAlgebra = currentAlgebra;
        this.currentInvokes = currentInvokes;
    }

    public AlgebraFlow(InputFormat<T> inputFormat, IMathToolInitializer algebraInitializer, String startAlgebra) {
        flowState = new FlowState();
        mathTool = algebraInitializer.initialize();
        currentAlgebra = mathTool.getAlgebra(startAlgebra);
        currentInvokes = new LinkedList<IFlowInvoke<?>>();
        if (currentAlgebra.getParamClass().equals(inputFormat.getInputType())) {
            IFlowInvoke<T> invoke = new IFlowInvoke<T>() {
                @Override
                public String getAlgebraName() {
                    return currentAlgebra.getAlgebraName();
                }

                @Override
                public List<IAlgebraItem<T>> perform() {
                    List<IAlgebraItem<T>> flow = inputFormat.read((Algebra<T>) currentAlgebra);
                    flowState.values = flow;
                    return flow;
                }


            };
            currentInvokes.add(invoke);
        } else {
            logger.error("Incorrect param type expected:" + currentAlgebra.getParamClass() + "found:" + inputFormat.getInputType());
        }


    }

    /**
     * perform operation for each element in flow with two elements of type T and return result type T
     *
     * @param operation operation name
     * @param element   second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.simple.IOperation
     */

    @Override
    public IAlgebraFlow<T> performOperation(String operation, T element) {
        if (!currentAlgebra.getParamClass().equals(element.getClass())) {
            NotMemberException exception = new NotMemberException("Incorrect param type expected:" + currentAlgebra.getParamClass() + "found:" + element.getClass());
            logger.error("Incorrect param type expected:" + currentAlgebra.getParamClass() + "found:" + element.getClass(), exception);
            throw exception;
        }
        if (currentAlgebra.hasOperation(operation)) {
            IFlowInvoke<T> invoke = new IFlowInvoke<T>() {
                @Override
                public String getAlgebraName() {
                    return currentAlgebra.getAlgebraName();
                }

                @Override
                public List<IAlgebraItem<T>> perform() {
                    List<IAlgebraItem<T>> flow = new ArrayList<IAlgebraItem<T>>();
                    for (IAlgebraItem<T> item : flowState.values) {
                        flow.add(item.performOperation(operation, element));

                    }
                    flowState.values = flow;
                    return flow;
                }

            };
            currentInvokes.add(invoke);
        } else {
            UnsupportedOperationException exception = new UnsupportedOperationException("Algebra " + currentAlgebra.getAlgebraName() + " has not operation" + operation + "operation type simple");
            logger.error("Algebra " + currentAlgebra.getAlgebraName() + " has not operation" + operation + "operation type simple", exception);
            throw exception;
        }
        return this;
    }

    /**
     * perform custom result operation for each element in flow with two elements of type T and return result type K
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomResultOperation
     */

    @Override
    @SuppressWarnings("unchecked")
    public <K> IAlgebraFlow<K> performCustomResultOperation(String operationName, T sElement) {
        if(!currentAlgebra.getParamClass().isInstance(sElement))
            throw new NotMemberException("Incorrect second operand for " + operationName);
        ICustomResultOperation<T> operation=(ICustomResultOperation<T>)currentAlgebra.getCustomResultOperation(operationName);
        if(operation==null) throw new UnsupportedOperationException("Missing operation " + operationName);
        Algebra<K> result=(Algebra<K>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<K>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.add(item.performCustomResultOperation(operationName,sElement));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<K>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     *
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ITransferOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K> IAlgebraFlow<K> performAlgebraTransfer(String operationName) {
        ITransferOperation<T> operation=(ITransferOperation<T>)currentAlgebra.getTransferOperation(operationName);
        if(operation==null) throw new UnsupportedOperationException("Missing operation " + operationName);
        Algebra<K> result=(Algebra<K>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<K>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.add(item.performAlgebraTransfer(operationName));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<K>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.IUnsafeOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K,V> IAlgebraFlow<K> performAlgebraUnsafe(String operationName,V element) {
        IUnsafeOperation<T> operation=(IUnsafeOperation<T>)currentAlgebra.getUnsafeOperationWithParam(operationName,element==null?null:element.getClass());
        if(operation==null) throw new UnsupportedOperationException("No matching operand type for " + operationName);
        Algebra<K> result=(Algebra<K>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<K>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.add(item.performUnsafeOperation(operationName,element));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<K>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform unsafe operation for each element in flow with two elements of type T and return result type T
     *
     * @param operation operation name
     * @param element   second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.flat.IFlatOperation
     */
    @Override
    public IAlgebraFlow<T> performFlatOperation(String operation, T element) {
        if (!currentAlgebra.getParamClass().equals(element.getClass())) {
            NotMemberException exception = new NotMemberException("Incorrect param type expected:" + currentAlgebra.getParamClass() + "found:" + element.getClass());
            logger.error("Incorrect param type expected:" + currentAlgebra.getParamClass() + "found:" + element.getClass(), exception);
            throw exception;
        }
        if (currentAlgebra.hasFlatOperation(operation)) {
            IFlowInvoke<T> invoke = new IFlowInvoke<T>() {
                @Override
                public String getAlgebraName() {
                    return currentAlgebra.getAlgebraName();
                }

                @Override
                public List<IAlgebraItem<T>> perform() {
                    List<IAlgebraItem<T>> flow = new ArrayList<IAlgebraItem<T>>();
                    for (IAlgebraItem<T> item : flowState.values) {
                        flow.addAll(item.performFlatOperation(operation, element));

                    }
                    flowState.values = flow;
                    return flow;
                }
            };
            currentInvokes.add(invoke);
        } else {
            UnsupportedOperationException exception = new UnsupportedOperationException("Algebra " + currentAlgebra.getAlgebraName() + " has not operation" + operation + "operation type simpleflat");
            logger.error("Algebra " + currentAlgebra.getAlgebraName() + " has not operation" + operation + "operation type simpleflat", exception);
            throw exception;
        }
        return this;
    }

    /**
     * perform operation for each element in flow with two elements of type T and return result type K
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomResultFlatOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K> IAlgebraFlow<K> performFlatCustomResultOperation(String operationName, T sElement) {
        if(!currentAlgebra.getParamClass().isInstance(sElement))
            throw new NotMemberException("Incorrect second operand for " + operationName);
        ICustomResultFlatOperation<T> operation=(ICustomResultFlatOperation<T>)currentAlgebra.getCustomResultFlatOperation(operationName);
        if(operation==null) throw new UnsupportedOperationException("Missing operation " + operationName);
        Algebra<K> result=(Algebra<K>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<K>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.addAll(item.performCustomResultFlatOperation(operationName,sElement));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<K>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     *
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ITransferFlatOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K> IAlgebraFlow<K> performFlatAlgebraTransfer(String operationName) {
        ITransferFlatOperation<T> operation=(ITransferFlatOperation<T>)currentAlgebra.getTransferFlatOperation(operationName);
        if(operation==null) throw new UnsupportedOperationException("No flat transfer " + operationName);
        Algebra<K> result=(Algebra<K>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<K>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.addAll(item.performAlgebraFlatTransfer(operationName));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<K>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.IUnsafeFlatOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K,V> IAlgebraFlow<K> performFlatAlgebraUnsafe(String operationName,V element) {
        IUnsafeFlatOperation<T> operation=(IUnsafeFlatOperation<T>)currentAlgebra.getUnsafeFlatOperationWithParam(operationName,element==null?null:element.getClass());
        if(operation==null) throw new UnsupportedOperationException("No matching operand type for " + operationName);
        Algebra<K> result=(Algebra<K>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<K>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<K>> perform() {
                List<IAlgebraItem<K>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.addAll(item.performUnsafeFlatOperation(operationName,element));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<K>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomMemberOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> IAlgebraFlow<T> performCustomMemberOperation(String operationName,V element) {
        ICustomMemberOperation<T> operation=(ICustomMemberOperation<T>)currentAlgebra.getCustomMemberOperationWithParam(operationName,element==null?null:element.getClass());
        if(operation==null) throw new UnsupportedOperationException("No matching operand type for " + operationName);
        Algebra<T> result=(Algebra<T>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<T>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.add(item.performCustomMemberOperation(operationName,element));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<T>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomMemberFlatOperation
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> IAlgebraFlow<T> performFlatCustomMemberOperation(String operationName,V element) {
        ICustomMemberFlatOperation<T> operation=(ICustomMemberFlatOperation<T>)currentAlgebra.getCustomMemberFlatOperationWithParam(operationName,element==null?null:element.getClass());
        if(operation==null) throw new UnsupportedOperationException("No matching operand type for " + operationName);
        Algebra<T> result=(Algebra<T>)mathTool.getAlgebra(operation.getAlgebraName());
        if(result==null) throw new AlgebraNotExistsException("Missing result algebra " + operation.getAlgebraName());
        currentInvokes.add(new IFlowInvoke<T>() {
            public String getAlgebraName() { return result.getAlgebraName(); }
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> values=new ArrayList<>();
                for(IAlgebraItem<T> item : flowState.values) values.addAll(item.performCustomMemberFlatOperation(operationName,element));
                flowState.values=values;
                return values;
            }
        });
        return new AlgebraFlow<T>(mathTool,flowState,result,currentInvokes);
    }

    /**
     * collect items from flow and return it as list of strings, toString method used
     *
     * @return List of items in string view
     */

    @Override
    public List<String> collect() {
        List<String> result = new ArrayList<>();
        for (IFlowInvoke invoke : currentInvokes) {
            flowState.values = invoke.perform();
        }
        for (IAlgebraItem item : flowState.values) {
            result.add(item.perform().getResult().toString());
        }
        return result;
    }

    /**
     * writes items to storage
     *
     * @param writer
     * @see IWriter
     */
    @Override
    public <K> void write(IWriter<K> writer) {
        writer.write(this.collectAlgebraItems());

    }

    /**
     * collects elements from flow as list of IAlgebraItems
     *
     * @return list of IAlgebraItems
     * @see IAlgebraItem
     */
    @Override
    public <K> List<IAlgebraItem<K>> collectAlgebraItems() {
        List<IAlgebraItem<K>> result = new ArrayList<>();
        for (IFlowInvoke invoke : currentInvokes) {
            flowState.values = invoke.perform();
        }
        for (IAlgebraItem<K> item : flowState.values) {
            result.add(item.perform());
        }
        return result;
    }

    /**
     * @return class which used for current AlgebraItem  parametrization
     */

    @Override
    public Class getCurrentAlgebraItemClass() {
        return this.currentAlgebra.getParamClass();
    }

    /**
     * @return current algebra name
     */
    @Override
    public String getCurrentAlgebraName() {
        return this.currentAlgebra.getAlgebraName();
    }

    /**
     * Set part of input for worker.
     *
     * @param part part of input
     * @see IPart
     */
    @Override
    public void setInput(IPart<T> part) {
        Algebra<T> algebra = (Algebra<T>) mathTool.getAlgebra(this.getInitAlgebraName());
        IFlowInvoke<T> invoke = new IFlowInvoke<T>() {
            @Override
            public String getAlgebraName() {
                return currentAlgebra.getAlgebraName();
            }

            @Override
            public List<IAlgebraItem<T>> perform() {
                List<IAlgebraItem<T>> flow = new ArrayList<>();
                for (T item : part.getContent()) {
                    flow.add(algebra.buildAlgebraItem(item));
                }
                flowState.values = flow;
                return flow;
            }
        };
        currentInvokes.set(0, invoke);
    }

    @Override
    public String getInitAlgebraName() {
        return currentInvokes.get(0).getAlgebraName();
    }
}
