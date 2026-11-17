# LCN Lọc Tương Tác Facebook (Android Kotlin + Jetpack Compose)

Ứng dụng Android hỗ trợ lọc tương tác bạn bè Facebook sử dụng **Facebook Graph API v19.0** kết hợp cơ chế xử lý hàng loạt an toàn.

## Luồng hoạt động 4 màn hình theo đúng đặc tả
1. **Màn hình 1 — Đăng nhập Facebook (Bắt buộc)**:
   - Xác thực tài khoản qua Facebook OAuth / Access Token (`GET /v19.0/me?fields=id,name,picture`).
   - Chỉ khi đăng nhập thành công mới chuyển sang màn hình Lựa chọn.
2. **Màn hình 2 — Lựa chọn bộ lọc**:
   - Cho phép chọn tiêu chí **Không tương tác** (0 Reaction/Comment trên bài đăng) và/hoặc **Bỏ theo dõi** (Unfollowed).
   - Nhấn nút **Quét** để gọi API phân tích và chuyển sang màn hình danh sách.
3. **Màn hình 3 — Danh sách bạn bè đã lọc**:
   - Giới hạn hiển thị tối đa **5.000 tài khoản / 1 trang** (`limit=5000`).
   - Mỗi bạn bè hiển thị trên **1 dòng** gồm **Avatar** và **Tên bạn bè**.
   - Hỗ trợ chọn từng tài khoản hoặc nhấn nút **Chọn tất cả**.
   - Cụm 3 nút chức năng dưới cùng: **Bỏ theo dõi**, **Huỷ kết bạn**, **Chặn**.
4. **Màn hình 4 — Tiến trình thực thi & Hoàn tất**:
   - Hiển thị thanh tiến trình (`ProgressBar`), phần trăm hoàn thành, tên tài khoản đang xử lý và nhật ký thực thi.
   - Khi hoàn tất 100%, hiển thị thông báo **Báo thành công** và tự động/nhấn nút quay về **Màn hình Lựa chọn**.

## Hướng dẫn Build APK
- **GitHub Actions**: Workflow `.github/workflows/build-apk.yml` tự động build APK release khi push hoặc tạo pull request vào nhánh `main`. Có thể chạy thủ công từ tab **Actions** bằng **Run workflow**. Tải artifact `LCN-FB-Filter-Release-APK` ở trang chạy workflow; APK được build nhưng chưa ký bằng khóa phát hành.
- **Build cục bộ**: Với JDK 17 và Gradle 8.2 đã cài, chạy `gradle assembleRelease`. APK đầu ra nằm tại `app/build/outputs/apk/release/`.
- **Android Studio**: Mở thư mục dự án bằng Android Studio Iguana trở lên, điền `facebook_app_id` trong `app/src/main/res/values/strings.xml` hoặc nhập trực tiếp Access Token trên màn hình đăng nhập.
