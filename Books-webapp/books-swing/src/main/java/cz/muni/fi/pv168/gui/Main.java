package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.books.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);
    static ResourceBundle texts = ResourceBundle.getBundle("MyTexts");
    static CustomerManager customerManager;
    static BookManager bookManager;
    static LeaseManager leaseManager;
    static MainForm mainForm;
    private static JFrame frame;
    private static CustomerEditDialog customerEditDialog;

    public static void main(String[] args) throws IOException {
        //create managers for manipulating data in database
        DataSource dataSource = cz.muni.fi.pv168.books.Main.getDataSource();
        customerManager = new CustomerManagerImpl(dataSource);
        bookManager = new BookManagerImpl(dataSource);
        leaseManager = new LeaseManagerImpl(dataSource);

        //create GUI
        EventQueue.invokeLater(() -> {
            frame = new JFrame(texts.getString("window.title"));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainForm = new MainForm();
            frame.setJMenuBar(createMenu());
            frame.setContentPane(mainForm.topPanel);
            frame.setPreferredSize(new Dimension(800, 600));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            customerEditDialog = new CustomerEditDialog(frame);
            customerEditDialog.pack();
        });
    }

    static void showCustomerEditDialog(Customer customer) {
        //set customer to edit
        customerEditDialog.setCustomer(customer);
        //position dialog to the center of the main window
        customerEditDialog.setLocationRelativeTo(frame);
        //make dialog visible
        customerEditDialog.setVisible(true);
    }

    /**
     * Creates the menu to be displayed on top of the window.
     *
     * @return menu
     */
    private static JMenuBar createMenu() {
        //main menu level
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu(texts.getString("menu.file.title"));
        JMenu helpMenu = new JMenu(texts.getString("menu.help.title"));
        menubar.add(fileMenu);
        menubar.add(createLookAndFeelMenu());
        menubar.add(Box.createHorizontalGlue());
        menubar.add(helpMenu);
        //menu File
        JMenuItem exitMenuItem = new JMenuItem(texts.getString("menu.exit"));
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(e -> System.exit(1));
        //menu Help
        JMenuItem aboutMenuItem = new JMenuItem(texts.getString("menu.about"));
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, texts.getString("menu.about.text"), texts.getString("menu.about"), JOptionPane.INFORMATION_MESSAGE));
        //allows to select the item using Alt-A while in the menu
        aboutMenuItem.setMnemonic('A');
        //adds shortcut CTRL-A to the whole application
        aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        //menu item for displaying system info
        JMenuItem systemMenuItem = new JMenuItem(texts.getString("menu.system"));
        helpMenu.add(systemMenuItem);
        systemMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Locale: " + Locale.getDefault() + "\n"
                        + "JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version") + "\n"
                        + "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version") + "\n"
                        + "CPUs: " + Runtime.getRuntime().availableProcessors(),
                texts.getString("menu.system"),
                JOptionPane.INFORMATION_MESSAGE));
        systemMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        return menubar;
    }

    /**
     * Creates menu for switching look-and-feels from available choices.
     */
    private static JMenu createLookAndFeelMenu() {
        JMenu laf = new JMenu(texts.getString("menu.laf.title"));
        for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            JMenuItem item = new JMenuItem(info.getName());
            laf.add(item);
            item.addActionListener(ev -> {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(mainForm.topPanel);
                } catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
                    log.error("cannot set Look-and-Feel " + info.getName(), ex);
                }
            });
        }
        return laf;
    }
}
