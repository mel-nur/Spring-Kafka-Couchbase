package com.melikenur.StreamCart.config;

import com.couchbase.client.java.Cluster;
import com.couchbase.transactions.Transactions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;

@Configuration
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Value("${spring.couchbase.connection-string}")
    private String connectionString;

    @Value("${spring.couchbase.username}")
    private String username;

    @Value("${spring.couchbase.password}")
    private String password;

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        // Bu konfigürasyon, spring-data-couchbase'in bağlanacağı bucket adını sağlar.
        // Uygulamanın configuration dosyasında da aynı bucket adı tanımlıdır.
        return "streamcart_data"; 
    }

    @Bean
    @Lazy // İşlem sınıfı yalnızca ihtiyaç duyulduğunda yüklenir
    public Transactions couchbaseTransactions(Cluster couchbaseCluster) {
        // Transaction Yöneticisini, Spring Data tarafından oluşturulan Cluster Bean'i kullanarak oluşturur
        return Transactions.create(couchbaseCluster);
    }
}
