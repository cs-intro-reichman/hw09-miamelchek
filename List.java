/** A linked list of character data objects. */
public class List {

    // Points to the first node in this list [cite: 45]
    private Node first;

    // The number of elements in this list
    private int size;
    
    /** Constructs an empty list. */
    public List() {
        first = null;
        size = 0;
    }
    
    /** Returns the number of elements in this list. */
    public int getSize() {
          return size;
    }

    /** Returns the CharData of the first element in this list. */
    public CharData getFirst() {
        if (first == null) {
            return null;
        }
        return first.cp;
    }

    /** Adds a CharData object to the beginning of this list. [cite: 89] */
    public void addFirst(char chr) {
        CharData cd = new CharData(chr);
        Node newN = new Node(cd, first);
        first = newN;
        size++;
    }
    
    /** Textual representation of this list. [cite: 90-92] */
    public String toString() {
        if (size == 0) {
            return "()";
        }
        StringBuilder str = new StringBuilder("(");
        Node current = first;
        while (current != null) {
            str.append(current.cp.toString());
            if (current.next != null) {
                str.append(" ");
            }
            current = current.next;
        }
        str.append(")");
        return str.toString();
    }

    /** Returns the index of the first CharData object with the given chr. [cite: 87-88] */
    public int indexOf(char chr) {
        Node current = first;
        int index = 0;
        while (current != null) {
            if (current.cp.chr == chr) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    /** Updates the counter or adds a new CharData. [cite: 98-100] */
    public void update(char chr) {
        int index = indexOf(chr);
        if (index == -1) {
            addFirst(chr);
        } else {
            CharData cd = get(index);
            cd.count++;
        }
    }

    /** Removes a CharData object from the list. [cite: 93-94] */
    public boolean remove(char chr) {
        Node current = first;
        Node prev = null;
        while (current != null) {
            if (current.cp.chr == chr) {
                if (prev == null) {
                    first = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    /** Returns the CharData object at the specified index. [cite: 95-96] */
    public CharData get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Node current = first;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.cp;
    }

    /** Returns an iterator over the elements in this list. [cite: 97] */
    public ListIterator listIterator(int index) {
        if (index < 0 || index > size) return null;
        Node current = first;
        for (int i = 0; i < index; i++) {
            if (current != null) current = current.next;
        }
        return new ListIterator(current);
    }
}