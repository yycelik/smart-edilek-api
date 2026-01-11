# Veritabanı Otomasyonu (Cüzdan ve Kredi Sistemi)

Bu klasördeki SQL dosyaları, kullanıcıların siparişleri (paket satın alımları) ile cüzdan kredileri arasındaki bağlantıyı veritabanı seviyesinde otomatize eden tetikleyicileri (trigger) ve prosedürleri içerir.

## Çalışma Mantığı

Sistem, uygulama kodundan bağımsız olarak, veri tutarlılığını sağlamak için veritabanı tetikleyicilerini kullanır. Bir sipariş "Ödendi" durumuna geçtiğinde, ilgili paketin kredisi otomatik olarak kullanıcının cüzdanına yansıtılır.

### 1. Hesaplama Prosedürü (`sp_recalculate_wallet`)

Cüzdan bakiyesini `credit_transaction` tablosundaki hareketlere göre baştan sona hesaplayan ana prosedürdür.

- **Girdiler:** `wallet_id`
- **İşlem:**
  - **Toplam Kredi:** Satın Alma (1), İade (3), Düzeltme (4) tiplerini toplar.
  - **Kullanılan Kredi:** Harcama (2) tiplerini toplar.
  - **Sonuç:** `credit_wallet` tablosundaki `total_credits`, `used_credits` ve `remaining_credits` alanlarını günceller.

### 2. Sipariş Tetikleyicileri (Order Triggers)

Kullanıcı bir paket satın aldığında kredinin yüklenmesini sağlar. `credit_amount` bilgisi `license_package` tablosundan alınır.

- **`trg_user_order_after_insert`**:
  - Yeni bir sipariş kaydı oluşturulduğunda çalışır.
  - Eğer sipariş durumu **PAID (2)** veya **CAMPAIGN (5)** ise, kullanıcının cüzdanını kontrol eder (yoksa oluşturur) ve krediyi yükler.

- **`trg_user_order_to_credit_transaction`**:
  - Mevcut bir sipariş güncellendiğinde çalışır.
  - Sipariş durumu değişip **PAID (2)** veya **CAMPAIGN (5)** olduğunda (örn: Beklemede -> Ödendi), krediyi yükler.

### 3. Cüzdan Senkronizasyon Tetikleyicileri

`credit_transaction` tablosunda yapılan herhangi bir manuel veya otomatik işlem sonrası cüzdan bakiyesinin her zaman doğru kalmasını sağlar.

- **`trg_wallet_after_insert`**
- **`trg_wallet_after_update`**
- **`trg_wallet_after_delete`**

Bu tetikleyicilerin her biri, işlem yapılan cüzdan için `sp_recalculate_wallet` prosedürünü tetikler.
