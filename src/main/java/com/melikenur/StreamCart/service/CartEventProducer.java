package com.melikenur.StreamCart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.melikenur.StreamCart.event.CartUpdateEvent;
/**
 * Sepet olaylarını Kafka'ya gönderen servis.
 * Bu sınıf, `CartUpdateEvent` nesnelerini konfigürasyonda tanımlı topic'e yazar.
 */
@Service
public class CartEventProducer {
    private final KafkaTemplate<String, CartUpdateEvent> kafkaTemplate;

    // application.properties'den topic adını çekiyoruz
    @Value("${streamcart.kafka.topic.cart-updates}")
    private String cartUpdatesTopic;

    public CartEventProducer(KafkaTemplate<String, CartUpdateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Olayı Kafka'ya asenkron olarak gönderir
    /**
     * `CartUpdateEvent` nesnesini konfigürasyonda tanımlı topic'e yollar.
     * @param event Gönderilecek sepet güncelleme olayı
     */
    public void sendCartUpdateEvent(CartUpdateEvent event) {
        // userId'yi key olarak kullanmak, aynı kullanıcının olaylarının sırasını garantileyebilir.
        kafkaTemplate.send(cartUpdatesTopic, event.getUserId(), event);
        // Basit loglama: üretim ortamı için SLF4J/logback tercih edilmelidir.
        System.out.println("Kafka'ya gönderilen olay: " + event); // Loglama amaçlı
    }
    
}
