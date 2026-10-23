import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListaMascotasForm extends JFrame {
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private Timer timerActualizacion;
    private final Color COLOR_PRIMARY = new Color(13, 148, 136);
    private final Color COLOR_BG = new Color(241, 245, 249);

    public ListaMascotasForm() {
        setTitle("Ecovet Huánuco - Historial y Estado de Spa");
        setSize(980, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (timerActualizacion != null) timerActualizacion.stop();
            }
        });

        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.setBackground(COLOR_BG);
        setContentPane(panelMain);

        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBackground(COLOR_PRIMARY);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel("Listado de Pacientes y Monitor en Vivo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Actualización en tiempo real de expedientes clínicos y fases de grooming");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(204, 251, 241));

        panelHeader.add(lblTitle);
        panelHeader.add(lblSub);
        panelMain.add(panelHeader, BorderLayout.NORTH);

        // Nueva columna de Observaciones agregada
        String[] columnas = {"ID", "Mascota", "Especie", "Raza", "Observaciones", "ID Cita", "Fase Spa Actual"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(35);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(226, 232, 240));
        tabla.setSelectionBackground(new Color(20, 184, 166));
        tabla.setSelectionForeground(Color.WHITE);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(15, 23, 42));
        header.setPreferredSize(new Dimension(0, 42));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Ajustar anchos
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelMain.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelBottom.setBackground(COLOR_BG);

        // Botones limpios sin símbolos Unicode no soportados
        JButton btnVolverMenu = crearBotonInteractiva("< Volver al Menú", new Color(100, 116, 139), new Color(71, 85, 105));
        JButton btnEliminar = crearBotonInteractiva("[ x ] Eliminar Paciente", new Color(225, 29, 72), new Color(190, 18, 60));
        JButton btnMonitor = crearBotonInteractiva("Abrir Monitor Individual >", COLOR_PRIMARY, new Color(15, 118, 110));

        panelBottom.add(btnVolverMenu);
        panelBottom.add(btnEliminar);
        panelBottom.add(btnMonitor);
        panelMain.add(panelBottom, BorderLayout.SOUTH);

        btnVolverMenu.addActionListener(e -> {
            if (timerActualizacion != null) timerActualizacion.stop();
            dispose();
        });
        btnEliminar.addActionListener(e -> eliminarMascotaSeleccionada());
        btnMonitor.addActionListener(e -> new MonitorSpaForm().setVisible(true));

        cargarDatosUnificados();
        timerActualizacion = new Timer(3000, e -> cargarDatosUnificados());
        timerActualizacion.start();
    }

    private JButton crearBotonInteractiva(String texto, Color colorBase, Color colorHover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(210, 40));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(colorHover); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(colorBase); }
        });
        return btn;
    }

    private void cargarDatosUnificados() {
        int selectedRow = tabla.getSelectedRow();
        modeloTabla.setRowCount(0);
        try (Connection conn = Conexion.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT m.id_mascota, m.nombre_mascota, m.especie, m.raza, m.tiene_observacion, " +
                 "IFNULL(c.id_cita, '-') AS id_cita, " +
                 "IFNULL(s.fase_actual, 'Sin Cita Registrada') AS fase_actual " +
                 "FROM mascotas m " +
                 "LEFT JOIN citas c ON m.id_mascota = c.id_mascota " +
                 "LEFT JOIN seguimiento_spa s ON c.id_cita = s.id_cita " +
                 "ORDER BY m.id_mascota DESC")) {

            while (rs.next()) {
                String faseSpa = rs.getString("fase_actual");
                if (!faseSpa.equals("Sin Cita Registrada")) { faseSpa = "Spa: " + faseSpa; }
                
                String obs = rs.getBoolean("tiene_observacion") ? "URGENTE / ATENCIÓN" : "-";

                Object[] fila = {
                    rs.getInt("id_mascota"),
                    rs.getString("nombre_mascota"),
                    rs.getString("especie"),
                    rs.getString("raza"),
                    obs,
                    rs.getString("id_cita"),
                    faseSpa
                };
                modeloTabla.addRow(fila);
            }
            if (selectedRow >= 0 && selectedRow < tabla.getRowCount()) {
                tabla.setRowSelectionInterval(selectedRow, selectedRow);
            }
        } catch (Exception ex) {
            // Ignorar errores silenciosos en la recarga
        }
    }

    private void eliminarMascotaSeleccionada() {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un paciente de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idMascota = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar al paciente con ID " + idMascota + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = Conexion.obtenerConexion();
                conn.setAutoCommit(false);

                PreparedStatement pstCitas = conn.prepareStatement("SELECT id_cita FROM citas WHERE id_mascota = ?");
                pstCitas.setInt(1, idMascota);
                ResultSet rsCitas = pstCitas.executeQuery();
                
                while (rsCitas.next()) {
                    PreparedStatement pstSpa = conn.prepareStatement("DELETE FROM seguimiento_spa WHERE id_cita = ?");
                    pstSpa.setInt(1, rsCitas.getInt("id_cita"));
                    pstSpa.executeUpdate();
                }

                PreparedStatement pst2 = conn.prepareStatement("DELETE FROM citas WHERE id_mascota = ?");
                pst2.setInt(1, idMascota);
                pst2.executeUpdate();

                PreparedStatement pst3 = conn.prepareStatement("DELETE FROM mascotas WHERE id_mascota = ?");
                pst3.setInt(1, idMascota);
                pst3.executeUpdate();

                conn.commit();
                cargarDatosUnificados();
            } catch (Exception ex) {
                if (conn != null) try { conn.rollback(); } catch (Exception ignored) {}
                JOptionPane.showMessageDialog(this, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
            }
        }
    }
}