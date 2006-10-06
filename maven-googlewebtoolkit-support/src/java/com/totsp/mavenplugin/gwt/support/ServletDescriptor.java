package com.totsp.mavenplugin.gwt.support;

public class ServletDescriptor
{

    private String className;
    private String path;

    public ServletDescriptor(String path, String className)
    {
        this.path = path;
        this.className = className;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
    
    public String toString()
    {
        return "Path:" + this.path + " Class:" + this.className;        
    }
}
