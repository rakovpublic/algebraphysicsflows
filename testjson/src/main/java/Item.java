public class Item {
    private InnerItem innerItem;

    public Item() {
    }

    public Item(InnerItem innerItem) {
        this.innerItem = innerItem;
    }

    public InnerItem getInnerItem() {
        return innerItem;
    }

    public void setInnerItem(InnerItem innerItem) {
        this.innerItem = innerItem;
    }

    @Override
    public String toString() {
        return "Item{" +
                "innerItem=" + innerItem.toString() +
                '}';
    }
}
