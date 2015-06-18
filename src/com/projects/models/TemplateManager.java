package com.projects.models;

/**
 * Created by Dan on 6/19/2015.
 */
public class TemplateManager extends System
{
    private Template template;
    public static final String PC_TEMPLATE_READY = "PC_TEMPLATE_READY";
    public static final String PC_TEMPLATE_SELECTED = "PC_TEMPLATE_SELECTED";

    public TemplateManager(Template template)
    {
        this.template = template;
    }

    @Override
    public void postSetupSync()
    {
        changeSupport.firePropertyChange(PC_TEMPLATE_READY, null, template);
    }

    public void structureTemplateSelected(Structure structure)
    {
        changeSupport.firePropertyChange(PC_TEMPLATE_SELECTED, null, structure);
    }
}
