package bizstock.ui;

import bizstock.dao.ProductDAO;
import bizstock.model.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de alertas de inventario.
 * Reemplaza AlertsFrame – ahora vive dentro del JTabbedPane.
 */
public class AlertsPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();

    private final DefaultTableModel criticalModel;
    private final DefaultTableModel lowModel;
    private final JLabel            lblStatus = new JLabel("Listo.");
    private final JLabel            lblBadge  = new JLabel();

    public AlertsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Nombre", "Cantidad actual", "Nivel crítico (<=)", "Nivel reorden (<=)"};

        criticalModel = buildModel(cols);
        lowModel      = buildModel(cols);

        JTable criticalTable = styledTable(criticalModel, new Color(255, 220, 220));
        JTable lowTable      = styledTable(lowModel,      new Color(255, 243, 210));

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("🔴 Crítico",         new JScrollPane(criticalTable));
        subTabs.addTab("🟡 Bajo (reorden)",  new JScrollPane(lowTable));
        subTabs.setFont(new Font("Arial", Font.PLAIN, 13));

        add(buildToolbar(), BorderLayout.NORTH);
        add(subTabs,        BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ─── Toolbar ───────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(240, 245, 252));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JButton btnRefresh = new JButton("🔄 Refrescar alertas");
        btnRefresh.setBackground(new Color(46, 116, 181));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRefresh.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnRefresh.addActionListener(e -> loadAlerts());

        lblBadge.setFont(new Font("Arial", Font.BOLD, 12));
        lblBadge.setBorder(new EmptyBorder(0, 16, 0, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(btnRefresh);
        left.add(lblBadge);

        JLabel hint = new JLabel("Los niveles se configuran por producto en el módulo Productos.");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);

        bar.add(left, BorderLayout.WEST);
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        bar.setBackground(new Color(248, 250, 255));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 220, 235)));
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 11));
        lblStatus.setForeground(Color.GRAY);
        bar.add(lblStatus);
        return bar;
    }

    // ─── Cargar alertas ────────────────────────────────────────────────────────
    public void loadAlerts() {
        try {
            List<Product> critical = productDAO.findCriticalAlerts();
            List<Product> low      = productDAO.findLowAlerts();

            criticalModel.setRowCount(0);
            for (Product p : critical) criticalModel.addRow(row(p));

            lowModel.setRowCount(0);
            for (Product p : low) lowModel.addRow(row(p));

            int total = critical.size() + low.size();
            lblStatus.setText("Actualizado – " + critical.size() + " críticos, " + low.size() + " bajos.");

            if (total == 0) {
                lblBadge.setText("✅ Sin alertas activas");
                lblBadge.setForeground(new Color(30, 130, 60));
            } else {
                lblBadge.setText("⚠ " + total + " productos requieren atención");
                lblBadge.setForeground(new Color(180, 40, 40));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private Object[] row(Product p) {
        return new Object[]{ p.getId(), p.getName(), p.getQuantity(), p.getCriticalLevel(), p.getReorderLevel() };
    }

    private DefaultTableModel buildModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable styledTable(DefaultTableModel model, Color rowColor) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        t.setGridColor(new Color(220, 230, 240));

        // Colorear todas las filas con el color de alerta
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) c.setBackground(rowColor);
                return c;
            }
        });
        return t;
    }
}
