package pe.edu.upeu.sysalmacen.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.sysalmacen.dtos.VentCarritoDTO;
import pe.edu.upeu.sysalmacen.mappers.VentCarritoMapper;
import pe.edu.upeu.sysalmacen.modelo.*;
import pe.edu.upeu.sysalmacen.repositorio.ICrudGenericoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IProductoRepository;
import pe.edu.upeu.sysalmacen.repositorio.IUsuarioRepository;
import pe.edu.upeu.sysalmacen.repositorio.IVentCarritoRepository;
import pe.edu.upeu.sysalmacen.servicio.IVentCarritoService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VentCarritoServiceImp extends CrudGenericoServiceImp<VentCarrito, Long> implements IVentCarritoService {
    private final IVentCarritoRepository repo;
    private final VentCarritoMapper ventCarritoMapper;
    private final IProductoRepository productoRepository;
    private final IUsuarioRepository usuarioRepository;

    @Override
    protected ICrudGenericoRepository<VentCarrito, Long> getRepo() {
        return repo;
    }

    @Override
    public VentCarritoDTO saveD(VentCarritoDTO.VentCarritoCADTO dto) {
        // Mapeo del DTO a la entidad VentCarrito
        VentCarrito to = ventCarritoMapper.toEntityFromCADTO(dto);

        // Verificar si el producto existe
        Producto toA = productoRepository.findById(dto.producto())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        // Verificar si el usuario existe
        Usuario toB = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Asignar el producto y el usuario al carrito
        to.setProducto(toA);
        to.setUsuario(toB);

        // Guardar el carrito en el repositorio
        VentCarrito regGuardado = repo.save(to);

        // Devolver el DTO correspondiente
        return ventCarritoMapper.toDTO(regGuardado);
    }

    @Override
    public VentCarritoDTO updateD(VentCarritoDTO.VentCarritoCADTO dto, Long id) {
        // Buscar el carrito a actualizar
        VentCarrito to = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        // Mapeo del DTO a la entidad
        VentCarrito toX = ventCarritoMapper.toEntityFromCADTO(dto);

        // Mantener el ID del carrito original
        toX.setIdCarrito(to.getIdCarrito());

        // Verificar si el producto y usuario existen
        Producto toA = productoRepository.findById(dto.producto())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        Usuario toB = usuarioRepository.findById(dto.usuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Asignar el producto y el usuario al carrito
        toX.setProducto(toA);
        toX.setUsuario(toB);

        // Guardar el carrito actualizado
        VentCarrito productoActualizado = repo.save(toX);

        // Devolver el DTO correspondiente
        return ventCarritoMapper.toDTO(productoActualizado);
    }

    @Override
    public List<VentCarrito> listaCarritoCliente(String dniruc) {
        // Obtener el carrito del cliente por DNI/RUC
        return repo.listaCarritoCliente(dniruc);
    }

    @Override
    public void deleteCarAll(String dniruc) {
        // Eliminar todos los carritos de un cliente por su DNI/RUC
        this.repo.deleteByDniruc(dniruc);
    }
}
