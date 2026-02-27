package bizstock.ui;

import bizstock.dao.InventoryMovementDAO;
import bizstock.dao.ProductDAO;
import bizstock.model.InventoryMovement;
import bizstock.model.Product;
import bizstock.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de movimientos de inventario (entradas / salidas).
 * Reemplaza MovementsFrame – ahora vive dentro del JTabbedPane.
 */
public class MovementsPanel extends JPanel {

    private final ProductDAO           productDAO  = new ProductDAO();
    private final InventoryMovementDAO movementDAO = new InventoryMovementDAO();
    private final InventoryService     service     = new InventoryService();

    private final JComboBox<ProductItem> cboProducts  = new JComboBox<>();
    private final JTextField             txtQty       = new JTextField(8);
    private final JTextField             txtNote      = new JTextField(26);
    private final JLabel                 lblAvailable = new JLabel("—");

    private final DefaultTableModel historyModel;
    private final JTable            historyTable;
    private final JLabel            lblStatus = new JLabel("Listo.");

    private int currentUserId = 1;

    public MovementsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] cols = {"Fecha / Hora", "Tipo", "Cantidad", "Usuario ID", "Nota"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(22);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        historyTable.setGridColor(new Color(220, 230, 240));

        add(buildFormPanel(),            BorderLayout.NORTH);
        add(buildHistoryPanel(),         BorderLayout.CENTER);
        add(buildStatusBar(),            BorderLayout.SOUTH);

        // Al cambiar de producto, refresca disponible e historial
        cboProducts.addActionListener(e -> refreshAll());
    }

    public void setCurrentUserId(int id) { this.currentUserId = id; }

    // ─── Cargar productos en el combo ─────────────────────────────────────────
    public void loadProducts() {
        try {
            cboProducts.removeAllItems();
            List<Product> list = productDAO.findAllActive();
            for (Product p : list) cboProducts.addItem(new ProductItem(p.getId(), p.getName()));
            if (cboProducts.getItemCount() > 0) cboProducts.setSelectedIndex(0);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // ─── Panel de formulario (arriba) ─────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(240, 245, 252));
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;

        // Fila 0 – Producto + disponible
        c.gridx = 0; c.gridy = 0;
        form.add(label("Producto:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
        form.add(cboProducts, c);
        c.gridx = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        JPanel dispPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dispPanel.setOpaque(false);
        dispPanel.add(label("Disponible:"));
        lblAvailable.setFont(new Font("Arial", Font.BOLD, 14));
        lblAvailable.setForeground(new Color(31, 78, 121));
        dispPanel.add(lblAvailable);
        form.add(dispPanel, c);

        // Fila 1 – Cantidad + Nota
        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE;
        form.add(label("Cantidad:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.NONE;
        form.add(txtQty, c);

        c.gridx = 2; c.gridy = 1;
        form.add(label("Nota opcional:"), c);
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
        form.add(txtNote, c);

        // Botones
        JButton btnIn      = actionBtn("⬆ Entrada",  new Color(40, 140, 70));
        JButton btnOut     = actionBtn("⬇ Salida",   new Color(180, 40, 40));
        JButton btnRefresh = actionBtn("🔄 Refrescar", new Color(46, 116, 181));

        btnIn.addActionListener(e -> doMovement(true));
        btnOut.addActionListener(e -> doMovement(false));
        btnRefresh.addActionListener(e -> { loadProducts(); refreshAll(); });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnBar.setOpaque(false);
        btnBar.add(btnIn);
        btnBar.add(btnOut);
        btnBar.add(Box.createHorizontalStrut(20));
        btnBar.add(btnRefresh);

        outer.add(form,   BorderLayout.CENTER);
        outer.add(btnBar, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(8, 0, 0, 0));
        JLabel title = new JLabel("Historial de movimientos (últimos 50)");
        title.setFont(new Font("Arial", Font.BOLD, 12));
        title.setForeground(new Color(60, 80, 120));
        p.add(title, BorderLayout.NORTH);
        p.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return p;
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

    // ─── Lógica de movimiento ─────────────────────────────────────────────────
    private void doMovement(boolean isIn) {
        Integer productId = selectedProductId();
        if (productId == null) { tip("Selecciona un producto."); return; }

        String qtyText = txtQty.getText().trim();
        if (qtyText.isEmpty()) { tip("Ingresa una cantidad."); return; }

        try {
            int qty = Integer.parseInt(qtyText);
            String note = txtNote.getText().trim();

            if (isIn) service.registerIn(productId, qty, currentUserId, note);
            else      service.registerOut(productId, qty, currentUserId, note);

            txtQty.setText("");
            txtNote.setText("");
            refreshAll();

            String tipo = isIn ? "Entrada" : "Salida";
            lblStatus.setText(tipo + " de " + qty + " unidades registrada.");

        } catch (NumberFormatException ex) {
            tip("La cantidad debe ser un número entero.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void refreshAll() {
        Integer pid = selectedProductId();
        if (pid == null) { lblAvailable.setText("—"); historyModel.setRowCount(0); return; }
        refreshAvailable(pid);
        refreshHistory(pid);
    }

    private void refreshAvailable(int pid) {
        try {
            int qty = service.getCurrentQty(pid);
            lblAvailable.setText(String.valueOf(qty));
            lblAvailable.setForeground(qty <= 5 ? new Color(180, 40, 40)
                                     : qty <= 10 ? new Color(180, 120, 0)
                                     : new Color(31, 78, 121));
        } catch (Exception ex) { lblAvailable.setText("?"); }
    }

    private void refreshHistory(int pid) {
        try {
            List<InventoryMovement> list = movementDAO.findByProduct(pid, 50);
            historyModel.setRowCount(0);
            for (InventoryMovement m : list) {
                String date = m.getCreatedAt() != null ? m.getCreatedAt().toString().replace('T', ' ') : "";
                historyModel.addRow(new Object[]{date, m.getMovementType(), m.getQuantity(), m.getUserId(), m.getNote()});
            }
        } catch (Exception ex) { showError(ex); }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private Integer selectedProductId() {
        ProductItem item = (ProductItem) cboProducts.getSelectedItem();
        return item == null ? null : item.id;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.PLAIN, 12)); return l;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    private void tip(String msg) { JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE); }
    private void showError(Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }

    // ─── Inner class item combo ────────────────────────────────────────────────
    private static class ProductItem {
        final int id; final String name;
        ProductItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return id + " – " + name; }
    }
}
