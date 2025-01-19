import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * This is a frame (child), which is the main program. It's a Text Editor.
 * Includes auto-saving, personalisation of theme, and more.
 * Allows to open and save files.
 * Preferences can be saved also locally.
 */
public class TextEditorFrame extends JFrame {
    static Boolean isMacos = null;
    final LinkedHashMap<String, Map<String, AbstractAction>> actionsCategory = new LinkedHashMap<>();
    File file;
    boolean needsUpdate = false;
    private final JTextArea area = new JTextArea();
    Thread autoSavingThread;
    boolean isAutoSave = false;
    private static Properties properties;
    private static final String PREF_FILE = "preferences.properties";
    private static final ArrayList<TextEditorFrame> frames = new ArrayList<>();

    public TextEditorFrame(File file) {
        if (isMacos == null) {
            isMacos = System.getProperty("os.name").toLowerCase().contains("mac");
        }
        if (isMacos) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 700);
        System.setProperty("name", "Text Editor");
        setLocationRelativeTo(null);

        // Define actions using a Map
        LinkedHashMap<String, AbstractAction> fileActions = new LinkedHashMap<>();
        LinkedHashMap<String, AbstractAction> editActions = new LinkedHashMap<>();
        actionsCategory.put("File", fileActions);
        actionsCategory.put("Edit", editActions);

        // Define menu actions with icons and keyboard shortcuts
        fileActions.put("New", new AbstractAction("New") {
            public void actionPerformed(ActionEvent e) {
                new TextEditorFrame(null);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        fileActions.put("Open", new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Choose destination.");
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = new File(jfc.getSelectedFile().getAbsolutePath());
                    open(file);
                }
            }
        });

        fileActions.put("Save", new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        fileActions.put("SaveAs", new AbstractAction("Save As") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    save(file);
                }
            }
        });

        fileActions.put("Preferences", new AbstractAction("Preferences") {
            public void actionPerformed(ActionEvent e) {
                PreferencesFrame.getInstance();
            }
        });

        editActions.put("Cut", new AbstractAction("Cut") {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Text Cut");
            }
        });

        editActions.put("Copy", new AbstractAction("Copy") {
            public void actionPerformed(ActionEvent e) {

            }
        });

        editActions.put("Paste", new AbstractAction("Paste") {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Text Pasted");
            }
        });

        // Add keyboard shortcuts (Accelerators)
        int CTRL = isMacos ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK; // If macOS use CMD
        fileActions.get("New").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, CTRL));  // ⌘ + N
        fileActions.get("Open").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, CTRL)); // ⌘ + O
        fileActions.get("Save").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL));  // ⌘ + S
        fileActions.get("SaveAs").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL | KeyEvent.SHIFT_DOWN_MASK));  // ⌘ + SHIFT + S        editActions.get("Cut").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, CTRL));   // ⌘ + X
        editActions.get("Copy").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL));  // ⌘ + C
        editActions.get("Paste").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, CTRL)); // ⌘ + V

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        for (var category : actionsCategory.entrySet()) {
            JMenu menu =  new JMenu(category.getKey());
            for (var action : category.getValue().entrySet()) {
                menu.add(action.getValue());
            }
            menuBar.add(menu);
        }
        setJMenuBar(menuBar);

        // Init text editor
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.setMargin(new Insets(10, 5, 10, 5));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Properties Update on open
        updateProperties(getProperties());


        // Add DocumentListener to detect changes
        area.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                needsUpdate = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                needsUpdate = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                needsUpdate = true;
            }
        });

        area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                needsUpdate = true;
            }
        });

        autoSavingThread = new Thread(() -> {
            while (true) {
                int sleep = 5000;
                if (!isAutoSave) {
                    sleep = 60000;
                }
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (needsUpdate) {
                    System.out.println("Save");
                    save();
                }
            }
        });

        if (file != null) {
            open(file);
            autoSavingThread.start();
        } else {
            fileActions.get("Save").setEnabled(false); // Disable save (Cannot save since no file opened)
            setTitle("Untitled"); // Default non-existing file name.
        }

        // Make the frame visible and bring to front.
        setVisible(true);
        toFront();
        frames.add(this);
    }

    /**
     * Closes the frame.
     * We must make sure it is saved and if not give the user a warning.
     * It makes sure that the frame is not empty (i.e. area is empty). Only gives warning when not empty.
     */
    public void close() {
        if ((file == null || (needsUpdate && !isAutoSave)) && !getText().trim().isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Do you want to save before exiting?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                save();
                dispose();
                frames.remove(this);
            } else if (option == JOptionPane.NO_OPTION) {
                dispose();
                frames.remove(this);
            }
        } else {
            if (isAutoSave && file != null) {
                save();
            }
            dispose();
            frames.remove(this);
        }
    }

    /**
     * Opens a file in a new frame, unless current frame is empty (i.e. no text in area).
     * @param file The file to open.
     */
    public void open(File file) {
        if (getText().trim().isEmpty()) {
            try {
                StringBuilder text = new StringBuilder();
                FileReader read = new FileReader(file);
                Scanner scan = new Scanner(read);
                while (scan.hasNextLine()) {
                    String line = scan.nextLine() + "\n";
                    text.append(line);
                }
                setText(text.toString());
                this.file = file;
                setTitle(file.getName());
                actionsCategory.get("File").get("Save").setEnabled(true);
            } catch (FileNotFoundException ignored) {
            }
        } else {
            toBack();
            new TextEditorFrame(file);
        }
    }

    /**
     * Save the file.
     * To save normally, the text editor must already have an existing path (file must exist and open),
     * otherwise we use save(File file) which defines new file for program (theoretically save as).
     */
    public void save() {
        try {
            File file = new File(this.file.getAbsolutePath());
            Writer writer = new FileWriter(file);
            writer.write(getText());
            writer.close();
            setTitle(file.getName());
            needsUpdate = false;
            if (!autoSavingThread.isAlive()) {
                autoSavingThread.start();
            }
        } catch (Exception e) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                save(file);
            }
        }
    }

    /**
     * Save as...
     * @param file the file which we will save to. File should be given to method. It uses its path.
     */
    public void save(File file) {
        this.file = file;
        save();
    }

    /**
     * Update the properties of the frame dynamically, resulting in a newer UI look and functions.
     * @param prop The variable which holds the properties to update to.
     */
    private void updateProperties(Properties prop) {
        area.setFont(new Font(prop.getProperty("fontFamily"), Font.PLAIN, Integer.parseInt(prop.getProperty("fontSize") )));
        area.setForeground(getColorFromName(prop.getProperty("fontColor")));
        area.setBackground(getColorFromName(prop.getProperty("bgColor")));
        setBackground(getColorFromName(prop.getProperty("bgColor")));
        if (prop.getProperty("autoSave").equals("true")) {
            isAutoSave = true;
        } else {
            isAutoSave = false;
        }
    }

    // Set text for area
    public void setText(String text) {
        area.setText(text);
    }

    // Get text for area
    public String getText() {
        return area.getText();
    }

    // Convert color to name
    public static Color getColorFromName(String colorName) {
        return switch (colorName) {
            case "Blue" -> Color.BLUE;
            case "Red" -> Color.RED;
            case "Green" -> Color.GREEN;
            case "Yellow" -> Color.YELLOW;
            case "Gray" -> Color.GRAY;
            case "White" -> Color.WHITE;
            default -> Color.BLACK;
        };
    }

    // Get properties
    public static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                FileInputStream in = new FileInputStream(PREF_FILE);
                properties.load(in);
            } catch (IOException e) {
                properties.setProperty("bgColor", "White");
                properties.setProperty("fontFamily", "Arial");
                properties.setProperty("fontSize", "16");
                properties.setProperty("fontColor", "Black");
                properties.setProperty("autoSave", "false");
            }
        }
        return properties;
    }

    // Set properties and save to file
    public static boolean setProperties(Properties prop) {
        properties = prop;
        try {
            FileOutputStream out = new FileOutputStream(PREF_FILE);
            properties.store(out, "User Preferences");
            for (TextEditorFrame frame : frames) {
                frame.updateProperties(prop);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // For testing purpose
    public static void main(String[] args) {
        new TextEditorFrame(null);
    }
}
