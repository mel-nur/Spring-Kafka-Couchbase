package com.melikenur.StreamCart.service;

import com.melikenur.StreamCart.event.CartUpdateEvent;
import com.melikenur.StreamCart.model.Cart;
import com.melikenur.StreamCart.model.CartItem;
import com.melikenur.StreamCart.repository.CartRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

/**
 * Sepet iş mantığını sağlayan servis sınıfı.
 *
 * - Sepet oluşturma / getirme
 * - Sepete ürün ekleme / miktar güncelleme
 * - Checkout işlemini başlatma (sipariş olayı üretme)
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartEventProducer eventProducer;
    private final KafkaTemplate<String, String> orderKafkaTemplate;
    // ProductRepository burada DI ile enjekte edilmeli, ancak şimdilik sadece sepet odaklı ilerliyoruz.

    public CartService(CartRepository cartRepository, CartEventProducer eventProducer, KafkaTemplate<String, String> orderKafkaTemplate) {
        this.cartRepository = cartRepository;
        this.eventProducer = eventProducer;
        this.orderKafkaTemplate = orderKafkaTemplate;
    }

    public Mono<Cart> getCartByUserId(String userId) {
        /**
         * Kullanıcının sepetini döner. Eğer sepet yoksa yeni bir sepet oluşturur ve kaydeder.
         * @param userId Sepetin sahibi olan kullanıcı kimliği
         * @return Reaktif olarak bulunan veya oluşturulan `Cart` nesnesi
         */
        // Sepet yoksa yeni bir sepet oluşturur
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    // Yeni sepet oluşturulurken hem 'id' hem 'userId' aynı atanmaktadır.
                    // Bu, kodun bazı yerlerinde (OrderProcessor) doğrudan userId ile belge alınmasını sağlar.
                    // Eğer farklı bir id stratejisi istenirse burada uyarlama yapılmalıdır.
                    Cart newCart = new Cart(userId, userId, Collections.emptyList(), Instant.now().toEpochMilli());
                    return cartRepository.save(newCart);
                }));
    }

    public Mono<Cart> addItemToCart(String userId, String productId, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalArgumentException("Miktar pozitif olmalıdır."));
        }

        /**
         * Sepete bir ürün ekler veya mevcutsa miktarını arttırır.
         * - Validasyon: miktar pozitif olmalıdır.
         * - Sepet yoksa önce oluşturulur.
         * - Sepet güncellendikten sonra ilgili Kafka olayı tetiklenir.
         *
         * @param userId Sepetin sahibi kullanıcı ID'si
         * @param productId Eklenecek ürünün ID'si
         * @param quantity Eklenecek miktar (pozitif)
         * @return Güncellenmiş `Cart` nesnesi
         */
        // 1. Sepeti al veya oluştur
        return getCartByUserId(userId)
                .flatMap(cart -> {
                    // 2. Mevcut ürünü bul
                    Optional<CartItem> existingItem = cart.getItems().stream()
                            .filter(item -> item.getProductId().equals(productId))
                            .findFirst();

                    if (existingItem.isPresent()) {
                        // Ürün varsa miktarını artır
                        existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
                    } else {
                        // Ürün yoksa yeni kalem ekle
                        cart.getItems().add(new CartItem(productId, quantity));
                    }
                    
                    cart.setLastUpdated(Instant.now().toEpochMilli());

                    // 3. Sepeti kaydet ve ardından Kafka olayını tetikle
                    return cartRepository.save(cart)
                            .doOnSuccess(savedCart -> {
                                // Sepet başarıyla kaydedildikten sonra ilgili değişiklik bilgisi Kafka'ya gönderilir.
                                // Burada gönderilen olay, stok rezervasyonu gibi downstream işlemleri tetikler.
                                eventProducer.sendCartUpdateEvent(new CartUpdateEvent(
                                        userId, productId, quantity, Instant.now()
                                ));
                            });
                });
    }
    @Value("${streamcart.kafka.topic.order-placements}")
    private String orderPlacementsTopic;
    
    public Mono<Void> checkoutCart(String userId) {
        /**
         * Checkout işlemini başlatır: sepeti bulur ve "ORDER_PLACED" olayını Kafka'ya yollar.
         * Bu metodun sorumluluğu yalnızca sipariş başlatma olayını üretmektir; gerçek ödeme/fulfillment
         * işlemleri ayrı sistemlerde ele alınır.
         *
         * @param userId Checkout yapılacak kullanıcı ID'si
         * @return Void (işlem asenkron olarak gerçekleştirilir)
         */
        // Sepeti bul ve OrderPlaced olayını tetikle
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Sepet bulunamadı: " + userId)))
                .flatMap(cart -> {
                    // Sepet içeriğini Kafka'ya olay olarak gönder (Payload basitçe userId olabilir)
                    // 'send' asenkron çalışır; burada dönen Future/Callback göz ardı edilmiştir.
                    // Üretici tarafında hata yakalama veya ack bekleme gerekiyorsa eklemeler yapılmalıdır.
                    orderKafkaTemplate.send(orderPlacementsTopic, userId, "ORDER_PLACED_FOR_" + userId);
                    return Mono.empty(); // Void döndürür
                });
    }

    // Çıkarma işlemi için benzer bir metot yazılabilir...
}
    
