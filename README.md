# StreamCart

StreamCart, reaktif bir alışveriş-sepeti örnek uygulamasıdır. Bu proje Spring WebFlux, Reactive Couchbase ve Spring Kafka kullanarak
basit sepet yönetimi, stok rezervasyonu ve sipariş işleme akışı gösterir. Yerel geliştirme için Docker Compose sağlanmıştır.

**Kısa not (kod değişiklikleri):** Servis içinde `removeItemFromCart` metodu eklendi; bu metod bir ürünün miktarını azaltır veya
adet 0'a inerse ürünü sepetten kaldırır ve değişiklik bilgisini Kafka'ya (negatif miktar) bir `CartUpdateEvent` olarak yollar.

## Teknolojiler
- Java 
- Spring Boot 3.3.x
- Spring WebFlux
- Spring Data Couchbase (Reactive)
- Spring Kafka
- Docker & Docker Compose
- Maven (wrapper: `mvnw` / `mvnw.cmd`)

## Gereksinimler
- Java 
- Docker & Docker Compose (Yerel servisleri ayakta tutmak için)

## Hızlı Başlangıç

1) Gerekli altyapıyı Docker Compose ile başlatın (Couchbase + Kafka):

```powershell
docker-compose up --build -d
```

2) Projeyi derleyin (testleri atlamak için):

```powershell
.\mvnw.cmd clean package -DskipTests
```

3) Uygulamayı çalıştırın:

```powershell
.\mvnw.cmd spring-boot:run
```

Uygulama varsayılan olarak `8000` portunda çalışır (bakınız `src/main/resources/application.properties`).


## Konfigürasyon
`src/main/resources/application.properties` içinde özellikle göz önünde bulundurulması gerekenler:

- `spring.couchbase.connection-string` (örn. `couchbase://127.0.0.1`)
- `spring.couchbase.username`
- `spring.couchbase.password`
- `spring.couchbase.bucket-name`
- `spring.kafka.bootstrap-servers` (örn. `localhost:9092`)
- `streamcart.kafka.topic.cart-updates`
- `streamcart.kafka.topic.order-placements`

Docker Compose dosyasında (sağlanan `docker-compose.yml`) Zookeeper, Kafka ve Couchbase servisleri bulunmaktadır; varsayılan portlar:

- Couchbase Web: `8091`
- Kafka: `9092`
- Uygulama: `8000`

## HTTP API - Uç Noktalar

- `GET /api/v1/carts/{userId}` — Kullanıcının sepetini döner. Eğer sepet yoksa servis yeni bir sepet oluşturur.
- `POST /api/v1/carts/{userId}/items` — Sepete öğe ekler (body JSON: `{ "productId": "p1", "quantity": 2 }`).
- `POST /api/v1/carts/{userId}/checkout` — Sepeti checkout'a alır; bu işlem Kafka'ya bir "ORDER_PLACED" tipi mesaj yollar.

Not: Kod tabanına `CartService.removeItemFromCart(userId, productId, quantity)` metodu eklendi.
Ayrıca bu metodu expose eden bir HTTP uç noktası eklendi:

- `DELETE /api/v1/carts/{userId}/items/{productId}?quantity={n}` — belirtilen miktarı (pozitif tamsayı) çıkarır.

## Örnek İstekler (port `8000` kullanılır)

cURL (sepete ekleme):

```bash
curl -X POST \
  http://localhost:8000/api/v1/carts/user123/items \
  -H 'Content-Type: application/json' \
  -d '{"productId":"p1","quantity":2}'
```

PowerShell (sepete ekleme):

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8000/api/v1/carts/user123/items -Body (@{productId='p1';quantity=2} | ConvertTo-Json) -ContentType 'application/json'
```

cURL (sepetten çıkarma - DELETE örneği):

```bash
curl -X DELETE "http://localhost:8000/api/v1/carts/user123/items/p1?quantity=1" -H "Accept: application/json"
```

PowerShell (sepetten çıkarma - DELETE örneği):

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8000/api/v1/carts/user123/items/p1?quantity=1" -ContentType 'application/json'
```
