package com.melikenur.StreamCart.service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster; 
import com.couchbase.client.java.Collection;
// TransactionResult import kaldırıldı; dönüş değeri kullanılmıyor
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.TransactionGetResult; // ZORUNLU EKLENDİ
import com.melikenur.StreamCart.model.Cart;
import com.melikenur.StreamCart.model.CartItem; 
import com.melikenur.StreamCart.model.Product;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Sipariş (order) olaylarını işleyen bileşen.
 * Gelen sipariş olayında ilgili sepeti transaction içinde alır,
 * rezervasyonları kesinleştirir ve sepeti siler.
 */
@Component
public class OrderProcessor {

    private final Transactions couchbaseTransactions;
    private final Collection cartCollection;
    private final Collection productCollection;

    // Constructor'ı, @Value'yu parametre olarak enjekte edecek şekilde düzeltiyoruz
    public OrderProcessor(
            Transactions couchbaseTransactions, 
            Cluster cluster,
            @Value("${spring.couchbase.bucket-name}") String bucketName) { // @Value BURADA KULLANILMALI
        
        this.couchbaseTransactions = couchbaseTransactions;

        // Bucket adını enjekte edilen parametreden alıyoruz
        Bucket bucket = cluster.bucket(bucketName);
        this.cartCollection = bucket.defaultCollection();
        this.productCollection = bucket.defaultCollection(); 
    }

    @KafkaListener(topics = "${streamcart.kafka.topic.order-placements}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderPlacement(String userId) {
        System.out.println("SIPARIŞ OLAYI ALINDI: " + userId);

        try {
            couchbaseTransactions.run(ctx -> {
                
                // 1. Sepeti Al
                TransactionGetResult cartResult = ctx.get(cartCollection, userId); 
                Cart cart = cartResult.contentAs(Cart.class);

                if (cart == null || cart.getItems().isEmpty()) {
                    System.err.println("Sepet boş veya bulunamadı. Geri alınıyor.");
                    ctx.rollback(); 
                    return; 
                }

                // 2. Stokları Kesinleştirme 
                for (CartItem item : cart.getItems()) {
                    TransactionGetResult productResult = ctx.get(productCollection, item.getProductId());
                    Product product = productResult.contentAs(Product.class);

                    // reservedStock'u düşür
                    product.setReservedStock(product.getReservedStock() - item.getQuantity());

                    // Belgeyi güncelle (replace)
                    ctx.replace(productResult, product); 
                }

                // 3. Sepeti Sil
                ctx.remove(cartResult); 
            });

            System.out.println("Sipariş İşlemi BAŞARILI. Kullanıcı: " + userId);
        } catch (Exception e) {
            System.err.println("Sipariş İşlemi HATA VERDİ: " + e.getMessage());
        }
    }
}