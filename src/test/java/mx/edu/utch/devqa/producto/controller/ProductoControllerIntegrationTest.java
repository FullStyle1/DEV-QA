package mx.edu.utch.devqa.producto.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mx.edu.utch.devqa.producto.model.Producto;
import mx.edu.utch.devqa.producto.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
@AutoConfigureMockMvc
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoRepository repository;

    @BeforeEach
    void resetRepository() {
        org.mockito.Mockito.reset(repository);
    }

    @Test
    void crearProductoValidoDevuelve201YUbicacion() throws Exception {
        when(repository.existsBySku("SKU-001")).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setId("id-1");
            return producto;
        });

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("SKU-001", "Producto de prueba")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/productos/id-1"))
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void crearProductoInvalidoDevuelve400ConDetalleDeValidacion() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("", " ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/productos"))
                .andExpect(jsonPath("$.details.sku").exists())
                .andExpect(jsonPath("$.details.nombre").exists());
    }

    @Test
    void listarProductosDevuelve200() throws Exception {
        when(repository.findAll()).thenReturn(List.of(producto("id-1", "SKU-001")));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"));
    }

    @Test
    void obtenerProductoExistenteDevuelve200() throws Exception {
        when(repository.findById("id-1")).thenReturn(Optional.of(producto("id-1", "SKU-001")));

        mockMvc.perform(get("/api/productos/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.nombre").value("Producto de prueba"));
    }

    @Test
    void obtenerProductoInexistenteDevuelve404ConMensaje() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe el producto con ID missing"))
                .andExpect(jsonPath("$.path").value("/api/productos/missing"));
    }

    @Test
    void skuDuplicadoDevuelve409() throws Exception {
        when(repository.existsBySku("SKU-001")).thenReturn(true);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("SKU-001", "Producto de prueba")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe un producto con SKU SKU-001"));
    }

    @Test
    void actualizarProductoExistenteDevuelve200() throws Exception {
        when(repository.findById("id-1")).thenReturn(Optional.of(producto("id-1", "SKU-001")));
        when(repository.existsBySkuAndIdNot("SKU-002", "id-1")).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/productos/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("SKU-002", "Producto actualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.sku").value("SKU-002"))
                .andExpect(jsonPath("$.nombre").value("Producto actualizado"));
    }

    @Test
    void eliminarProductoExistenteDevuelve204() throws Exception {
        Producto producto = producto("id-1", "SKU-001");
        when(repository.findById("id-1")).thenReturn(Optional.of(producto));
        doNothing().when(repository).delete(producto);

        mockMvc.perform(delete("/api/productos/id-1"))
                .andExpect(status().isNoContent())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(""));
    }

    @Test
    void eliminarProductoInexistenteDevuelve404() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/productos/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String productoJson(String sku, String nombre) {
        return """
                {
                  "sku": "%s",
                  "nombre": "%s",
                  "descripcion": "Evidencia de integracion",
                  "precio": 99.90,
                  "categoria": "Pruebas"
                }
                """.formatted(sku, nombre);
    }

    private Producto producto(String id, String sku) {
        return new Producto(id, sku, "Producto de prueba", "Evidencia de integracion",
                new BigDecimal("99.90"), "Pruebas");
    }
}
