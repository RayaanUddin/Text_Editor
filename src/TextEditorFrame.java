import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class TextEditorFrame extends JFrame {
    static Boolean isMacos = null;
    final LinkedHashMap<String, Map<String, AbstractAction>> actionsCategory = new LinkedHashMap<>();
    File file;
    boolean needsUpdate = false;
    private final JTextArea area = new JTextArea();
    Thread autosaving;
    public TextEditorFrame(File file) {
        if (isMacos == null) {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                isMacos = true;
            } else {
                isMacos = false;
            }
        }
        if (isMacos) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        int CTRL = isMacos ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 700);
        setTitle("Untitled");
        System.setProperty("name", "Text Editor");
        setLocationRelativeTo(null);

        // Set Mnemonics (Alt+Key to open menus)
        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Define actions using a Map
        Map<String, AbstractAction> fileActions = new HashMap<>();
        Map<String, AbstractAction> editActions = new HashMap<>();
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
        fileActions.get("New").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, CTRL));  // ⌘ + N
        fileActions.get("Open").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, CTRL)); // ⌘ + O
        fileActions.get("Save").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL));  // ⌘ + S
        fileActions.get("SaveAs").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL | KeyEvent.SHIFT_DOWN_MASK));  // ⌘ + SHIFT + S        editActions.get("Cut").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, CTRL));   // ⌘ + X
        editActions.get("Copy").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL));  // ⌘ + C
        editActions.get("Paste").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, CTRL)); // ⌘ + V

        // Create and add menu items dynamically
        for (var category : actionsCategory.entrySet()) {
            JMenu menu =  new JMenu(category.getKey());
            for (var action : category.getValue().entrySet()) {
                menu.add(action.getValue());
            }
            menuBar.add(menu);
        }

        // Set the menu bar for the frame
        setJMenuBar(menuBar);

        // Update text editor
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.setMargin(new Insets(10, 5, 10, 5));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

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

        autosaving = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (needsUpdate) {
                    save();
                }
            }
        });

        if (file != null) {
            open(file);
            autosaving.start();
        } else {
            fileActions.get("Save").setEnabled(false);
        }

        // Make the frame visible
        setVisible(true);
        toFront();
    }

    public void close() {
        if (file == null && !getText().trim().isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Do you want to save before exiting?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                save();
                dispose();
            } else if (option == JOptionPane.NO_OPTION) {
                dispose(); // Close window without saving
            }
        } else {
            if (autosaving.isAlive()) {
                save();
            }
            dispose();
        }
    }

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

    public void save() {
        try {
            File file = new File(this.file.getAbsolutePath());
            Writer writer = new FileWriter(file);
            writer.write(getText());
            writer.close();
            setTitle(file.getName());
            if (!autosaving.isAlive()) {
                autosaving.start();
            }
        } catch (Exception e) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                save(file);
            }
        }
    }

    public void save(File file) {
        this.file = file;
        save();
    }

    public void setText(String text) {
        area.setText(text);
    }

    public String getText() {
        return area.getText();
    }

    // For testing purpose
    public static void main(String[] args) {
        JFrame frame = new TextEditorFrame(null);
    }
}
