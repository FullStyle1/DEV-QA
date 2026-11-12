package mx.edu.utch.devqa.producto.service;

import java.util.List;
import mx.edu.utch.devqa.producto.exception.DatoDuplicadoException;
import mx.edu.utch.devqa.producto.exception.RecursoNoEncontradoException;
import mx.edu.utch.devqa.producto.model.Producto;
import mx.edu.utch.devqa.producto.repository.ProductoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Producto crear(Producto producto) {
        producto.setId(null);
        if (repository.existsBySku(producto.getSku())) {
            throw new DatoDuplicadoException("Ya existe un producto con SKU " + producto.getSku());
        }
        return guardar(producto);
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtener(String id) {
        return buscarPorId(id);
    }

    public Producto actualizar(String id, Producto producto) {
        buscarPorId(id);
        if (repository.existsBySkuAndIdNot(producto.getSku(), id)) {
            throw new DatoDuplicadoException("Ya existe un producto con SKU " + producto.getSku());
        }
        producto.setId(id);
        return guardar(producto);
    }

    public void eliminar(String id) {
        repository.delete(buscarPorId(id));
    }

    private Producto buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con ID " + id));
    }

    private Producto guardar(Producto producto) {
        try {
            return repository.save(producto);
        } catch (DuplicateKeyException exception) {
            throw new DatoDuplicadoException("Ya existe un producto con SKU " + producto.getSku());
        }
    }
}
