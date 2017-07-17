package ring.imp;

import operations.simple.IOperation;
import ring.IRing;
import rules.IValidationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 27.03.2017.
 */
public class Ring<T extends Object> implements IRing<T> {

    private IOperation<T> sumOperation=null;
    private  IOperation<T> multOperation=null;
    private List<IValidationRule<T>> memberRules= new ArrayList<IValidationRule<T>>();
    private T ringMember=null;

    Ring(IOperation<T> sumOperation, IOperation<T> multOperation, List<IValidationRule<T>> memberRules, T ringMember) {
        this.sumOperation = sumOperation;
        this.multOperation = multOperation;
        this.memberRules = memberRules;
        this.ringMember = ringMember;
    }

private IRing<T> getNewMember(T member){
    IRing<T> res=null;
    if(member!=null){
        res=new Ring(this.sumOperation,this.multOperation,this.memberRules,member);
    }
    return res;
}
    @Override
    public IRing<T> multiply(IRing<T> ringMember) {
        IRing<T> res=null;
        if(ringMember!=null){
            res= getNewMember(this.multOperation.performOperation(this.ringMember,ringMember.getMemberValue()));
        }
        return res;
    }

    @Override
    public List<IValidationRule<T>> getMemberRules() {
        return this.memberRules;
    }

    @Override
    public T getMemberValue() {
        return this.ringMember;
    }

    @Override
    public IRing<T> sum(IRing<T> ringMember) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ring<?> ring = (Ring<?>) o;

        if (!sumOperation.equals(ring.sumOperation)) return false;
        if (!multOperation.equals(ring.multOperation)) return false;
        if (memberRules != null ? !memberRules.equals(ring.memberRules) : ring.memberRules != null) return false;
        return ringMember.equals(ring.ringMember);

    }

    @Override
    public int hashCode() {
        int result = sumOperation.hashCode();
        result = 31 * result + multOperation.hashCode();
        result = 31 * result + (memberRules != null ? memberRules.hashCode() : 0);
        result = 31 * result + ringMember.hashCode();
        return result;
    }
}
