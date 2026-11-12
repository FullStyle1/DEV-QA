package mx.edu.utch.devqa.producto.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mx.edu.utch.devqa.producto.exception.DatoDuplicadoException;
import mx.edu.utch.devqa.producto.exception.RecursoNoEncontradoException;
import mx.edu.utch.devqa.producto.model.Producto;
import mx.edu.utch.devqa.producto.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(repository);
    }

    @Test
    void crearProductoCorrectamente() {
        Producto producto = producto(null, "SKU-001");
        when(repository.existsBySku("SKU-001")).thenReturn(false);
        when(repository.save(producto)).thenAnswer(invocation -> {
            Producto guardado = invocation.getArgument(0);
            guardado.setId("id-1");
            return guardado;
        });

        Producto resultado = service.crear(producto);

        assertEquals("id-1", resultado.getId());
        assertEquals("SKU-001", resultado.getSku());
        verify(repository).save(producto);
    }

    @Test
    void rechazarCreacionConSkuDuplicado() {
        Producto producto = producto(null, "SKU-001");
        when(repository.existsBySku("SKU-001")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> service.crear(producto));

        verify(repository).existsBySku("SKU-001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void convertirConflictoDelIndiceUnicoAlCrear() {
        Producto producto = producto(null, "SKU-001");
        when(repository.existsBySku("SKU-001")).thenReturn(false);
        when(repository.save(producto)).thenThrow(new DuplicateKeyException("duplicate key"));

        assertThrows(DatoDuplicadoException.class, () -> service.crear(producto));
    }

    @Test
    void listarProductos() {
        List<Producto> productos = List.of(producto("id-1", "SKU-001"), producto("id-2", "SKU-002"));
        when(repository.findAll()).thenReturn(productos);

        assertEquals(productos, service.listar());
    }

    @Test
    void obtenerProductoExistente() {
        Producto producto = producto("id-1", "SKU-001");
        when(repository.findById("id-1")).thenReturn(Optional.of(producto));

        assertEquals(producto, service.obtener("id-1"));
    }

    @Test
    void lanzarExcepcionAlObtenerProductoInexistente() {
        when(repository.findById("id-1")).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.obtener("id-1")
        );

        assertTrue(exception.getMessage().contains("id-1"));
    }

    @Test
    void actualizarProductoExistente() {
        Producto existente = producto("id-1", "SKU-001");
        Producto cambios = producto(null, "SKU-002");
        when(repository.findById("id-1")).thenReturn(Optional.of(existente));
        when(repository.existsBySkuAndIdNot("SKU-002", "id-1")).thenReturn(false);
        when(repository.save(cambios)).thenReturn(cambios);

        Producto resultado = service.actualizar("id-1", cambios);

        assertEquals("id-1", resultado.getId());
        assertEquals("SKU-002", resultado.getSku());
        verify(repository).save(cambios);
    }

    @Test
    void permitirConservarElSkuDelMismoProducto() {
        Producto existente = producto("id-1", "SKU-001");
        Producto cambios = producto(null, "SKU-001");
        when(repository.findById("id-1")).thenReturn(Optional.of(existente));
        when(repository.existsBySkuAndIdNot("SKU-001", "id-1")).thenReturn(false);
        when(repository.save(cambios)).thenReturn(cambios);

        assertEquals("id-1", service.actualizar("id-1", cambios).getId());
    }

    @Test
    void rechazarActualizacionConSkuDeOtroProducto() {
        Producto cambios = producto(null, "SKU-002");
        when(repository.findById("id-1")).thenReturn(Optional.of(producto("id-1", "SKU-001")));
        when(repository.existsBySkuAndIdNot("SKU-002", "id-1")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> service.actualizar("id-1", cambios));
    }

    @Test
    void convertirConflictoDelIndiceUnicoAlActualizar() {
        Producto cambios = producto(null, "SKU-002");
        when(repository.findById("id-1")).thenReturn(Optional.of(producto("id-1", "SKU-001")));
        when(repository.existsBySkuAndIdNot("SKU-002", "id-1")).thenReturn(false);
        when(repository.save(cambios)).thenThrow(new DuplicateKeyException("duplicate key"));

        assertThrows(DatoDuplicadoException.class, () -> service.actualizar("id-1", cambios));
    }

    @Test
    void noActualizarProductoInexistente() {
        when(repository.findById("id-1")).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.actualizar("id-1", producto(null, "SKU-002"))
        );
    }

    @Test
    void eliminarProductoExistente() {
        Producto existente = producto("id-1", "SKU-001");
        when(repository.findById("id-1")).thenReturn(Optional.of(existente));

        service.eliminar("id-1");

        verify(repository).delete(existente);
    }

    @Test
    void noEliminarProductoInexistente() {
        when(repository.findById("id-1")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar("id-1"));
    }

    private Producto producto(String id, String sku) {
        return new Producto(id, sku, "Libro de prueba", "Descripcion de prueba",
                new BigDecimal("99.90"), "Libros");
    }
}
