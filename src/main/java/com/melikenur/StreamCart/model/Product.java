package com.melikenur.StreamCart.model;

import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ürün modelini temsil eder.
 *
 * Alanlar:
 * - `id`: Ürün dokümanının kimliği
 * - `name`: Ürün adı
 * - `description`: Ürün açıklaması
 * - `price`: Birim fiyat
 * - `availableStock`: Mevcut satışa açık stok
 * - `reservedStock`: Rezerv edilmiş stok miktarı
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Product {

    @Id
    private String id;

    @Field
    private String name;

    @Field
    private String description;

    @Field
    private double price;

    @Field
    private int availableStock;
    
    @Field
    private int reservedStock;
    
}
