package comp3506.assn2.utils;

/**
 * A node in the linked list
 */
public class Node {

    private Node next;      // Link to the next node
    private Object value;   // Contain a value

    /**
     * Constructor. Create a node holding a certain value
     *
     * @param value value to contain
     */
    public Node(Object value) {
        next = null;
        this.value = value;
    }

    /**
     * Set the next node
     *
     * @param node the next node to link to
     */
    public void setNext(Node node) {
        next = node;
    }

    /**
     * @return the next node
     */
    public Node getNext() {
        return next;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return a new node that contains the same value
     */
    public Node copy() {
        return new Node(value);
    }
}
