package dev.twalidaziz.cms.material;

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
@RequestMapping(value = "/api/materials", produces = MediaType.APPLICATION_JSON_VALUE)
public class MaterialResource {

    private final MaterialService materialService;

    public MaterialResource(final MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialDTO>> getAllMaterials() {
        return ResponseEntity.ok(materialService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialDTO> getMaterial(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(materialService.get(id));
    }

    @PostMapping
    public ResponseEntity<Long> createMaterial(@RequestBody @Valid final MaterialDTO materialDTO) {
        final Long createdId = materialService.create(materialDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateMaterial(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final MaterialDTO materialDTO) {
        materialService.update(id, materialDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable(name = "id") final Long id) {
        materialService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
