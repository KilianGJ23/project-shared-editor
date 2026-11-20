package co.edu.uptc.view;

import co.edu.uptc.presenter.IClientPresenter;
import co.edu.uptc.presenter.IConnectionView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectionFrame extends JFrame implements IConnectionView {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "1372";
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton connectButton;
    private JLabel errorLabel;
    private IClientPresenter presenter;

    public ConnectionFrame() {
        super("Editor Colaborativo Java - Conexion");
        initComponents();
    }

    @Override
    public void setPresenter(IClientPresenter presenter) {
        this.presenter = presenter;
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        mainPanel.add(buildHeaderLabel(), BorderLayout.NORTH);
        mainPanel.add(buildFormPanel(), BorderLayout.CENTER);
        mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private JLabel buildHeaderLabel() {
        JLabel title = new JLabel("Conexion al Servidor Colaborativo");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        return title;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.hostField = new JTextField(DEFAULT_HOST, 18);
        this.portField = new JTextField(DEFAULT_PORT, 18);
        this.usernameField = new JTextField(18);
        addFormRow(form, gbc, 0, "Servidor (IP / Host):", this.hostField);
        addFormRow(form, gbc, 1, "Puerto TCP:", this.portField);
        addFormRow(form, gbc, 2, "Nombre de usuario:", this.usernameField);
        return form;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(6, 8));
        this.errorLabel = new JLabel(" ");
        this.errorLabel.setForeground(new Color(183, 28, 28));
        this.errorLabel.setPreferredSize(new Dimension(340, 20));
        this.connectButton = new JButton("Conectar");
        this.connectButton.addActionListener(e -> onConnectClicked());
        getRootPane().setDefaultButton(this.connectButton);
        bottom.add(this.errorLabel, BorderLayout.NORTH);
        bottom.add(this.connectButton, BorderLayout.SOUTH);
        return bottom;
    }

    private void onConnectClicked() {
        if (this.presenter != null) {
            String host = this.hostField.getText().trim();
            String username = this.usernameField.getText().trim();
            int port = parsePort(this.portField.getText().trim());
            if (host.isEmpty()) {
                showConnectionError("Debe ingresar la IP o host del servidor.");
            } else if (port <= 0) {
                showConnectionError("El puerto debe ser un numero valido.");
            } else {
                this.errorLabel.setText("Conectando...");
                this.presenter.connectToServer(host, port, username);
            }
        }
    }

    private int parsePort(String rawPort) {
        int port = -1;
        try {
            int parsed = Integer.parseInt(rawPort);
            if (parsed >= 1 && parsed <= 65535) {
                port = parsed;
            }
        } catch (NumberFormatException ex) {
            port = -1;
        }
        return port;
    }

    @Override
    public void showConnectionWindow() {
        setVisible(true);
        this.usernameField.requestFocusInWindow();
    }

    @Override
    public void closeConnectionWindow() {
        setVisible(false);
    }

    @Override
    public void showConnectionError(String message) {
        this.errorLabel.setText(message);
    }

    @Override
    public void setConnectEnabled(boolean enabled) {
        this.connectButton.setEnabled(enabled);
    }
}
