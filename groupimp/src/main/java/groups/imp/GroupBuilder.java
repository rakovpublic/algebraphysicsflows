package groups.imp;

import exceptions.NotMemberException;
import groups.IGroup;
import rules.IValidationRule;
import operations.simple.IOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 25.03.2017.
 */
public class GroupBuilder<T > {
    private IOperation<T> multiplyOperation;
    private List<IValidationRule<T>> memberRules;
    private static final Logger logger = LogManager.getLogger(GroupBuilder.class);
    private T eElement;

    public GroupBuilder(IOperation<T> multiplyOperation, List<IValidationRule<T>> memberRules, T eElement) {
        this.multiplyOperation = multiplyOperation;
        this.memberRules = memberRules;
        this.eElement=eElement;
    }
    public IGroup<T> buildGroupMember(T member){
        IGroup<T> res= null;
        try {
            res=new Group<T>(this.multiplyOperation,this.memberRules,member);
        }catch (NotMemberException ex){
            logger.error("Element value"+member.toString()+" is not matches rules(Element is not member of this group). Please check group rules and element value.",ex);
        }
        return  res;
    }

    public void setElement(T eElement) {
        this.eElement = eElement;
    }
    public IGroup<T> buildEMember(){
        return  buildGroupMember(eElement);
    }
}
