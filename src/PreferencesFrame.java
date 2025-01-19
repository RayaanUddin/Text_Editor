import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;

public class PreferencesFrame extends JFrame {
    private static PreferencesFrame instance; // Singleton instance
    private JComboBox<String> colorSelector, fontSelector, fontSizeSelector, fontColorSelector;
    private JCheckBox autoSaveCheckBox;
    private JPanel previewPanel;
    private JTextArea sampleText;
    private Properties properties;

    // Private constructor for Singleton Pattern
    private PreferencesFrame() {
        setTitle("Preferences");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Allow closing
        this.properties = TextEditorFrame.getProperties();

        // Create Preview Panel
        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(400, 100));
        updateBackgroundColor(properties.getProperty("bgColor", "White"));

        // Sample text to preview font changes
        sampleText = new JTextArea("Sample Text");
        sampleText.setEditable(false);
        sampleText.setPreferredSize(new Dimension(200, 50));
        updateFontSettings(properties.getProperty("fontFamily", "Arial"),
                Integer.parseInt(properties.getProperty("fontSize", "16")),
                properties.getProperty("fontColor", "Black"));

        // Dropdown for Background Color
        colorSelector = new JComboBox<>(new String[]{"White", "Blue", "Red", "Green", "Yellow", "Gray"});
        colorSelector.setSelectedItem(properties.getProperty("bgColor", "White"));
        colorSelector.addActionListener(e -> updateBackgroundColor((String) colorSelector.getSelectedItem()));

        // Dropdown for Font Family
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        fontSelector = new JComboBox<>(fonts);
        fontSelector.setSelectedItem(properties.getProperty("fontFamily", "Arial"));
        fontSelector.addActionListener(e -> updateFontSettings(
                (String) fontSelector.getSelectedItem(),
                Integer.parseInt((String) fontSizeSelector.getSelectedItem()),
                (String) fontColorSelector.getSelectedItem()));

        // Dropdown for Font Size
        String[] fontSizes = {"12", "14", "16", "18", "20", "22", "24", "28"};
        fontSizeSelector = new JComboBox<>(fontSizes);
        fontSizeSelector.setSelectedItem(properties.getProperty("fontSize", "16"));
        fontSizeSelector.addActionListener(e -> updateFontSettings(
                (String) fontSelector.getSelectedItem(),
                Integer.parseInt((String) fontSizeSelector.getSelectedItem()),
                (String) fontColorSelector.getSelectedItem()));

        // Dropdown for Font Color
        fontColorSelector = new JComboBox<>(new String[]{"Black", "Blue", "Red", "Green", "Gray"});
        fontColorSelector.setSelectedItem(properties.getProperty("fontColor", "Black"));
        fontColorSelector.addActionListener(e -> updateFontSettings(
                (String) fontSelector.getSelectedItem(),
                Integer.parseInt((String) fontSizeSelector.getSelectedItem()),
                (String) fontColorSelector.getSelectedItem()));

        // Auto-Save Checkbox
        autoSaveCheckBox = new JCheckBox("Enable Auto-Save", Boolean.parseBoolean(properties.getProperty("autoSave", "true")));

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        JButton okButton = new JButton("OK");

        // Apply button (Saves properties but keeps window open)
        applyButton.addActionListener(e -> savePreferences());

        // OK button (Saves properties and closes the window)
        okButton.addActionListener(e -> {
            savePreferences();
            dispose();
            instance = null;
        });

        buttonPanel.add(applyButton);
        buttonPanel.add(okButton);

        // Layout setup
        JPanel controlsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        controlsPanel.add(new JLabel("Background Color:"));
        controlsPanel.add(colorSelector);
        controlsPanel.add(new JLabel("Font Family:"));
        controlsPanel.add(fontSelector);
        controlsPanel.add(new JLabel("Font Size:"));
        controlsPanel.add(fontSizeSelector);
        controlsPanel.add(new JLabel("Font Color:"));
        controlsPanel.add(fontColorSelector);
        controlsPanel.add(autoSaveCheckBox);

        previewPanel.add(sampleText);
        add(previewPanel, BorderLayout.NORTH);
        add(controlsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add window listener to reset instance when closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instance = null; // Set instance to null when closing
            }
        });

        setVisible(true);
    }

    // Singleton getInstance() method
    public static PreferencesFrame getInstance() {
        if (instance == null) {
            instance = new PreferencesFrame();
        } else {
            instance.toFront(); // Bring existing window to front
            instance.requestFocus();
        }
        return instance;
    }

    // Updates the background color preview
    private void updateBackgroundColor(String colorName) {
        Color color = TextEditorFrame.getColorFromName(colorName);
        previewPanel.setBackground(color);
    }

    // Updates font settings
    private void updateFontSettings(String fontFamily, int fontSize, String fontColor) {
        sampleText.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
        sampleText.setForeground(TextEditorFrame.getColorFromName(fontColor));
    }

    // Saves properties to a file
    private void savePreferences() {
        properties.setProperty("bgColor", (String) colorSelector.getSelectedItem());
        properties.setProperty("fontFamily", (String) fontSelector.getSelectedItem());
        properties.setProperty("fontSize", (String) fontSizeSelector.getSelectedItem());
        properties.setProperty("fontColor", (String) fontColorSelector.getSelectedItem());
        properties.setProperty("autoSave", String.valueOf(autoSaveCheckBox.isSelected()));

        if (TextEditorFrame.setProperties(this.properties)) {
            JOptionPane.showMessageDialog(this, "Preferences Saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error saving properties!", "Error", JOptionPane.ERROR_MESSAGE);
        }


    }
}