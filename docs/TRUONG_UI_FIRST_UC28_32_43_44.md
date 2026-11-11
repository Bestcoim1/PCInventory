# PCInventory — UI-first cho UC28–32, UC43–44

**Trạng thái: bản giao diện phục vụ tài liệu Use Case (demo dữ liệu).**
**Người thực hiện báo cáo:** Trường. Phần code tích hợp Builder sẽ cần đối chiếu với Hùng.

## Phạm vi hiện thực

UI được gắn trực tiếp vào `AppPage.BUILDER` của `feature/ui-shell-v1`. Không tạo ứng dụng riêng, không sửa `MainFrame` và không ghi đè module thành viên khác.

| UC | Trạng thái UI | Cách tạo ảnh minh họa |
|---|---|---|
| **UC28** | Danh sách Mainboard, cảnh báo RAM, chọn bỏ/thay RAM, xác nhận/hủy | Chọn kịch bản **UC28**, chọn `MB-LGA-DDR4`, bấm **Chọn / Thay linh kiện**, chụp hộp thoại điều chỉnh và sau xác nhận |
| **UC29** | Chọn RAM, số lượng, hiển thị DDR/bus/dung lượng không tương thích | Chọn **UC29**, `RAM-DDR5-6000`, bấm **Chọn / Thay linh kiện**, chụp hộp thoại lỗi |
| **UC30** | `ADD`, `REPLACE`, `SET_QUANTITY`, `lineId`, số lượng | Chọn **UC30**, `SSD-SATA`, `ADD`, số lượng 2, bấm chọn (vượt số cổng) |
| **UC31** | Chọn Case, cảnh báo GPU hoặc tản không vừa | Chọn **UC31**, `CASE-COMPACT`, bấm chọn |
| **UC32** | Chọn PSU, cảnh báo không đủ W hoặc sai form factor | Chọn **UC32**, `PSU-500`, bấm chọn |
| **UC43** | Kết quả kiểm tra cuối: VALID/INVALID và danh sách lỗi/pending | Chọn **UC43**, bấm **UC43 · Kiểm tra cuối cấu hình** |
| **UC44** | Bảng từng linh kiện: cần, còn, thiếu, trạng thái | Chọn **UC44 thiếu RAM** hoặc **UC44 không xác minh**, bấm **UC44 · Kiểm tra tồn** |
| **VALID** | Cấu hình hợp lệ và tồn đầy đủ | Chọn **Cấu hình mẫu hợp lệ**, bấm UC43 rồi tiếp tục UC44 |

### Lưu ảnh PNG cho tài liệu

- Nút **Xuất PNG** ở toolbar: chụp toàn bộ nội dung `BuilderPanel` (không bao gồm sidebar MainFrame của nhóm).
- Mọi hộp thoại lỗi, điều chỉnh UC28, kết quả UC43/UC44 đều có nút **Xuất PNG** riêng.
- Chụp ảnh phải ghi rõ nguồn là **UI prototype/dữ liệu demo**, không trình bày như nghiệp vụ MySQL/Java đã hoạt động.
- Đặt tên theo UC như `UC28_Adjustment.png`, `UC29_RAM_Error.png`, `UC30_Storage_Add_Error.png`, `UC43_FinalValidation.png`, `UC44_Stock_INSUFFICIENT.png`.

## Cấu trúc file

- `BuilderPanel.java` — Java Swing `JPanel` nhận thao tác, hiển thị danh sách, tổng giá, trạng thái và chuyển tới dialog. Thay `BuilderPanel` placeholder trong skeleton.
- `BuilderDemoData.java` — catalog và trạng thái **giả lập**, các kiểm tra kỹ thuật minh họa, không phải domain service chính thức.
- `BuilderDialogs.java` — các hộp thoại bước UC28, UC29–32, UC43 và UC44.
- `BuilderImageExport.java` — xuất PNG chính xác nội dung Swing đang hiển thị.
- `BuilderUiStyles.java` — màu sắc, font, viền và format VND.
- `BuilderUiFirstSelfTest.java` — test mock theo các UC và test khởi tạo `JPanel` ở môi trường headless.

**NetBeans:** Bộ UI này sử dụng Swing tạo bằng Java (không phải NetBeans `.form` generated). Có thể biên dịch/chỉnh sửa Java trong NetBeans; **chưa** có Design Mode kéo-thả cho các thành phần bên trong BuilderPanel. Không tạo `.form` giả gây hỏng GUI Builder.

## Mapping với Sequence Diagram

| Nhóm message SD | Thành phần UI ở bản demo | Khi nối nghiệp vụ thật |
|---|---|---|
| `getCandidates(targetType,filter)` | Chọn nhóm trên sidebar nội bộ / lọc ô tìm kiếm | View → PCBuilderController → BuildService → ComponentDAO/StockDAO → CompatibilityPort |
| `getStorageCandidates(operation,lineId,filter)` | Nhóm Storage + thao tác và dòng đích | Controller gọi đúng API với `BuildContext` |
| `applySelection(...)` / `applyStorageSelection(...)` | Bấm **Chọn / Thay linh kiện** | Controller xử lý async, khóa nút trong lúc chạy |
| `previewAdjustment`, `confirmMainboardChange`, `cancelSelection` | UC28 có dialog lựa chọn, xác nhận hoặc hủy | Phải chuyển sang bản sao của BuildService; không dùng thuật toán demo |
| `validateFinal(snapshot)` | UC43 hiện VALID/INVALID/mã lỗi | Thay bằng `CompatibilityPort.validateFinal` qua FinalValidationController |
| `checkAvailability(...)` | UC44 hiện bảng required/available/shortage | Thay bằng InventoryService + StockDAO, yêu cầu snapshot và expectedRevision |

### Quy tắc quan trọng

- Đổi Mainboard có RAM không tương thích: đưa ra phương án điều chỉnh do **nhân viên xác nhận**, không tự xóa RAM.
- Lựa chọn thất bại và hủy: giữ nguyên cấu hình, giá và revision.
- UC29 là quy tắc tương thích, không cần tự tạo Use Case mới.
- UC30 `SET_QUANTITY` thay số lượng tuyệt đối, không cộng dồn.
- UC43 chỉ kiểm tra cấu hình cuối; **không** đọc kho hoặc trừ kho.
- UC44 chỉ đọc tồn theo từng componentId; thiếu bản ghi tồn phải là **UNVERIFIABLE**. **Không** giữ chỗ, trừ hàng hoặc tạo PC.
- Mọi dữ liệu trên màn hình là **hư cấu minh họa**, không lấy từ MySQL hoặc bảng seed thật.

## Chạy thử

1. Trong NetBeans, mở nhánh `feature/truong-ui-first` như một Java with Ant project. Đảm bảo JDK22 và lib Connector/J của project vẫn được cấu hình.
2. Run `pcinventory.app.Main`, vào mục **Xây dựng cấu hình** trên sidebar.
3. Dùng combobox **Kịch bản** ở góc trên để mở từng UC.
4. Chạy qua các trường hợp thành công/thất bại rồi xuất ảnh.
5. CI tự compile toàn bộ `src` và chạy `BuilderUiFirstSelfTest` (không cần MySQL).

## Những gì CHƯA hoàn thành

- Chưa có UI kết nối `BuildService` / `CompatibilityPort` / DAO/MySQL thật; dữ liệu demo không đồng nhất với dữ liệu seed.
- Chưa thực hiện UC42 (lắp ráp và trừ kho) hoặc UC quản trị của thành viên khác.
- Chưa kiểm thử bằng GUI trên Windows/NetBeans trong máy người dùng.
- Chưa có `.form` editable kéo-thả cho BuilderPanel; nếu giảng viên bắt buộc Design Mode, cần làm bước chuyển đổi sau khi khóa layout.
- Không được dùng bộ UI này để chứng minh rằng toàn bộ nghiệp vụ UC28–32/43/44 đã được cài đặt backend.

**Đề xuất:** sau khi thu thập ảnh dùng cho tài liệu, thay dần `BuilderDemoData` bằng adapter tới các service đã chốt trên `feature/truong-compatibility` và BuildService của Hùng. Giữ nguyên layout `BuilderPanel`.
