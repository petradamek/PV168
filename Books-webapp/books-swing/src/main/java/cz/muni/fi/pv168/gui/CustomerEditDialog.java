package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.books.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CustomerEditDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(CustomerEditDialog.class);

    private JTextField nameTextField;
    private JTextField emailTextField;
    private JTextField phoneTextField;
    private JTextArea addressArea;
    private Customer customer;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private boolean editing;

    CustomerEditDialog(JFrame frame) {
        super(frame);
        setPreferredSize(new Dimension(400, 300));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Sets the customer to edit. If the id is null, the dialog changes title.
     */
    void setCustomer(Customer customer) {
        if (customer.getId() == null) {
            setTitle(Main.texts.getString("customer.dialog.new.title"));
            editing = false;
        } else {
            setTitle(Main.texts.getString("customer.dialog.edit.title"));
            editing = true;
        }
        this.customer = customer;
        nameTextField.setText(customer.getFullname());
        emailTextField.setText(customer.getEmail());
        phoneTextField.setText(customer.getPhone());
        addressArea.setText(customer.getAddress());
    }

    private void onOK() {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String phone = phoneTextField.getText();
        String address = addressArea.getText();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    Main.texts.getString("customer.dialog.warning.text"),
                    Main.texts.getString("customer.dialog.warning.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean changed = (!name.equals(customer.getFullname()))
                || (!email.equals(customer.getEmail()))
                || (!phone.equals(customer.getPhone()))
                || (!address.equals(customer.getAddress()));
        if (changed) {
            customer.setFullname(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address);
            log.debug("changed data, saving ...");
            if (editing) {
                Main.mainForm.customersForm.customersTableModel.updateCustomer(customer);
            } else {
                Main.mainForm.customersForm.customersTableModel.addCustomer(customer);
            }
        } else {
            log.debug("No change, not saving...");
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
