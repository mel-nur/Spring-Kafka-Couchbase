package com.melikenur.StreamCart.repository;

import com.melikenur.StreamCart.model.Cart;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import reactor.core.publisher.Mono;

/**
 * Veri erişim arayüzü: Cart belgeleri için reactive operasyonlar sağlar.
 * Ek olarak kullanıcı kimliğine göre sepet getirme metodu tanımlıdır.
 */
public interface CartRepository extends ReactiveCouchbaseRepository<Cart, String> {
    Mono<Cart> findByUserId(String userId);
}
