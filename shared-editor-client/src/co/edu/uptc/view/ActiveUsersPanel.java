package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ActiveUsersPanel extends JPanel {
    private static final String ADMIN_ROLE = "ADMIN";

    private JPanel usersContainer;

    public ActiveUsersPanel() {
        super(new BorderLayout(4, 6));
        initComponents();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(210, 400));
        setBorder(BorderFactory.createTitledBorder("Usuarios conectados"));
        this.usersContainer = new JPanel();
        this.usersContainer.setLayout(new BoxLayout(this.usersContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(this.usersContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshUsers(List<String[]> usersData) {
        this.usersContainer.removeAll();
        for (int i = 0; i < usersData.size(); i++) {
            String[] entry = usersData.get(i);
            int colorId = parseColorId(entry[1]);
            boolean isAdmin = ADMIN_ROLE.equals(entry[2]);
            this.usersContainer.add(createUserBadge(entry[0], colorId, isAdmin));
        }
        this.usersContainer.revalidate();
        this.usersContainer.repaint();
    }

    private JPanel createUserBadge(String username, int colorId, boolean isAdmin) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel colorDot = new JLabel(" ");
        colorDot.setOpaque(true);
        colorDot.setBackground(UserColorPalette.getAccentColor(colorId));
        colorDot.setPreferredSize(new Dimension(14, 14));
        colorDot.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        String displayName = isAdmin ? username + " (Admin)" : username;
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font(Font.SANS_SERIF, isAdmin ? Font.BOLD : Font.PLAIN, 12));
        badge.add(colorDot);
        badge.add(nameLabel);
        return badge;
    }

    private int parseColorId(String rawId) {
        int colorId = 0;
        try {
            colorId = Integer.parseInt(rawId.trim());
        } catch (NumberFormatException ex) {
            colorId = 0;
        }
        return colorId;
    }
}
