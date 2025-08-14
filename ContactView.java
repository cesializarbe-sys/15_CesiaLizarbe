package vallegrande.edu.pe.view;

import vallegrande.edu.pe.controller.ContactController;
import vallegrande.edu.pe.model.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * ContactView - Reto de Diseño
 * Colores pastel, botones redondeados, paneles suaves, tipografía moderna
 */
public class ContactView extends JFrame {

    private final ContactController controller;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPanel rootPanel;
    private JPanel buttonsPanel;
    private JScrollPane scrollPane;
    private RoundedButton addBtn;
    private RoundedButton deleteBtn;

    // Colores pastel
    private final Color bgRoot = new Color(250, 245, 255); // lavanda muy suave
    private final Color bannerBg = new Color(230, 240, 255); // azul pastel claro
    private final Color cardBg = new Color(241, 246, 255); // celeste muy suave
    private final Color headerBg = new Color(186, 225, 255); // azul pastel más fuerte
    private final Color rowEven = Color.WHITE;
    private final Color rowOdd = new Color(236, 240, 255);
    private final Color selectBg = new Color(255, 224, 230); // rosa pastel al seleccionar
    private final Color primaryBtn = new Color(174, 214, 241); // celeste pastel
    private final Color dangerBtn = new Color(255, 183, 197); // rosa pastel
    private final Color textColor = new Color(50, 50, 50);

    public ContactView(ContactController controller) {
        super("Agenda MVC Swing - Vallegrande");
        this.controller = controller;
        initUI();
        loadContacts();
        JOptionPane.showMessageDialog(this, "¡Bienvenida! 😊 Lista tus contactos y gestiona fácilmente.", "Bienvenida", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Font baseFont = new Font("Segoe UI", Font.PLAIN, 16);
        setUIFont(baseFont);

        rootPanel = new JPanel(new BorderLayout(12,12));
        rootPanel.setBorder(new EmptyBorder(16,16,16,16));
        rootPanel.setBackground(bgRoot);
        setContentPane(rootPanel);

        // Tabla
        tableModel = new DefaultTableModel(new String[]{"ID","Nombre","Email","Teléfono"},0){
            @Override public boolean isCellEditable(int row, int column){ return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(baseFont);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.getTableHeader().setFont(baseFont.deriveFont(Font.BOLD,18f));
        table.getTableHeader().setBackground(headerBg);
        table.getTableHeader().setForeground(textColor);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column){
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                if(isSelected){
                    c.setBackground(selectBg);
                } else {
                    c.setBackground(row % 2 == 0 ? rowEven : rowOdd);
                }
                setForeground(textColor);
                setBorder(new EmptyBorder(8,12,8,12));
                return c;
            }
        });

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(cardBg,1,true));
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        // Botones
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));
        buttonsPanel.setBackground(bgRoot);

        addBtn = new RoundedButton("➕ Agregar");
        deleteBtn = new RoundedButton("🗑 Eliminar");
        styleButton(addBtn, primaryBtn);
        styleButton(deleteBtn, dangerBtn);

        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        rootPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Acciones
        addBtn.addActionListener(e -> {
            showAddContactDialog();
        });

        deleteBtn.addActionListener(e -> deleteSelectedContact());
    }

    private void styleButton(RoundedButton btn, Color base){
        btn.setBackground(base);
        btn.setForeground(textColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD,16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10,20,10,20));
        btn.setHoverBackground(base.darker());
    }

    private void loadContacts(){
        SwingWorker<List<Contact>, Void> worker = new SwingWorker<>() {
            @Override protected List<Contact> doInBackground() { return controller.list(); }
            @Override protected void done(){
                try{
                    tableModel.setRowCount(0);
                    for(Contact c:get()){
                        tableModel.addRow(new Object[]{c.id(),c.name(),c.email(),c.phone()});
                    }
                } catch(Exception ex){
                    JOptionPane.showMessageDialog(ContactView.this,"Error al cargar contactos","Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showAddContactDialog(){
        AddContactDialog dialog = new AddContactDialog(this,controller);
        dialog.setVisible(true);
        if(dialog.isSucceeded()){
            loadContacts();
            JOptionPane.showMessageDialog(this,"Contacto agregado con éxito ✅","Éxito",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedContact(){
        int selectedRow = table.getSelectedRow();
        if(selectedRow==-1){
            JOptionPane.showMessageDialog(this,"Selecciona un contacto para eliminar ⚠️","Atención",JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(selectedRow);
        String name = (String) tableModel.getValueAt(row,1);
        String id = (String) tableModel.getValueAt(row,0);

        int confirm = JOptionPane.showConfirmDialog(this,"¿Seguro que deseas eliminar a \""+name+"\"?","Confirmar eliminación",JOptionPane.YES_NO_OPTION);
        if(confirm==JOptionPane.YES_OPTION){
            controller.delete(id);
            loadContacts();
            JOptionPane.showMessageDialog(this,"Contacto eliminado 🗑️","Eliminado",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void setUIFont(Font f){
        UIManager.put("Button.font",f);
        UIManager.put("Label.font",f);
        UIManager.put("Table.font",f);
        UIManager.put("TableHeader.font",f.deriveFont(Font.BOLD));
        UIManager.put("TextField.font",f);
        UIManager.put("OptionPane.messageFont",f);
        UIManager.put("OptionPane.buttonFont",f);
    }

    // =========================
    // Botón redondeado simple
    // =========================
    private static class RoundedButton extends JButton{
        private Color hoverBg = null;
        public RoundedButton(String text){ super(text); setContentAreaFilled(false); setFocusPainted(false); }
        public void setHoverBackground(Color c){ hoverBg=c; }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getModel().isRollover() && hoverBg!=null ? hoverBg : getBackground();
            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}