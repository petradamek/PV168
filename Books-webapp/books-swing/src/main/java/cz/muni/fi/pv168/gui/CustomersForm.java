package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.books.Customer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class CustomersForm {

    JPanel customersTopPanel;
    JTable customersTable;
    CustomersTableModel customersTableModel;
    private JButton createButton;
    private JButton deleteButton;
    private JButton editButton;
    private JButton randomButton;

    CustomersForm() {
        createButton.addActionListener(e -> Main.showCustomerEditDialog(new Customer()));
        editButton.addActionListener(e -> Main.showCustomerEditDialog(customersTableModel.getCustomerAtRow(getSelectedTableRow())));
        deleteButton.addActionListener(e -> customersTableModel.deleteCustomerAtRow(getSelectedTableRow()));
        randomButton.addActionListener(e -> customersTableModel.addRandomCustomer());
    }

    /**
     * Gets index of the selected row of the table.
     */
    private int getSelectedTableRow() {
        return customersTable.convertRowIndexToModel(customersTable.getSelectedRow());
    }

    private void createUIComponents() {
        //create data model
        customersTableModel = new CustomersTableModel(Main.customerManager);
        //create table with the data model
        customersTable = new JTable(customersTableModel);
        //allow to select at most a single line
        customersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //enable and disable buttons based on whether a line is selected
        customersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = customersTable.getSelectedRowCount() > 0;
            deleteButton.setEnabled(selected);
            editButton.setEnabled(selected);
        });
        customersTable.getColumnModel().getColumn(0).setPreferredWidth(3);
    }
}
