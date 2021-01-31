package org.cloudbus.cloudsim.examples.power.planetlab;


import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelCubic;

import java.io.*;
import java.util.*;

public class GAS {
    private int ChrNum = 30;
    private String[] ipop = new String[ChrNum];
    public int generation = 0;
    public static final int GENE = GAConfig.GENE;
    private double bestfitness = Double.MAX_VALUE;
    public int bestgeneration;
    private String beststr;
    private List<Myhost> myhostList = new ArrayList<>();
    private double avgTemper;
    private static List<Vm> GaVmList = new ArrayList<>();
    private static List<PowerHost> GaHostList = new ArrayList<>();
//    protected static List<PowerHost> hostList = new ArrayList<>();
    private  List<List<Vm>> vmList = new ArrayList<List<Vm>>();
    private int[][]  vmTohost= new int[ChrNum][GAConfig.VMNum];
    private Map<Double,int[]> MinEnergy = new TreeMap<>();
//    PowerModel model = new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
    PowerModel model = new PowerModelCubic(200,0.3);
    private Float[] initTemper = new Float[GAConfig.HostNum];
    private int MBest = 1;
    private PriorityQueue<vmTohostWithEmergy> bestPop = new PriorityQueue<>();
    private int[] besttuple;
    private File fe;
    private int calTime;
    private int[] best;

    public GAS(List<Vm> temvmList, List<PowerHost> hostList)
    {
        GaHostList.addAll(hostList);
        GaVmList.addAll(temvmList);
        this.fe = new File("E:\\cloudsim\\cloudsim-cloudsim-4.0\\modules\\cloudsim-examples\\src\\main\\java\\org\\cloudbus\\cloudsim\\examples\\power\\planetlab\\result.txt");
        calTime = 0;
    }
    public void initVmList()
    {
        for(int i = 0;i<ChrNum;i++)
        {
            List<Vm> temvmList = new ArrayList<>();
            for(Vm vm : GaVmList)
                temvmList.add(vm.clone());
            Random randonm = new Random();
            for(int j = 0;j < GAConfig.VMNum;j++)
            {
                int randomPos = randonm.nextInt(GAConfig.VMNum-1);
                Vm tempVm = temvmList.get(j);
                temvmList.set(j, temvmList.get(randomPos));
                temvmList.set(randomPos,tempVm);
            }
            vmList.add(temvmList);
        }
    }

    public void initHostList()
    {
//        hostList = Helper.createHostList(1024);
        int i = 0;
        for(PowerHost host : GaHostList)
        {
            Myhost temHost = new Myhost();
            temHost.setTotalCpu(host.getTotalMips());
            temHost.setTotalRam(host.getRam());
            temHost.setTotalBw(host.getBw());
            temHost.setTemperature(host.getTemperature());
            temHost.setAvaliableCpu(host.getTotalMips());
            temHost.setAvaliableRam(host.getRam());
            temHost.setAvaliableBw(host.getBw());
            temHost.setId(host.getId());
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
                double maxTemper = Double.MIN_VALUE;
                int targetHostId = -1;
                int vmId = vmList.get(i).get(vms).getId();
                double targetTemper = 0;
                for(int hosts = 0;hosts < GAConfig.HostNum;hosts++)
                {
                    if(isSuitable(vmList.get(i).get(vms), myhostList.get(hosts)))
                    {
                        double temper[] = calculateTemper(myhostList.get(hosts) ,vmList.get(i).get(vms));
                        if(maxTemper < temper[0])
                        {
                            maxTemper = temper[0];
                            targetHostId = myhostList.get(hosts).getId();
                            targetTemper = temper[1];
                        }
                    }
                    if(hosts == GAConfig.HostNum && targetTemper == 0)
                        Log.printLine("no sutitable host for vm");
                }
//                Log.printLine("targetHostId is" + targetHostId);
                myhostList.get(targetHostId).setAvaliableCpu((int) (myhostList.get(targetHostId).getAvaliableCpu() - vmList.get(i).get(vms).getMips()));
                myhostList.get(targetHostId).setAvaliableRam((myhostList.get(targetHostId).getAvaliableRam() - vmList.get(i).get(vms).getRam()));
                myhostList.get(targetHostId).setAvaliableBw((myhostList.get(targetHostId).getAvaliableBw() - vmList.get(i).get(vms).getBw()));
                vmTohost[i][vmId] = targetHostId;
                myhostList.get(targetHostId).setTemperature(targetTemper);
            }
            Double curPower = 0.0;
            for(Myhost host : myhostList)
            {
                curPower += model.getPower(getUtilization(host));
            }
            if(bestPop.size() < MBest)
            {
                vmTohostWithEmergy temp = new vmTohostWithEmergy(curPower,vmTohost[i]);
//                bestPop.add(temp);
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
            clearHost();
            System.out.printf(i + "  round complete");
            Log.printLine();
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
            int pos = (int)(Math.random() * GAConfig.VMNum * Math.log(GAConfig.HostNum) / Math.log(2));
//            int select = (int)(Math.random() * MBest);
            tem1 = ipop[i].substring(0,pos) + code(best).substring(pos);
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
//            Log.printLine("ipop[i] length is" + ipop[GenNum].length());
            if(ipop[GenNum].charAt(pos) == '0')
                a = '1';
            else
                a = '0';
            if(pos == 0)
                temp = a + ipop[GenNum].substring(pos + 1);
            else
            {
                if(pos == GAConfig.GENE -1)
                    temp = ipop[GenNum].substring(0,GAConfig.GENE -1) + a;
                else
                    temp = ipop[GenNum].substring(0,pos) + a + ipop[GenNum].substring(pos+1);
            }
            ipop[GenNum] = temp;
        }
    }

    public void updateBestPop()
    {

    }


    /*
    待改
     */
    public void select() throws IOException {
        double evals[] = new double[ChrNum];
        double p[] = new double[ChrNum];
        double q[] = new double[ChrNum];
        double F = 0;
        double G = 0;
        calTime++;
//        FileWriter fw = new FileWriter(fe.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fe,true),"UTF-8"));
        for(int i = 0;i<ChrNum;i++)
        {
            evals[i] = getFitness(vmTohost[i]);
            bw.write(String.valueOf(evals[i]));
            bw.write("\n");
            if(evals[i] < bestfitness)
            {
                bestfitness = evals[i];
                bestgeneration = generation;
                beststr = code(vmTohost[i]);
                Log.printLine("get best result in generation:" + this.generation);
                best = vmTohost[i].clone();
            }
            F = F + evals[i];
        }
        bw.close();
        for(int i = 0;i<ChrNum;i++)
        {
            evals[i] = F/evals[i];
            G += evals[i];
        }
        for(int i = 0;i<ChrNum;i++)
        {
            p[i] = evals[i] / G;
            if(p[i] == 0)
                p[i] = 0.001;
            if(i == 0)
                q[i] = p[i];
            else
                q[i] = q[i - 1] + p[i];
        }
        /*
        在选取的时候是否创建副本
         */
        for(int i = 0;i<ChrNum;i++)
        {
            double r = Math.random();
            if(r <= q[0])
                ipop[i] = ipop[0];
            else
            {
                for(int j = 0;j<ChrNum;j++)
                {
                    if(r < q[j])
                        ipop[i] = ipop[j];
                }
            }
        }
    }


    /*
    energy与temper相关参数待改
     */
    public Double getFitness(int[] allocate)
    {
        double energy = 0.0;
        double temper = 0.0;
//        double temppp = 0.0;
//        double utila = 0;
        if(isSuitable(allocate)) {
            for (int i = 0; i < GAConfig.VMNum; i++) {
                myhostList.get(allocate[i]).updateCpu((int) GaVmList.get(i).getMips());
            }
            for (Myhost host : myhostList) {
//                utila= getUtilization(host);
                energy += model.getPower(getUtilization(host));
                temper += host.getTemperature() / (60 - calculateTemper((double) getUtilization(host)));
//                temppp = host.getTemperature();
            }
        }
        clearHost();
        double rere = energy + temper*15;
        return  energy + temper*15;
    }

    public double getUtilization(Myhost myhost)
    {
//        int a = myhost.getTotalCpu()-myhost.getAvaliableCpu();
//        int b = myhost.getTotalCpu();
        return ((double)(myhost.getTotalCpu()-myhost.getAvaliableCpu()) / myhost.getTotalCpu());
    }
    public void clearHost()
    {
        int i = 0;
        for(Myhost host : myhostList)
        {
            host.setAvaliableCpu(host.getTotalCpu());
            host.setAvaliableRam(host.getTotalRam());
            host.setAvaliableBw(host.getTotalBw());
            host.setTemperature(initTemper[i]);
            i++;
        }
    }


    public boolean isSuitable(Vm vm,Myhost myhost)
    {
        boolean res = (vm.getBw() <= myhost.getAvaliableBw()) && (vm.getMips() <= myhost.getAvaliableCpu()) &&
                (vm.getRam() <= myhost.getAvaliableRam());
        return ((vm.getBw() <= myhost.getAvaliableBw()) && (vm.getMips() <= myhost.getAvaliableCpu()) &&
                (vm.getRam() <= myhost.getAvaliableRam()));
    }
    public boolean isSuitable(int[] vmtohost)
    {
        int[][] hostcapcity = new int[3][GAConfig.HostNum];
        for(int i = 0;i<GAConfig.VMNum;i++)
        {
            hostcapcity[0][vmtohost[i]] += GaVmList.get(i).getMips();
            hostcapcity[1][vmtohost[i]] += GaVmList.get(i).getRam();
            hostcapcity[2][vmtohost[i]] += GaVmList.get(i).getBw();
            if(hostcapcity[0][vmtohost[i]] > myhostList.get(vmtohost[i]).getTotalCpu() ||
                    hostcapcity[1][vmtohost[i]] > myhostList.get(vmtohost[i]).getTotalRam() ||
                        hostcapcity[0][vmtohost[i]] > myhostList.get(vmtohost[i]).getTotalBw())
                return false;
        }
        return true;
    }

    public double[] calculateTemper(Myhost myhost, Vm vm)
    {
        double cpuUtilization = (double) (myhost.getTotalCpu()-myhost.getAvaliableCpu()+vm.getMips())/myhost.getTotalCpu();
//        int cpuUtilization = myhost.getTotalCpu()-myhost.getAvaliableCpu()-;
        double res[] = {0,0};
        float a = (float) 32.4031331148812;
        float b = (float) -0.105871112063592;
        float c = (float) 0.019012207217;
        float d = (float) -0.000103336673855697;
        double targetTemper = a + b * cpuUtilization + Math.pow(c,2) * cpuUtilization + Math.pow(d,3) * cpuUtilization;
        res[0] = Math.abs(myhost.getTemperature()-avgTemper) + Math.abs(targetTemper - avgTemper);
        res[1] = targetTemper;
        return res;
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
        String res = "";
        int len2 = (int)Math.ceil((Math.log(GAConfig.HostNum) / Math.log(2)));
        for(int gen:pop)
        {
            temp = Integer.toBinaryString(gen);
            int len = temp.length();
            if(len < len2)
            {
                for(int i = 0;i<len2-len;i++)
                    temp = "0" + temp;
            }
            res = res + temp;
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


