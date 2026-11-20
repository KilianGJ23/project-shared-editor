package co.edu.uptc.view;

import co.edu.uptc.presenter.IClientPresenter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CodeEditorPanel extends JPanel implements LineInteractionHandler {
    private JLabel activeFileLabel;
    private JPanel linesContainer;
    private JButton insertLineButton;
    private JButton deleteLineButton;
    private JButton unlockLineButton;
    private List<CodeLineRow> lineRows;
    private IClientPresenter presenter;

    public CodeEditorPanel() {
        super(new BorderLayout(4, 6));
        this.lineRows = new ArrayList<>();
        initComponents();
    }

    public void setPresenter(IClientPresenter presenter) {
        this.presenter = presenter;
    }

    private void initComponents() {
        setBorder(BorderFactory.createTitledBorder("Area de edicion colaborativa"));
        add(buildToolbar(), BorderLayout.NORTH);
        this.linesContainer = new JPanel();
        this.linesContainer.setLayout(new BoxLayout(this.linesContainer, BoxLayout.Y_AXIS));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(this.linesContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        this.activeFileLabel = new JLabel("Sin archivo seleccionado");
        this.activeFileLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        this.insertLineButton = new JButton("+ Insertar linea");
        this.deleteLineButton = new JButton("- Eliminar linea");
        this.unlockLineButton = new JButton("Liberar linea");
        this.insertLineButton.addActionListener(e -> onInsertRequested());
        this.deleteLineButton.addActionListener(e -> onDeleteRequested());
        this.unlockLineButton.addActionListener(e -> onUnlockRequested());
        actions.add(this.insertLineButton);
        actions.add(this.deleteLineButton);
        actions.add(this.unlockLineButton);
        topBar.add(this.activeFileLabel, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);
        return topBar;
    }

    public void renderLines(String fileName, List<String[]> linesData, String localUsername) {
        this.activeFileLabel.setText("Archivo actual: " + fileName);
        this.linesContainer.removeAll();
        this.lineRows.clear();
        for (int i = 0; i < linesData.size(); i++) {
            String[] entry = linesData.get(i);
            int lineNum = parseInteger(entry[0], i + 1);
            String owner = entry[1];
            int colorId = parseInteger(entry[2], -1);
            CodeLineRow row = new CodeLineRow(lineNum, entry[3], this);
            if (!owner.isEmpty() && colorId >= 0) {
                row.applyLock(owner, colorId, owner.equals(localUsername));
            }
            this.lineRows.add(row);
            this.linesContainer.add(row);
        }
        this.linesContainer.revalidate();
        this.linesContainer.repaint();
    }

    public void markLineLocked(int lineNumber, String username, int colorId, boolean ownedByLocal) {
        CodeLineRow row = findRow(lineNumber);
        if (row != null) {
            row.applyLock(username, colorId, ownedByLocal);
        }
    }

    public void clearLineHighlight(int lineNumber) {
        CodeLineRow row = findRow(lineNumber);
        if (row != null) {
            row.clearLock();
        }
    }

    public void updateSingleLine(int lineNumber, String content) {
        CodeLineRow row = findRow(lineNumber);
        if (row != null) {
            row.setCodeText(content);
        }
    }

    public String getFullDisplayedCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.lineRows.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(this.lineRows.get(i).getCodeText());
        }
        return builder.toString();
    }

    private CodeLineRow findRow(int lineNumber) {
        CodeLineRow found = null;
        int index = lineNumber - 1;
        if (index >= 0 && index < this.lineRows.size()) {
            found = this.lineRows.get(index);
        }
        return found;
    }

    private int parseInteger(String value, int fallback) {
        int result = fallback;
        try {
            result = Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            result = fallback;
        }
        return result;
    }

    @Override
    public void onLineClicked(int lineNumber) {
        if (this.presenter != null) {
            this.presenter.requestLockLine(lineNumber);
        }
    }

    @Override
    public void onLineEdited(String newContent) {
        if (this.presenter != null) {
            this.presenter.requestEditLine(newContent);
        }
    }

    @Override
    public void onInsertRequested() {
        if (this.presenter != null) {
            this.presenter.requestInsertLine("");
        }
    }

    private void onDeleteRequested() {
        if (this.presenter != null) {
            this.presenter.requestDeleteLine();
        }
    }

    @Override
    public void onUnlockRequested() {
        if (this.presenter != null) {
            this.presenter.requestUnlockLine();
        }
    }
}
