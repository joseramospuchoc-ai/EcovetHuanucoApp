import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    private final Color COLOR_PRIMARY = new Color(13, 148, 136); // Verde Teal
    private final Color COLOR_BG = new Color(241, 245, 249);      // Fondo gris claro
    private final Color COLOR_CARD = Color.WHITE;

    public LoginForm() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Ecovet Huánuco - Acceso al Sistema");
        setSize(440, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(COLOR_BG);
        setContentPane(panel);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(COLOR_CARD);
        card.setBounds(25, 20, 375, 260);
        card.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        panel.add(card);

        JLabel lblTitle = new JLabel("ECOVET HUÁNUCO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBounds(15, 15, 345, 25);
        card.add(lblTitle);

        JLabel lblEmail = new JLabel("Correo Electrónico:");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmail.setForeground(new Color(30, 41, 59));
        lblEmail.setBounds(25, 55, 150, 20);
        card.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setBounds(25, 78, 325, 30);
        card.add(txtEmail);

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(new Color(30, 41, 59));
        lblPassword.setBounds(25, 115, 150, 20);
        card.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setBounds(25, 138, 325, 30);
        card.add(txtPassword);

        btnIngresar = new JButton("Ingresar al Sistema");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setBackground(COLOR_PRIMARY);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setOpaque(true);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setBounds(25, 190, 325, 38);
        card.add(btnIngresar);

        btnIngresar.addActionListener(e -> autenticarUsuario());
    }

    private void autenticarUsuario() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Conexion.obtenerConexion()) {
            String sql = "SELECT * FROM usuarios WHERE email = ? AND password_hash = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("nombre");
                String rol = rs.getString("rol");
                dispose();
                new DashboardForm(nombre, rol).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}