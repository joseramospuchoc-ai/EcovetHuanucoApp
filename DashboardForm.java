import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardForm extends JFrame {
    private final Color COLOR_PRIMARY = new Color(13, 148, 136);
    private final Color COLOR_HOVER = new Color(15, 118, 110);
    private final Color COLOR_BG = new Color(241, 245, 249);

    public DashboardForm(String nombreUsuario, String rol) {
        setTitle("Ecovet Huánuco - Centro de Control");
        setSize(520, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(COLOR_BG);
        setContentPane(mainPanel);

        JPanel header = new JPanel(null);
        header.setBackground(COLOR_PRIMARY);
        header.setBounds(0, 0, 520, 95);
        mainPanel.add(header);

        JLabel lblWelcome = new JLabel("Bienvenido(a), " + nombreUsuario);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setBounds(30, 20, 450, 25);
        header.add(lblWelcome);

        JLabel lblRole = new JLabel("Rol: " + rol + " | Sede Central Huánuco");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(new Color(204, 251, 241));
        lblRole.setBounds(30, 50, 450, 20);
        header.add(lblRole);

        JButton btnRegistrar = crearBotonInteractiva("[ + ]  Registrar Paciente y Cita Spa", 130, COLOR_PRIMARY, COLOR_HOVER);
        JButton btnLista = crearBotonInteractiva("[ = ]  Ver Historial y Monitor en Vivo", 195, COLOR_PRIMARY, COLOR_HOVER);
        JButton btnCerrar = crearBotonInteractiva("[ x ]  Cerrar Sesión Segura", 270, new Color(225, 29, 72), new Color(190, 18, 60));

        mainPanel.add(btnRegistrar);
        mainPanel.add(btnLista);
        mainPanel.add(btnCerrar);

        btnRegistrar.addActionListener(e -> new MascotaForm().setVisible(true));
        btnLista.addActionListener(e -> new ListaMascotasForm().setVisible(true));
        btnCerrar.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
    }

    private JButton crearBotonInteractiva(String texto, int y, Color colorBase, Color colorHover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBounds(40, y, 425, 45);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(colorHover); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(colorBase); }
        });
        return btn;
    }
}