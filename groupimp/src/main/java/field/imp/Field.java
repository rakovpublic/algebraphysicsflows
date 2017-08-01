package field.imp;

import field.IField;
import operations.simple.IOperation;
import ring.IRing;
import ring.imp.Ring;
import rules.IValidationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 28.07.2017.
 */
public class Field<T> implements IField<T> {
    private IOperation<T> sumOperation=null;
    private  IOperation<T> multOperation=null;
    private List<IValidationRule<T>> memberRules= new ArrayList<IValidationRule<T>>();
    private T ringMember=null;
    Field(IOperation<T> sumOperation, IOperation<T> multOperation, List<IValidationRule<T>> memberRules, T ringMember) {
        this.sumOperation = sumOperation;
        this.multOperation = multOperation;
        this.memberRules = memberRules;
        this.ringMember = ringMember;
    }

    private IField<T> getNewMember(T member){
        IField<T> res=null;
        if(member!=null){
            res=new Field(this.sumOperation,this.multOperation,this.memberRules,member);
        }
        return res;
    }
    @Override
    public IField<T> multiply(IField<T> ringMember) {
        IField<T> res=null;
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
    public IField<T> sum(IField<T> ringMember) {
        IField<T> res=null;
        if(ringMember!=null){
            res= getNewMember(this.sumOperation.performOperation(this.ringMember,ringMember.getMemberValue()));
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field<?> field = (Field<?>) o;

        if (sumOperation != null ? !sumOperation.equals(field.sumOperation) : field.sumOperation != null) return false;
        if (multOperation != null ? !multOperation.equals(field.multOperation) : field.multOperation != null)
            return false;
        if (memberRules != null ? !memberRules.equals(field.memberRules) : field.memberRules != null) return false;
        return ringMember != null ? ringMember.equals(field.ringMember) : field.ringMember == null;

    }

    @Override
    public int hashCode() {
        int result = sumOperation != null ? sumOperation.hashCode() : 0;
        result = 31 * result + (multOperation != null ? multOperation.hashCode() : 0);
        result = 31 * result + (memberRules != null ? memberRules.hashCode() : 0);
        result = 31 * result + (ringMember != null ? ringMember.hashCode() : 0);
        return result;
    }
}
