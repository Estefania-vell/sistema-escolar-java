import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class GestionEscuelaDB {

    // Datos de conexión a PostgreSQL
    static String url = "jdbc:postgresql://localhost:5432/escuela";
    static String user = "postgres";
    static String password = "55826P4M1.A"; // CAMBIA ESTO si pusiste otra contraseña
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int opcion = 0;

        while (opcion != 4) {
            System.out.println("\n--- SISTEMA ESCOLAR (Base de Datos) ---");
            System.out.println("1. Dar de alta (Registrar alumno)");
            System.out.println("2. Mostrar lista de alumnos");
            System.out.println("3. Dar de baja (Eliminar alumno)");
            System.out.println("4. Salir");
            System.out.print("Elige una opción: ");
            
            opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1: darDeAlta(); break;
                case 2: mostrarLista(); break;
                case 3: darDeBaja(); break;
                case 4: System.out.println("Saliendo..."); break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    // 1. INSERTAR (Alta)
    public static void darDeAlta() {
        System.out.print("Nombre del alumno: ");
        String nombre = scanner.nextLine();
        System.out.print("Edad: ");
        int edad = scanner.nextInt();
        scanner.nextLine();

        String sql = "INSERT INTO alumnos (nombre, edad) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            pstmt.setInt(2, edad);
            pstmt.executeUpdate();
            System.out.println("✅ ¡Alumno registrado en la base de datos!");
            
        } catch (SQLException e) {
            System.out.println("❌ Error al registrar: " + e.getMessage());
        }
    }

    // 2. CONSULTAR (Lista)
    public static void mostrarLista() {
        String sql = "SELECT id, nombre, edad FROM alumnos ORDER BY id";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- LISTA DE ALUMNOS (PostgreSQL) ---");
            boolean hayAlumnos = false;
            
            while (rs.next()) {
                hayAlumnos = true;
                System.out.println("ID: " + rs.getInt("id") + " | Nombre: " + rs.getString("nombre") + " | Edad: " + rs.getInt("edad"));
            }
            
            if (!hayAlumnos) System.out.println("📭 No hay alumnos registrados.");
            
        } catch (SQLException e) {
            System.out.println("❌ Error al consultar: " + e.getMessage());
        }
    }

    // 3. ELIMINAR (Baja)
    public static void darDeBaja() {
        System.out.print("Ingresa el ID del alumno a eliminar: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        String sql = "DELETE FROM alumnos WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("🗑️ Alumno eliminado correctamente.");
            } else {
                System.out.println("❌ No se encontró ningún alumno con ese ID.");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Error al eliminar: " + e.getMessage());
        }
    }
}