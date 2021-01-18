package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.examples.power.RunnerAbstract;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * The example runner for the PlanetLab workload.
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
public class PlanetLabRunner extends RunnerAbstract {

	/**
	 * Instantiates a new planet lab runner.
	 * 
	 * @param enableOutput the enable output
	 * @param outputToFile the output to file
	 * @param inputFolder the input folder
	 * @param outputFolder the output folder
	 * @param workload the workload
	 * @param vmAllocationPolicy the vm allocation policy
	 * @param vmSelectionPolicy the vm selection policy
	 * @param parameter the parameter
	 */

	private List<PowerHost> new_hostList;
	public PlanetLabRunner(
			boolean enableOutput,
			boolean outputToFile,
			String inputFolder,
			String outputFolder,
			String workload,
			String vmAllocationPolicy,
			String vmSelectionPolicy,
			String parameter) {
		super(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
	}

	public PlanetLabRunner(
			boolean enableOutput,
			boolean outputToFile,
			String inputFolder,
			String outputFolder,
			String workload,
			String vmAllocationPolicy,
			String vmSelectionPolicy,
			String parameter,
			List<PowerHost> new_hostList,
			List<Vm> new_vmList) {

		super(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter,
				new_hostList,
				new_vmList);


	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.examples.power.RunnerAbstract#init(java.lang.String)
	 */
	@Override
	protected void init(String inputFolder) {
		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			broker = Helper.createBroker();
			int brokerId = broker.getId();

			cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
//			vmList = Helper.createVmList(brokerId, cloudletList.size());
//			hostList = Helper.createHostList(PlanetLabConstants.NUMBER_OF_HOSTS);
//			hostList = this.new_hostList;
//			selectSortForHost();
			printInitTemper();
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}
	}

	protected void selectSortForHost()
	{
		for(int i = 0;i < hostList.size();i++)
		{
			int max_index = i;
			double cur_cpu = hostList.get(i).getPeList().get(0).getPeProvisioner().getAvailableMips();
			for(int j = i+1;j<hostList.size();j++)
			{
				if(hostList.get(j).getPeList().get(0).getPeProvisioner().getAvailableMips() > cur_cpu)
				{
					cur_cpu = hostList.get(j).getPeList().get(0).getPeProvisioner().getAvailableMips();
					max_index = j;
				}
			}
			Collections.swap(hostList,i,max_index);
		}
	}
	protected void printInitTemper()
	{
		Log.printLine("----------------" + "init temper is" +"------------------");
		for(Host host : hostList)
		{
			Log.print(host.getTemperature() + "     ");
		}
	}

}
