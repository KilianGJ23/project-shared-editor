package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CodeLineRow extends JPanel {
    private static final int ROW_HEIGHT = 28;
    private static final Color GUTTER_BG = new Color(238, 238, 238);
    private static final Color BORDER_GRAY = new Color(210, 210, 210);

    private int lineNumber;
    private JLabel numberLabel;
    private JTextField codeField;
    private JLabel ownerBadge;
    private boolean ownedByLocalUser;
    private boolean syncingText;

    public CodeLineRow(int lineNumber, String content, LineInteractionHandler handler) {
        super(new BorderLayout(4, 0));
        this.lineNumber = lineNumber;
        this.ownedByLocalUser = false;
        this.syncingText = false;
        initRow(content, handler);
    }

    private void initRow(String content, LineInteractionHandler handler) {
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        setPreferredSize(new Dimension(560, ROW_HEIGHT));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_GRAY));
        this.numberLabel = buildNumberLabel();
        this.codeField = buildCodeField(content, handler);
        this.ownerBadge = buildOwnerBadge();
        add(this.numberLabel, BorderLayout.WEST);
        add(this.codeField, BorderLayout.CENTER);
        add(this.ownerBadge, BorderLayout.EAST);
    }

    private JLabel buildNumberLabel() {
        JLabel label = new JLabel(String.valueOf(this.lineNumber), SwingConstants.RIGHT);
        label.setOpaque(true);
        label.setBackground(GUTTER_BG);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(42, ROW_HEIGHT));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_GRAY),
                BorderFactory.createEmptyBorder(0, 4, 0, 8)));
        return label;
    }

    private JTextField buildCodeField(String content, LineInteractionHandler handler) {
        JTextField field = new JTextField(content);
        field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        field.setEditable(false);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        attachMouseListener(field, handler);
        attachDocumentListener(field, handler);
        attachKeyListener(field, handler);
        return field;
    }

    private void attachMouseListener(JTextField field, LineInteractionHandler handler) {
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!ownedByLocalUser) {
                    handler.onLineClicked(lineNumber);
                }
            }
        });
    }

    private void attachDocumentListener(JTextField field, LineInteractionHandler handler) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifyTextChange(handler);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifyTextChange(handler);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyTextChange(handler);
            }
        });
    }

    private void attachKeyListener(JTextField field, LineInteractionHandler handler) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (ownedByLocalUser && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handler.onInsertRequested();
                } else if (ownedByLocalUser && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    handler.onUnlockRequested();
                }
            }
        });
    }

    private void notifyTextChange(LineInteractionHandler handler) {
        if (this.ownedByLocalUser && !this.syncingText) {
            handler.onLineEdited(this.codeField.getText());
        }
    }

    private JLabel buildOwnerBadge() {
        JLabel badge = new JLabel("");
        badge.setOpaque(true);
        badge.setVisible(false);
        badge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return badge;
    }

    public void applyLock(String username, int colorId, boolean ownedByLocal) {
        this.ownedByLocalUser = ownedByLocal;
        Color accent = UserColorPalette.getAccentColor(colorId);
        Color tint = UserColorPalette.getSoftTintColor(colorId);
        this.codeField.setEditable(ownedByLocal);
        this.codeField.setBackground(tint);
        this.ownerBadge.setText(username);
        this.ownerBadge.setBackground(accent);
        this.ownerBadge.setVisible(true);
        setBorder(BorderFactory.createMatteBorder(1, 3, 1, 1, accent));
        if (ownedByLocal) {
            this.codeField.requestFocusInWindow();
        }
        revalidate();
        repaint();
    }

    public void clearLock() {
        this.ownedByLocalUser = false;
        this.codeField.setEditable(false);
        this.codeField.setBackground(Color.WHITE);
        this.ownerBadge.setText("");
        this.ownerBadge.setVisible(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_GRAY));
        revalidate();
        repaint();
    }

    public void setCodeText(String content) {
        this.syncingText = true;
        this.codeField.setText(content != null ? content : "");
        this.syncingText = false;
    }

    public String getCodeText() {
        return this.codeField.getText();
    }

    public int getLineNumber() {
        return this.lineNumber;
    }
}
