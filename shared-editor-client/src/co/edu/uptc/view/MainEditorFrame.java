package co.edu.uptc.view;

import co.edu.uptc.presenter.IClientPresenter;
import co.edu.uptc.presenter.IEditorView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainEditorFrame extends JFrame implements IEditorView {
    private FileExplorerPanel fileExplorerPanel;
    private CodeEditorPanel codeEditorPanel;
    private ActiveUsersPanel activeUsersPanel;
    private JLabel sessionUserLabel;
    private JLabel sessionColorBadge;
    private JLabel statusBarLabel;
    private IClientPresenter presenter;

    public MainEditorFrame() {
        super("Editor de Codigo Colaborativo (Java)");
        initLayout();
        initWindowCloseListener();
    }

    @Override
    public void setPresenter(IClientPresenter presenter) {
        this.presenter = presenter;
        this.fileExplorerPanel.setPresenter(presenter);
        this.codeEditorPanel.setPresenter(presenter);
    }

    private void initLayout() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(960, 580));
        setPreferredSize(new Dimension(1120, 680));
        this.fileExplorerPanel = new FileExplorerPanel();
        this.codeEditorPanel = new CodeEditorPanel();
        this.activeUsersPanel = new ActiveUsersPanel();
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(buildTopSessionBar(), BorderLayout.NORTH);
        content.add(this.fileExplorerPanel, BorderLayout.WEST);
        content.add(this.codeEditorPanel, BorderLayout.CENTER);
        content.add(this.activeUsersPanel, BorderLayout.EAST);
        content.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildTopSessionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBorder(BorderFactory.createEtchedBorder());
        this.sessionColorBadge = new JLabel(" ");
        this.sessionColorBadge.setOpaque(true);
        this.sessionColorBadge.setPreferredSize(new Dimension(16, 16));
        this.sessionColorBadge.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.sessionUserLabel = new JLabel("Sesion no iniciada");
        this.sessionUserLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        bar.add(this.sessionColorBadge);
        bar.add(this.sessionUserLabel);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        this.statusBarLabel = new JLabel("Listo");
        this.statusBarLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        statusPanel.add(this.statusBarLabel, BorderLayout.WEST);
        return statusPanel;
    }

    private void initWindowCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (presenter != null) {
                    presenter.disconnectFromServer();
                }
                dispose();
                System.exit(0);
            }
        });
    }

    @Override
    public void showEditorWindow(String username, int colorId, boolean isAdmin) {
        String roleText = isAdmin ? "Administrador" : "Colaborador";
        this.sessionColorBadge.setBackground(UserColorPalette.getAccentColor(colorId));
        this.sessionUserLabel.setText("Usuario: " + username + "  |  Rol: " + roleText);
        this.fileExplorerPanel.setAdminControlsEnabled(isAdmin);
        setVisible(true);
    }

    @Override
    public void closeEditorWindow() {
        setVisible(false);
    }

    @Override
    public void updateConnectedUsers(List<String[]> usersData) {
        this.activeUsersPanel.refreshUsers(usersData);
    }

    @Override
    public void updateFileList(List<String> fileNames) {
        this.fileExplorerPanel.updateFiles(fileNames);
    }

    @Override
    public void setAdminPermissions(boolean isAdmin) {
        this.fileExplorerPanel.setAdminControlsEnabled(isAdmin);
        String currentText = this.sessionUserLabel.getText();
        int separatorIndex = currentText.indexOf("|");
        if (separatorIndex > 0) {
            String prefix = currentText.substring(0, separatorIndex + 1);
            String roleText = isAdmin ? "  Rol: Administrador" : "  Rol: Colaborador";
            this.sessionUserLabel.setText(prefix + roleText);
        }
    }

    @Override
    public void displayFileLines(String fileName, List<String[]> linesData, String localUsername) {
        this.codeEditorPanel.renderLines(fileName, linesData, localUsername);
    }

    @Override
    public void highlightLockedLine(int lineNumber, String ownerUsername, int colorId, boolean ownedByLocalUser) {
        this.codeEditorPanel.markLineLocked(lineNumber, ownerUsername, colorId, ownedByLocalUser);
    }

    @Override
    public void clearLineLock(int lineNumber) {
        this.codeEditorPanel.clearLineHighlight(lineNumber);
    }

    @Override
    public void updateLineContent(int lineNumber, String content) {
        this.codeEditorPanel.updateSingleLine(lineNumber, content);
    }

    @Override
    public String getFullDisplayedCode() {
        return this.codeEditorPanel.getFullDisplayedCode();
    }

    @Override
    public void showStatusMessage(String message) {
        this.statusBarLabel.setText(message);
    }

    @Override
    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.ERROR_MESSAGE);
    }
}
