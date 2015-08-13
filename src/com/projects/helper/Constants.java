package com.projects.helper;

import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 6/8/2015.
 */
public class Constants
{
    public static final String SMART_GRID_FILE = "grid";
    public static final String STRATEGIES_FILE_PATH = "./strategies/";
    public static final String TEMPLATE_FILE_PATH = "/data/SmartGridTemplate.xml";
    public static final String HOUSE_IMAGE_PATH = "/images/House.png";
    public static final String POWER_PLANT_IMAGE_PATH = "/images/PowerPlant.png";
    public static final String HOURS_AND_MINUTES_FORMAT = "HH:mm";
    public static final long FIXED_SIMULATION_RATE_MILLISECONDS = 33; // 30 frames a second
    public static final double FIXED_SIMULATION_RATE_SECONDS = 0.0333333;
    public static final long SECONDS_IN_DAY = TimeUnit.DAYS.toSeconds(1);
    public static final long DAYS_IN_WEEK = 7;
}
