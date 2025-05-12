package pe.edu.upeu.sysalmacen.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.sysalmacen.dtos.UsuarioDTO;
import pe.edu.upeu.sysalmacen.security.JwtTokenUtil;
import pe.edu.upeu.sysalmacen.security.JwtUserDetailsService;
import pe.edu.upeu.sysalmacen.servicio.IUsuarioService;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {
    private final IUsuarioService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;

    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody @Valid UsuarioDTO.CredencialesDto credentialsDto, HttpServletRequest request) {
        // Validar y obtener el usuario desde el servicio
        UsuarioDTO userDto = userService.login(credentialsDto);
        
        // Obtener los detalles del usuario usando el servicio de detalles del JWT
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(credentialsDto.user());

        // Generar el token y asignarlo al usuario
        String token = jwtTokenUtil.generateToken(userDetails);
        userDto.setToken(token);

        // Guardar la información de la sesión del usuario
        request.getSession().setAttribute("USER_SESSION", userDto.getUser());

        // Retornar el DTO del usuario con el token generado
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody @Valid UsuarioDTO.UsuarioCrearDto user) {
        // Registrar el usuario a través del servicio
        UsuarioDTO createdUser = userService.register(user);

        // Obtener los detalles del usuario registrado
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.user());

        // Generar el token para el usuario registrado
        String token = jwtTokenUtil.generateToken(userDetails);
        createdUser.setToken(token);

        // Devolver una respuesta con el usuario creado y su token
        URI location = URI.create("/users/" + createdUser.getUser());
        return ResponseEntity.created(location).body(createdUser);
    }
}
