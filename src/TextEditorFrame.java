import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextEditorFrame extends JFrame {
    static Boolean isMacos = null;
    public TextEditorFrame() {
        if (isMacos == null) {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                isMacos = true;
            } else {
                isMacos = false;
            }
        }
        if (isMacos) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MyApp");
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);

        // Set Mnemonics (Alt+Key to open menus)
        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Define actions using a Map
        Map<String, AbstractAction> fileActions = new HashMap<>();
        Map<String, AbstractAction> editActions = new HashMap<>();
        LinkedHashMap<String, Map<String, AbstractAction>> actionsCategory = new LinkedHashMap<>();
        actionsCategory.put("File", fileActions);
        actionsCategory.put("Edit", editActions);

        // Define menu actions with icons and keyboard shortcuts
        fileActions.put("New", new AbstractAction("New") {
            public void actionPerformed(ActionEvent e) {
                new TextEditorFrame();
            }
        });

        fileActions.put("Open", new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) {
                System.out.println("File Opened");
            }
        });

        fileActions.put("Exit", new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
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
        fileActions.get("New").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_DOWN_MASK));  // ⌘ + N
        fileActions.get("Open").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_DOWN_MASK)); // ⌘ + O
        fileActions.get("Exit").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_DOWN_MASK));  // ⌘ + Q
        editActions.get("Cut").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK));   // ⌘ + X
        editActions.get("Copy").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK));  // ⌘ + C
        editActions.get("Paste").putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK)); // ⌘ + V

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

        // Make the frame visible
        setVisible(true);
    }



    // For testing purpose
    public static void main(String[] args) {
        JFrame frame = new TextEditorFrame();
    }
}
