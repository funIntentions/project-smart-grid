package com.projects.gui.panel;

import com.projects.gui.SubscribedView;
import com.projects.models.Appliance;
import com.projects.models.ElectricityUsageSchedule;
import com.projects.models.Structure;
import com.projects.models.TimeSpan;
import com.projects.systems.simulation.World;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.List;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 5/27/2015.
 */
public class SelectionInfoPanel extends JPanel implements SubscribedView
{
    private ChartPanel chartPanel;
    private XYSeriesCollection data;

    public SelectionInfoPanel()
    {
        setLayout(new BorderLayout());

        chartPanel = new ChartPanel(createChart());
        chartPanel.setVisible(false);

        add(chartPanel, BorderLayout.CENTER);
    }

    public void modelPropertyChange(PropertyChangeEvent event)
    {
        if (event.getPropertyName().equals(World.PC_STRUCTURE_SELECTED))
        {
            chartPanel.setVisible(true);
            data.removeAllSeries();
            data.addSeries(calculateStructuresLoadProfileDataSeries((Structure)event.getNewValue()));
        }
    }

    private XYSeries calculateStructuresLoadProfileDataSeries(Structure structure)
    {
        XYSeries series = new XYSeries(structure.getName());

        long timeSpanLength = TimeUnit.MINUTES.toSeconds(30);
        int numTimeSpans = 48;
        long previous = 0;

        for (int time = 0; time < numTimeSpans; ++time)
        {
            java.util.List<Appliance> appliances = (java.util.List)structure.getAppliances();
            double usageDuringTimeSpan = 0;

            for (Appliance appliance : appliances)
            {
                usageDuringTimeSpan += appliance.getElectricityUsageSchedule().getElectricityUsageDuringSpan(new TimeSpan(previous, time * timeSpanLength)) > 0 ? appliance.getAverageConsumption() : 0;
            }

            previous = time * timeSpanLength;

            series.add(time + 1, usageDuringTimeSpan);
        }

        return series;
    }

    private JFreeChart createChart() {
        XYSeries series1 = new XYSeries("Structure");
        data = new XYSeriesCollection(series1);
        return ChartFactory.createXYLineChart(
                "Load Profile",  // chart title
                "Time of Day",
                "Usage (Watts)",
                data
        );
    }
}
