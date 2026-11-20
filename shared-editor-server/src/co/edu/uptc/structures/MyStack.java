package co.edu.uptc.structures;

/** Pila enlazada: inserta y remueve siempre por el tope (LIFO). */
public class MyStack<T> {
	private Node<T> top;
	private int size;

	public boolean push(T value) {
		Node<T> newTop = new Node<>(value);
		newTop.setNext(top);
		top = newTop;
		size++;
		return true;
	}

	public T pop() {
		if (isEmpty()) {
			return null;
		}
		T value = top.getValue();
		top = top.getNext();
		size--;
		return value;
	}

	public T peek() {
		return isEmpty() ? null : top.getValue();
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return top == null;
	}
}
