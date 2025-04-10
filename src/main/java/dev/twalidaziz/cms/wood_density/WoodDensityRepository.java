package dev.twalidaziz.cms.wood_density;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WoodDensityRepository extends JpaRepository<WoodDensity, Long> {

    @Query("""
        SELECT new dev.twalidaziz.cms.wood_density.WoodDensitySelectorRecord(w.id, w.name, w.code)
        FROM WoodDensity w
    """)
    List<WoodDensitySelectorRecord> findAllWithNameIdAndCode();

    @Query("""
        SELECT new dev.twalidaziz.cms.wood_density.WoodDensityListRecord(w.id, w.code, w.name, w.squareFootagePrice)
        FROM WoodDensity w
    """)
    Page<WoodDensityListRecord> findAllWithNameIdCodeAndPrice(Pageable pageable);

    @Query("""
        SELECT w.code
        FROM WoodDensity w
        WHERE w.id = :id
    """)
    String findCodeById(@Param("id") Long id);

    @Query("""
        SELECT new dev.twalidaziz.cms.wood_density.WoodDensityListRecord(w.id, w.code, w.name, w.squareFootagePrice)
        FROM WoodDensity w
        WHERE w.code LIKE :keyword OR w.name LIKE %:keyword%
    """)
    Page<WoodDensityListRecord> findByCodeOrName(@Param("keyword") String code, Pageable pageable);

    @Query("""
        SELECT w.squareFootagePrice
        FROM WoodDensity w
        WHERE w.id = :id
    """)
    BigDecimal findSquareFootagePriceById(@Param("id") Long id);

}
