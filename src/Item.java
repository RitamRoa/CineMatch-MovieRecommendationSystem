/**
 * Abstract base class representing a generic item in the system.
 * Demonstrates ABSTRACTION and INHERITANCE in OOP.
 */
public abstract class Item {
    private String id;
    private String name;

    public Item(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Abstract method — subclasses must implement their own display
    public abstract void displayInfo();

    // Getters and Setters — ENCAPSULATION
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "[" + id + "] " + name;
    }
}
