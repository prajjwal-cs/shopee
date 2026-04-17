package com.prajjwal.productservice.repository;

import com.prajjwal.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(UUID skuId);

    Optional<Product> findBySkuAndActiveTrue(UUID sku);

    @Query("""
            SELECT p FROM Product p
            JOIN FETCH p.category c
            WHERE p.active = true
            AND  (:keyword IS NULL OR
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
                LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:categoryId IS NULL OR c.id = :categoryId)
    """)
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") UUID categoryId,
                                 Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(UUID id);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int reduceStock(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    void addStock(@Param("id") UUID id, @Param("quantity") int quantity);
}