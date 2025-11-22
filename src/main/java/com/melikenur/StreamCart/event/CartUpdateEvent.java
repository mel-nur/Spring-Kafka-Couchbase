package com.melikenur.StreamCart.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Sepet güncelleme olayını (event) temsil eder.
 * Bu olay, sepete ekleme/çıkarma gibi işlemler sonucunda üretilir ve
 * stok yönetimi gibi downstream işlemci bileşenleri tarafından tüketilir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartUpdateEvent {
    private String userId;
    private String productId;
    private int quantityChange;
    private Instant timestamp; 
}