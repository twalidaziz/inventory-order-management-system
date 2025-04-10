package dev.twalidaziz.cms.item;

import dev.twalidaziz.cms.material.Material;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ItemRepository extends JpaRepository<Item, Long> {

    Item findFirstByMaterials(Material material);

    List<Item> findAllByMaterials(Material material);

    boolean existsBySkuIgnoreCase(String sku);

    @Query("""
        SELECT new dev.twalidaziz.cms.item.ItemListRecord(i.id, i.sku, i.name, i.quantity, i.inUse, i.size, i.unitPrice, i.lastUpdated)
        FROM Item i
    """)
    Page<ItemListRecord> findAllItems(Pageable pageable);

    @Query("""
        SELECT new dev.twalidaziz.cms.item.ItemListRecord(i.id, i.sku, i.name, i.quantity, i.inUse, i.size, i.unitPrice, i.lastUpdated)
        FROM Item i
        WHERE i.category = :category
    """)
    Page<ItemListRecord> findItemsByCategory(@Param("category") ItemCategory category, Pageable pageable);

    @Query("""
        SELECT new dev.twalidaziz.cms.item.ItemListRecord(i.id, i.sku, i.name, i.quantity, i.inUse, i.size, i.unitPrice, i.lastUpdated)
        FROM Item i
        WHERE i.sku LIKE :keyword OR i.name LIKE %:keyword%
    """)
    Page<ItemListRecord> findBySkuOrName(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT i.unitPrice
        FROM Item i
        WHERE i.id = :id
    """)
    BigDecimal findUnitPriceById(@Param("id") Long id);

    @Query("""
        SELECT COUNT(i)
        FROM Item i
        WHERE i.quantity < 5 AND i.quantity > 0
    """)
    long countLowStock(@Param("level") long level);

    @Query("""
        SELECT COUNT(i)
        FROM Item i
        WHERE i.quantity = 0
    """)
    long countOutOfStock();

    /**
    @Query("""
        SELECT new dev.twalidaziz.cms.item.ItemEditRecord(i.id, i.sku, i.name, i.density.id, i.size, i.unitPrice, i.quantity, ,i.lastUpdated)
        FROM Item i
        WHERE i.id = :id
    """)
    ItemEditRecord findItemById(@Param("id") Long id);
    **/

}
