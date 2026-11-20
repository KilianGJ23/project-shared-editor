package co.edu.uptc.structures;

public class NodeAVL<T> {
    private T value;
    private int balanceFactor;
    private NodeAVL<T> left;
    private NodeAVL<T> right;

    public NodeAVL(T value) {
        this.value = value;
        this.balanceFactor = 0;
        this.left = null;
        this.right = null;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public int getBalanceFactor() {
        return balanceFactor;
    }

    public void setBalanceFactor(int balanceFactor) {
        this.balanceFactor = balanceFactor;
    }

    public NodeAVL<T> getLeft() {
        return left;
    }

    public void setLeft(NodeAVL<T> left) {
        this.left = left;
    }

    public NodeAVL<T> getRight() {
        return right;
    }

    public void setRight(NodeAVL<T> right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return "" + value;
    }

}
