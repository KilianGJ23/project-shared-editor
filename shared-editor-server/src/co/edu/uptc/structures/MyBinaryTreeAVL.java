package co.edu.uptc.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MyBinaryTreeAVL<T> {
    private NodeAVL<T> root;
    private Comparator<T> comparator;

    public MyBinaryTreeAVL() {
        this.root = null;
        this.comparator = null;
    }

    public MyBinaryTreeAVL(Comparator<T> comparator) {
        this.root = null;
        this.comparator = comparator;
    }

    private int compare(T a, T b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        }
        return ((Comparable<T>) a).compareTo(b);
    }

    public int balanceFactor() {
        return root == null ? 0 : balanceFactor(root);
    }

    private int balanceFactor(NodeAVL<T> node) {
        if (node == null) {
            return 0;
        }
        return height(node.getLeft()) - height(node.getRight());
    }

    public int height() {
        return height(root);
    }

    public int height(NodeAVL<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(height(node.getLeft()), height(node.getRight()));
    }

    public boolean add(T value) {
        if (contains(value)) {
            return false;
        }
        root = add(root, value);
        return true;
    }

    private NodeAVL<T> add(NodeAVL<T> current, T value) {
        if (current == null) {
            return new NodeAVL<>(value);
        }

        int cmp = compare(value, current.getValue());
        if (cmp < 0) {
            current.setLeft(add(current.getLeft(), value));
        } else if (cmp > 0) {
            current.setRight(add(current.getRight(), value));
        } else {
            return current;
        }

        return rebalance(current);
    }

    public boolean remove(T value) {
        if (!contains(value)) {
            return false;
        }
        root = remove(root, value);
        return true;
    }

    private NodeAVL<T> remove(NodeAVL<T> current, T value) {
        if (current == null) {
            return null;
        }

        int cmp = compare(value, current.getValue());
        if (cmp < 0) {
            current.setLeft(remove(current.getLeft(), value));
        } else if (cmp > 0) {
            current.setRight(remove(current.getRight(), value));
        } else {
            if (current.getLeft() == null && current.getRight() == null) {
                return null;
            }

            if (current.getLeft() == null) {
                return current.getRight();
            }
            if (current.getRight() == null) {
                return current.getLeft();
            }

            T minRight = findMin(current.getRight());
            current.setValue(minRight);
            current.setRight(remove(current.getRight(), minRight));
        }
        return rebalance(current);
    }

    private T findMin(NodeAVL<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node.getValue();
    }

    public boolean contains(T value) {
        return find(value) != null;
    }

    public T find(T value) {
        return find(root, value);
    }

    private T find(NodeAVL<T> current, T value) {
        if (current == null) {
            return null;
        }

        int cmp = compare(value, current.getValue());
        if (cmp < 0) {
            return find(current.getLeft(), value);
        } else if (cmp > 0) {
            return find(current.getRight(), value);
        } else {
            return current.getValue();
        }
    }

    public List<T> inOrder() {
        List<T> list = new ArrayList<>();
        inOrder(root, list);
        return list;
    }

    private void inOrder(NodeAVL<T> current, List<T> list) {
        if (current != null) {
            inOrder(current.getLeft(), list);
            list.add(current.getValue());
            inOrder(current.getRight(), list);
        }
    }

    public boolean isEmpty() {
        return root == null;
    }

    public NodeAVL<T> getRoot() {
        return root;
    }

    public void setRoot(NodeAVL<T> root) {
        this.root = root;
        updateBalanceFactors(this.root);
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    private NodeAVL<T> rotateRight(NodeAVL<T> node) {
        NodeAVL<T> newRoot = node.getLeft();
        NodeAVL<T> movedSubtree = newRoot.getRight();

        newRoot.setRight(node);
        node.setLeft(movedSubtree);

        updateBalanceFactor(node);
        updateBalanceFactor(newRoot);

        return newRoot;
    }

    private NodeAVL<T> rotateLeft(NodeAVL<T> node) {
        NodeAVL<T> newRoot = node.getRight();
        NodeAVL<T> movedSubtree = newRoot.getLeft();

        newRoot.setLeft(node);
        node.setRight(movedSubtree);

        updateBalanceFactor(node);
        updateBalanceFactor(newRoot);

        return newRoot;
    }

    private NodeAVL<T> rebalance(NodeAVL<T> node) {
        updateBalanceFactor(node);

        int factor = node.getBalanceFactor();

        if (factor > 1) {
            if (node.getLeft().getBalanceFactor() < 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }
            return rotateRight(node);
        }

        if (factor < -1) {
            if (node.getRight().getBalanceFactor() > 0) {
                node.setRight(rotateRight(node.getRight()));
            }
            return rotateLeft(node);
        }

        return node;
    }

    private void updateBalanceFactor(NodeAVL<T> node) {
        node.setBalanceFactor(height(node.getLeft()) - height(node.getRight()));
    }

    private void updateBalanceFactors(NodeAVL<T> node) {
        if (node == null) {
            return;
        }
        updateBalanceFactors(node.getLeft());
        updateBalanceFactors(node.getRight());
        updateBalanceFactor(node);
    }
}
