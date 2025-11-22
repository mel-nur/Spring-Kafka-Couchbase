package com.melikenur.StreamCart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sepet içindeki tek bir kalemi temsil eder.
 * - `productId`: Ürünün kimliği
 * - `quantity`: Bu kalemdeki miktar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private int quantity;
}
