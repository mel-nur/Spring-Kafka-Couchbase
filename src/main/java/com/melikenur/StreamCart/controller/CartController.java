package com.melikenur.StreamCart.controller;

import com.melikenur.StreamCart.service.CartService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

// Sepete Ürün Ekleme isteği için basit bir DTO (Data Transfer Object)
class ItemRequest {
    public String productId;
    public int quantity;
}

/**
 * HTTP API uç noktalarını sağlayan REST kontrolcüsü.
 * Sepetle ilgili istekleri alır ve `CartService`'e yönlendirir.
 */
@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public Mono<com.melikenur.StreamCart.model.Cart> getCart(@PathVariable String userId) {
        /**
         * Belirtilen kullanıcıya ait sepeti döner.
         * Eğer sepet yoksa servis yeni bir sepet oluşturur ve döner.
         *
         * @param userId Sepetin sahibi kullanıcı ID'si
         * @return Reaktif `Cart` nesnesi
         */
        // Belirtilen kullanıcıya ait sepeti reaktif olarak döner.
        // Eğer sepet yoksa servis yeni sepet oluşturup dönecektir.
        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/{userId}/items")
    public Mono<com.melikenur.StreamCart.model.Cart> addItem(
            @PathVariable String userId,
            @RequestBody ItemRequest request) {
        /**
         * Sepete ürün ekleme uç noktası.
         * İstek gövdesindeki `productId` ve `quantity` alanları kullanılarak `CartService` çağrılır.
         *
         * @param userId Sepetin sahibi kullanıcı ID'si
         * @param request İstek gövdesi (productId, quantity)
         * @return Güncellenmiş `Cart` nesnesi
         */
        // İstek gövdesinden gelen productId ve quantity ile sepete öğe ekleme isteğini servis seviyesine iletir.
        // Girdi validasyonu serviste veya burada (@Valid) eklenebilir.
        return cartService.addItemToCart(userId, request.productId, request.quantity);
    }

    @PostMapping("/{userId}/checkout")
    public Mono<String> checkout(@PathVariable String userId) {
        /**
         * Kullanıcının sepetini checkout'a alır ve sipariş başlatma olayı üretir.
         * Yanıt kısa bir onay mesajı içerir; gerçek durum takibi ayrı bir sistemde yapılmalıdır.
         *
         * @param userId Checkout yapılacak kullanıcı ID'si
         * @return Kısa onay mesajı
         */
        // Sipariş başlatma işlemini servise yönlendir
        return cartService.checkoutCart(userId)
            .thenReturn("Sipariş başlatıldı: " + userId + ". Stoklar kesinleştiriliyor.");
    }
}