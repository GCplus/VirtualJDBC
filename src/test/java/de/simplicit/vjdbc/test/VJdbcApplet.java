// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.test;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

public class VJdbcApplet extends Applet {
    private static final long serialVersionUID = 3257850974046533684L;
    
    private JButton addButton;
    private JButton changeButton;
    private JButton deleteButton;
    private JTable tableOfAddresses;
    private AddressTableModel modelOfAddresses = new AddressTableModel();

    public void init() {
        try {
            Class.forName("de.simplicit.vjdbc.VirtualDriver").newInstance();

            setLayout(new GridBagLayout());
            setBackground(Color.GRAY);

            addButton = new JButton("Add");
            add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.3, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
            changeButton = new JButton("Change");
            add(changeButton, new GridBagConstraints(1, 0, 1, 1, 0.3, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
            deleteButton = new JButton("Delete");
            add(deleteButton, new GridBagConstraints(2, 0, 1, 1, 0.3, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addAddress();
                }
            });
            
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteAddresses();
                }
            });
            
            tableOfAddresses = new JTable(modelOfAddresses);
            add(tableOfAddresses, new GridBagConstraints(0, 1, 3, 10, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));

            refreshAdresses();
        } catch(InstantiationException | ClassNotFoundException | IllegalAccessException | HeadlessException e) {
            e.printStackTrace();
        }
    }

    private void addAddress() {
        AddressDialog adrDialog = new AddressDialog();

        adrDialog.setSize(400, 200);
        adrDialog.setModal(true);
        adrDialog.show();

        if(!adrDialog.isCancelled()) {
            Connection conn = null;
            try {
                conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("insert into Address (Name, Street, City) values (?, ?, ?)");
                stmt.setString(1, adrDialog.getName());
                stmt.setString(2, adrDialog.getStreet());
                stmt.setString(3, adrDialog.getCity());
                stmt.executeUpdate();
                stmt.close();
            } catch(SQLException e) {
                e.printStackTrace();
            } finally {
                if(conn != null) {
                    try {
                        conn.close();
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                }

                refreshAdresses();
            }
        }
    }
    
    private void deleteAddresses() {
        if(tableOfAddresses.getSelectedRowCount() > 0) {
            int[] selrows = tableOfAddresses.getSelectedRows();
            Connection conn = null;
            try {
                conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("delete from address where id = ?");
                for(int i = 0; i < selrows.length; i++) {
                    Object[] row = modelOfAddresses.getSelectedItem(selrows[i]);
                    Integer id = (Integer)row[0];
                    stmt.setInt(1, id.intValue());
                    stmt.executeUpdate();
                }
            } catch(SQLException e) {
                e.printStackTrace();
            } finally {
                if(conn != null) {
                    try {
                        conn.close();
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                }
                
                refreshAdresses();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        URL codebase = getCodeBase();
        String vjdbcurl = "jdbc:vjdbc:servlet:" + codebase.toString() + "vjdbc";
        return DriverManager.getConnection(vjdbcurl);
    }

    private void refreshAdresses() {
        Connection conn = null;
        try {
            modelOfAddresses.clear();
            conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from address");
            while(rs.next()) {
                modelOfAddresses.addAddress(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
            stmt.close();
            modelOfAddresses.fireTableDataChanged();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class AddressDialog extends JDialog {
        private static final long serialVersionUID = 3258416127268042038L;
        
        private JTextField name;
        private JTextField street;
        private JTextField city;
        private JButton ok;
        private JButton cancel;

        private boolean cancelled;

        AddressDialog() {
            Container cont = getContentPane();
            cont.setLayout(new GridBagLayout());

            this.name = new JTextField();
            this.street = new JTextField();
            this.city = new JTextField();

            this.ok = new JButton("OK");
            this.cancel = new JButton("Cancel");

            Insets insets = new Insets(2, 5, 2, 5);

            cont.add(new JLabel("Name:"),
                     new GridBagConstraints(0, 0, 1, 1, 0.2, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
            cont.add(name,
                     new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
            cont.add(new JLabel("Street:"),
                     new GridBagConstraints(0, 1, 1, 1, 0.2, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
            cont.add(street,
                     new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
            cont.add(new JLabel("City:"),
                     new GridBagConstraints(0, 2, 1, 1, 0.2, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
            cont.add(city,
                     new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(ok);
            buttonPanel.add(cancel);

            cont.add(buttonPanel,
                     new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));

            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelled = false;
                    AddressDialog.this.dispose();
                }
            });

            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelled = true;
                    AddressDialog.this.dispose();
                }
            });
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public String getName() {
            return name.getText();
        }

        public String getStreet() {
            return street.getText();
        }

        public String getCity() {
            return city.getText();
        }
    }
    
    private static class AddressTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 3689627012692391472L;
        
        private static final int COLUMN_ID = 0;
        private static final int COLUMN_NAME = 1;
        private static final int COLUMN_STREET = 2;
        private static final int COLUMN_CITY = 3;
        private static final int COLUMN_COUNT = 4;
        private ArrayList addresses;
        
        public AddressTableModel() {
            this.addresses = new ArrayList();
        }
        
        public void clear() {
            addresses.clear();
        }
        
        public Object[] getSelectedItem(int index) {
            return (Object[]) addresses.get(index);
        }
        
        public void addAddress(int id, String name, String street, String city) {
            Object[] address = new Object[COLUMN_COUNT];
            address[COLUMN_ID] = new Integer(id);
            address[COLUMN_NAME] = name;
            address[COLUMN_STREET] = street;
            address[COLUMN_CITY] = city;
            addresses.add(address);
        }

        public String getColumnName(int column) {
            switch(column) {
                case COLUMN_ID:
                    return "Id";
                case COLUMN_NAME:
                    return "Name";
                case COLUMN_STREET:
                    return "Street";
                case COLUMN_CITY:
                    return "City";
            }
            return "";
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public int getRowCount() {
            return addresses.size();
        }

        public Object getValueAt(int row, int column) {
            Object[] address = (Object[]) addresses.get(row);
            return address[column];
        }
    }
}
