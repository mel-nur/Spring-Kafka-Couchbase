package com.melikenur.StreamCart.repository;
import com.melikenur.StreamCart.model.Product;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import reactor.core.publisher.Mono;
/**
 * Veri erişim arayüzü: Product belgeleri için reactive operasyonlar sağlar.
 */
public interface ProductRepository extends ReactiveCouchbaseRepository<Product, String> {
    
    // Ürün ID'sine göre arama (ReactiveCouchbaseRepository bunu zaten sağlar)
    Mono<Product> findById(String id);
}