package dev.twalidaziz.cms.supplier;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/suppliers", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupplierResource {

    private final SupplierService supplierService;

    public SupplierResource(final SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplier(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(supplierService.get(id));
    }

    @PostMapping
    public ResponseEntity<Long> createSupplier(@RequestBody @Valid final SupplierDTO supplierDTO) {
        final Long createdId = supplierService.create(supplierDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateSupplier(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final SupplierDTO supplierDTO) {
        supplierService.update(id, supplierDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable(name = "id") final Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
