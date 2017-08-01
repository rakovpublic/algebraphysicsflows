package field.imp;

import field.IField;
import operations.simple.IOperation;
import ring.IRing;
import ring.imp.Ring;
import rules.IValidationRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 28.07.2017.
 */
public class FieldBuilder<T> {
    private IOperation<T> multOperation;
    private IOperation<T> sumOperation;
    private List<IValidationRule<T>> memberRules;
    private T neutralElement;
    private  T neutralMultElement;

    public FieldBuilder(IOperation<T> multOperation, IOperation<T> sumOperation, List<IValidationRule<T>> memberRules, T neutralElement, T neutralMultElement) {
        this.multOperation = multOperation;
        this.sumOperation = sumOperation;
        this.memberRules = memberRules;
        this.neutralElement = neutralElement;
        this.neutralMultElement = neutralMultElement;
    }

    public IField<T> buildMember(T element){
        return new Field<T>(this.sumOperation,this.multOperation,this.memberRules,element);
    }
    public IField<T> buildNeutralMember(){
        return new Field<T>(this.sumOperation,this.multOperation,this.memberRules,this.neutralElement);
    }
    public IField<T> buildNeutralMultMember(){
        return new Field<T>(this.sumOperation,this.multOperation,this.memberRules,this.neutralMultElement);
    }
    public List<IField<T>> buildRing(List<T> members){
        LinkedList<IField<T>> res= new LinkedList<IField<T>>();
        for(T elem:members){
            res.add(this.buildMember(elem));
        }
        return res;
    }
}
