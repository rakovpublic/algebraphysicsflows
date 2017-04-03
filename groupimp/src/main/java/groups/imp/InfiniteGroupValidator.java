package groups.imp;

import groups.IGroup;
import rules.ILockedRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Rakovskyi Dmytro on 25.03.2017.
 */
public class InfiniteGroupValidator<T>{
    private GroupBuilder<T> groupBuilder;
    private static final Logger logger = LogManager.getLogger(InfiniteGroupValidator.class);
    public InfiniteGroupValidator(GroupBuilder<T> builder ){
        groupBuilder=builder;
    }
    public boolean validateGroupLocked(ILockedRule<T> lockedRule) {
        return lockedRule.validate();
    }


    public boolean validateGroupAssociativity(T fMember, T sMember,T tMember) {
        IGroup<T> firstMember= groupBuilder.buildGroupMember(fMember);
        IGroup<T> secondMember=groupBuilder.buildGroupMember(sMember);
        IGroup<T> thirdMember=groupBuilder.buildGroupMember(tMember);
        if(firstMember==null||secondMember==null||thirdMember==null){
            logger.error("one of the following elements is not member of the group: "+fMember.toString()+" , "+sMember.toString()+" , "+tMember.toString());
            return false;
        }
        IGroup<T> fResult=firstMember.multiply(secondMember.multiply(thirdMember));
        IGroup<T> sResult=firstMember.multiply(secondMember).multiply(thirdMember);
        return fResult.equals(sResult);
    }


    public boolean validateGroupReversalElement(T reversalElement,T element) {
        IGroup<T> reversalMember=groupBuilder.buildGroupMember(reversalElement);
        IGroup<T> member=groupBuilder.buildGroupMember(element);
        IGroup<T> eMember=groupBuilder.buildEMember();
        if(reversalElement==null||member==null||eMember==null){
            logger.error("one of the following elements is not member of the group: "+reversalMember.toString()+" , "+member.toString()+" , "+eMember.toString());
            return false;
        }
        return eMember.equals(member.multiply(reversalMember));
    }


    public boolean validateGroupEElement(T element) {
        IGroup<T> member=groupBuilder.buildGroupMember(element);
        IGroup<T> eMember=groupBuilder.buildEMember();
        if(member==null||eMember==null){
            logger.error("one of the following elements is not member of the group: "+member.toString()+" , "+eMember.toString());
            return false;
        }
        return member.equals(member.multiply(eMember))&&member.equals(eMember.multiply(member));
    }
    public Boolean isAbbelGroup(T fElement, T sElement) {
        IGroup<T> fMember=groupBuilder.buildGroupMember(fElement);
        IGroup<T> sMember=groupBuilder.buildGroupMember(sElement);
        IGroup<T> eMember=groupBuilder.buildEMember();
        if(fMember==null||sMember==null||eMember==null) {
            logger.error("one of the following elements is not member of the group: " + fMember.toString() + " , " + sMember.toString() + " , " + eMember);
            return false;
        }
        if(fMember.equals(eMember)||sElement.equals(eMember)){
            logger.error("Cannot check abell group with eMember");
            return null;
        }
        return fMember.multiply(sMember).equals(sMember.multiply(fMember));
    }

}
