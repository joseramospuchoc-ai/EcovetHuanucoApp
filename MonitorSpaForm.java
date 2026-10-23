import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MonitorSpaForm extends JFrame {
    private JTextField txtIdCita;
    private JProgressBar progressBar;
    private JLabel lblEstado;
    private JButton btnAvanzarFase;
    private String[] fases = {"Recepción", "Baño", "Corte", "Listo"};
    private int indiceFase = 0;
    private int idCitaRealEncontrada = -1; // Almacena la cita real vinculada

    private final Color COLOR_PRIMARY = new Color(13, 148, 136);
    private final Color COLOR_BG = new Color(241, 245, 249);
    private final Color COLOR_BTN = new Color(15, 118, 110);

    public MonitorSpaForm() {
        setTitle("Ecovet Huánuco - Monitor de Spa en Vivo");
        setSize(440, 330);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(COLOR_BG);
        setContentPane(panel);

        JLabel lblTitle = new JLabel("Seguimiento de Grooming y Spa", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBounds(20, 15, 385, 25);
        panel.add(lblTitle);

        // Nota aclaratoria para el usuario
        JLabel lblSub = new JLabel("(Puede ingresar el ID de la Mascota o ID de Cita)", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setBounds(20, 38, 385, 18);
        panel.add(lblSub);

        JLabel lblCita = new JLabel("Buscar ID:");
        lblCita.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCita.setForeground(new Color(51, 65, 85));
        lblCita.setBounds(25, 70, 110, 25);
        panel.add(lblCita);

        txtIdCita = new JTextField();
        txtIdCita.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtIdCita.setBounds(110, 68, 175, 32);
        panel.add(txtIdCita);

        JButton btnConsultar = new JButton("Buscar");
        btnConsultar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConsultar.setBackground(COLOR_PRIMARY);
        btnConsultar.setForeground(Color.WHITE);
        btnConsultar.setFocusPainted(false);
        btnConsultar.setOpaque(true);
        btnConsultar.setBorderPainted(false);
        btnConsultar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConsultar.setBounds(295, 68, 95, 32);
        panel.add(btnConsultar);

        progressBar = new JProgressBar(0, 3);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBar.setForeground(COLOR_PRIMARY);
        progressBar.setBounds(25, 125, 365, 35);
        panel.add(progressBar);

        lblEstado = new JLabel("Fase actual del servicio: -", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstado.setForeground(new Color(30, 41, 59));
        lblEstado.setBounds(25, 175, 365, 25);
        panel.add(lblEstado);

        btnAvanzarFase = new JButton("Avanzar Fase de Spa ->");
        btnAvanzarFase.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAvanzarFase.setBackground(COLOR_BTN);
        btnAvanzarFase.setForeground(Color.WHITE);
        btnAvanzarFase.setFocusPainted(false);
        btnAvanzarFase.setOpaque(true);
        btnAvanzarFase.setBorderPainted(false);
        btnAvanzarFase.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAvanzarFase.setBounds(25, 225, 365, 42);
        panel.add(btnAvanzarFase);

        btnConsultar.addActionListener(e -> consultarEstadoInteligente());
        btnAvanzarFase.addActionListener(e -> actualizarFaseSpa());
    }

    private void consultarEstadoInteligente() {
        String input = txtIdCita.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un ID.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Conexion.obtenerConexion()) {
            // Buscamos primero si el ID corresponde directamente a una cita, o bien a una mascota
            String sql = "SELECT s.id_cita, s.fase_actual FROM seguimiento_spa s " +
                         "JOIN citas c ON s.id_cita = c.id_cita " +
                         "WHERE c.id_cita = ? OR c.id_mascota = ? LIMIT 1";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int idVal = Integer.parseInt(input);
            pstmt.setInt(1, idVal);
            pstmt.setInt(2, idVal);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                idCitaRealEncontrada = rs.getInt("id_cita");
                String fase = rs.getString("fase_actual");
                lblEstado.setText("Cita #" + idCitaRealEncontrada + " - Fase: " + fase);
                
                for (int i = 0; i < fases.length; i++) {
                    if (fases[i].equalsIgnoreCase(fase)) {
                        indiceFase = i;
                        progressBar.setValue(i);
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró un seguimiento de spa activo para ese ID.", "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                idCitaRealEncontrada = -1;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número de ID válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarFaseSpa() {
        if (idCitaRealEncontrada == -1) {
            JOptionPane.showMessageDialog(this, "Primero busque y seleccione una cita válida.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (indiceFase < fases.length - 1) {
            indiceFase++;
            String nuevaFase = fases[indiceFase];
            try (Connection conn = Conexion.obtenerConexion()) {
                String sql = "UPDATE seguimiento_spa SET fase_actual = ?, ultima_actualizacion = NOW() WHERE id_cita = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nuevaFase);
                pstmt.setInt(2, idCitaRealEncontrada);
                pstmt.executeUpdate();

                progressBar.setValue(indiceFase);
                lblEstado.setText("Cita #" + idCitaRealEncontrada + " - Fase: " + nuevaFase);
                JOptionPane.showMessageDialog(this, "Estado actualizado a: " + nuevaFase, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Este servicio ya ha finalizado.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}