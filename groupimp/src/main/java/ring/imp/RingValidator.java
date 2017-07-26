package ring.imp;

import ring.IRing;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 17.07.2017.
 */
public class RingValidator<T> {
    private RingBuilder<T> ringBuilder;
    private List<IRing<T>> ringList;

    public RingValidator(RingBuilder<T> ringBuilder, List<T> ringList) {
        this.ringBuilder = ringBuilder;
        this.ringList = ringBuilder.buildRing(ringList);
    }

    public boolean isCommutativeSum() {
        for (int i = 0; i < ringList.size(); i++) {
            for (int j = 0; j < ringList.size(); j++) {
                if (!ringList.get(i).sum(ringList.get(j)).getMemberValue().equals(ringList.get(j).sum(ringList.get(i)).getMemberValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAssociativeSum() {
        for (int i = 0; i < ringList.size(); i++) {
            for (int j = 0; j < ringList.size(); j++) {
                for (int k = 0; i < ringList.size(); k++) {
                    if (!ringList.get(i).sum(ringList.get(j)).sum(ringList.get(k)).getMemberValue().equals(ringList.get(k).sum(ringList.get(j).sum(ringList.get(i))).getMemberValue())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean hasNeutral() {
        for(int i=0;i<ringList.size();i++){
            if(!ringList.get(i).sum(ringBuilder.buildNeutralMember()).getMemberValue().equals(ringList.get(i).getMemberValue())){
                return false;
            }
        }
        return true;
    }

    public boolean hasRevers() {
        for(int i=0;i<ringList.size();i++){
            boolean flag=false;
            for (int j=0;j<ringList.size();j++){
                if(ringList.get(i).sum(ringList.get(j)).getMemberValue().equals(ringBuilder.buildNeutralMember().getMemberValue())){
                    flag=true;
                    break;
                }
            }
            if (!flag){
                return false;
            }
        }
        return true;
    }

    public boolean isAssociativeMult() {
        for(int i=0;i<ringList.size();i++){
            for (int j=0;j<ringList.size();j++){
                for(int k=0;k<ringList.size();k++){
                    if(!ringList.get(i).multiply(ringList.get(j)).multiply(ringList.get(k)).getMemberValue().equals(ringList.get(j).multiply(ringList.get(k)).multiply(ringList.get(i)).getMemberValue()))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isDistributive() {
        for(int i=0;i<ringList.size();i++){
            for (int j=0;j<ringList.size();j++){
                for(int k=0;k<ringList.size();k++){
                    if(!ringList.get(i).multiply(ringList.get(j)).sum(ringList.get(k)).getMemberValue().equals(ringList.get(i).multiply(ringList.get(j)).sum(ringList.get(i).multiply(ringList.get(k))).getMemberValue()))
                    {
                        return false;
                    }
                    if(!ringList.get(i).multiply(ringList.get(j)).sum(ringList.get(k)).getMemberValue().equals(ringList.get(j).multiply(ringList.get(i)).sum(ringList.get(k).multiply(ringList.get(i))).getMemberValue()))
                    {
                        return false;
                    }
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
    public IdealMeta getIdealMeta(List<T> elements){
        return null;
    }

}
