package org.cloudbus.cloudsim.examples.power.planetlab;


import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

public class createVmHost {
    public List<PowerHost> new_host;
    public List<Vm> new_vm;


    public void createHost()
    {
        List<PowerHost> new_host1 = Helper.createHostList(12);
        for(PowerHost host : new_host1)
            new_host.add(host);
    }
}
