package co.edu.uptc.structures;

import java.util.Objects;

/** Lista doblemente enlazada: cada nodo conoce su anterior y su siguiente. */
public class MyDoubleList<T> {
	private DoubleNode<T> head;

	public MyDoubleList() {
		this.head = null;
	}

	public boolean addFirst(T value) {
		DoubleNode<T> newNode = new DoubleNode<>(value);

		if (head == null) {
			head = newNode;
		} else {
			newNode.setNext(head);
			head.setPrevious(newNode);
			head = newNode;
		}

		return true;
	}

	public boolean addFinal(T value) {
		DoubleNode<T> newNode = new DoubleNode<>(value);

		if (head == null) {
			head = newNode;
		} else {
			DoubleNode<T> current = head;
			while (current.getNext() != null) {
				current = current.getNext();
			}
			newNode.setPrevious(current);
			current.setNext(newNode);
		}

		return true;
	}

	public boolean removeFirst() {
		if (head == null) {
			return false;
		}

		if (head.getNext() == null) {
			head = null;
			return true;
		}

		DoubleNode<T> next = head.getNext();
		next.setPrevious(null);
		head.setNext(null);
		head = next;

		return true;
	}

	public boolean removeFinal() {
		if (head == null) {
			return false;
		}

		if (head.getNext() == null) {
			head = null;
			return true;
		}

		DoubleNode<T> current = head;
		while (current.getNext() != null) {
			current = current.getNext();
		}

		current.getPrevious().setNext(null);
		current.setPrevious(null);

		return true;
	}

	public int size() {
		int count = 0;
		DoubleNode<T> current = head;
		while (current != null) {
			count++;
			current = current.getNext();
		}
		return count;
	}

	public boolean contains(T value) {
		DoubleNode<T> current = head;
		while (current != null) {
			if (Objects.equals(value, current.getValue())) {
				return true;
			}
			current = current.getNext();
		}
		return false;
	}

	public boolean isEmpty() {
		return head == null;
	}

	@Override
	public String toString() {
		if (head == null) {
			return "DoubleList [head=nil]";
		}

		StringBuilder sb = new StringBuilder("DoubleList [head=");
		DoubleNode<T> current = head;
		while (current != null) {
			sb.append("[").append(current.getValue()).append("]");
			if (current.getNext() != null) {
				sb.append(" <-> ");
			}
			current = current.getNext();
		}
		sb.append("]");
		return sb.toString();
	}
}
