package mx.edu.utch.devqa.producto.service;

import java.util.List;
import java.util.NoSuchElementException;
import mx.edu.utch.devqa.producto.model.Producto;
import mx.edu.utch.devqa.producto.repository.ProductoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        return repository.save(producto);
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtener(String id) {
        return buscarPorId(id);
    }

    public Producto actualizar(String id, Producto producto) {
        buscarPorId(id);
        producto.setId(id);
        return repository.save(producto);
    }

    public void eliminar(String id) {
        repository.delete(buscarPorId(id));
    }

    private Producto buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el producto con ID " + id));
    }
}
