# Release Audit Report - Shizuku Manager

**Dự án:** Shizuku Manager  
**Thư mục:** `shizuku-manager/`  
**Ngôn ngữ chính:** Java (Android), Gradle Kotlin DSL  
**Ngày audit:** 2026-11-22  
**Người audit:** AndroidHarness Agent

---

## 1. Tong Quan

Bao cao nay danh gia nguy co chat luong va bao mat cua source code truuoc khi phat hien cong khoi tren GitHub. Moi van de duoc phan loai theo muc do:

- **CRITICAL** - Can xu li ngay truoc khi cong khoi (bao mat, mat loi)
- **HIGH** - Can xu li de dam bao chat luong engineering
- **MEDIUM** - Nhan quyen xu li sau CRITICAL/HIGH
- **LOW** - Van de nho, co the bo qua neu thoi gian han chet
- **IMPROVEMENT** - De xuan khuyen khich, khong bat buoc

---

## 2. Danh Sach Van De

### CRITICAL

#### C1. Keystore bi mat duoc luu trong repository
- **Vi tri:** `shizuku-manager/release-key.jks` (tap tin nhi phan) va goc workspace `release-key.jks`
- **Nguyen nguon:** Tap tin keystore chua private key dung de ky phat hanh Release. Neu cong khoi, tat ca nguoi co the tai ve va lay private key.
- **Phuong an xu ly:**
  1. Xoa `release-key.jks` khoi working tree (khong commit).
  2. Them vao `.gitignore`.
  3. Tach cau hinh signing khoi source cong khoi qua `signing.properties.example` + bien truong toi / Gradle properties.
  4. Build Release phai bao loi ro rang neu thieu cau hinh signing.

#### C2. Mat khau keo trong gradle.properties
- **Vi tri:** `shizuku-manager/gradle.properties` dong 31-32
- **Noi dung:**
  ```
  keystore.password=[redacted]
  keystore.keyPassword=Minhthu2103@
  ```
- **Nguyen nguon:** Mat thuc te duoc luu trong file cau hinh Gradle. Neu commit, mat khau se co trong lich su Git va co the duoc tim kiem.
- **Phuong an xu ly:**
  1. Xoa mat khau thuc te khoi `gradle.properties`, thay bang gia tri trong ngoac hoac bo trong.
  2. Them `gradle.properties` (neu chua co) vao `.gitignore` hoac chi commit `gradle.properties.example`.
  3. Nguoi dung tu tao `gradle.properties` locally voi mat khau cua minh.

#### C3. Tap tin create_keystore.sh chua mat khau thuc te
- **Vi tri:** Goc workspace `create_keystore.sh` dong 9-11
- **Noi dung:** `STORE_PASS='Minhthu2103@'`, `KEY_PASS='Minhthu2103@'`
- **Nguyen nguon:** Tap tin script tao keystore luu mat khau thuc te trong code. Neu duoc copy vao repository, mat khau bi ro.
- **Phuong an xu ly:** Xoa hoac khong them script nay vao repository cong khoi. Neu can, cung dan muc `signing.properties.example` thay the.

#### C4. Keystore-info.txt ro thong tin may ca nhan
- **Vi tri:** Goc workspace `keystore-info.txt`
- **Noi dung:** Duong dan may `/sdcard/codeonthegoprojects/release-key.jks`, thanh pho Ho Chi Minh, tinh Binh Duong, quoc gia VN, kieu mat ma RSA, kich thuoc khoa 2048, han su dung 10000 ngay.
- **Nguyen nguon:** Tap tin thong tin may ca nhan khong nen co trong repository cong khoi.
- **Phuong an xu ly:** Xoa khoi working tree, them vao `.gitignore`.

#### C5. Duong dan keystore duoc hardcode trong build.gradle.kts
- **Vi tri:** `shizuku-manager/app/build.gradle.kts` dong 24
- **Noi dung:** `storeFile = file("../release-key.jks")`
- **Nguyen nguon:** Duong dan tuyet doi toi tap tin keystore ca nhan. Nguoi khac clone ve khong co tap tin nay va build Release se that bai.
- **Phuong an xu ly:** Thay bang duong dan tu `signing.properties` hoac bien truong toi. Neu thieu cau hinh, Release build phai bao loi ro rang.

---

### HIGH

#### H1. Ten package com.example chung chung
- **Vi tri:** `android { namespace = "com.example" }` trong `app/build.gradle.kts`, va tat ca package Java `com.example.*`
- **Nguyen nguon:** `com.example` la ten package mac dinh cua Android Studio, khong phan biet du an va co the trung voi cac du an khac tren Google Play.
- **Phuong an xu ly:** Thay bang ten package danh dau du an, vi du `com.shizukumanager.app` hoac `moe.shizuku.manager`. Can cap nhat tat ca tham chieu trong AndroidManifest.xml, Java source, va build config.

#### H2. Khong co .gitignore cho tap tin bi mat o goc workspace
- **Vi tri:** Goc workspace (`release-key.jks`, `keystore-info.txt`, `create_keystore.sh`)
- **Nguyen nguon:** Cac tap tin nay o ngoai thu muc `shizuku-manager/` va khong duoc bao hoa boi `.gitignore` cua du an neu git duoc khoi tao o goc workspace.
- **Phuong an xu ly:** Tao `.gitignore` o goc workspace hoac trong `shizuku-manager/` bao gom cac tap nhi phan keystore, file thong tin mat, script tao khoa.

#### H3. Comments Java tieng Viet co dau
- **Vi tri:** Tat ca file `*.java` trong `shizuku-manager/app/src/main/java/`
- **Nguyen nguon:** Comments duoc viet bang tieng Viet co dau (vi du: "Khoi tao", "Quan ly"). Yeu cau cua du an la tieng Viet KHONG dau de dam bao ket xuat va doc sua deu dung.
- **Phuong an xu ly:** Chuyen tat ca comment Java sang tieng Viet khong dau (xem Phase 4). Khong thay doi logic code.

#### H4. Thua plugin Gradle khong su dung
- **Vi tri:** `shizuku-manager/build.gradle.kts` dong 4-7, `gradle/libs.versions.toml`
- **Noi dung:** Khai bao `kotlin.compose`, `ksp`, `google.services`, `secrets` plugin nhung code chua su dung Compose, Firebase, hoac Google Services.
- **Phuong an xu ly:** Xoa cac plugin va dependency khong su dung de giam thoi gian build va khoi luong cau hinh.

---

### MEDIUM

#### M1. Lop AppContainer phep truy cap toan bo thanh phan
- **Vi tri:** `shizuku-manager/app/src/main/java/com/example/di/AppContainer.java`
- **Nguyen nguon:** Manual DI container cung cap getter cho tat ca repository, manager, executor. Dep den nhung lop khong can thiet co the truy cap thanh phan khong lien quan.
- **Phuong an xu ly:** Giu nguyen vi la Manual DI don gian. Them Javadoc gan ngan danh cho cac getter can thiet. Khong can tai Dagger/Hilt.

#### M2. Xu ly exception bo trong ShizukuConnectionManager.cleanup()
- **Vi tri:** `ShizukuConnectionManager.java` dong 279: `catch (Throwable ignored)`
- **Nguyen nguon:** Nuot het exception khi dong ket noi. Co the lam an mat loi nguy trong giai段 giai phong tai nguyen.
- **Phuong an xu ly:** Ghi log warning thay cho bo hoan toan, hoac duoi comments ly do bo hoan toan.

#### M3. PackageSafetyGuard danh sach goi he thong co the khong du
- **Vi tri:** `PackageSafetyGuard.java` dong 10-22
- **Nguyen nguon:** Danh sach goi he thong quan trong la hardcoded. Mot so thiet ban Android co the co them goi he thong khac.
- **Phuong an xu ly:** Giu nguyen vi danh sach nay bao che cac goi thong thuong. Them comment ly do chon cac goi nay.

---

### LOW

#### L1. Build directory da co trong repository
- **Vi tri:** `shizuku-manager/build/`, `.gradle/`
- **Nguyen nguon:** Tap tin da build khong nen duoc luu trong repository.
- **Phuong an xu ly:** Them `/build` va `/.gradle` vao `.gitignore`.

#### L2. .env duoc ignore nhung .env.example co placeholder API key
- **Vi tri:** `shizuku-manager/.env.example`
- **Noi dung:** `# GEMINI_API_KEY=[redacted]`
- **Nguyen nguon:** Placeholder API key trong file mau co the lam nhieu nguoi dung.
- **Phuong an xu ly:** Xoa dong placeholder hoac thay thanh mot mo ta ro rang rang nguoi dung can tu cung cap.

#### L3. Thu muc Screenshot chua anh
- **Vi tri:** `shizuku-manager/Screenshot/` co 6 file PNG
- **Nguyen nguon:** Anh chup man hinh thuc te, co the su dung trong README. Can kiem tra xem co hop le khong.
- **Phuong an xu ly:** Giu nguyen, tham kieu trong README.md.

---

### IMPROVEMENT

#### I1. Ten package com.example nen thay the
- Xem H1.

#### I2. Docstring Java co the bo sung
- Mot so lop khong co JavaDoc hoac chi co comment 1 dong ngan. Bo sung JavaDoc gan ngan cho cac lop cong khai va phuong thuc quan trong.

#### I3. Cau hinh ProGuard/R8
- `proguard-rules.pro` chua quy tac cu the. Neu bat minifyEnabled cho Release, can them quy tac de giu cac class model, entity, va Shizuku AIDL.

#### I4. Version code va version name
- `versionCode = 1`, `versionName = "1.0.0"` - phu hop cho phien ban dau tien.

---

## 3. Ke Hoach Xu Li (Theo Thu Tu Uu Tien)

| Thu tu | Van de | Muc do | Hanh dong |
|--------|--------|--------|-----------|
| 1 | C1-C5 | CRITICAL | Xoa tap tin bi mat, tach signing config, cap nhat `.gitignore` |
| 2 | H1-H4 | HIGH | Doi ten package, bo plugin khong su dung, chuyen comment Java |
| 3 | M1-M3 | MEDIUM | Bo sung Javadoc, xu ly exception, kiem tra safety guard |
| 4 | L1-L3 | LOW | Cap nhat .gitignore, xoa .env.example placeholder |
| 5 | I1-I4 | IMPROVEMENT | Bo sung doc, ProGuard rules |

---

## 4. Ket Luan

Source code co chat luong engineering tot, su dung Manual DI + Repository + MVVM, co kiem thu don vi, va xu ly loi ro rang. Nhung co nguy co bao mat NGHI EM TRONG do luu keystore, mat khau, va duong dan may ca nhan trong repository. Phai hoan toan giai quyet cac van de CRITICAL truoc khi cong khoi.

Sau khi xu li xong cac van de CRITICAL va HIGH, repository co the duoc chuyen sang Phase 3 (Code Refinement) va Phase 4 (Java Comment Standardization).
