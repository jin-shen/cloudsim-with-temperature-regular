package org.cloudbus.cloudsim.examples.power.planetlab;


import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;

import java.util.*;

public class GAS {
    private int ChrNum = 30;
    private String[] ipop = new String[ChrNum];
    private int generation = 0;
    public static final int GENE = GAConfig.GENE;
    private double bestfitness = Double.MAX_VALUE;
    private int bestgeneration;
    private String beststr;
    private List<Myhost> myhostList;
    private double avgTemper;
    private static List<Vm> temvmList;
    protected static List<PowerHost> hostList;
    private  List<List<Vm>> vmList = new ArrayList<List<Vm>>();
    private int[][]  vmTohost= new int[ChrNum][GAConfig.VMNum];
    private Map<Double,int[]> MinEnergy = new TreeMap<>();
    PowerModel model = new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
    private Float[] initTemper = new Float[GAConfig.HostNum];
    private int MBest = 20;
    private Queue<vmTohostWithEmergy> bestPop = new PriorityQueue<>();
//    private
    public void initVmList(List<Vm> temvmList)
    {
//        temvmList = Helper.createVmList(0,512);
        for(int i = 0;i<ChrNum;i++)
        {
            Random randonm = new Random();
            for(int j = 0;j < 512;j++)
            {
                int randomPos = randonm.nextInt(511);
                Vm tempVm = temvmList.get(j);
                temvmList.set(j, temvmList.get(randomPos));
                temvmList.set(randomPos,tempVm);
            }
            vmList.add(temvmList);
        }
    }

    public void initHostList(List<PowerHost> hostList)
    {
//        hostList = Helper.createHostList(1024);
        int i = 0;
        for(PowerHost host : hostList)
        {
            Myhost temHost = new Myhost();
            temHost.setTotalCpu(host.getTotalMips());
            temHost.setTotalRam(host.getRam());
            temHost.setTotalBw(host.getBw());
            temHost.setTemperature(host.getTemperature());
            temHost.setAvaliableCpu(host.getTotalMips());
            temHost.setAvaliableRam(host.getRam());
            temHost.setAvaliableBw(host.getBw());
            myhostList.add(temHost);
            avgTemper += host.getTemperature();
            initTemper[i] = host.getTemperature();
            i++;
        }
        avgTemper = avgTemper / GAConfig.HostNum;
    }

    public void initpop()
    {
        for(int i = 0;i < ChrNum;i++)
        {
            for(int vms = 0;vms < GAConfig.VMNum;vms++)
            {
                double minTemper = Double.MAX_VALUE;
                int targetHostId = -1;
                int vmId = vmList.get(i).get(vms).getId();
                for(int hosts = 0;hosts < GAConfig.HostNum;hosts++)
                {
                    if(isSuitable(vmList.get(i).get(hosts), myhostList.get(hosts)))
                    {
                        double temper = calculateTemper(myhostList.get(hosts) ,vmList.get(i).get(vms));
                        if(minTemper > temper)
                        {
                            minTemper = temper;
                            targetHostId = myhostList.get(hosts).getId();
                        }
                    }
                }
                myhostList.get(targetHostId).setAvaliableCpu((int) (myhostList.get(targetHostId).getAvaliableCpu() + vmList.get(i).get(vms).getMips()));
                vmTohost[i][vmId] = targetHostId;
                myhostList.get(targetHostId).setTemperature(minTemper);
            }
            Double curPower = 0.0;
            for(Myhost host : myhostList)
            {
                curPower += model.getPower(getUtilization(host));
            }
            if(bestPop.size() < MBest)
            {
                vmTohostWithEmergy temp = new vmTohostWithEmergy(curPower,vmTohost[i]);
                bestPop.add(temp);
            }
            else
            {
                if(bestPop.peek().Power > curPower)
                {
                    bestPop.remove();
                    bestPop.add(new vmTohostWithEmergy(curPower,vmTohost[i]));
                }
            }
            ipop[i] = code(vmTohost[i]);
        }
    }

    static Comparator<vmTohostWithEmergy> tohost = new Comparator<vmTohostWithEmergy>()
    {

        @Override
        public int compare(vmTohostWithEmergy o1, vmTohostWithEmergy o2) {
            return (int)(o1.Power - o2.Power);
        }
    };

    public void cross()
    {
        String tem1,tem2;
        for(int i = 0;i < ChrNum ;i++)
        {
            int pos = (int)(Math.random() * GAConfig.VMNum * Math.sqrt(GAConfig.HostNum));
//            int select = (int)(Math.random() * MBest);
            tem1 = ipop[i].substring(0,pos) + code(bestPop.peek().vmTohost).substring(pos);
            ipop[i] = tem1;
        }
    }
    /*
    *基因突变率为0.1%
    */

    public void mutation()
    {
        int mutaNum = (int)(GAConfig.GENE * ChrNum * 0.001);
        for(int i = 0;i < mutaNum;i++)
        {
            char a;
            String temp;
            int GenNum = (int) (Math.random() * ChrNum);
            int pos = (int) (Math.random() * GAConfig.GENE);
            if(ipop[GenNum].charAt(pos) == '0')
                a = '1';
            else
                a = '0';
            if(pos == 0)
                temp = a + ipop[GenNum].substring(pos + 1);
            else
            {
                if(pos != GAConfig.GENE * ChrNum -1)
                    temp = a + ipop[GenNum].substring(pos);
                else
                    temp = ipop[GenNum].substring(0,pos) + a + ipop[GenNum].substring(pos);
            }
            ipop[GenNum] = temp;
        }
    }

    public void updateBestPop()
    {

    }

    public void select()
    {

    }


    /*
    energy与temper相关参数待改
     */
    public Double getFitness(int[] allocate)
    {
        double energy = 0.0;
        double temper = 0.0;
        for(int i = 0; i<GAConfig.VMNum;i++)
        {
            myhostList.get(allocate[i]).updateCpu((int)temvmList.get(i).getMips());
        }
        for(Myhost host : myhostList)
        {
            energy += model.getPower(getUtilization(host));
            temper += host.getTemperature()/(60-calculateTemper((double) getUtilization(host)));
        }
        clearHost();
        return  energy + temper;
    }

    public int getUtilization(Myhost myhost)
    {
        return (myhost.getTotalCpu()-myhost.getAvaliableCpu()) / myhost.getTotalCpu();
    }
    public void clearHost()
    {
        int i = 0;
        for(Myhost host : myhostList)
        {
            host.setAvaliableCpu(host.getTotalCpu());
            host.setAvaliableRam(host.getAvaliableRam());
            host.setAvaliableBw(host.getAvaliableBw());
            host.setTemperature(initTemper[i]);
            i++;
        }
    }


    public boolean isSuitable(Vm vm,Myhost myhost)
    {
        return (vm.getBw() <= myhost.getAvaliableBw()) && (vm.getMips() <= myhost.getAvaliableCpu()) &&
                (vm.getRam() <= myhost.getAvaliableRam());
    }

    public double calculateTemper(Myhost myhost, Vm vm)
    {
        double cpuUtilization = (double) (myhost.getTotalCpu()-myhost.getAvaliableCpu()+vm.getMips())/myhost.getTotalCpu();
//        int cpuUtilization = myhost.getTotalCpu()-myhost.getAvaliableCpu()-;
        float a = (float) 32.4031331148812;
        float b = (float) -0.105871112063592;
        float c = (float) 0.019012207217;
        float d = (float) -0.000103336673855697;
        double targetTemper = a + b * cpuUtilization + Math.pow(c,2) * cpuUtilization + Math.pow(d,3) * cpuUtilization;
        return targetTemper + myhost.getTemperature() - avgTemper;
    }

    public double calculateTemper(Double utilization)
    {
//        double cpuUtilization = (double) (myhost.getTotalCpu()-myhost.getAvaliableCpu()+vm.getMips())/myhost.getTotalCpu();
//        int cpuUtilization = myhost.getTotalCpu()-myhost.getAvaliableCpu()-;
        float a = (float) 32.4031331148812;
        float b = (float) -0.105871112063592;
        float c = (float) 0.019012207217;
        float d = (float) -0.000103336673855697;
        double targetTemper = a + b * utilization + Math.pow(c,2) * utilization + Math.pow(d,3) * utilization;
        return targetTemper;
    }

    public String code(int[] pop)
    {
        String temp = "";
        String res = new String();
        for(int gen:pop)
        {
            temp = Integer.toBinaryString(gen);
            if(temp.length() < Math.sqrt(GAConfig.HostNum))
            {
                for(int i = 0;i<temp.length();i++)
                    temp = "0" + temp;
            }
            res.concat(temp);
        }
        return res;
    }

    public int[] decode(String str)
    {
        int[] res = new int[(int)Math.sqrt(GAConfig.HostNum)];
        int tempint;
        String tempstr = "";
        int j = 0;
        for(int i = 0;i<str.length();i++)
        {
            if(i%Math.sqrt(GAConfig.HostNum) == 0)
            {
                tempint = Integer.parseInt(tempstr, 2);
                tempstr = "";
                res[j++] = tempint;
            }
            else
                tempstr += str.charAt(i);
        }
        return res;
    }
}


