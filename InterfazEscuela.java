import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InterfazEscuela extends JFrame {

    private JTextField txtId; // Nuevo campo oculto para el ID
    private JTextField txtNombre;
    private JTextField txtEdad;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JButton btnGuardar;
    private JButton btnEditar;
    private JButton btnEliminar;

    private static final String URL = "jdbc:postgresql://localhost:5432/escuela";
    private static final String USER = "postgres";
    private static final String PASSWORD = "55826P4M1.A"; // Tu contraseña

    public InterfazEscuela() {
        setTitle("Sistema Escolar - Gestión de Alumnos");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Fuente general más grande
        Font fuente = new Font("Arial", Font.PLAIN, 14);
        UIManager.put("Label.font", fuente);
        UIManager.put("Button.font", fuente);
        UIManager.put("TextField.font", fuente);
        UIManager.put("Table.font", fuente);

        // --- Panel Superior (Formulario) ---
        JPanel panelFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFormulario.setBackground(new Color(230, 240, 255)); // Color de fondo azul clarito

        txtId = new JTextField(3);
        txtId.setVisible(false); // Ocultamos el ID visualmente, pero lo usaremos por debajo

        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField(15);
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Edad:"));
        txtEdad = new JTextField(5);
        panelFormulario.add(txtEdad);

        btnGuardar = new JButton("Guardar Nuevo");
        btnGuardar.setBackground(new Color(40, 167, 69)); // Verde
        btnGuardar.setForeground(Color.WHITE);
        
        btnEditar = new JButton("Guardar Cambios");
        btnEditar.setBackground(new Color(0, 123, 255)); // Azul
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setEnabled(false); // Desactivado hasta que se seleccione uno

        panelFormulario.add(btnGuardar);
        panelFormulario.add(btnEditar);

        add(panelFormulario, BorderLayout.NORTH);

        // --- Panel Central (Tabla) ---
        String[] columnas = {"ID", "Nombre", "Edad"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Evitar que editen directamente en la tabla
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(25); // Filas más altas
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Panel Inferior (Botones de Acción) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton btnRefrescar = new JButton("Refrescar Lista");
        btnEliminar = new JButton("Eliminar Seleccionado");
        btnEliminar.setBackground(new Color(220, 53, 69)); // Rojo
        btnEliminar.setForeground(Color.WHITE);
        
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- EVENTOS ---

        // Al hacer clic en una fila de la tabla, llenar los campos de arriba
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                int fila = tabla.getSelectedRow();
                txtId.setText(modeloTabla.getValueAt(fila, 0).toString());
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtEdad.setText(modeloTabla.getValueAt(fila, 2).toString());
                
                // Cambiamos el modo: habilitamos Editar y deshabilitamos Guardar
                btnGuardar.setEnabled(false);
                btnEditar.setEnabled(true);
            }
        });

        btnGuardar.addActionListener(e -> insertarAlumno());
        btnEditar.addActionListener(e -> actualizarAlumno());
        btnEliminar.addActionListener(e -> eliminarAlumno());
        btnRefrescar.addActionListener(e -> cargarAlumnos());

        // Limpiar selección y volver al modo normal
        btnRefrescar.addActionListener(e -> {
            tabla.clearSelection();
            txtId.setText("");
            txtNombre.setText("");
            txtEdad.setText("");
            btnGuardar.setEnabled(true);
            btnEditar.setEnabled(false);
        });

        cargarAlumnos(); // Cargar datos al inicio
    }

    // --- MÉTODOS ---

    private void cargarAlumnos() {
        modeloTabla.setRowCount(0);
        String sql = "SELECT id, nombre, edad FROM alumnos ORDER BY id";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modeloTabla.addRow(new Object[]{rs.getInt("id"), rs.getString("nombre"), rs.getInt("edad")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void insertarAlumno() {
        if (txtNombre.getText().isEmpty() || txtEdad.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Llena todos los campos", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO alumnos (nombre, edad) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtNombre.getText());
            pstmt.setInt(2, Integer.parseInt(txtEdad.getText()));
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "¡Alumno guardado!");
            limpiarYRefrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void actualizarAlumno() {
        String sql = "UPDATE alumnos SET nombre = ?, edad = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtNombre.getText());
            pstmt.setInt(2, Integer.parseInt(txtEdad.getText()));
            pstmt.setInt(3, Integer.parseInt(txtId.getText()));
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "¡Alumno actualizado!");
            limpiarYRefrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void eliminarAlumno() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un alumno", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String sql = "DELETE FROM alumnos WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Alumno eliminado");
            limpiarYRefrescar();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void limpiarYRefrescar() {
        txtId.setText("");
        txtNombre.setText("");
        txtEdad.setText("");
        btnGuardar.setEnabled(true);
        btnEditar.setEnabled(false);
        tabla.clearSelection();
        cargarAlumnos();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfazEscuela().setVisible(true));
    }
}