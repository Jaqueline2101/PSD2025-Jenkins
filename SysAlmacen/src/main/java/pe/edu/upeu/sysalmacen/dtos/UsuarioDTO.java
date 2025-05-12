package pe.edu.upeu.sysalmacen.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioDTO {
    private Long idUsuario;
    @NotNull
    private String user;
    @NotNull
    private String clave;  // Descomentada para ser utilizada
    @NotNull
    private String estado;
    private String token;

    public record CredencialesDto(String user, char[] clave) {
        // Sobrescribiendo equals() para comparar el contenido de la matriz clave
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CredencialesDto that = (CredencialesDto) o;
            return user.equals(that.user) && java.util.Arrays.equals(clave, that.clave);
        }

        // Sobrescribiendo hashCode() para basarse en el contenido de la matriz clave
        @Override
        public int hashCode() {
            int result = user.hashCode();
            result = 31 * result + java.util.Arrays.hashCode(clave);
            return result;
        }

        // Sobrescribiendo toString() para representar el contenido de la matriz clave
        @Override
        public String toString() {
            return "CredencialesDto{" +
                    "user='" + user + '\'' +
                    ", clave=" + java.util.Arrays.toString(clave) + // Convierte la matriz a String
                    '}';
        }
    }

    public record UsuarioCrearDto(String user, char[] clave, String rol, String estado) { }
}
