package com.melikenur.StreamCart.model;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.util.List;

/**
 * Sepet modelini temsil eder.
 *
 * - `id`: Couchbase belgesi kimliği
 * - `userId`: Sepetin sahibi olan kullanıcı kimliği
 * - `items`: Sepetteki ürün kalemleri
 * - `lastUpdated`: Son güncelleme zaman damgası (epoch ms)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Cart {
    @Id
    private String id;

    @Field
    private String userId;
    
    @Field
    private List<CartItem> items;

    @Field
    private long lastUpdated;
}
