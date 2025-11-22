package com.melikenur.StreamCart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melikenur.StreamCart.event.CartUpdateEvent;
import com.melikenur.StreamCart.model.Product;
import com.melikenur.StreamCart.repository.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Kafka'dan gelen sepet güncelleme olaylarını dinler ve stok rezervasyon/serbest bırakma işlemlerini yapar.
 */
@Component
public class InventoryConsumer {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper; // JSON Deserialization için
    private final CartEventProducer eventProducer; // İleride hata olayları göndermek için

    // DI ile gerekli bağımlılıkları enjekte ediyoruz
    public InventoryConsumer(ProductRepository productRepository, CartEventProducer eventProducer) {
        this.productRepository = productRepository;
        this.eventProducer = eventProducer;
        this.objectMapper = new ObjectMapper(); 
    }

    // application.properties'deki topic adını dinler ve groupId'yi kullanır
    @KafkaListener(topics = "${streamcart.kafka.topic.cart-updates}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void handleCartUpdate(CartUpdateEvent event) {
        System.out.println("Kafka'dan Sepet Güncelleme Olayı alındı: " + event);

        // Sepet güncellemelerine karşılık stok ayırma (reservation) mantığı
        processStockReservation(event)
            .subscribe(
                // Başarılı işlem sonrası
                updatedProduct -> System.out.println("Stok başarıyla ayarlandı/serbest bırakıldı: " + updatedProduct.getId()),
                // Hata durumunda
                error -> {
                    System.err.println("Stok işlenirken hata oluştu: " + error.getMessage());
                    // Buraya ileride stok yetersizliği durumunda hata fırlatma mantığı eklenecek
                }
            );
    }

    /**
     * Reaktif olarak stok ayırma/serbest bırakma işlemini gerçekleştirir.
     */
    private Mono<Product> processStockReservation(CartUpdateEvent event) {
        return productRepository.findById(event.getProductId())
            .switchIfEmpty(Mono.error(new RuntimeException("Ürün bulunamadı: " + event.getProductId())))
            .flatMap(product -> {
                int change = event.getQuantityChange();
                int newReservedStock = product.getReservedStock() + change;
                
                // 1. Stok fazlası kontrolü (Ekleme işlemi için)
                if (change > 0 && product.getAvailableStock() < change) {
                    return Mono.error(new RuntimeException("STOK YETERSİZ! Ürün: " + product.getName()));
                }
                
                // 2. Ayırılmış stok kontrolü (Çıkarma işlemi için, negatif olmamalı)
                if (newReservedStock < 0) {
                    // Sepetten çıkarılan miktar, daha önce ayrılan miktardan fazla olamaz
                    return Mono.error(new RuntimeException("Ayırılmış stok negatif olamaz."));
                }

                // Stok güncellemesi: Mevcut stok azalır, Ayrılmış stok artar (Ekleme ise) veya tersi (Çıkarma ise)
                product.setAvailableStock(product.getAvailableStock() - change);
                product.setReservedStock(newReservedStock);
                
                // Couchbase'e güncellenmiş Product belgesini kaydet
                return productRepository.save(product);
            });
    }
}