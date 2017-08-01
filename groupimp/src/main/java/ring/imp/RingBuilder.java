package ring.imp;

import operations.simple.IOperation;
import ring.IRing;
import rules.IValidationRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 17.07.2017.
 */
public class RingBuilder<T> {
    private IOperation<T> multOperation;
    private IOperation<T> sumOperation;
    private List<IValidationRule<T>> memberRules;
    private T neutralElement;

    public RingBuilder(IOperation<T> multOperation, IOperation<T> sumOperation, List<IValidationRule<T>> memberRules, T neutralElement) {
        this.multOperation = multOperation;
        this.sumOperation = sumOperation;
        this.memberRules = memberRules;
        this.neutralElement = neutralElement;
    }
    public IRing<T> buildMember(T element){
        return new Ring<T>(this.sumOperation,this.multOperation,this.memberRules,element);
    }
    public IRing<T> buildNeutralMember(){
        return new Ring<T>(this.sumOperation,this.multOperation,this.memberRules,this.neutralElement);
    }
    public List<IRing<T>> buildRing(List<T> members){
        LinkedList<IRing<T>> res= new LinkedList<IRing<T>>();
        for(T elem:members){
            res.add(this.buildMember(elem));
        }
        return res;
    }
}
