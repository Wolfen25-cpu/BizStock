package bizstock.ui;

import bizstock.dao.UserDAO;
import bizstock.dao.ProductDAO;
import bizstock.model.User;
import bizstock.util.PdfExporter;
import bizstock.util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana principal única de BizStock.
 * Usa CardLayout para alternar entre el panel de Login y el panel principal de la app.
 * El panel principal usa JTabbedPane para que Productos, Movimientos y Alertas
 * estén todos dentro de la misma ventana sin abrir ventanas nuevas.
 */
public class MainFrame extends JFrame {

    // Nombres de las "cards" para el CardLayout
    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_APP   = "APP";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     rootPanel  = new JPanel(cardLayout);

    // Paneles reutilizables (se crean una vez)
    private LoginPanel     loginPanel;
    private AppPanel       appPanel;

    public MainFrame() {
        super("BizStock");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setMinimumSize(new Dimension(800, 540));
        setLocationRelativeTo(null);

        loginPanel = new LoginPanel(this);
        appPanel   = new AppPanel(this);

        rootPanel.add(loginPanel, CARD_LOGIN);
        rootPanel.add(appPanel,   CARD_APP);

        add(rootPanel);
        showLogin();
    }

    /** Muestra la pantalla de Login y limpia la sesión. */
    public void showLogin() {
        Session.clear();
        setTitle("BizStock – Login");
        loginPanel.reset();
        cardLayout.show(rootPanel, CARD_LOGIN);
    }

    /** Muestra la pantalla principal después de autenticarse. */
    public void showApp() {
        User u = Session.getCurrentUser();
        setTitle("BizStock – " + u.getUsername() + " [" + u.getRole() + "]");
        appPanel.onSessionStart();
        cardLayout.show(rootPanel, CARD_APP);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Panel de Login (interno a MainFrame)
    // ─────────────────────────────────────────────────────────────────────────
    static class LoginPanel extends JPanel {

        private final MainFrame  owner;
        private final UserDAO    userDAO = new UserDAO();
        private final JTextField txtUser = new JTextField(18);
        private final JPasswordField txtPass = new JPasswordField(18);
        private final JLabel    lblError = new JLabel(" ");

        LoginPanel(MainFrame owner) {
            this.owner = owner;
            setLayout(new GridBagLayout());
            setBackground(new Color(245, 247, 250));
            buildUI();
        }

        void reset() {
            txtUser.setText("");
            txtPass.setText("");
            lblError.setText(" ");
            txtUser.requestFocusInWindow();
        }

        private void buildUI() {
            JPanel card = new JPanel(new GridBagLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225), 1),
                new EmptyBorder(36, 48, 36, 48)
            ));

            GridBagConstraints c = new GridBagConstraints();
            c.insets  = new Insets(8, 8, 8, 8);
            c.fill    = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 2;

            // Título
            JLabel title = new JLabel("BizStock", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 26));
            title.setForeground(new Color(31, 78, 121));
            c.gridy = 0;
            card.add(title, c);

            JLabel sub = new JLabel("Sistema de Gestión de Inventario", SwingConstants.CENTER);
            sub.setFont(new Font("Arial", Font.PLAIN, 12));
            sub.setForeground(Color.GRAY);
            c.gridy = 1;
            card.add(sub, c);

            c.gridy = 2;
            card.add(Box.createVerticalStrut(10), c);

            // Usuario
            c.gridwidth = 1; c.gridy = 3; c.gridx = 0; c.fill = GridBagConstraints.NONE;
            card.add(new JLabel("Usuario:"), c);
            c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            card.add(txtUser, c);

            // Contraseña
            c.gridwidth = 1; c.gridy = 4; c.gridx = 0; c.fill = GridBagConstraints.NONE;
            card.add(new JLabel("Contraseña:"), c);
            c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            card.add(txtPass, c);

            // Error label
            lblError.setForeground(new Color(180, 40, 40));
            lblError.setFont(new Font("Arial", Font.PLAIN, 11));
            c.gridwidth = 2; c.gridx = 0; c.gridy = 5;
            card.add(lblError, c);

            // Botón
            JButton btnLogin = new JButton("Entrar");
            btnLogin.setBackground(new Color(31, 78, 121));
            btnLogin.setForeground(Color.WHITE);
            btnLogin.setFocusPainted(false);
            btnLogin.setFont(new Font("Arial", Font.BOLD, 13));
            btnLogin.setPreferredSize(new Dimension(200, 36));
            btnLogin.addActionListener(e -> doLogin());

            c.gridy = 6;
            card.add(btnLogin, c);

            // Enter en password
            txtPass.addActionListener(e -> doLogin());
            txtUser.addActionListener(e -> txtPass.requestFocusInWindow());

            add(card);
        }

        private void doLogin() {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            if (u.isEmpty()) {
                lblError.setText("Ingrese su usuario.");
                return;
            }
            try {
                User user = userDAO.authenticate(u, p);
                if (user == null) {
                    lblError.setText("Usuario o contraseña incorrectos.");
                    txtPass.setText("");
                    return;
                }
                Session.setCurrentUser(user);
                owner.showApp();
            } catch (Exception ex) {
                lblError.setText("Error: " + ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Panel principal de la app (con JTabbedPane)
    // ─────────────────────────────────────────────────────────────────────────
    static class AppPanel extends JPanel {

        private final MainFrame    owner;
        private final JLabel       lblUser  = new JLabel();
        private final JTabbedPane  tabs     = new JTabbedPane(JTabbedPane.LEFT);

        private ProductsPanel  productsPanel;
        private MovementsPanel movementsPanel;
        private AlertsPanel    alertsPanel;

        AppPanel(MainFrame owner) {
            this.owner = owner;
            setLayout(new BorderLayout());
            buildUI();
        }

        /** Llamado cuando el usuario inicia sesión: actualiza datos de sesión en los paneles. */
        void onSessionStart() {
            User u = Session.getCurrentUser();
            lblUser.setText("  " + u.getUsername() + "  |  " + u.getRole() + "  ");
            movementsPanel.setCurrentUserId(u.getId());
            // Cargar datos frescos al entrar
            productsPanel.loadProducts();
            alertsPanel.loadAlerts();
            movementsPanel.loadProducts();
            tabs.setSelectedIndex(0);
        }

        private void buildUI() {
            productsPanel  = new ProductsPanel();
            movementsPanel = new MovementsPanel();
            alertsPanel    = new AlertsPanel();

            // Tabs con íconos de texto
            tabs.addTab("📦 Productos",   productsPanel);
            tabs.addTab("🔄 Movimientos", movementsPanel);
            tabs.addTab("🚨 Alertas",     alertsPanel);
            tabs.setFont(new Font("Arial", Font.PLAIN, 13));

            // Barra superior
            JPanel topBar = buildTopBar();
            add(topBar, BorderLayout.NORTH);
            add(tabs,   BorderLayout.CENTER);
        }

        private JPanel buildTopBar() {
            JPanel bar = new JPanel(new BorderLayout());
            bar.setBackground(new Color(31, 78, 121));
            bar.setBorder(new EmptyBorder(6, 12, 6, 12));

            JLabel logo = new JLabel("BizStock");
            logo.setFont(new Font("Arial", Font.BOLD, 18));
            logo.setForeground(Color.WHITE);

            lblUser.setFont(new Font("Arial", Font.PLAIN, 12));
            lblUser.setForeground(new Color(200, 220, 255));

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton btnPdfInv    = makeBarButton("📄 PDF Inventario");
            JButton btnPdfReorder= makeBarButton("📄 PDF Reorden");
            JButton btnLogout    = makeBarButton("⬅ Salir");
            btnLogout.setForeground(new Color(255, 180, 180));

            btnPdfInv.addActionListener(e -> exportPdf(false));
            btnPdfReorder.addActionListener(e -> exportPdf(true));
            btnLogout.addActionListener(e -> owner.showLogin());

            right.add(lblUser);
            right.add(btnPdfInv);
            right.add(btnPdfReorder);
            right.add(new JSeparator(JSeparator.VERTICAL));
            right.add(btnLogout);

            bar.add(logo, BorderLayout.WEST);
            bar.add(right, BorderLayout.EAST);
            return bar;
        }

        private JButton makeBarButton(String text) {
            JButton b = new JButton(text);
            b.setFont(new Font("Arial", Font.PLAIN, 12));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(46, 116, 181));
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(4, 10, 4, 10));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        private void exportPdf(boolean reorder) {
            try {
                String path = reorder ? PdfExporter.exportReorder() : PdfExporter.exportInventory();
                JOptionPane.showMessageDialog(owner,
                    "✅ PDF generado:\n" + path, "Exportación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner,
                    "Error al generar PDF:\n" + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
