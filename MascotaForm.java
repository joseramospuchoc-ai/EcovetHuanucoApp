import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MascotaForm extends JFrame {
    private JTextField txtNombreMascota, txtEspecie, txtRaza;
    private JSpinner spinFechaNac;
    private JCheckBox chkObservacion;
    private JButton btnGuardar, btnVolver;
    private final Color COLOR_PRIMARY = new Color(13, 148, 136);
    private final Color COLOR_BG = new Color(241, 245, 249);

    public MascotaForm() {
        setTitle("Ecovet Huánuco - Expediente Digital");
        setSize(460, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(COLOR_BG);
        setContentPane(mainPanel);

        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setBounds(25, 20, 395, 435);
        card.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        mainPanel.add(card);

        JLabel lblTitle = new JLabel("Nuevo Expediente Clínico", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBounds(15, 20, 365, 25);
        card.add(lblTitle);

        int y = 65;
        txtNombreMascota = crearCampoTexto(card, "Nombre del Paciente:", y);
        txtEspecie = crearCampoTexto(card, "Especie (Ej. Perro, Gato):", y + 55);
        txtRaza = crearCampoTexto(card, "Raza:", y + 110);
        
        // Selector de Fecha Nativo
        JLabel lblFecha = new JLabel("Nacimiento:");
        lblFecha.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFecha.setForeground(new Color(51, 65, 85));
        lblFecha.setBounds(25, y + 165, 170, 20);
        card.add(lblFecha);

        spinFechaNac = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinFechaNac, "yyyy-MM-dd");
        spinFechaNac.setEditor(dateEditor);
        spinFechaNac.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinFechaNac.setBounds(195, y + 160, 175, 32);
        spinFechaNac.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225), 1, true), new EmptyBorder(2, 5, 2, 5)));
        card.add(spinFechaNac);

        // Checkbox de Observación
        chkObservacion = new JCheckBox("Marcar si requiere cuidado especial / observación");
        chkObservacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkObservacion.setForeground(new Color(225, 29, 72));
        chkObservacion.setBackground(Color.WHITE);
        chkObservacion.setFocusPainted(false);
        chkObservacion.setBounds(22, y + 210, 350, 25);
        card.add(chkObservacion);

        btnGuardar = crearBotonInteractiva("Guardar Expediente y Generar Spa", 325, COLOR_PRIMARY, new Color(15, 118, 110));
        btnVolver = crearBotonInteractiva("<- Volver al Listado General", 375, new Color(100, 116, 139), new Color(71, 85, 105));

        card.add(btnGuardar);
        card.add(btnVolver);

        btnGuardar.addActionListener(e -> registrarMascotaYSpa());
        btnVolver.addActionListener(e -> { dispose(); new ListaMascotasForm().setVisible(true); });
    }

    private JTextField crearCampoTexto(JPanel panel, String labelText, int yPos) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(51, 65, 85));
        lbl.setBounds(25, yPos, 170, 20);
        panel.add(lbl);

        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBounds(195, yPos - 5, 175, 32);
        txt.setBorder(new CompoundBorder(new LineBorder(new Color(203, 213, 225), 1, true), new EmptyBorder(5, 10, 5, 10)));
        panel.add(txt);
        return txt;
    }

    private JButton crearBotonInteractiva(String texto, int y, Color colorBase, Color colorHover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBounds(25, y, 345, 40);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(colorHover); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(colorBase); }
        });
        return btn;
    }

    private void registrarMascotaYSpa() {
        String nombreMascota = txtNombreMascota.getText().trim();
        String especie = txtEspecie.getText().trim();
        String raza = txtRaza.getText().trim();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fechaNac = sdf.format((Date) spinFechaNac.getValue());
        boolean tieneObs = chkObservacion.isSelected();

        if (nombreMascota.isEmpty() || especie.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete el nombre y la especie.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Conexion.obtenerConexion()) {
            Statement stmtService = conn.createStatement();
            stmtService.executeUpdate("INSERT IGNORE INTO servicios (id_servicio, nombre_servicio, tipo, precio) VALUES (1, 'Baño General y Spa', 'Spa', 40.00)");

            // ID Usuario se asigna automáticamente como 1 internamente
            String sqlMascota = "INSERT INTO mascotas (id_usuario, nombre_mascota, especie, raza, fecha_nac, tiene_observacion) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtMascota = conn.prepareStatement(sqlMascota, Statement.RETURN_GENERATED_KEYS);
            pstmtMascota.setInt(1, 1);
            pstmtMascota.setString(2, nombreMascota);
            pstmtMascota.setString(3, especie);
            pstmtMascota.setString(4, raza);
            pstmtMascota.setString(5, fechaNac);
            pstmtMascota.setBoolean(6, tieneObs);
            pstmtMascota.executeUpdate();

            ResultSet rsKeys = pstmtMascota.getGeneratedKeys();
            int idMascota = 0;
            if (rsKeys.next()) { idMascota = rsKeys.getInt(1); }

            String sqlCita = "INSERT INTO citas (id_mascota, id_servicio, fecha, hora, estado) VALUES (?, 1, CURDATE(), CURTIME(), 'En Atención')";
            PreparedStatement pstmtCita = conn.prepareStatement(sqlCita, Statement.RETURN_GENERATED_KEYS);
            pstmtCita.setInt(1, idMascota);
            pstmtCita.executeUpdate();

            ResultSet rsCitaKeys = pstmtCita.getGeneratedKeys();
            int idCita = 0;
            if (rsCitaKeys.next()) { idCita = rsCitaKeys.getInt(1); }

            String sqlSpa = "INSERT INTO seguimiento_spa (id_cita, fase_actual, ultima_actualizacion) VALUES (?, 'Recepción', NOW())";
            PreparedStatement pstmtSpa = conn.prepareStatement(sqlSpa);
            pstmtSpa.setInt(1, idCita);
            pstmtSpa.executeUpdate();

            JOptionPane.showMessageDialog(this, "¡Registro exitoso!\nSe generó la Cita de Spa automáticamente con ID: [ " + idCita + " ]", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new ListaMascotasForm().setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar en la base de datos.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}