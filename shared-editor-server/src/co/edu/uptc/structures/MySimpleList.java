package co.edu.uptc.structures;

public class MySimpleList<T> {
    private Node<T> head;

    public MySimpleList() {
        this.head = null;
    }

    public void insert(T value) {
        Node<T> newNode = new Node<T>(value);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
    }

    public boolean exits(T value) {
        boolean exits = false;
        Node<T> current = head;
        if (head != null) 
            if (current.getValue().equals(value)) {
                exits = true;
            } else {
                while (current.getNext() != null) {
                    current = current.getNext();
                    if (current.getValue().equals(value)) {
                        exits = true;
                    }
                }
            }
        return exits;
    }

    public boolean remove(T value) {
        boolean validation = false;
        Node<T> current = head;
        Node<T> preview = null;

        if (head != null && head.getValue().equals(value)) {
            head = head.getNext();
            validation = true;
        } else {
            while (current != null && !validation) {
                if (current.getValue().equals(value)) {
                    preview.setNext(current.getNext());
                    validation = true;
                }
                preview = current;
                current = current.getNext();
            }
        }
        return validation;
    }

    public boolean isEmpty() {
        boolean empty = false;
        if (head == null) {
            empty = true;
        }
        return empty;
    }

    public void printList() {
        Node<T> current = head;
        while (current != null) {
            System.out.print(current.getValue() + " --> ");
            current = current.getNext();
        }
        System.out.println("null");
    }
}
