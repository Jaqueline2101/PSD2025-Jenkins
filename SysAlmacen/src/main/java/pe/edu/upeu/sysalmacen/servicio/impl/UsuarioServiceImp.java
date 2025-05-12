@Override
public UsuarioDTO register(UsuarioDTO.UsuarioCrearDto userDto) {
    Optional<Usuario> optionalUser = repo.findOneByUser(userDto.user());
    if (optionalUser.isPresent()) {
        throw new ModelNotFoundException("Login already exists", HttpStatus.BAD_REQUEST);
    }
    Usuario user = userMapper.toEntityFromCADTO(userDto);
    user.setClave(passwordEncoder.encode(CharBuffer.wrap(userDto.clave())));

    // Guardar el usuario
    Usuario savedUser = repo.save(user);

    // Obtener el rol basado en el nombre
    Rol r = null;
    switch (userDto.rol()) {
        case "ADMIN":
            r = rolService.getByNombre(Rol.RolNombre.ADMIN)
                .orElseThrow(() -> new ModelNotFoundException("Role ADMIN not found", HttpStatus.NOT_FOUND));
            break;
        case "DBA":
            r = rolService.getByNombre(Rol.RolNombre.DBA)
                .orElseThrow(() -> new ModelNotFoundException("Role DBA not found", HttpStatus.NOT_FOUND));
            break;
        default:
            r = rolService.getByNombre(Rol.RolNombre.USER)
                .orElseThrow(() -> new ModelNotFoundException("Role USER not found", HttpStatus.NOT_FOUND));
            break;
    }

    // Asignar el rol al usuario a través de la entidad UsuarioRol
    iurService.save(UsuarioRol.builder()
            .usuario(savedUser)
            .rol(r)
            .build());

    return userMapper.toDTO(savedUser);
}
