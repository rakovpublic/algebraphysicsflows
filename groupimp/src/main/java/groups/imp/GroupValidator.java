package groups.imp;

import exceptions.InvalidGroupException;
import groups.IGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 29.03.2017.
 */
public class GroupValidator <T >{
    private GroupBuilder<T> groupBuilder;
    private static final Logger logger = LogManager.getLogger(InfiniteGroupValidator.class);
    private List<IGroup<T>> groupElements;
    public GroupValidator(GroupBuilder<T> builder, List<T> groupElements){
        groupBuilder=builder;
        buildElements(builder,groupElements);
    }
    private void buildElements(GroupBuilder<T> builder, List<T> elements){
        groupElements= new LinkedList<IGroup<T>>();
        for (T element:elements){
            IGroup<T> gMember= builder.buildGroupMember(element);
            if(gMember==null){
                throw  new InvalidGroupException();
            }
            groupElements.add(gMember);
        }

    }
    public boolean validateGroupLocked() {
        for (IGroup<T> fMeber:groupElements){
            for(IGroup<T> sMeber:groupElements){
                if(!groupElements.contains(fMeber.multiply(sMeber))){
                    return false;
                }
            }
        }
        return true;
    }


    public boolean validateGroupAssociativity() {
        for (IGroup<T> fMeber:groupElements){
            for(IGroup<T> sMeber:groupElements){
                for(IGroup<T> tMeber:groupElements){
                    IGroup<T> fResult=fMeber.multiply(sMeber.multiply(tMeber));
                    IGroup<T> sResult=fMeber.multiply(sMeber).multiply(tMeber);
                    if(!fResult.equals(sResult)){
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public boolean validateGroupReversalElement() {
        IGroup<T> eElement= groupBuilder.buildEMember();
        for (IGroup<T> fMeber:groupElements){
            boolean flag=false;
            for(IGroup<T> sMeber:groupElements){
                if(fMeber.multiply(sMeber).equals(eElement)){
                    flag=true;
                }
            }
            if(!flag){
                return false;
            }
        }
        return true;
    }


    public boolean validateGroupEElement() {
        IGroup<T> eElement= groupBuilder.buildEMember();
        for (IGroup<T> fMeber:groupElements){
            if(!fMeber.equals(fMeber.multiply(eElement))||!fMeber.equals(eElement.multiply(fMeber))){
                return false;
            }
        }
        return true;
    }
    public Boolean isAbbelGroup() {
        for (IGroup<T> fMeber:groupElements){
            for(IGroup<T> sMeber:groupElements){
                if(!fMeber.multiply(sMeber).equals(sMeber.multiply(fMeber))){
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isLeftIdeal(List<T> elements){
        return true;
    }
    public boolean isRightIdeal(List<T> elements){
        return true;
    }
    public boolean isIdeal(List<T> elements){
        return true;
    }

}
