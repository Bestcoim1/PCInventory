package pcinventory.model;
/** Maps database form_factor values '2_5', '3_5', and 'M_2'. */
public enum StorageFormFactor {
    INCH_2_5("2_5"), INCH_3_5("3_5"), M_2("M_2");
    private final String databaseValue;
    StorageFormFactor(String databaseValue) { this.databaseValue = databaseValue; }
    public String getDatabaseValue() { return databaseValue; }
    public static StorageFormFactor fromDatabase(String value) {
        for (StorageFormFactor form : values()) {
            if (form.databaseValue.equals(value)) return form;
        }
        throw new IllegalArgumentException("Unknown storage form factor: " + value);
    }
}
