import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InterfazEscuela extends JFrame {

    // Componentes de la ventana
    private JTextField txtNombre;
    private JTextField txtEdad;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    // Datos de conexión (los mismos de tu otro archivo)
    private static final String URL = "jdbc:postgresql://localhost:5432/escuela";
    private static final String USER = "postgres";
    private static final String PASSWORD = "55826P4M1.A"; // CAMBIA ESTO SI PUSISTE OTRA

    public InterfazEscuela() {
        // Configuración básica de la ventana
        setTitle("Sistema Escolar - Interfaz Gráfica");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en la pantalla
        setLayout(new BorderLayout(10, 10)); // Diseño de la ventana

        // --- 1. Panel Superior (Formulario para agregar) ---
        JPanel panelFormulario = new JPanel(new FlowLayout());
        
        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField(15);
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Edad:"));
        txtEdad = new JTextField(5);
        panelFormulario.add(txtEdad);

        JButton btnGuardar = new JButton("Guardar Alumno");
        panelFormulario.add(btnGuardar);

        add(panelFormulario, BorderLayout.NORTH);

        // --- 2. Panel Central (Tabla de datos) ---
        String[] columnas = {"ID", "Nombre", "Edad"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modeloTabla);
        
        // Agregamos la tabla a un "Scroll" para que se puedan ver muchos registros
        JScrollPane scrollPane = new JScrollPane(tabla);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Panel Inferior (Botones de acción) ---
        JPanel panelBotones = new JPanel(new FlowLayout());
        
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        JButton btnActualizar = new JButton("Refrescar Lista");
        
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- 4. Acciones de los Botones ---

        // Acción al hacer clic en "Guardar"
        btnGuardar.addActionListener(e -> guardarAlumno());

        // Acción al hacer clic en "Refrescar Lista"
        btnActualizar.addActionListener(e -> cargarAlumnos());

        // Acción al hacer clic en "Eliminar"
        btnEliminar.addActionListener(e -> eliminarAlumno());

        // Cargar los datos al abrir la ventana
        cargarAlumnos();
    }

    // --- MÉTODOS DE BASE DE DATOS ---

    private void cargarAlumnos() {
        modeloTabla.setRowCount(0); // Limpiar la tabla
        String sql = "SELECT id, nombre, edad FROM alumnos ORDER BY id";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getInt("edad")
                };
                modeloTabla.addRow(fila); // Agregar fila a la tabla visual
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarAlumno() {
        String nombre = txtNombre.getText().trim();
        String edadStr = txtEdad.getText().trim();

        if (nombre.isEmpty() || edadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor llena todos los campos", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO alumnos (nombre, edad) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setInt(2, Integer.parseInt(edadStr));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "¡Alumno guardado con éxito!");
            
            // Limpiar campos y refrescar tabla
            txtNombre.setText("");
            txtEdad.setText("");
            cargarAlumnos();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarAlumno() {
        int filaSeleccionada = tabla.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un alumno de la tabla primero", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener el ID del alumno de la fila seleccionada (columna 0)
        int id = (int) modeloTabla.getValueAt(filaSeleccionada, 0);

        String sql = "DELETE FROM alumnos WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Alumno eliminado.");
            cargarAlumnos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODO PRINCIPAL ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazEscuela ventana = new InterfazEscuela();
            ventana.setVisible(true);
        });
    }
}