package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simulation of a heterogeneous power aware data center that only applied DVFS, but no dynamic
 * optimization of the VM allocation. The adjustment of the hosts' power consumption according to
 * their CPU utilization is happening in the PowerDatacenter class.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class Dvfs {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		boolean outputToFile = false;
		String inputFolder = Dvfs.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		String workload = "20110421"; // PlanetLab workload
		String vmAllocationPolicy = "dvfs"; // DVFS policy without VM migrations
		String vmSelectionPolicy = "";
		String parameter = "";
		List<PowerHost> new_hostList = Helper.createHostList(800);
		List<Vm> new_vmList = Helper.createVmList(2, 1024);
		String policy[] = {"dvfs","wf","bf,hybrid"};
		GAS ga = new GAS(new_vmList,new_hostList);
		ga.initVmList();
		ga.initHostList();
		ga.initpop();
		Random random = new Random();
		for(int i = 0;i < 5000;i++)
            {
            	ga.select();
            	double r1 = random.nextDouble();
            	double r2 = random.nextDouble();
            	if(r1 < 0.6)
				ga.cross();
            	if(r2 <0.2)
				ga.mutation();
            	ga.generation = i;
//                Log.printLine(i + " round");
            }
//		new PlanetLabRunner(
//				enableOutput,
//				outputToFile,
//				inputFolder,
//				outputFolder,
//				workload,
//				vmAllocationPolicy,
//				vmSelectionPolicy,
//				parameter);

		for(String pol:policy)
		{
			List<PowerHost> hostlist = new ArrayList<>();
			for(PowerHost host : new_hostList)
				hostlist.add(host.clone());
			List<Vm> vmList  = new ArrayList<>();
			for(Vm vm : new_vmList)
				vmList.add(vm.clone());
			Log.printLine("----------------------" + pol + "result----------------------");
			PlanetLabRunner planetLabRunner = new PlanetLabRunner(
					enableOutput,
					outputToFile,
					inputFolder,
					outputFolder,
					workload,
					pol,
					vmSelectionPolicy,
					parameter,
					hostlist,
					vmList);
		}
	}

}
