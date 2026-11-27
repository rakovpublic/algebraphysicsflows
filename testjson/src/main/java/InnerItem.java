import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = InnerItemDeserializer.class)
public class InnerItem {
    private String className;
    private InnerInnerItem innerItem;

    public InnerItem(String className, InnerInnerItem innerItem) {
        this.className = className;
        this.innerItem = innerItem;
    }

    public InnerItem() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public InnerInnerItem getInnerItem() {
        return innerItem;
    }

    public void setInnerItem(InnerInnerItem innerItem) {
        this.innerItem = innerItem;
    }

    @Override
    public String toString() {
        return "InnerItem{" +
                "className='" + className + '\'' +
                ", innerItem=" + innerItem.toString() +
                '}';
    }
}
