package mx.edu.utch.devqa.producto.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import mx.edu.utch.devqa.producto.model.Producto;

public interface ProductoRepository extends MongoRepository<Producto, String> {
}
