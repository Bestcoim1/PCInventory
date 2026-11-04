# DATA_DICTIONARY_VI — Từ điển dữ liệu PCInventory

> **Phiên bản 1.0 · 04/11/2026.** Nguồn đối chiếu: `database/schema_v1.sql` trong repository PCInventory (`main`, blob `f17d8daa...`), Database Design v1 và Tài liệu nội bộ v0.11 – Design Contract v1.

> **Quan trọng:** Tên tiếng Việt không dấu trong tài liệu chỉ là **tên diễn giải**; hệ thống **không đổi tên vật lý các bảng/cột MySQL**.

**Phạm vi kiểm kê thực tế:** 27 bảng · 139 cột · 33 khóa ngoại (đối chiếu từ `schema_v1.sql`).

## 1. Quy ước sử dụng

- **MySQL và JDBC:** dùng nguyên tên tiếng Anh `snake_case` như DDL; tên Việt hóa dùng trong chú thích, SQL alias `AS` và lời giải thích trên UML.
- **Java:** giữ tên class/phương thức của hợp đồng; tham chiếu [UML_NAMING_CONVENTION.md](UML_NAMING_CONVENTION.md).
- **Nguồn sự thật:** các kiểu, khóa và `CHECK` dưới đây lấy từ DDL, còn logic tính tương thích/transaction phải kiểm tra ở Java Service.
- **Tiền:** SQL `DECIMAL(15,2)` / Java `BigDecimal`. Giá hiện hành, giá khi lưu và giá khi lắp là ba thời điểm khác nhau.
- **Phân công:** Đức UC01–09; Hảo UC10–12, UC37–41; Hùng UC13, UC26–27, UC33–36; Nga UC14–21; Hoàng UC22–25, UC42, UC45–49; Trường UC28–32, UC43–44.

## 2. Mục lục 27 bảng

| Bảng MySQL | Tên Việt không dấu | Ý nghĩa |
|---|---|---|
| `user_account` | `tai_khoan` | Tai khoan dang nhap, vai tro, trang thai |
| `component_category` | `danh_muc_linh_kien` | Danh muc quan ly linh kien, khong phai loai ky thuat |
| `brand` | `hang_san_xuat` | Hang san xuat linh kien |
| `component` | `linh_kien` | Thong tin chung, ma, loai ky thuat va gia hien hanh |
| `cpu_spec` | `thong_so_cpu` | Socket, GPU tich hop, tan CPU di kem |
| `mainboard_spec` | `thong_so_mainboard` | Socket, RAM, SATA/M.2 va form factor |
| `ram_spec` | `thong_so_ram` | Chuan, bus, dung luong RAM |
| `storage_spec` | `thong_so_o_luu_tru` | Loai o, giao tiep, dung luong, kich thuoc |
| `gpu_spec` | `thong_so_gpu` | Chieu dai VGA, cong suat PSU toi thieu |
| `psu_spec` | `thong_so_nguon` | Cong suat thuc va chuan nguon |
| `cpu_cooler_spec` | `thong_so_tan_cpu` | Tan khi/tan nuoc, chieu cao hoac radiator |
| `pc_case_spec` | `thong_so_vo_may` | Gioi han lap linh kien, quat va khay o |
| `case_fan_spec` | `thong_so_quat_case` | Kich co quat Case |
| `mainboard_ram_bus` | `tap_bus_ram_mainboard` | Tap toc do bus RAM Mainboard ho tro |
| `cooler_supported_socket` | `tap_socket_tan_cpu` | Tap socket tan CPU ho tro |
| `case_supported_mb_form` | `tap_chuan_mainboard_vo` | Tap form factor Mainboard Case ho tro |
| `case_supported_psu_form` | `tap_chuan_nguon_vo` | Tap chuan PSU Case ho tro |
| `case_supported_fan_size` | `tap_co_quat_vo` | Tap kich co quat Case ho tro |
| `supplier` | `nha_cung_cap` | Thong tin nha cung cap |
| `import_receipt` | `phieu_nhap` | Phieu nhap va trang thai xac nhan |
| `import_receipt_line` | `chi_tiet_phieu_nhap` | Dong phieu nhap, so luong va gia mua |
| `stock_balance` | `ton_kho` | So luong ton cua tung linh kien tai mot kho |
| `pc_configuration` | `cau_hinh_pc` | Cau hinh PC da luu, tong gia snapshot |
| `configuration_item` | `chi_tiet_cau_hinh` | Dong linh kien, so luong va gia snapshot |
| `assembled_pc` | `pc_thanh_pham` | PC da lap, nguon cau hinh, trang thai |
| `assembled_pc_item` | `chi_tiet_pc_thanh_pham` | Snapshot linh kien khi lap PC |
| `stock_movement` | `bien_dong_kho` | Lich su nhap/xuat va nguon nghiep vu |

## 3. Từ điển 139 thuộc tính theo bảng

### 3.1. `user_account` — `tai_khoan`

**Ý nghĩa:** Tai khoan dang nhap, vai tro, trang thai. **Phối hợp:** Duc.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `username` | `ten_dang_nhap` | `VARCHAR(60)` | UNIQUE; NOT NULL |
| `password_hash` | `mat_khau_bam` | `VARCHAR(255)` | NOT NULL |
| `display_name` | `ten_hien_thi` | `VARCHAR(120)` | NOT NULL |
| `role` | `vai_tro` | `VARCHAR(10)` | NOT NULL; mặc định 'EMPLOYEE' |
| `account_status` | `trang_thai_tai_khoan` | `VARCHAR(10)` | NOT NULL; mặc định 'ACTIVE' |
| `created_at` | `ngay_tao` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |

**Ràng buộc `CHECK`:** `chk_user_role`, `chk_user_status`. Xem biểu thức cụ thể trong `schema_v1.sql`.
**`UNIQUE`:** `username`.

### 3.2. `component_category` — `danh_muc_linh_kien`

**Ý nghĩa:** Danh muc quan ly linh kien, khong phai loai ky thuat. **Phối hợp:** Duc.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `category_name` | `ten_danh_muc` | `VARCHAR(100)` | UNIQUE; NOT NULL |
| `description` | `mo_ta` | `VARCHAR(500)` | có thể NULL |

**`UNIQUE`:** `category_name`.

### 3.3. `brand` — `hang_san_xuat`

**Ý nghĩa:** Hang san xuat linh kien. **Phối hợp:** Duc.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `brand_name` | `ten_hang` | `VARCHAR(100)` | UNIQUE; NOT NULL |
| `description` | `mo_ta` | `VARCHAR(500)` | có thể NULL |

**`UNIQUE`:** `brand_name`.

### 3.4. `component` — `linh_kien`

**Ý nghĩa:** Thong tin chung, ma, loai ky thuat va gia hien hanh. **Phối hợp:** Duc / Hao / Hung / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `component_code` | `ma_linh_kien` | `VARCHAR(50)` | UNIQUE; NOT NULL |
| `component_name` | `ten_linh_kien` | `VARCHAR(200)` | NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | NOT NULL |
| `category_id` | `danh_muc_id` | `BIGINT UNSIGNED` | FK → `component_category.id`; NOT NULL |
| `brand_id` | `hang_id` | `BIGINT UNSIGNED` | FK → `brand.id`; NOT NULL |
| `current_unit_price` | `don_gia_hien_hanh` | `DECIMAL(15,2)` | NOT NULL |
| `is_active` | `dang_hoat_dong` | `BOOLEAN` | NOT NULL; mặc định TRUE |
| `created_at` | `ngay_tao` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |
| `updated_at` | `ngay_cap_nhat` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |

**Khóa ngoại:** `category_id` → `component_category(id)`; `brand_id` → `brand(id)`.
**Ràng buộc `CHECK`:** `chk_component_type`, `chk_component_price`, `chk_component_active`. Xem biểu thức cụ thể trong `schema_v1.sql`.
**`UNIQUE`:** `component_code`, `id, component_type`.

### 3.5. `cpu_spec` — `thong_so_cpu`

**Ý nghĩa:** Socket, GPU tich hop, tan CPU di kem. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'CPU' |
| `socket` | `socket_cpu` | `VARCHAR(40)` | NOT NULL |
| `has_integrated_graphics` | `co_gpu_tich_hop` | `BOOLEAN` | NOT NULL |
| `includes_stock_cooler` | `co_tan_cpu_di_kem` | `BOOLEAN` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_cpu_spec_type`, `chk_cpu_boolean`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.6. `mainboard_spec` — `thong_so_mainboard`

**Ý nghĩa:** Socket, RAM, SATA/M.2 va form factor. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'MAINBOARD' |
| `socket` | `socket_cpu` | `VARCHAR(40)` | NOT NULL |
| `supported_ram_type` | `chuan_ram_ho_tro` | `VARCHAR(16)` | NOT NULL |
| `max_ram_capacity_gb` | `tong_ram_toi_da_gb` | `INT UNSIGNED` | NOT NULL |
| `form_factor` | `chuan_kich_thuoc` | `VARCHAR(40)` | NOT NULL |
| `sata_port_count` | `so_cong_sata` | `INT UNSIGNED` | NOT NULL |
| `m2_slot_count` | `so_khe_m2` | `INT UNSIGNED` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_mainboard_spec_type`, `chk_mb_ram_capacity`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.7. `ram_spec` — `thong_so_ram`

**Ý nghĩa:** Chuan, bus, dung luong RAM. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'RAM' |
| `ram_type` | `chuan_ram` | `VARCHAR(16)` | NOT NULL |
| `bus_mhz` | `bus_mhz` | `INT UNSIGNED` | NOT NULL |
| `capacity_gb` | `dung_luong_gb` | `INT UNSIGNED` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_ram_spec_type`, `chk_ram_capacity`, `chk_ram_bus`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.8. `storage_spec` — `thong_so_o_luu_tru`

**Ý nghĩa:** Loai o, giao tiep, dung luong, kich thuoc. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'STORAGE' |
| `storage_type` | `loai_o` | `VARCHAR(8)` | NOT NULL |
| `capacity_gb` | `dung_luong_gb` | `INT UNSIGNED` | NOT NULL |
| `interface_type` | `giao_tiep_o` | `VARCHAR(12)` | NOT NULL |
| `form_factor` | `chuan_kich_thuoc` | `VARCHAR(8)` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_storage_spec_type`, `chk_storage_type`, `chk_storage_interface`, `chk_storage_form`, `chk_storage_capacity`, `chk_storage_pair`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.9. `gpu_spec` — `thong_so_gpu`

**Ý nghĩa:** Chieu dai VGA, cong suat PSU toi thieu. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'GPU' |
| `length_mm` | `chieu_dai_mm` | `INT UNSIGNED` | NOT NULL |
| `minimum_psu_wattage` | `nguon_toi_thieu_w` | `INT UNSIGNED` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_gpu_spec_type`, `chk_gpu_length`, `chk_gpu_psu`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.10. `psu_spec` — `thong_so_nguon`

**Ý nghĩa:** Cong suat thuc va chuan nguon. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'PSU' |
| `real_wattage` | `cong_suat_thuc_w` | `INT UNSIGNED` | NOT NULL |
| `psu_form_factor` | `chuan_nguon` | `VARCHAR(40)` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_psu_spec_type`, `chk_psu_wattage`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.11. `cpu_cooler_spec` — `thong_so_tan_cpu`

**Ý nghĩa:** Tan khi/tan nuoc, chieu cao hoac radiator. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'CPU_COOLER' |
| `cooling_type` | `loai_tan_nhiet` | `VARCHAR(8)` | NOT NULL |
| `height_mm` | `chieu_cao_mm` | `INT UNSIGNED` | có thể NULL |
| `radiator_size` | `kich_thuoc_radiator` | `VARCHAR(8)` | NOT NULL; mặc định 'NONE' |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_cpu_cooler_spec_type`, `chk_cooler_type`, `chk_cooler_rad`, `chk_cooler_shape`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.12. `pc_case_spec` — `thong_so_vo_may`

**Ý nghĩa:** Gioi han lap linh kien, quat va khay o. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'CASE' |
| `max_cpu_cooler_height_mm` | `chieu_cao_tan_cpu_toi_da_mm` | `INT UNSIGNED` | NOT NULL |
| `max_gpu_length_mm` | `chieu_dai_gpu_toi_da_mm` | `INT UNSIGNED` | NOT NULL |
| `max_case_fan_count` | `so_quat_vo_toi_da` | `INT UNSIGNED` | NOT NULL |
| `included_case_fan_count` | `so_quat_vo_di_kem` | `INT UNSIGNED` | NOT NULL; mặc định 0 |
| `drive_bay_25_count` | `so_khay_o_2_5` | `INT UNSIGNED` | NOT NULL; mặc định 0 |
| `drive_bay_35_count` | `so_khay_o_3_5` | `INT UNSIGNED` | NOT NULL; mặc định 0 |
| `max_radiator_size` | `kich_thuoc_radiator_toi_da` | `VARCHAR(8)` | NOT NULL; mặc định 'NONE' |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_pc_case_spec_type`, `chk_case_radiator`, `chk_case_gpu_height`, `chk_case_fan_counts`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.13. `case_fan_spec` — `thong_so_quat_case`

**Ý nghĩa:** Kich co quat Case. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `component_type` | `loai_linh_kien_ky_thuat` | `VARCHAR(20)` | FK → `component.component_type`; NOT NULL; mặc định 'CASE_FAN' |
| `size_mm` | `kich_thuoc_mm` | `INT UNSIGNED` | NOT NULL |

**Khóa ngoại:** `component_id, component_type` → `component(id, component_type)`.
**Ràng buộc `CHECK`:** `chk_case_fan_spec_type`, `chk_casefan_size`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.14. `mainboard_ram_bus` — `tap_bus_ram_mainboard`

**Ý nghĩa:** Tap toc do bus RAM Mainboard ho tro. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `mainboard_id` | `mainboard_id` | `BIGINT UNSIGNED` | PK; FK → `mainboard_spec.component_id`; NOT NULL |
| `bus_mhz` | `bus_mhz` | `INT UNSIGNED` | PK; NOT NULL |

**Khóa ngoại:** `mainboard_id` → `mainboard_spec(component_id)`.
**Ràng buộc `CHECK`:** `chk_mb_bus_positive`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.15. `cooler_supported_socket` — `tap_socket_tan_cpu`

**Ý nghĩa:** Tap socket tan CPU ho tro. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `cooler_id` | `tan_cpu_id` | `BIGINT UNSIGNED` | PK; FK → `cpu_cooler_spec.component_id`; NOT NULL |
| `socket` | `socket_cpu` | `VARCHAR(40)` | PK; NOT NULL |

**Khóa ngoại:** `cooler_id` → `cpu_cooler_spec(component_id)`.

### 3.16. `case_supported_mb_form` — `tap_chuan_mainboard_vo`

**Ý nghĩa:** Tap form factor Mainboard Case ho tro. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `case_id` | `vo_may_id` | `BIGINT UNSIGNED` | PK; FK → `pc_case_spec.component_id`; NOT NULL |
| `form_factor` | `chuan_kich_thuoc` | `VARCHAR(40)` | PK; NOT NULL |

**Khóa ngoại:** `case_id` → `pc_case_spec(component_id)`.

### 3.17. `case_supported_psu_form` — `tap_chuan_nguon_vo`

**Ý nghĩa:** Tap chuan PSU Case ho tro. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `case_id` | `vo_may_id` | `BIGINT UNSIGNED` | PK; FK → `pc_case_spec.component_id`; NOT NULL |
| `form_factor` | `chuan_kich_thuoc` | `VARCHAR(40)` | PK; NOT NULL |

**Khóa ngoại:** `case_id` → `pc_case_spec(component_id)`.

### 3.18. `case_supported_fan_size` — `tap_co_quat_vo`

**Ý nghĩa:** Tap kich co quat Case ho tro. **Phối hợp:** Duc / Truong.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `case_id` | `vo_may_id` | `BIGINT UNSIGNED` | PK; FK → `pc_case_spec.component_id`; NOT NULL |
| `size_mm` | `kich_thuoc_mm` | `INT UNSIGNED` | PK; NOT NULL |

**Khóa ngoại:** `case_id` → `pc_case_spec(component_id)`.
**Ràng buộc `CHECK`:** `chk_case_fan_size`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.19. `supplier` — `nha_cung_cap`

**Ý nghĩa:** Thong tin nha cung cap. **Phối hợp:** Nga.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `supplier_name` | `ten_nha_cung_cap` | `VARCHAR(160)` | NOT NULL |
| `address` | `dia_chi` | `VARCHAR(300)` | có thể NULL |
| `phone` | `so_dien_thoai` | `VARCHAR(30)` | có thể NULL |
| `created_at` | `ngay_tao` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |


### 3.20. `import_receipt` — `phieu_nhap`

**Ý nghĩa:** Phieu nhap va trang thai xac nhan. **Phối hợp:** Nga.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `receipt_code` | `ma_phieu_nhap` | `VARCHAR(50)` | UNIQUE; NOT NULL |
| `supplier_id` | `nha_cung_cap_id` | `BIGINT UNSIGNED` | FK → `supplier.id`; NOT NULL |
| `created_by` | `nguoi_tao_id` | `BIGINT UNSIGNED` | FK → `user_account.id`; NOT NULL |
| `status` | `trang_thai` | `VARCHAR(12)` | NOT NULL; mặc định 'PENDING' |
| `total_amount` | `tong_tien` | `DECIMAL(15,2)` | NOT NULL; mặc định 0 |
| `created_at` | `ngay_tao` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |
| `confirmed_by` | `nguoi_xac_nhan_id` | `BIGINT UNSIGNED` | FK → `user_account.id`; có thể NULL |
| `confirmed_at` | `ngay_xac_nhan` | `DATETIME` | có thể NULL |

**Khóa ngoại:** `supplier_id` → `supplier(id)`; `created_by` → `user_account(id)`; `confirmed_by` → `user_account(id)`.
**Ràng buộc `CHECK`:** `chk_receipt_status`, `chk_receipt_amount`, `chk_receipt_confirm`. Xem biểu thức cụ thể trong `schema_v1.sql`.
**`UNIQUE`:** `receipt_code`.

### 3.21. `import_receipt_line` — `chi_tiet_phieu_nhap`

**Ý nghĩa:** Dong phieu nhap, so luong va gia mua. **Phối hợp:** Nga.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `receipt_id` | `phieu_nhap_id` | `BIGINT UNSIGNED` | FK → `import_receipt.id`; NOT NULL |
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | FK → `component.id`; NOT NULL |
| `quantity` | `so_luong` | `INT UNSIGNED` | NOT NULL |
| `purchase_unit_price` | `don_gia_nhap` | `DECIMAL(15,2)` | NOT NULL |

**Khóa ngoại:** `receipt_id` → `import_receipt(id)`; `component_id` → `component(id)`.
**Ràng buộc `CHECK`:** `chk_receipt_line_qty`, `chk_receipt_line_price`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.22. `stock_balance` — `ton_kho`

**Ý nghĩa:** So luong ton cua tung linh kien tai mot kho. **Phối hợp:** Hoang / Truong (UC44).

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | PK; FK → `component.id`; NOT NULL |
| `quantity_on_hand` | `so_luong_ton` | `INT` | NOT NULL; mặc định 0 |
| `updated_at` | `ngay_cap_nhat` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |

**Khóa ngoại:** `component_id` → `component(id)`.
**Ràng buộc `CHECK`:** `chk_balance_nonnegative`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.23. `pc_configuration` — `cau_hinh_pc`

**Ý nghĩa:** Cau hinh PC da luu, tong gia snapshot. **Phối hợp:** Hao.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `configuration_code` | `ma_cau_hinh` | `VARCHAR(50)` | UNIQUE; NOT NULL |
| `configuration_name` | `ten_cau_hinh` | `VARCHAR(180)` | NOT NULL |
| `created_by` | `nguoi_tao_id` | `BIGINT UNSIGNED` | FK → `user_account.id`; NOT NULL |
| `saved_total_price` | `tong_gia_luc_luu` | `DECIMAL(15,2)` | NOT NULL; mặc định 0 |
| `created_at` | `ngay_tao` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |
| `updated_at` | `ngay_cap_nhat` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |

**Khóa ngoại:** `created_by` → `user_account(id)`.
**Ràng buộc `CHECK`:** `chk_config_price`. Xem biểu thức cụ thể trong `schema_v1.sql`.
**`UNIQUE`:** `configuration_code`.

### 3.24. `configuration_item` — `chi_tiet_cau_hinh`

**Ý nghĩa:** Dong linh kien, so luong va gia snapshot. **Phối hợp:** Hao.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `configuration_id` | `cau_hinh_id` | `BIGINT UNSIGNED` | FK → `pc_configuration.id`; NOT NULL |
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | FK → `component.id`; NOT NULL |
| `quantity` | `so_luong` | `INT UNSIGNED` | NOT NULL |
| `saved_unit_price` | `don_gia_luc_luu` | `DECIMAL(15,2)` | NOT NULL |

**Khóa ngoại:** `configuration_id` → `pc_configuration(id)`; `component_id` → `component(id)`.
**Ràng buộc `CHECK`:** `chk_config_item_qty`, `chk_config_item_price`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.25. `assembled_pc` — `pc_thanh_pham`

**Ý nghĩa:** PC da lap, nguon cau hinh, trang thai. **Phối hợp:** Hoang.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `pc_code` | `ma_pc` | `VARCHAR(50)` | UNIQUE; NOT NULL |
| `pc_name` | `ten_pc` | `VARCHAR(180)` | NOT NULL |
| `source_configuration_id` | `cau_hinh_nguon_id` | `BIGINT UNSIGNED` | FK → `pc_configuration.id`; NOT NULL |
| `assembled_by` | `nguoi_lap_id` | `BIGINT UNSIGNED` | FK → `user_account.id`; NOT NULL |
| `assembled_at` | `ngay_lap_rap` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |
| `status` | `trang_thai` | `VARCHAR(12)` | NOT NULL; mặc định 'AVAILABLE' |
| `assembly_total_price` | `tong_gia_lap_rap` | `DECIMAL(15,2)` | NOT NULL |

**Khóa ngoại:** `source_configuration_id` → `pc_configuration(id)`; `assembled_by` → `user_account(id)`.
**Ràng buộc `CHECK`:** `chk_pc_status`, `chk_pc_price`. Xem biểu thức cụ thể trong `schema_v1.sql`.
**`UNIQUE`:** `pc_code`.

### 3.26. `assembled_pc_item` — `chi_tiet_pc_thanh_pham`

**Ý nghĩa:** Snapshot linh kien khi lap PC. **Phối hợp:** Hoang.

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `assembled_pc_id` | `pc_thanh_pham_id` | `BIGINT UNSIGNED` | FK → `assembled_pc.id`; NOT NULL |
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | FK → `component.id`; NOT NULL |
| `component_code_snapshot` | `ma_linh_kien_luc_lap` | `VARCHAR(50)` | NOT NULL |
| `component_name_snapshot` | `ten_linh_kien_luc_lap` | `VARCHAR(200)` | NOT NULL |
| `quantity` | `so_luong` | `INT UNSIGNED` | NOT NULL |
| `assembly_unit_price` | `don_gia_luc_lap` | `DECIMAL(15,2)` | NOT NULL |

**Khóa ngoại:** `assembled_pc_id` → `assembled_pc(id)`; `component_id` → `component(id)`.
**Ràng buộc `CHECK`:** `chk_pc_item_qty`, `chk_pc_item_price`. Xem biểu thức cụ thể trong `schema_v1.sql`.

### 3.27. `stock_movement` — `bien_dong_kho`

**Ý nghĩa:** Lich su nhap/xuat va nguon nghiep vu. **Phối hợp:** Hoang / Nga (UC21).

| Cột SQL | Tên tiếng Việt không dấu | Kiểu dữ liệu | Khóa, null, mặc định |
|---|---|---|---|
| `id` | `id` | `BIGINT UNSIGNED` | PK; NOT NULL |
| `component_id` | `linh_kien_id` | `BIGINT UNSIGNED` | FK → `component.id`; NOT NULL |
| `quantity_delta` | `so_luong_tang_giam` | `INT` | NOT NULL |
| `movement_type` | `loai_bien_dong` | `VARCHAR(4)` | NOT NULL |
| `source_receipt_id` | `phieu_nhap_nguon_id` | `BIGINT UNSIGNED` | FK → `import_receipt.id`; có thể NULL |
| `source_assembled_pc_id` | `pc_thanh_pham_nguon_id` | `BIGINT UNSIGNED` | FK → `assembled_pc.id`; có thể NULL |
| `performed_by` | `nguoi_thuc_hien_id` | `BIGINT UNSIGNED` | FK → `user_account.id`; NOT NULL |
| `created_at` | `ngay_tao` | `DATETIME` | NOT NULL; mặc định CURRENT_TIMESTAMP |

**Khóa ngoại:** `component_id` → `component(id)`; `source_receipt_id` → `import_receipt(id)`; `source_assembled_pc_id` → `assembled_pc(id)`; `performed_by` → `user_account(id)`.
**Ràng buộc `CHECK`:** `chk_movement_origin`. Xem biểu thức cụ thể trong `schema_v1.sql`.

## 4. Quan hệ và quy tắc cần hiểu đúng

| Chủ đề | Quy ước giải thích |
|---|---|
| `component` / `component_category` | `component_type` là loại kỹ thuật cố định, còn `category_id` là danh mục quản lý. **Không** suy ra Java subclass từ `category_name`. |
| 9 bảng `*_spec` | Ánh xạ lớp con `Component` bằng bảng 1–1; khóa ngoại kép `(component_id, component_type)` chặn sai loại. Java Service phải bảo đảm đúng một dòng spec tương ứng. |
| 5 bảng tập thuộc tính | `mainboard_ram_bus` là tập bus cụ thể; các bảng Case/Cooler mô tả tập socket/form factor/kích cỡ quạt được hỗ trợ. |
| `stock_balance` | Mỗi linh kiện được kỳ vọng có một dòng tồn. **Thiếu dòng tồn là dữ liệu không xác minh**, không mặc nhiên hiểu bằng 0. |
| `stock_movement` | `IN`: delta dương và nguồn phiếu nhập; `OUT`: delta âm và nguồn PC thành phẩm. UC21/UC42 phải ghi trong transaction. |
| `pc_configuration` | `saved_total_price` cùng `configuration_item.saved_unit_price` là snapshot lúc lưu, **không phải** giá hiện hành. |
| `assembled_pc` | Mỗi lần lắp thành công tạo một PC, snapshot riêng. Một configuration có thể sinh nhiều PC; không hard-delete configuration đã được PC tham chiếu. |
| Quạt/tản đi kèm | `included_case_fan_count` và `includes_stock_cooler` là thuộc tính sản phẩm, không tự phát sinh dòng xuất kho rời. |
| Quản lý phiên bản cấu hình | **Chưa có cột `version`** ở `pc_configuration`. `updated_at` có trong DDL nhưng chưa chứng minh được cơ chế optimistic locking đủ mạnh. UC40/UC43/UC44 phải chốt điều này trước khi vẽ `currentRevision`. |

### 4.1. Giá trị miền được `CHECK` ràng buộc

| Cột | Giá trị hợp lệ theo DDL |
|---|---|
| `user_account.role` | `EMPLOYEE`, `ADMIN` |
| `user_account.account_status` | `ACTIVE`, `LOCKED` |
| `component.component_type` | `CPU`, `MAINBOARD`, `RAM`, `STORAGE`, `GPU`, `PSU`, `CPU_COOLER`, `CASE`, `CASE_FAN` |
| `storage_spec.storage_type` | `SSD`, `HDD` |
| `storage_spec.interface_type` | `SATA`, `M2_NVME` |
| `storage_spec.form_factor` | `2_5`, `3_5`, `M_2` |
| `cpu_cooler_spec.cooling_type` | `AIR`, `LIQUID` |
| `cpu_cooler_spec.radiator_size`, `pc_case_spec.max_radiator_size` | `NONE`, `MM240`, `MM360` |
| `case_fan_spec.size_mm`, `case_supported_fan_size.size_mm` | `120`, `140` |
| `import_receipt.status` | `PENDING`, `CONFIRMED` |
| `assembled_pc.status` | `AVAILABLE`, `INACTIVE` |
| `stock_movement.movement_type` | `IN`, `OUT` cùng điều kiện `quantity_delta` và nguồn FK |

## 5. Ví dụ truy vấn SQL sử dụng tên Việt không dấu ở kết quả

**UC10 (Hảo) — Tra cứu linh kiện:**

```sql
SELECT component_code AS ma_linh_kien, component_name AS ten_linh_kien,
       current_unit_price AS don_gia_hien_hanh
FROM component;
```

**UC22 (Hoàng) — Xem số lượng tồn:**

```sql
SELECT c.component_code AS ma_linh_kien,
       c.component_name AS ten_linh_kien,
       s.quantity_on_hand AS so_luong_ton
FROM component AS c
LEFT JOIN stock_balance AS s ON s.component_id = c.id;
```

> Dùng `LEFT JOIN` ở ví dụ để phát hiện linh kiện thiếu dòng tồn; đừng vội `COALESCE(...,0)` che dữ liệu bất thường.

**UC44 (Trường) — Gộp nhu cầu linh kiện trong cấu hình ĐÃ LƯU:**

```sql
SELECT component_id AS linh_kien_id,
       SUM(quantity) AS so_luong_can
FROM configuration_item
WHERE configuration_id = ?
GROUP BY component_id;
```

> Với cấu hình nháp trong Builder, lấy danh sách dòng từ snapshot của Hùng rồi gộp theo `componentId`; không ghi nháp vào bảng `configuration_item` chỉ để truy vấn. UC44 chỉ kiểm tra, không trừ hay giữ hàng.

## 6. Bảo trì đồng bộ

- File nguồn DDL: [`database/schema_v1.sql`](../database/schema_v1.sql), seed: [`database/seed_demo_v1.sql`](../database/seed_demo_v1.sql), kiểm chứng: [`database/verify_schema_v1.sql`](../database/verify_schema_v1.sql).
- Khi đổi bảng/cột thật: cần migration SQL, cập nhật DAO/Model, Sequence Diagram và test; cập nhật lại từ điển này sau.
- Không thêm thuộc tính chưa có ở schema thành “sự thật” chỉ vì một bản Sequence Diagram nhắc tới.
- Quy ước đặt tên UML/Java nằm tại [`UML_NAMING_CONVENTION.md`](UML_NAMING_CONVENTION.md).