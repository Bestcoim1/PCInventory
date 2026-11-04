package pcinventory.model;
public enum RadiatorSize {
    NONE(0), MM240(240), MM360(360);
    private final int millimeters;
    RadiatorSize(int millimeters) { this.millimeters = millimeters; }
    public int getMillimeters() { return millimeters; }
}
