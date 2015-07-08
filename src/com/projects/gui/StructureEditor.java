package com.projects.gui;

import com.projects.gui.panel.DevicePanel;
import com.projects.gui.table.*;
import com.projects.input.listeners.*;
import com.projects.helper.DeviceType;
import com.projects.helper.StructureType;
import com.projects.management.SystemController;
import com.projects.models.*;
import com.projects.systems.StructureManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 6/9/2015.
 */
public class StructureEditor implements SubscribedView
{
    private JLabel nameLabel;
    private JLabel numberOfUnitsLabel;
    private JLabel infoLabel;
    private JTextField nameField;
    private JFormattedTextField numberOfUnitsField;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel propertiesPanel;
    private DeviceTabbedPane deviceTabbedPane;
    private JDialog creationDialog;
    private PropertiesTable devicePropertiesTable;
    private PropertiesTable buildingPropertiesTable;
    private JTable deviceUsageTable;
    private UsageTable usageTable;
    private SystemController controller;

    private JPanel deviceButtonPanel;
    private JPanel inputUnitInfoPanel, inputCompositeUnitInfoPanel;
    private JPanel notificationPanel;
    private JScrollPane devicePropertiesScrollPane;
    private JScrollPane deviceUsageScrollPane;
    private JPanel deviceUsagePanel;
    private JPanel creationControlButtons;

    private StructureType structureType;

    private JButton newUsageButton;
    private JButton removeUsageButton;

    public StructureEditor(JFrame frame, SystemController systemController)
    {
        controller = systemController;

        nameField = new JTextField(14);
        numberOfUnitsField = new ImprovedFormattedTextField(NumberFormat.getIntegerInstance(), 1);
        numberOfUnitsField.setColumns(4);
        numberOfUnitsLabel = new JLabel("Number of Units: ");
        infoLabel = new JLabel("");
        nameLabel = new JLabel("Structure's Name: ");
        JPanel creationPanel = new JPanel(new GridLayout(1, 2));
        leftPanel = new JPanel(new BorderLayout(10,10));
        rightPanel = new JPanel(new BorderLayout(10,10));
        propertiesPanel = new JPanel(new GridLayout(2, 1));

        createComponents();

        creationPanel.add(leftPanel);
        creationPanel.add(rightPanel);
        creationDialog = new JDialog(frame, "Structure Editor", true);
        creationDialog.setContentPane(creationPanel);
        creationDialog.setPreferredSize(new Dimension(800, 600));
        creationDialog.setResizable(false);
        creationDialog.setVisible(false);
        structureType = StructureType.NO_STRUCTURE;

        creationDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                close();
            }
        });
    }

    public void display(Structure structure)
    {
        nameField.setText(structure.getName());
        numberOfUnitsField.setValue(structure.getNumberOfUnits());
        structureType = structure.getType();

        switch (structureType)
        {
            case SINGLE_UNIT:
            {
                setupSingleUnitEditorComponents(structure);
            } break;
            case COMPOSITE_UNIT:
            {
                setupCompositeUnitEditorComponents(structure);
            } break;
            case POWER_PLANT:
            {
                setupPowerPlantEditorComponents(structure);
            } break;
        }

        creationDialog.pack();
        creationDialog.setVisible(true);
    }

    private void completeEditing()
    {
        controller.editStructuresName(nameField.getText());
        controller.editStructuresNumberOfUnits(((Number) numberOfUnitsField.getValue()).intValue());
        nameField.setText("");
        numberOfUnitsField.setValue(1);
        controller.editingComplete();
    }

    private void close()
    {
        switch (structureType)
        {
            case SINGLE_UNIT:
            {
                removeSingleUnitEditorComponents();
            } break;
            case COMPOSITE_UNIT:
            {
                removeCompositeUnitEditorComponents();
            } break;
            case POWER_PLANT:
            {
                removePowerPlantEditorComponents();
            } break;
        }

        structureType = StructureType.NO_STRUCTURE;
        newUsageButton.setEnabled(false);
        removeUsageButton.setEnabled(false);

        buildingPropertiesTable.clearTable();
        devicePropertiesTable.clearTable();
        usageTable.clearTable();
        creationDialog.setVisible(false);
    }

    private void createComponents()
    {
        notificationPanel = new JPanel(new FlowLayout());
        notificationPanel.add(infoLabel);

        TableModelListener devicePropertiesTableListener = new DevicePropertiesTableListener(controller);
        devicePropertiesTable = new PropertiesTable();
        devicePropertiesTable.addTableModelListener(devicePropertiesTableListener);
        JTable propertyTable = new JTable(devicePropertiesTable) {
            private static final long serialVersionUID = 1L;
            private Class editingClass;

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                editingClass = null;
                int modelColumn = convertColumnIndexToModel(column);
                if (modelColumn == 1) {
                    Object value = getModel().getValueAt(row, modelColumn);
                    if (value == null) {
                        value = 0.0;
                        getModel().setValueAt(value, row, column);
                    }
                    Class rowClass = value.getClass();
                    return getDefaultRenderer(rowClass);
                } else {
                    return super.getCellRenderer(row, column);
                }
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                editingClass = null;
                int modelColumn = convertColumnIndexToModel(column);
                if (modelColumn == 1) {
                    editingClass = getModel().getValueAt(row, modelColumn).getClass();
                    return getDefaultEditor(editingClass);
                } else {
                    return super.getCellEditor(row, column);
                }
            }
            //  This method is also invoked by the editor when the value in the editor
            //  component is saved in the TableModel. The class was saved when the
            //  editor was invoked so the proper class can be created.

            @Override
            public Class getColumnClass(int column) {
                return editingClass != null ? editingClass : super.getColumnClass(column);
            }
        };

        devicePropertiesScrollPane = new JScrollPane(propertyTable);
        devicePropertiesScrollPane.setBorder(BorderFactory.createTitledBorder("Device Properties"));

        ObjectPropertiesTableListener objectPropertiesTableListener = new ObjectPropertiesTableListener(controller);
        buildingPropertiesTable = new PropertiesTable();
        buildingPropertiesTable.addTableModelListener(objectPropertiesTableListener);
        JTable buildingPropertyTable = new JTable(buildingPropertiesTable)
        {
            private static final long serialVersionUID = 1L;
            private Class editingClass;

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                editingClass = null;
                int modelColumn = convertColumnIndexToModel(column);
                if (modelColumn == 1)
                {
                    Object value = getModel().getValueAt(row, modelColumn);
                    if (value == null)
                    {
                        value = 0.0;
                        getModel().setValueAt(value, row, column);
                    }
                    Class rowClass = value.getClass();
                    return getDefaultRenderer(rowClass);
                } else {
                    return super.getCellRenderer(row, column);
                }
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                editingClass = null;
                int modelColumn = convertColumnIndexToModel(column);
                if (modelColumn == 1) {
                    editingClass = getModel().getValueAt(row, modelColumn).getClass();
                    return getDefaultEditor(editingClass);
                } else {
                    return super.getCellEditor(row, column);
                }
            }
            //  This method is also invoked by the editor when the value in the editor
            //  component is saved in the TableModel. The class was saved when the
            //  editor was invoked so the proper class can be created.

            @Override
            public Class getColumnClass(int column) {
                return editingClass != null ? editingClass : super.getColumnClass(column);
            }
        };

        JScrollPane buildingPropertiesScrollPane = new JScrollPane(buildingPropertyTable);
        buildingPropertiesScrollPane.setBorder(BorderFactory.createTitledBorder("Building Properties"));

        propertiesPanel.add(buildingPropertiesScrollPane);
        propertiesPanel.add(devicePropertiesScrollPane);

        propertiesPanel.setPreferredSize(new Dimension(400, 300));

        usageTable = new UsageTable();
        usageTable.addTableModelListener(new DeviceUsageTableListener(controller));
        deviceUsageTable = new JTable(usageTable);
        deviceUsageTable.setDefaultEditor(Date.class, new TimeEditor());
        TimeRenderer renderer = new TimeRenderer();
        deviceUsageTable.setDefaultRenderer(Date.class, renderer);
        deviceUsageScrollPane = new JScrollPane(deviceUsageTable);

        AbstractAction newUsageAction = new AbstractAction("Add Time Span")
        {
            @Override
            public void actionPerformed(ActionEvent e) {

                SimpleDateFormat format = new SimpleDateFormat("HH:mm");

                try
                {
                    Date date = format.parse("00:00");
                    Date date1 = format.parse("00:00");

                    Object[] newData = {date, date1};
                    usageTable.addRow(newData);
                    controller.addTimeSpanUsage(new TimeSpan(0,0));
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        };

        AbstractAction removeUsageAction = new AbstractAction("Remove Time Span")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (deviceUsageTable.getSelectedRow() >= 0)
                {
                    controller.removeTimeSpanUsage(deviceUsageTable.getSelectedRow());
                    usageTable.removeRow(deviceUsageTable.getSelectedRow());
                }

            }
        };

        deviceUsagePanel = new JPanel(new GridLayout(2,1));
        deviceUsagePanel.setPreferredSize(new Dimension(400, 200));
        deviceUsagePanel.setBorder(BorderFactory.createTitledBorder("Daily Device Usage"));
        deviceUsagePanel.add(deviceUsageScrollPane);

        JPanel usageControls = new JPanel(new GridLayout(1, 2));
        newUsageButton = new JButton(newUsageAction);
        newUsageButton.setEnabled(false);
        removeUsageButton = new JButton(removeUsageAction);
        removeUsageButton.setEnabled(false);
        usageControls.add(newUsageButton);
        usageControls.add(removeUsageButton);
        deviceUsagePanel.add(usageControls);

        AbstractAction okAction = new AbstractAction("OK")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!conflictsExist())
                {
                    completeEditing();
                    close();
                }
            }
        };

        AbstractAction cancelAction = new AbstractAction("Cancel")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        };

        creationControlButtons = new JPanel(new FlowLayout());
        creationControlButtons.add(new JButton(okAction));
        creationControlButtons.add(new JButton(cancelAction));

        inputCompositeUnitInfoPanel = new JPanel(new GridLayout(2,2));
        inputCompositeUnitInfoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        inputUnitInfoPanel = new JPanel(new GridLayout(1,2));
        inputUnitInfoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

        DeviceSelectedListener deviceSelectedListener = new DeviceSelectedListener(controller);
        DeviceTableListener deviceTableListener = new DeviceTableListener(controller);
        deviceTabbedPane = new DeviceTabbedPane(deviceTableListener, deviceSelectedListener);
        deviceTabbedPane.setBorder(BorderFactory.createTitledBorder("Building's Devices"));
        deviceTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                devicePropertiesTable.clearTable();
            }
        });

        DeviceTabbedPane unitDeviceTabbedPane = new DeviceTabbedPane(deviceTableListener, deviceSelectedListener);
        unitDeviceTabbedPane.setBorder(BorderFactory.createTitledBorder("Unit's Devices"));

        JPanel compositeUnitDevicePanes = new JPanel(new GridLayout(2, 1));
        compositeUnitDevicePanes.add(unitDeviceTabbedPane);
        compositeUnitDevicePanes.add(deviceTabbedPane);

        AbstractAction removeSelection = new AbstractAction("Remove Device")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                DevicePanel devicePanel = (DevicePanel)deviceTabbedPane.getSelectedComponent();
                int selectedRow = devicePanel.getTable().getSelectedRow();
                if (selectedRow >= 0)
                {
                    DeviceTable deviceTable = (DeviceTable)devicePanel.getTable().getModel();
                    Device device =  deviceTable.getRow(selectedRow);
                    deviceTable.removeRow(selectedRow);

                    devicePropertiesTable.clearTable();
                    controller.removeDevice(device.getId());
                    removeUsageButton.setEnabled(false);
                    newUsageButton.setEnabled(false);
                }
            }
        };

        AbstractAction addApplianceAction = new AbstractAction("Add Appliance")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                controller.addDeviceToStructure(DeviceType.APPLIANCE);
            }
        };

        AbstractAction addSourceAction = new AbstractAction("Add Energy Source")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                controller.addDeviceToStructure(DeviceType.ENERGY_SOURCE);
            }
        };

        AbstractAction addStorageAction = new AbstractAction("Add Energy Storage")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                controller.addDeviceToStructure(DeviceType.ENERGY_STORAGE);
            }
        };

        deviceButtonPanel = new JPanel(new GridLayout(1, 4));
        deviceButtonPanel.add(new JButton(addApplianceAction));
        deviceButtonPanel.add(new JButton(addSourceAction));
        deviceButtonPanel.add(new JButton(addStorageAction));
        deviceButtonPanel.add(new JButton(removeSelection));
    }

    private void populateStructureDevicesAndProperties(Structure structure)
    {
        deviceTabbedPane.clearTables();

        List<Device> devices = structure.getAppliances();
        for (Device device : devices)
        {
            deviceTabbedPane.addAppliance(device);
        }

        devices = structure.getEnergySources();
        for (Device device : devices)
        {
            deviceTabbedPane.addEnergySource(device);
        }

        devices = structure.getEnergyStorageDevices();
        for (Device device : devices)
        {
            deviceTabbedPane.addEnergyStorage(device);
        }

        buildingPropertiesTable.clearTable();
        List<Property> properties = structure.getProperties();

        for (Property property : properties)
        {
            Object[] row = {property.getName(), property.getValue()};
            buildingPropertiesTable.addRow(row);
        }
    }

    private void setupSingleUnitEditorComponents(Structure structure)
    {
        inputUnitInfoPanel.add(nameLabel);
        inputUnitInfoPanel.add(nameField);

        //rightPanel.add(notificationPanel, BorderLayout.PAGE_START); // TODO: use grid bag so that I can have more components
        rightPanel.add(propertiesPanel, BorderLayout.PAGE_START);
        rightPanel.add(deviceUsagePanel, BorderLayout.CENTER);
        rightPanel.add(creationControlButtons, BorderLayout.PAGE_END);

        leftPanel.add(inputUnitInfoPanel, BorderLayout.PAGE_START);
        leftPanel.add(deviceTabbedPane, BorderLayout.CENTER);
        leftPanel.add(deviceButtonPanel, BorderLayout.PAGE_END);

        populateStructureDevicesAndProperties(structure);
    }

    private void removeSingleUnitEditorComponents()
    {
        inputUnitInfoPanel.remove(nameLabel);
        inputUnitInfoPanel.remove(nameField);

        rightPanel.remove(notificationPanel);
        rightPanel.remove(propertiesPanel);
        rightPanel.remove(deviceUsagePanel);
        rightPanel.remove(creationControlButtons);

        leftPanel.remove(inputUnitInfoPanel);
        leftPanel.remove(deviceTabbedPane);
        leftPanel.remove(deviceButtonPanel);
    }

    private void setupCompositeUnitEditorComponents(Structure structure)
    {
        inputCompositeUnitInfoPanel.add(nameLabel);
        inputCompositeUnitInfoPanel.add(nameField);
        inputCompositeUnitInfoPanel.add(numberOfUnitsLabel);
        inputCompositeUnitInfoPanel.add(numberOfUnitsField);

        //rightPanel.add(notificationPanel, BorderLayout.PAGE_START);
        rightPanel.add(propertiesPanel, BorderLayout.PAGE_START);
        rightPanel.add(deviceUsagePanel, BorderLayout.CENTER);
        rightPanel.add(creationControlButtons, BorderLayout.PAGE_END);

        leftPanel.add(inputCompositeUnitInfoPanel, BorderLayout.PAGE_START);
        leftPanel.add(deviceTabbedPane, BorderLayout.CENTER);
        leftPanel.add(deviceButtonPanel, BorderLayout.PAGE_END);

        populateStructureDevicesAndProperties(structure);
    }

    private void removeCompositeUnitEditorComponents()
    {
        inputCompositeUnitInfoPanel.remove(nameLabel);
        inputCompositeUnitInfoPanel.remove(nameField);
        inputCompositeUnitInfoPanel.remove(numberOfUnitsLabel);
        inputCompositeUnitInfoPanel.remove(numberOfUnitsField);

        //rightPanel.remove(notificationPanel);
        rightPanel.remove(propertiesPanel);
        rightPanel.remove(deviceUsagePanel);
        rightPanel.remove(creationControlButtons);

        leftPanel.remove(inputCompositeUnitInfoPanel);
        leftPanel.remove(deviceTabbedPane);
        leftPanel.remove(deviceButtonPanel);
    }

    private void setupPowerPlantEditorComponents(Structure structure)
    {
        inputUnitInfoPanel.add(nameLabel);
        inputUnitInfoPanel.add(nameField);

        rightPanel.add(notificationPanel, BorderLayout.PAGE_START);
        rightPanel.add(creationControlButtons, BorderLayout.PAGE_END);

        leftPanel.add(inputUnitInfoPanel, BorderLayout.PAGE_START);

        propertiesPanel.remove(devicePropertiesScrollPane);
        leftPanel.add(propertiesPanel, BorderLayout.CENTER);

        populateStructureDevicesAndProperties(structure);
    }

    private void removePowerPlantEditorComponents()
    {
        inputUnitInfoPanel.remove(nameLabel);
        inputUnitInfoPanel.remove(nameField);

        rightPanel.remove(notificationPanel);
        rightPanel.remove(creationControlButtons);

        leftPanel.remove(inputUnitInfoPanel);
        leftPanel.remove(propertiesPanel);
        propertiesPanel.add(devicePropertiesScrollPane);
    }

    Boolean conflictsExist()
    {
        String newName = nameField.getText();

        if (newName.equals(""))
        {
            infoLabel.setText("Name cannot be blank");
            return true;
        }

        return false;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent event)
    {
        if (event.getPropertyName().equals(StructureManager.PC_ADD_DEVICE))
        {
            Device device = (Device)event.getNewValue();

            switch(device.getType())
            {
                case APPLIANCE:
                {
                    deviceTabbedPane.addAppliance(device);
                } break;
                case ENERGY_SOURCE:
                {
                    deviceTabbedPane.addEnergySource(device);
                } break;
                case ENERGY_STORAGE:
                {
                    deviceTabbedPane.addEnergyStorage(device);
                } break;
            }
        }
        else if (event.getPropertyName().equals(StructureManager.PC_DEVICE_SELECTED))
        {
            String format = "%02d:%02d";
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            devicePropertiesTable.clearTable();
            usageTable.clearTable();

            Device device = (Device)event.getNewValue();
            List<Property> properties = device.getProperties();

            newUsageButton.setEnabled(true);
            removeUsageButton.setEnabled(true);

            for (Property property : properties)
            {
                Object[] row = {property.getName(), property.getValue()};
                devicePropertiesTable.addRow(row);
            }

            for (TimeSpan timeSpan : device.getElectricityUsageSchedule().getActiveTimeSpans())
            {
                long fromHours = TimeUnit.SECONDS.toHours((long)timeSpan.from);
                long fromMinutes = TimeUnit.SECONDS.toMinutes((long)timeSpan.from) - TimeUnit.HOURS.toMinutes(fromHours);

                long toHours = TimeUnit.SECONDS.toHours((long)timeSpan.to);
                long toMinutes = TimeUnit.SECONDS.toMinutes((long)timeSpan.to) - TimeUnit.HOURS.toMinutes(toHours);

                String fromTime = String.format(format, fromHours, fromMinutes);
                String toTime = String.format(format, toHours, toMinutes);

                try
                {
                    Date from = dateFormat.parse(fromTime);
                    Date to = dateFormat.parse(toTime);

                    Object[] deviceUsage = {from, to};
                    usageTable.addRow(deviceUsage);
                }
                catch (ParseException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

    public String getName()
    {
        return nameField.getText();
    }
}
