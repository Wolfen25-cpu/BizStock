package bizstock.ui;

import bizstock.dao.ProductDAO;
import bizstock.model.Product;
import bizstock.util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel de gestión de productos.
 * Reemplaza ProductsFrame – ahora es un JPanel que vive dentro del JTabbedPane.
 */
public class ProductsPanel extends JPanel {

    private final ProductDAO       productDAO = new ProductDAO();
    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final JLabel            lblStatus  = new JLabel("Listo.");

    public ProductsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Nombre", "Precio", "Cantidad", "Reorden", "Crítico", "CategoríaID", "MarcaID"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setGridColor(new Color(220, 230, 240));

        add(buildToolbar(),       BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildStatusBar(),     BorderLayout.SOUTH);
    }

    // ─── Toolbar ───────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bar.setBackground(new Color(240, 245, 252));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)));

        JButton btnRefresh = btn("🔄 Refrescar",  new Color(46, 116, 181));
        JButton btnAdd     = btn("➕ Agregar",     new Color(40, 140, 70));
        JButton btnEdit    = btn("✏️ Editar",      new Color(180, 120, 20));
        JButton btnDelete  = btn("🗑️ Eliminar",    new Color(180, 40, 40));

        btnRefresh.addActionListener(e -> loadProducts());
        btnAdd    .addActionListener(e -> showAddDialog());
        btnEdit   .addActionListener(e -> showEditDialog());
        btnDelete .addActionListener(e -> doDelete());

        // Solo admins pueden eliminar
        btnDelete.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                updateDeleteVisibility(btnDelete);
            }
        });

        bar.add(btnRefresh);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);
        return bar;
    }

    private void updateDeleteVisibility(JButton btnDelete) {
        if (Session.getCurrentUser() != null) {
            btnDelete.setEnabled(Session.getCurrentUser().isAdmin());
        }
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

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.PLAIN, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        return b;
    }

    // ─── Carga de datos ────────────────────────────────────────────────────────
    public void loadProducts() {
        try {
            List<Product> products = productDAO.findAllActive();
            tableModel.setRowCount(0);
            for (Product p : products) {
                tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getPrice(), p.getQuantity(),
                    p.getReorderLevel(), p.getCriticalLevel(), p.getCategoryId(), p.getBrandId()
                });
            }
            lblStatus.setText(products.size() + " productos activos.");
        } catch (Exception ex) {
            showError("Error cargando productos", ex);
        }
    }

    // ─── Agregar producto ──────────────────────────────────────────────────────
    private void showAddDialog() {
        JTextField fName   = new JTextField("Nuevo Producto", 20);
        JTextField fDesc   = new JTextField("Descripción", 20);
        JTextField fPrice  = new JTextField("0.00", 10);
        JTextField fQty    = new JTextField("0", 6);
        JTextField fReord  = new JTextField("10", 6);
        JTextField fCrit   = new JTextField("5", 6);
        JTextField fCatId  = new JTextField("1", 4);
        JTextField fBrandId= new JTextField("1", 4);

        JPanel form = buildForm(
            "Nombre:", fName, "Descripción:", fDesc,
            "Precio:", fPrice, "Cantidad inicial:", fQty,
            "Nivel reorden:", fReord, "Nivel crítico:", fCrit,
            "Categoría ID:", fCatId, "Marca ID:", fBrandId
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Agregar Producto",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            Product p = new Product();
            p.setName(fName.getText().trim());
            p.setDescription(fDesc.getText().trim());
            p.setPrice(new BigDecimal(fPrice.getText().trim()));
            p.setQuantity(Integer.parseInt(fQty.getText().trim()));
            p.setReorderLevel(Integer.parseInt(fReord.getText().trim()));
            p.setCriticalLevel(Integer.parseInt(fCrit.getText().trim()));
            p.setCategoryId(Integer.parseInt(fCatId.getText().trim()));
            p.setBrandId(Integer.parseInt(fBrandId.getText().trim()));

            int newId = productDAO.insert(p);
            loadProducts();
            lblStatus.setText("Producto agregado con ID " + newId + ".");
        } catch (Exception ex) {
            showError("No se pudo agregar el producto", ex);
        }
    }

    // ─── Editar producto ───────────────────────────────────────────────────────
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { tip("Selecciona un producto primero."); return; }

        int id = (int) tableModel.getValueAt(row, 0);

        JTextField fName   = new JTextField(str(tableModel.getValueAt(row, 1)), 20);
        JTextField fPrice  = new JTextField(str(tableModel.getValueAt(row, 2)), 10);
        JTextField fQty    = new JTextField(str(tableModel.getValueAt(row, 3)), 6);
        JTextField fReord  = new JTextField(str(tableModel.getValueAt(row, 4)), 6);
        JTextField fCrit   = new JTextField(str(tableModel.getValueAt(row, 5)), 6);
        JTextField fCatId  = new JTextField(str(tableModel.getValueAt(row, 6)), 4);
        JTextField fBrandId= new JTextField(str(tableModel.getValueAt(row, 7)), 4);

        JPanel form = buildForm(
            "Nombre:", fName, "Precio:", fPrice,
            "Cantidad:", fQty, "Nivel reorden:", fReord,
            "Nivel crítico:", fCrit, "Categoría ID:", fCatId,
            "Marca ID:", fBrandId, null, null
        );

        int res = JOptionPane.showConfirmDialog(this, form, "Editar Producto – ID " + id,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            Product p = new Product();
            p.setId(id);
            p.setName(fName.getText().trim());
            p.setDescription("");
            p.setPrice(new BigDecimal(fPrice.getText().trim()));
            p.setQuantity(Integer.parseInt(fQty.getText().trim()));
            p.setReorderLevel(Integer.parseInt(fReord.getText().trim()));
            p.setCriticalLevel(Integer.parseInt(fCrit.getText().trim()));
            p.setCategoryId(Integer.parseInt(fCatId.getText().trim()));
            p.setBrandId(Integer.parseInt(fBrandId.getText().trim()));

            boolean ok = productDAO.update(p);
            if (ok) { loadProducts(); lblStatus.setText("Producto ID " + id + " actualizado."); }
            else    { tip("No se pudo actualizar."); }
        } catch (Exception ex) {
            showError("Error editando producto", ex);
        }
    }

    // ─── Eliminar producto ─────────────────────────────────────────────────────
    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { tip("Selecciona un producto primero."); return; }

        int id     = (int) tableModel.getValueAt(row, 0);
        String name = str(tableModel.getValueAt(row, 1));

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desactivar el producto «" + name + "»?\n(No se borra de la BD, solo se desactiva.)",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = productDAO.softDelete(id);
            if (ok) { loadProducts(); lblStatus.setText("Producto ID " + id + " desactivado."); }
            else    { tip("No se pudo eliminar."); }
        } catch (Exception ex) {
            showError("Error eliminando producto", ex);
        }
    }

    // ─── Helpers de UI ────────────────────────────────────────────────────────
    private JPanel buildForm(Object... labelAndField) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        int row = 0;
        for (int i = 0; i < labelAndField.length; i += 2) {
            if (labelAndField[i] == null) { row++; continue; }
            c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.EAST; c.fill = GridBagConstraints.NONE;
            p.add(new JLabel((String) labelAndField[i]), c);
            c.gridx = 1; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
            p.add((Component) labelAndField[i + 1], c);
            row++;
        }
        return p;
    }

    private void tip(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}
