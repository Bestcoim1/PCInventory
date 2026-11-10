# Trường — Compatibility + Inventory UC28–32, UC43–44 (v1)

> Mục tiêu: có mã nguồn thật để review trước khi tích hợp với `BuildService` của Hùng. Đây là nhánh feature, **không phải** bản đã được tích hợp vào UI/UC42. Java 22.

## 1. Những gì đã cài đặt

- `pcinventory.model.Component` trừu tượng; 9 subclass typed: `Cpu`, `Mainboard`, `Ram`, `Storage`, `Gpu`, `Psu`, `CpuCooler`, `PcCase`, `CaseFan`.
- `PCConfiguration` chứa `List<ConfigurationItem>` bất biến. Mỗi dòng có `lineId` riêng và quantity > 0; có thể có nhiều dòng RAM, Storage, Case Fan.
- `CompatibilityPort` giữ đúng 3 chữ ký trong Design Contract v1:
  `filterCompatible(config, targetType, candidates)`,
  `revalidateDependencies(config)`, `validateFinal(config)`.
- `CompatibilityService` pure Java (không DAO, không UI, không đổi cấu hình, không tính giá). Bao phủ socket, RAM DDR/bus/dung lượng, số cổng SATA/M.2, khay 2.5/3.5 inch, Case/Mainboard/PSU form, GPU chiều dài và PSU tối thiểu, CPU cooler/socket/chiều cao/radiator, Case Fan.
- `validateFinal` thêm thành phần bắt buộc CPU, Mainboard, RAM, Storage, Case, PSU; GPU/Cooler/Fan khi cần; pending chưa giải quyết cấm kết luận hợp lệ.
- `InventoryService` (UC44) nhận snapshot **đã được tầng điều phối xác minh revision** và `StockBalanceReader` đọc hàng; gộp quantity theo component ID; trả `StockCheckResult` với `AVAILABLE`, `INSUFFICIENT`, `UNVERIFIABLE`, `INVALID_REQUEST`. **Không cập nhật kho**.
- `StockDAO` chỉ đọc bảng `stock_balance` qua JDBC, giữ cả trường hợp mất dòng tồn.

## 2. Cách Hùng gọi (không viết thay UC26/27/33–36)

```java
CompatibilityPort compatibility = new CompatibilityService();

// Hùng tự tạo context đã bỏ đúng dòng cần REPLACE,
// tự load ứng viên đầy đủ (gồm subtype + các set), giá và tồn qua DAO.
List<Component> suitable = compatibility.filterCompatible(
    selectionContext, ComponentType.MAINBOARD, candidates);

// Hùng tạo proposedConfig trên COPY, KHÔNG cập nhật BuildContext thật.
ValidationResult checks = compatibility.revalidateDependencies(proposedConfig);
if (!checks.isCompatible()) {
    // Hiển thị checks.getErrors(), lựa chọn xử lý phụ thuộc/REJECTED.
} else {
    // Hùng tính giá, so sánh revision và applyTogether đúng 1 lần.
}
```

**Lưu ý filter:** kiểm thử ứng viên như thêm 1 đơn vị vào `selectionContext`. Trước khi REPLACE CPU/Mainboard/GPU/PSU/Case/Cooler, Hùng phải loại đúng slot cũ *trong bản sao*; RAM/Storage/Fan phải bỏ đúng lineId nếu REPLACE/SET_QUANTITY. Số lượng cuối chỉ hợp lệ sau `revalidateDependencies(proposedConfig)`.

`ValidationResult.getAffectedTypes()` chỉ gợi ý những loại cần xem xét, không tự xóa bất kỳ dòng nào. Người dùng xác nhận điều chỉnh trong UC27/28 do Hùng xử lý.

## 3. Tích hợp UC43 và UC44

- **UC43**: tầng điều phối chốt/capture snapshot có source identity và revision. Sau khi xác minh snapshot còn hiện hành, gọi `compatibility.validateFinal(snapshot)`; **không** kiểm tra tồn kho hay tính giá.
- **UC44**: chỉ tiếp tục khi snapshot vẫn hiện hành và cùng identity với kết quả UC43 đã hợp lệ. Tầng điều phối gọi `new InventoryService(new StockDAO()).checkAvailability(snapshot)`.
- **Không tự suy diễn DB revision**: `pc_configuration` hiện không có cột `version`. Saved-source revision và cập nhật UC40 phải chốt với Hảo trước khi code tầng điều phối.
- **UC42**: Hoàng phải kiểm tra lại số lượng trong transaction và trừ kho có điều kiện; UC44 AVAILABLE không giữ chỗ.

## 4. Phạm vi chưa triển khai (có chủ ý)

- **Không** viết Builder UI, BuildContext, PriceCalculator, `applyTogether` hoặc UC13/26/27/33–36 của Hùng.
- **Không** viết CRUD, mapper DB cho chín subtype (Đức/Hảo phụ trách dữ liệu). Khi Đức cung cấp DAO, cần map `component` + bảng `*_spec` + các bảng `*_supported` thành model immutable; đối chiếu enum và bộ dữ liệu seed.
- **Không** viết saved configuration revision/controller của Hảo, hoặc transaction lắp PC của Hoàng.
- **Chưa có integration tests với MySQL**; test hiện tại là in-memory rule tests + mocked balance reader.

## 5. Chạy test

CI trên GitHub sẽ compile Java 22 và chạy các kiểm thử độc lập CSDL từ `test/pcinventory/CompatibilityAndInventorySelfTest.java`.

Trong project NetBeans (Java with Ant): `Clean and Build`, sau đó Run File trên test main (hoặc cấu hình test run bằng javac/java). Không cần khai báo mật khẩu để chạy self-tests.

## 6. Những quyết định cần Hùng/Đức/Hảo/Hoàng cùng duyệt trước merge

1. Xác nhận `lineId`, lớp model bất biến và hàm giá/working price do Hùng sở hữu.
2. Xác nhận Đức sẽ ánh xạ đúng 9 lớp subtype, đặc biệt 5 bảng tập thuộc tính. Không hardcode demo specs trong DAO.
3. Xác nhận nguồn snapshot/revision với Hảo trước khi nối UC43/UC44 vào cấu hình đã lưu.
4. Hoàng xác nhận interface `StockBalanceReader` là read-only, UC42 giữ transaction độc lập.
5. Các rule mở rộng chưa mô hình hóa (RAM slot count, M.2 lane-sharing, power budget chi tiết) **không được tự phỏng đoán**.

## 7. Quy tắc review

- Ba phương thức `CompatibilityPort` **không đổi**.
- Code không chứa mật khẩu hoặc cấu hình máy riêng.
- PR chỉ thêm Model, DTO, Service, StockDAO (đọc) và test/docs. Không sửa UC của người khác.
- Nếu cần đổi DTO chung, hãy mở discussion trước khi merge.
