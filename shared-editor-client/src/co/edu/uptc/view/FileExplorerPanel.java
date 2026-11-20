package co.edu.uptc.view;

import co.edu.uptc.presenter.IClientPresenter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileExplorerPanel extends JPanel {
    private DefaultListModel<String> fileListModel;
    private JList<String> fileList;
    private JButton newFileButton;
    private JButton importFileButton;
    private JButton exportFileButton;
    private IClientPresenter presenter;
    private boolean updatingList;

    public FileExplorerPanel() {
        super(new BorderLayout(4, 6));
        this.updatingList = false;
        initComponents();
    }

    public void setPresenter(IClientPresenter presenter) {
        this.presenter = presenter;
    }

    private void initComponents() {
        setPreferredSize(new Dimension(215, 400));
        setBorder(BorderFactory.createTitledBorder("Archivos (.java)"));
        this.fileListModel = new DefaultListModel<>();
        this.fileList = new JList<>(this.fileListModel);
        this.fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.fileList.addListSelectionListener(e -> onFileSelected(e.getValueIsAdjusting()));
        add(new JScrollPane(this.fileList), BorderLayout.CENTER);
        add(buildButtonsPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildButtonsPanel() {
        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 5));
        this.newFileButton = new JButton("Nuevo archivo .java");
        this.importFileButton = new JButton("Importar .java");
        this.exportFileButton = new JButton("Exportar .java");
        this.newFileButton.setToolTipText("Solo disponible para el Administrador");
        this.importFileButton.setToolTipText("Solo disponible para el Administrador");
        this.exportFileButton.setToolTipText("Guardar el archivo actual en su equipo");
        this.newFileButton.addActionListener(e -> onNewFileClicked());
        this.importFileButton.addActionListener(e -> openImportChooser());
        this.exportFileButton.addActionListener(e -> openExportChooser());
        buttons.add(this.newFileButton);
        buttons.add(this.importFileButton);
        buttons.add(this.exportFileButton);
        return buttons;
    }

    private void onFileSelected(boolean adjusting) {
        if (!adjusting && !this.updatingList && this.presenter != null) {
            String selected = this.fileList.getSelectedValue();
            if (selected != null) {
                this.presenter.selectFile(selected);
            }
        }
    }

    public void updateFiles(List<String> fileNames) {
        String previousSelection = this.fileList.getSelectedValue();
        this.updatingList = true;
        this.fileListModel.clear();
        for (int i = 0; i < fileNames.size(); i++) {
            this.fileListModel.addElement(fileNames.get(i));
        }
        if (previousSelection != null && fileNames.contains(previousSelection)) {
            this.fileList.setSelectedValue(previousSelection, true);
        } else if (!fileNames.isEmpty()) {
            this.fileList.setSelectedIndex(0);
        }
        this.updatingList = false;
    }

    public void setAdminControlsEnabled(boolean enabled) {
        this.newFileButton.setEnabled(enabled);
        this.importFileButton.setEnabled(enabled);
    }

    public String getSelectedFileName() {
        return this.fileList.getSelectedValue();
    }

    private void onNewFileClicked() {
        if (this.presenter != null) {
            String name = JOptionPane.showInputDialog(this, "Nombre del archivo (ej. Main.java):");
            if (name != null && !name.trim().isEmpty()) {
                this.presenter.createNewFile(name.trim());
            }
        }
    }

    private void openImportChooser() {
        if (this.presenter != null) {
            JFileChooser chooser = createJavaFileChooser();
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                this.presenter.importJavaFile(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private void openExportChooser() {
        if (this.presenter != null) {
            JFileChooser chooser = createJavaFileChooser();
            int option = chooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                this.presenter.exportCurrentFile(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private JFileChooser createJavaFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos Java (*.java)", "java"));
        return chooser;
    }
}
