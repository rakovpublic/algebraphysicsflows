package groups.imp;

import exceptions.NotMemberException;
import groups.IGroup;
import rules.IValidationRule;
import operations.simple.IOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 23.03.2017.
 */
public  class Group<T> implements IGroup<T> {
    private static final Logger logger = LogManager.getLogger(Group.class);
    private T groupMemberValue;
    private IOperation<T> multiplyOperation;
    private List<IValidationRule<T>> memberRules= new ArrayList<IValidationRule<T>>();

    Group(IOperation<T> operation, List<IValidationRule<T>> memberRules, T groupMemberValue){
        this.multiplyOperation=operation;
        this.memberRules=memberRules;
        if(checkRules(groupMemberValue)){
            this.groupMemberValue=groupMemberValue;
        }else {
            throw new NotMemberException();
        }
    }

    private boolean checkRules(T member){
        for(IValidationRule<T> rule:memberRules){
            if(!rule.validate(member)){
                logger.error("Element "+member.toString()+" is not match this rule "+rule.getDescription() +"  i.e. not member of this group.");
                return false;
            }
        }
        return true;
    }
    private IGroup<T> getNewMember(T member){
        IGroup<T> res= null;
        try {
            res=new Group<T>(this.multiplyOperation,this.memberRules,member);
        }catch (NotMemberException ex){
            logger.error("Element value"+member.toString()+" is not matches rules i.e. not member of this group. Please check group rules and member value.",ex);
        }
        return  res;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group<?> group = (Group<?>) o;

        if (groupMemberValue != null ? !groupMemberValue.equals(group.groupMemberValue) : group.groupMemberValue != null)
            return false;
        if (multiplyOperation != null ? !multiplyOperation.equals(group.multiplyOperation) : group.multiplyOperation != null)
            return false;
        return memberRules != null ? memberRules.equals(group.memberRules) : group.memberRules == null;

    }

    @Override
    public int hashCode() {
        int result = groupMemberValue != null ? groupMemberValue.hashCode() : 0;
        result = 31 * result + (multiplyOperation != null ? multiplyOperation.hashCode() : 0);
        result = 31 * result + (memberRules != null ? memberRules.hashCode() : 0);
        return result;
    }


    @Override
    public IGroup<T> multiply(IGroup<T> groupMember) {
        IGroup<T> res=null;
        if(groupMember!=null){
            try{
                res=this.getNewMember(multiplyOperation.performOperation(groupMember.getMemberValue(), this.groupMemberValue));
            }catch (NotMemberException ex){
                logger.error("Group is not locked i.e. element "+groupMember.getMemberValue().toString() +" is not match some rules check log please(Not member of this group). Also it means this group not valid.");
                return null;
            }
        }
        return res;
    }

    @Override
    public List<IValidationRule<T>> getMemberRules() {
        return this.memberRules;
    }



    @Override
    public T getMemberValue() {
        return this.groupMemberValue;
    }
}
