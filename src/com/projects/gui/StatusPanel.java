package com.projects.gui;

import com.projects.management.Task;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;

/**
 * Created by Dan on 6/15/2015.
 */
public class StatusPanel extends JPanel implements SubscribedView
{
    JLabel statusLabel;

    public StatusPanel(String initialStatus)
    {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        statusLabel = new JLabel(" " + initialStatus);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel);
    }

    public void modelPropertyChange(PropertyChangeEvent event)
    {
        if (event.getPropertyName().equals(Task.PC_TASK_UPDATE))
        {
            String update = (String)event.getNewValue();
            System.out.println(update);
            statusLabel.setText(" " + update);
        }
    }
}
