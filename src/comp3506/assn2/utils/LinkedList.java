package comp3506.assn2.utils;

/**
 * A singly linked list data structure
 */
public class LinkedList {

    private Node tail;   // the end of the list
    private Node head;   // the start of the list
    private int size;    // the number of elements in the list

    /**
     * Constructor. Create an empty linked list
     */
    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Add a new node at the end
     *
     * @param node the new node
     */
    public void addNode(Node node) {
        if (node != null) {
            if (tail == null) {
                head = node;
            } else {
                tail.setNext(node);
            }
            tail = node;
            size++;
        }
    }

    /**
     * @return the start of the list
     */
    public Node getHead() {
        return head;
    }

    /**
     * @return the end of the list
     */
    public Node getTail() {
        return tail;
    }

    /**
     * Set the end of the list
     *
     * @param node the new end
     */
    public void setTail(Node node) {
        if (node != null) {
            tail = node;
        }
    }

    /**
     * @return the number of all elements
     */
    public int getSize() {
        return size;
    }

    /**
     * Add all elements in a list into the current list
     * Create a copy of the new linked list to avoid changing the other list's structure
     *
     * @param newList the new list to add
     */
    public void addAll(LinkedList newList) {
        if (newList != null) {
            Node newNode = newList.getHead();
            while (newNode != null) {
                addNode(newNode.copy());
                newNode = newNode.getNext();
            }
        }
    }
}
