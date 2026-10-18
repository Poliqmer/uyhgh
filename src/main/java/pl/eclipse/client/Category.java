package pl.eclipse.client;

public enum Category {
    CLIENT("Client"), HUD("HUD"), COMBAT("Combat"), MOVEMENT("Movement"), WORLD("World");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
