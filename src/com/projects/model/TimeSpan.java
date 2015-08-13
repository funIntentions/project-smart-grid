package com.projects.model;

import com.projects.helper.DateUtil;
import javafx.beans.property.*;

import java.time.LocalTime;

/**
 * Created by Dan on 7/3/2015.
 */
public class TimeSpan
{
    private ObjectProperty<LocalTime> from;
    private ObjectProperty<LocalTime> to;
    private BooleanProperty monday;
    private BooleanProperty tuesday;
    private BooleanProperty wednesday;
    private BooleanProperty thursday;
    private BooleanProperty friday;
    private BooleanProperty saturday;
    private BooleanProperty sunday;

    public TimeSpan(LocalTime from, LocalTime to)
    {
        this.from = new SimpleObjectProperty<>(from);
        this.to = new SimpleObjectProperty<>(to);
        monday = new SimpleBooleanProperty(true);
        tuesday = new SimpleBooleanProperty(true);
        wednesday = new SimpleBooleanProperty(true);
        thursday = new SimpleBooleanProperty(true);
        friday = new SimpleBooleanProperty(true);
        saturday = new SimpleBooleanProperty(true);
        sunday = new SimpleBooleanProperty(true);
    }

    public LocalTime getFrom()
    {
        return from.get();
    }

    public ObjectProperty<LocalTime> fromProperty()
    {
        return from;
    }

    public void setFrom(LocalTime from)
    {
        this.from.set(from);
    }

    public LocalTime getTo()
    {
        return to.get();
    }

    public ObjectProperty<LocalTime> toProperty()
    {
        return to;
    }

    public void setTo(LocalTime to)
    {
        this.to.set(to);
    }

    public boolean getMonday()
    {
        return monday.get();
    }

    public BooleanProperty mondayProperty()
    {
        return monday;
    }

    public boolean getTuesday()
    {
        return tuesday.get();
    }

    public BooleanProperty tuesdayProperty()
    {
        return tuesday;
    }

    public boolean getWednesday()
    {
        return wednesday.get();
    }

    public BooleanProperty wednesdayProperty()
    {
        return wednesday;
    }

    public boolean getThursday()
    {
        return thursday.get();
    }

    public BooleanProperty thursdayProperty()
    {
        return thursday;
    }

    public boolean getFriday()
    {
        return friday.get();
    }

    public BooleanProperty fridayProperty()
    {
        return friday;
    }

    public boolean getSaturday()
    {
        return saturday.get();
    }

    public BooleanProperty saturdayProperty()
    {
        return saturday;
    }

    public boolean getSunday()
    {
        return sunday.get();
    }

    public BooleanProperty sundayProperty()
    {
        return sunday;
    }
}
