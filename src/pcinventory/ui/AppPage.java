package pcinventory.ui;

/**
 * Stable navigation identifiers shared by all feature teams.
 * Labels may change without breaking CardLayout registration.
 */
public enum AppPage {
    DASHBOARD("Tổng quan", "Kiểm tra kết nối và tình trạng dự án", "Nhóm chung"),
    COMPONENTS("Linh kiện", "Danh mục, tìm kiếm và quản lý linh kiện", "Đức / Hảo"),
    INVENTORY("Kho linh kiện", "Tồn kho và lịch sử biến động", "Hoàng"),
    IMPORTS("Nhập hàng", "Nhà cung cấp và phiếu nhập kho", "Nga"),
    BUILDER("Xây dựng cấu hình", "Chọn linh kiện và kiểm tra tương thích", "Hùng / Trường"),
    SAVED_CONFIGS("Cấu hình đã lưu", "Lưu, xem và quản lý cấu hình PC", "Hảo"),
    ASSEMBLED_PCS("PC thành phẩm", "Lắp ráp và quản lý PC hoàn chỉnh", "Hoàng");

    private final String title;
    private final String description;
    private final String owner;

    AppPage(String title, String description, String owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }
}
