package dev.twalidaziz.cms.wood_density;

import dev.twalidaziz.cms.order.OrderRecord;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Transactional
public class WoodDensityService {

    private final WoodDensityRepository woodDensityRepository;

    public WoodDensityService(WoodDensityRepository woodDensityRepository) {
        this.woodDensityRepository = woodDensityRepository;
    }

    public BigDecimal getSquareFootagePriceById(Long id) {
        return woodDensityRepository.findSquareFootagePriceById(id);
    }

    public Page<WoodDensityListRecord> getAllWithNameIdCodeAndPrice(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return woodDensityRepository.findAllWithNameIdCodeAndPrice(pageable);
    }

    public Page<WoodDensityListRecord> getAllByCodeOrName(String keyword, int page, int size,
                                                          String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return woodDensityRepository.findByCodeOrName(keyword, pageable);
    }

}
