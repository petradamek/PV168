package cz.muni.fi.pv168.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainForm {

    JPanel topPanel;
    JPanel customersPanel;
    JPanel booksPanel;
    JPanel leasesPanel;

    CustomersForm customersForm;

    private void createUIComponents() {
        customersForm = new CustomersForm();
        customersPanel = customersForm.customersTopPanel;
    }

}
