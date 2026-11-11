# PCInventory

Bài tập lớn Nhập môn Công nghệ phần mềm – hệ thống quản lý kho linh kiện PC kết hợp xây dựng cấu hình tương thích.

## Môi trường

- Apache NetBeans 26, **Java with Ant** (không sử dụng Maven).
- Dự án hiện cấu hình `javac.source=22` và `javac.target=22`: thống nhất **JDK 22** cho cả nhóm, hoặc nâng cấp đồng loạt qua Pull Request.
- Java Swing và MySQL Server 8.0.x; JDBC driver ở `lib/mysql-connector-j-26.7.0.jar`.

## Khởi chạy trên máy mới

1. Clone repository, dùng **NetBeans → File → Open Project** mở thư mục `PCInventory`.
2. Trong MySQL Workbench, dùng **File → Open SQL Script**, chạy `database/schema_v1.sql` rồi `database/seed_demo_v1.sql` trên CSDL mới. **Không chạy lại schema/seed trên CSDL đã có dữ liệu.**
3. Tạo tài khoản MySQL ứng dụng `pc_app` có quyền cần thiết đối với `pc_inventory` (hoặc dùng tài khoản cục bộ được quản trị viên cấp).
4. Sao chép `config/db.properties.example` thành `config/db.properties`, sau đó điền mật khẩu của máy mình. **Tuyệt đối không commit `db.properties`.**
5. Trong NetBeans, chọn **Clean and Build**, sau đó **Run Project (F6)**.
6. Trang **Tổng quan** → **Kiểm tra JDBC**. Nếu dữ liệu seed được nạp thành công sẽ hiển thị **12 linh kiện**; nếu chưa có DB, giao diện vẫn mở bình thường.

## Cấu trúc nguồn

- `pcinventory.app.Main`: chạy UI trên Swing Event Dispatch Thread.
- `pcinventory.ui.MainFrame`: cửa sổ dùng chung, thanh điều hướng và CardLayout.
- `pcinventory.ui.AppPage`: mã trang, tiêu đề và người phụ trách.
- `pcinventory.ui.dashboard.DashboardPanel`: trang Tổng quan + kiểm tra JDBC không chặn giao diện.
- `pcinventory.dao.ComponentDAO`: truy vấn thử `COUNT(*)`, không chứa SQL trong UI.
- `pcinventory.util.DBConnection`: đọc kết nối từ `config/db.properties`.
- Các package dưới `pcinventory.ui`: 6 trang nghiệp vụ độc lập hiện là placeholder.

## Quy ước NetBeans GUI Builder

`MainFrame` và các placeholder giai đoạn đầu là Swing viết bằng Java; chúng **không có file .form** nên không hiển thị tab **Design**.

Khi phụ trách một chức năng, thành viên có thể tạo **New → Swing GUI Forms → JPanel Form** tại package của mình (ví dụ `pcinventory.ui.inventory.InventoryView`). Sau đó nhóm tích hợp bằng cách thay đăng ký tương ứng trong constructor `MainFrame`:

```java
registerPage(AppPage.INVENTORY, new InventoryView());
```

Các lớp JPanel Form của NetBeans sẽ gồm `.java` và `.form`: **commit cả hai**. Không tự chỉnh phần mã được NetBeans đánh dấu Generated Code. Nếu muốn thay hẳn class placeholder cùng tên, hãy tạo form mới trước rồi chuyển đăng ký khi PR được duyệt.

## Chia việc và GitHub

- **Trường**: tương thích, xác thực cấu hình và kiểm tra tồn kho; **Hùng**: PC Builder.
- **Hoàng**: tồn kho và PC thành phẩm; **Hảo**: tra cứu linh kiện và cấu hình đã lưu.
- **Nga**: nhà cung cấp và nhập kho; **Đức**: tài khoản, danh mục, hãng và quản trị linh kiện.

Mỗi người làm trên `feature/<ten-module>`, mở Pull Request tới `main`. Tránh sửa `MainFrame`, `AppPage`, `DBConnection`, schema hoặc các contract chung nếu chưa thống nhất với nhóm.

> Đây là **UI skeleton**, không phải các màn hình nghiệp vụ đã hoàn thành. Chưa có đăng nhập/phân quyền và chưa áp dụng các quy tắc tương thích hay transaction nghiệp vụ.
