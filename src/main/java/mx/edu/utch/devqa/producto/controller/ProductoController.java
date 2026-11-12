package mx.edu.utch.devqa.producto.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mx.edu.utch.devqa.producto.dto.ProductoRequest;
import mx.edu.utch.devqa.producto.dto.ProductoResponse;
import mx.edu.utch.devqa.producto.model.Producto;
import mx.edu.utch.devqa.producto.service.ProductoService;
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
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        Producto creado = service.crear(toEntity(request));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.getId())).body(toResponse(creado));
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return service.listar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable String id) {
        return toResponse(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable String id, @Valid @RequestBody ProductoRequest request) {
        return toResponse(service.actualizar(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private Producto toEntity(ProductoRequest request) {
        return new Producto(null, request.sku(), request.nombre(), request.descripcion(),
                request.precio(), request.categoria());
    }

    private ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getSku(), producto.getNombre(),
                producto.getDescripcion(), producto.getPrecio(), producto.getCategoria());
    }
}
