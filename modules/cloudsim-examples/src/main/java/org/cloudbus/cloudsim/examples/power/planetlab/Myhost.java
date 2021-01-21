package org.cloudbus.cloudsim.examples.power.planetlab;

public class Myhost {
    private int avaliableCpu;
    private int avaliableRam;
    private long avaliableBw;
    private double temperature;
    private  int totalCpu;
    private int totalRam;
    private long totalBw;
    private int Id;

    public void setAvaliableCpu(int cpu)
    {
        this.avaliableCpu = cpu;
    }

    public void setAvaliableRam(int ram)
    {
        this.avaliableRam = ram;
    }
    public void setAvaliableBw(long bw)
    {
        this.avaliableBw = bw;
    }
    public void setTemperature(double tem)
    {
        this.temperature = tem;
    }
    public void setTotalCpu(int cpu)
    {
        this.totalCpu = cpu;
    }
    public void setTotalRam(int ram)
    {
        this.totalRam = ram;
    }
    public void setTotalBw(long bw)
    {
        this.totalBw = bw;
    }
    public int getTotalCpu()
    {
        return this.totalCpu;
    }
    public int getAvaliableCpu()
    {
        return this.avaliableCpu;
    }
    public int getAvaliableRam()
    {
        return this.avaliableRam;
    }
    public long getAvaliableBw()
    {
        return this.avaliableBw;
    }
    public double getTemperature()
    {
        return this.temperature;
    }
    public int getTotalRam() { return this.totalRam; }
    public long getTotalBw() { return this.totalBw; }
    public void setId(int id)
    {
        this.Id = id;
    }
    public int getId()
    {
        return this.Id;
    }
    public void updateCpu(int cpu)
    {
        this.avaliableCpu+=cpu;
    }
}
