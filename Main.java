import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando sistema Ecovet Huánuco...");
        Connection conn = Conexion.obtenerConexion();
        
        if (conn != null) {
            System.out.println("Sistema listo para operar con la BD en XAMPP.");
        } else {
            System.out.println("Fallo al conectar con la base de datos.");
        }
    }
}