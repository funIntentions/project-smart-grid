package com.projects.simulation;

import com.projects.Main;
import com.projects.model.*;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 6/24/2015.
 */
public class DemandManager
{
    private List<SingleUnitStructure> structures;
    private HashMap<Integer, Float> structureExpenses;
    private HashMap<Integer, Float> structureEnvironmentalImpact;
    private HashMap<Integer, List<Float>> structureLoadProfiles;
    private HashMap<Integer, List<Float>> structureDemandProfiles;
    private List<Integer> demandProfileForToday;
    private float timeOverflow;
    private double usageInWattsPerHour = 0;
    private double totalUsageInkWh = 0;
    private double electricityDemand = 0;
    private boolean dailyDemandProfileReady = false;
    private Main main;

    public DemandManager()
    {
        structureExpenses = new HashMap<>();
        structureEnvironmentalImpact = new HashMap<>();
        structures = new ArrayList<SingleUnitStructure>();
        structureLoadProfiles = new HashMap<Integer, List<Float>>();
        structureDemandProfiles = new HashMap<Integer, List<Float>>();
        demandProfileForToday = new ArrayList<Integer>();
        timeOverflow = 0;
    }

    public void setMain(Main main)
    {
        this.main = main;

        main.selectedWorldStructureProperty().addListener((observable, oldValue, newValue) ->
        {
            if (isConsumer(newValue))
                main.getStructureDetailsPaneController().setStructureData(newValue, getLoadProfile(newValue));
            else
                main.getStructureDetailsPaneController().setStructureData(newValue, new ArrayList<>());
        });
    }

    public List<Float> getLoadProfile(Structure structure)
    {
        return structureLoadProfiles.get(structure.getId());
    }

    public List<Float> getDemandProfile(Structure structure)
    {
        return structureDemandProfiles.get(structure.getId());
    }

    public void calculateLoadProfiles()
    {
        structureLoadProfiles.clear();

        int secondsInDay = (int)TimeUnit.DAYS.toSeconds(1);
        int interval = 60;
        int length = secondsInDay/interval;

        for (SingleUnitStructure structure : structures)
        {
            List<Float> loadProfile = new ArrayList<Float>();
            List<Appliance> appliances = (List)structure.getAppliances();

            for (int time = 0; time < length; ++time)
            {
                loadProfile.add(0f);

                for (Appliance appliance : appliances)
                {
                    if (appliance.isOnAtTime(time * interval))
                    {
                        float sum = loadProfile.get(time) + (float) appliance.getUsageConsumption();
                        loadProfile.set(time, sum);
                    }
                    else
                    {
                        float sum = loadProfile.get(time) + (float) appliance.getStandbyConsumption();
                        loadProfile.set(time, sum);
                    }
                }
            }

            structureLoadProfiles.put(structure.getId(), loadProfile);
        }
    }

    public void calculateDemandProfiles(StorageManager storageManager)
    {
        structureDemandProfiles.clear();

        for (SingleUnitStructure structure : structures)
        {
            List<Float> loadProfile = structureLoadProfiles.get(structure.getId());
            List<Float> demandProfile = new ArrayList<Float>();

            int length = loadProfile.size();
            for (int time = 0; time < length; ++time)
            {
                float storageDemand = storageManager.getStructuresStorageDemandAtTime(structure, time);
                float loadDemand = loadProfile.get(time);
                float totalDemand = loadDemand + storageDemand;

                if (totalDemand < 0) // Storage device released energy is greater than the current load
                {
                    totalDemand = 0;
                }

                demandProfile.add(totalDemand);
            }

            structureDemandProfiles.put(structure.getId(), demandProfile);
        }
    }

    public void calculateDaysExpenses(List<Float> pricesForDay)
    {
        for (Structure structure : structures)
        {
            List<Float> structuresDemandProfile = structureDemandProfiles.get(structure.getId());

            Float totalExpenses = structureExpenses.get(structure.getId());

            if (totalExpenses == null)
                totalExpenses = 0f;

            for (int time = 0; time < structuresDemandProfile.size(); ++time)
            {
                Float demandAtThisTime = structuresDemandProfile.get(time);
                Float priceAtThisTime = pricesForDay.get(time);

                Float newExpense = (demandAtThisTime/(1000 * TimeUnit.HOURS.toMinutes(1))) * priceAtThisTime; // Convert Watts this minute to kWh TODO: change from watts per minute to kWatts per minute
                totalExpenses += newExpense;
            }

            structureExpenses.put(structure.getId(), totalExpenses);
        }
    }

    public void calculateDaysEnvironmentalImpact(List<Float> todaysEmissions)
    {

        for (Structure structure : structures)
        {
            List<Float> structuresDemandProfile = structureDemandProfiles.get(structure.getId());

            Float totalEmissions = structureEnvironmentalImpact.get(structure.getId());

            if (totalEmissions == null)
                totalEmissions = 0f;

            for (int time = 0; time < structuresDemandProfile.size(); ++time)
            {
                Float demandAtThisTime = structuresDemandProfile.get(time);
                Float emissionsAtThieTime = todaysEmissions.get(time);

                Float newExpense = (demandAtThisTime/(1000 * TimeUnit.HOURS.toMinutes(1))) * emissionsAtThieTime; // Convert Watts this minute to kWh TODO: change from watts per minute to kWatts per minute
                totalEmissions += newExpense;
            }

            structureEnvironmentalImpact.put(structure.getId(), totalEmissions);
        }
    }

    public void resetDay()
    {
        demandProfileForToday.clear();
        dailyDemandProfileReady = false;
        processOverflowBuffer();
    }

    private void processOverflowBuffer()
    {
        for (int time = 0; time < timeOverflow; ++time)
        {
            electricityDemand = 0;

            for (List<Float> demandProfile : structureDemandProfiles.values())
            {
                electricityDemand += demandProfile.get(time);
            }

            demandProfileForToday.add((int) electricityDemand);
        }

        timeOverflow = 0;
    }

    public void calculateDemand(double timeElapsedInSeconds, double totalTimeElapsedInSeconds)
    {
        int previousElapsedSecondsThisDay = (int)(Math.floor(totalTimeElapsedInSeconds - timeElapsedInSeconds) % TimeUnit.DAYS.toSeconds(1));
        int previousElapsedMinutesThisDay = (int)TimeUnit.SECONDS.toMinutes(previousElapsedSecondsThisDay);
        int totalElapsedMinutesThisDay = (int)TimeUnit.SECONDS.toMinutes((long)totalTimeElapsedInSeconds % TimeUnit.DAYS.toSeconds(1));


        if (previousElapsedMinutesThisDay > totalElapsedMinutesThisDay)
        {
            timeOverflow = totalElapsedMinutesThisDay;

            totalElapsedMinutesThisDay = (int)TimeUnit.DAYS.toMinutes(1);
        }

        for (int time = previousElapsedMinutesThisDay; time < totalElapsedMinutesThisDay; ++time)
        {
            electricityDemand = 0;

            for (List<Float> demandProfile : structureDemandProfiles.values())
            {
                electricityDemand += demandProfile.get(time);
            }

            demandProfileForToday.add((int) electricityDemand);
        }

        if (demandProfileForToday.size() == TimeUnit.DAYS.toMinutes(1))
            dailyDemandProfileReady = true;
    }

    public void reset()
    {
        usageInWattsPerHour = 0;
        totalUsageInkWh = 0;
        structureExpenses.clear();
        structureEnvironmentalImpact.clear();
        resetDay();
    }

    private boolean isConsumer(Structure structure)
    {
        return (structureLoadProfiles.get(structure.getId()) != null);
    }

    public boolean removeStructure(Structure structureToRemove)
    {
        for (Structure structure : structures)
        {
            if (structure.getId() == structureToRemove.getId())
            {
                structures.remove(structure);
                return true;
            }
        }

        return false;
    }

    public void removeAllStructures()
    {
        structures.clear();
    }

    public boolean syncStructures(SingleUnitStructure changedStructure)
    {
        int structureIndex = -1;

        for (int i = 0; i < structures.size(); ++i)
        {
            if (changedStructure.getId() == structures.get(i).getId())
            {
                structureIndex = i;
            }
        }

        int applianceCount = changedStructure.getAppliances().size();

        if (structureIndex < 0 && applianceCount > 0)
        {
            structures.add(changedStructure);
        }
        else if (structureIndex >=0)
        {
            if (applianceCount > 0)
            {
                structures.set(structureIndex, changedStructure);
            }
            else
            {
                structures.remove(structureIndex);
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    public double getTotalUsageInkWh()
    {
        return totalUsageInkWh;
    }

    public double getUsageInWattsPerHour()
    {
        return usageInWattsPerHour;
    }

    public double getElectricityDemand()
    {
        return electricityDemand;
    }

    public List<Integer> getDemandProfileForToday()
    {
        return demandProfileForToday;
    }

    public List<SingleUnitStructure> getStructures()
    {
        return structures;
    }

    public HashMap<Integer, Float> getStructureExpenses()
    {
        return structureExpenses;
    }

    public HashMap<Integer, Float> getStructureEnvironmentalImpact()
    {
        return structureEnvironmentalImpact;
    }

    public boolean isDailyDemandProfileReady()
    {
        return dailyDemandProfileReady;
    }
}

