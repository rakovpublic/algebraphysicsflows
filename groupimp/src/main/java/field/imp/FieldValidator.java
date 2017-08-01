package field.imp;

import field.IField;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.07.2017.
 */
public class FieldValidator<T> {
    private FieldBuilder<T> fieldBuilder;
    private List<IField<T>> fieldList;

    public FieldValidator(FieldBuilder<T> ringBuilder, List<T> ringList) {
        this.fieldBuilder = ringBuilder;
        this.fieldList = ringBuilder.buildRing(ringList);
    }

    public boolean isCommutativeSum() {
        for (int i = 0; i < fieldList.size(); i++) {
            for (int j = 0; j < fieldList.size(); j++) {
                if (!fieldList.get(i).sum(fieldList.get(j)).getMemberValue().equals(fieldList.get(j).sum(fieldList.get(i)).getMemberValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAssociativeSum() {
        for (int i = 0; i < fieldList.size(); i++) {
            for (int j = 0; j < fieldList.size(); j++) {
                for (int k = 0; i < fieldList.size(); k++) {
                    if (!fieldList.get(i).sum(fieldList.get(j)).sum(fieldList.get(k)).getMemberValue().equals(fieldList.get(k).sum(fieldList.get(j).sum(fieldList.get(i))).getMemberValue())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean hasNeutralSum() {
        for(int i = 0; i< fieldList.size(); i++){
            if(!fieldList.get(i).sum(fieldBuilder.buildNeutralMember()).getMemberValue().equals(fieldList.get(i).getMemberValue())){
                return false;
            }
        }
        return true;
    }

    public boolean hasReversSum() {
        for(int i = 0; i< fieldList.size(); i++){
            boolean flag=false;
            for (int j = 0; j< fieldList.size(); j++){
                if(fieldList.get(i).sum(fieldList.get(j)).getMemberValue().equals(fieldBuilder.buildNeutralMember().getMemberValue())){
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
    public boolean isCommutativeMult(){
        for (int i = 0; i < fieldList.size(); i++) {
            for (int j = 0; j < fieldList.size(); j++) {
                if (!fieldList.get(i).multiply(fieldList.get(j)).getMemberValue().equals(fieldList.get(j).multiply(fieldList.get(i)).getMemberValue())) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean hasNeutralMult() {
        for(int i = 0; i< fieldList.size(); i++){
            if(!fieldList.get(i).multiply(fieldBuilder.buildNeutralMultMember()).getMemberValue().equals(fieldList.get(i).getMemberValue())){
                return false;
            }
        }
        return true;
    }

    public boolean hasReversMult() {
        for(int i = 0; i< fieldList.size(); i++){
            boolean flag=false;
            for (int j = 0; j< fieldList.size(); j++){
                if(fieldList.get(i).multiply(fieldList.get(j)).getMemberValue().equals(fieldBuilder.buildNeutralMultMember().getMemberValue())){
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
        for(int i = 0; i< fieldList.size(); i++){
            for (int j = 0; j< fieldList.size(); j++){
                for(int k = 0; k< fieldList.size(); k++){
                    if(!fieldList.get(i).multiply(fieldList.get(j)).multiply(fieldList.get(k)).getMemberValue().equals(fieldList.get(j).multiply(fieldList.get(k)).multiply(fieldList.get(i)).getMemberValue()))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isDistributive() {
        for(int i = 0; i< fieldList.size(); i++){
            for (int j = 0; j< fieldList.size(); j++){
                for(int k = 0; k< fieldList.size(); k++){
                    if(!fieldList.get(i).multiply(fieldList.get(j)).sum(fieldList.get(k)).getMemberValue().equals(fieldList.get(i).multiply(fieldList.get(j)).sum(fieldList.get(i).multiply(fieldList.get(k))).getMemberValue()))
                    {
                        return false;
                    }
                    if(!fieldList.get(i).multiply(fieldList.get(j)).sum(fieldList.get(k)).getMemberValue().equals(fieldList.get(j).multiply(fieldList.get(i)).sum(fieldList.get(k).multiply(fieldList.get(i))).getMemberValue()))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
