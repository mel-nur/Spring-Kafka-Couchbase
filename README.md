docker-compose up --build
# StreamCart

StreamCart, reaktif bir alışveriş-sepeti örnek uygulamasıdır. Bu proje, Spring WebFlux + Reactive Couchbase + Spring Kafka altyapısını kullanarak
basit bir sepet yönetimi, stok rezervasyonu ve sipariş işleme akışı sunar. Proje Docker ile yerel olarak çalıştırılabilir.

## Teknolojiler
- Java 
- Spring Boot 
- Spring Data Couchbase 
- Spring Kafka
- Docker & Docker Compose
- Maven (wrapper: `mvnw`)

## Hedef
Bu repo, Couchbase ve Kafka entegrasyonlarını kullanarak gerçek dünya benzeri bir mikroservis akışı göstermeyi amaçlar: sepet oluşturma, güncelleme,
stok rezervasyonu (consumer) ve sipariş kesinleştirme (transactional işlemler).

## Hızlı Başlangıç

1. (Tercih) Docker & Docker Compose çalıştırın:

```powershell
docker-compose up --build
```

3. Maven ile projeyi derleyin (testleri atlamak için):

```powershell
.\mvnw clean package -DskipTests
```

4. Uygulamayı doğrudan çalıştırın veya Docker ile ayağa kaldırın (varsayılan port `8000`):

```powershell
.\mvnw spring-boot:run
```

## Konfigürasyon
`src/main/resources/application.properties` içinde aşağıdaki anahtarlar önemlidir:

- `spring.couchbase.connection-string`
- `spring.couchbase.username`
- `spring.couchbase.password`
- `spring.kafka.bootstrap-servers`
- `spring.kafka.consumer.group-id`
- `streamcart.kafka.topic.cart-updates`
- `streamcart.kafka.topic.order-placements`


## API - Örnek Uç Noktalar

- `GET /api/v1/carts/{userId}` — Kullanıcının sepetini döner.
- `POST /api/v1/carts/{userId}/items` — Sepete item ekler.
  - Gövde (JSON): `{ "productId": "p1", "quantity": 2 }`
- `POST /api/v1/carts/{userId}/checkout` — Sepeti checkout'a alır (sipariş olayı üretir).

cURL örneği (sepete ekleme):

```bash
curl -X POST \
  http://localhost:8080/api/v1/carts/user123/items \
  -H 'Content-Type: application/json' \
  -d '{"productId":"p1","quantity":2}'
```

PowerShell örneği:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/carts/user123/items -Body (@{productId='p1';quantity=2} | ConvertTo-Json) -ContentType 'application/json'
```

