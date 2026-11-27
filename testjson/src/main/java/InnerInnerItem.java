public class InnerInnerItem {
    private String string;
    private Integer aDouble;

    public InnerInnerItem() {
    }

    public InnerInnerItem(String string, Integer aDouble) {
        this.string = string;
        this.aDouble = aDouble;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Integer getaDouble() {
        return aDouble;
    }

    public void setaDouble(Integer aDouble) {
        this.aDouble = aDouble;
    }

    @Override
    public String toString() {
        return "InnerInnerItem{" +
                "string='" + string + '\'' +
                ", aDouble=" + aDouble +
                '}';
    }
}
