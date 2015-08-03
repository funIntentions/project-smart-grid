package com.projects.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Created by Dan on 6/26/2015.
 */
public class EnergySource extends Device
{
    private DoubleProperty ratedDCVoltage;
    private DoubleProperty ratedACVoltage;
    private DoubleProperty current;

    public EnergySource(String name, int id, double ratedACVoltage, double ratedDCVoltage, double current)
    {
        super(name, id);

        this.ratedACVoltage = new SimpleDoubleProperty(ratedACVoltage);
        this.ratedDCVoltage = new SimpleDoubleProperty(ratedDCVoltage);
        this.current = new SimpleDoubleProperty(current);
    }

    public double getRatedDCVoltage()
    {
        return ratedDCVoltage.get();
    }

    public DoubleProperty ratedDCVoltageProperty()
    {
        return ratedDCVoltage;
    }

    public void setRatedDCVoltage(double ratedDCVoltage)
    {
        this.ratedDCVoltage.set(ratedDCVoltage);
    }

    public double getRatedACVoltage()
    {
        return ratedACVoltage.get();
    }

    public DoubleProperty ratedACVoltageProperty()
    {
        return ratedACVoltage;
    }

    public void setRatedACVoltage(double ratedACVoltage)
    {
        this.ratedACVoltage.set(ratedACVoltage);
    }

    public double getCurrent()
    {
        return current.get();
    }

    public DoubleProperty currentProperty()
    {
        return current;
    }

    public void setCurrent(double current)
    {
        this.current.set(current);
    }
}
