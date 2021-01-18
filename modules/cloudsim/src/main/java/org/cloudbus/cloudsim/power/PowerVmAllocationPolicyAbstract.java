/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;



/**
 * An abstract power-aware VM allocation policy.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerVmAllocationPolicyAbstract extends VmAllocationPolicy {

	/** The map map where each key is a VM id and
         * each value is the host where the VM is placed. */
	private final Map<String, Host> vmTable = new HashMap<String, Host>();
	private final float a = (float) 32.4031331148812;
	private final float b = (float) -0.105871112063592;
	private final float c = (float) 0.019012207217;
	private final float d = (float) -0.000103336673855697;
	/**
	 * Instantiates a new PowerVmAllocationPolicyAbstract.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicyAbstract(List<? extends Host> list, String name) {
		super(list, name);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		if(this.getPolicyName().equals("bf"))
			return allocateHostForVm(vm, findHostForVm_bf(vm));
		else if(this.getPolicyName().equals("wf"))
			return allocateHostForVm(vm,findHostForVm_wf(vm));
		else if(this.getPolicyName().equals("dvfs"))
			return allocateHostForVm(vm, findHostForVm(vm));
		else
//			return allocateHostForVm(vm,findHostForVm_hybrid())
		return allocateHostForVm(vm,findHostForVm(vm));
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host == null) {
			Log.formatLine("%.2f: No suitable host found for VM #" + vm.getId() + "\n", CloudSim.clock());
			return false;
		}
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			update_temper(host);
			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of VM #" + vm.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());
		return false;
	}
	public void update_temper(Host host)
	{
		float cur_cpu = (float) (1 - (host.getAvailableMips()/host.getTotalMips()));
		host.setTemperature(a + b * cur_cpu + c * cur_cpu * cur_cpu + d * cur_cpu * cur_cpu * cur_cpu);
	}
	/**
	 * Finds the first host that has enough resources to host a given VM.
	 * 
	 * @param vm the vm to find a host for it
	 * @return the first host found that can host the VM
	 */
	public PowerHost findHostForVm(Vm vm) {
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(vm)) {
				return host;
			}
		}
		return null;
	}

	public PowerHost findHostForVm_bf(Vm vm)
	{
		selectSmallSortForHost();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(vm)) {
				return host;
			}
		}
		return null;
	}
	public PowerHost findHostForVm_wf(Vm vm)
	{
		selectBigSortForHost();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(vm)) {
				return host;
			}
		}
		return null;
	}
//	public PowerHost findHostForVm_hybrid()
//	{
//
//	}

	protected void selectBigSortForHost()
	{
		for(int i = 0;i < this.getHostList().size();i++)
		{
			int max_index = i;
			double cur_cpu = this.getHostList().get(i).getPeList().get(0).getPeProvisioner().getAvailableMips();
			for(int j = i+1;j<this.getHostList().size();j++)
			{
				if(this.getHostList().get(j).getPeList().get(0).getPeProvisioner().getAvailableMips() > cur_cpu)
				{
					cur_cpu = this.getHostList().get(j).getPeList().get(0).getPeProvisioner().getAvailableMips();
					max_index = j;
				}
			}
			Collections.swap(this.getHostList(),i,max_index);
		}
	}

	protected void selectSmallSortForHost()
	{
		for(int i = 0;i < this.getHostList().size();i++)
		{
			int min_index = i;
			double cur_cpu = this.getHostList().get(i).getPeList().get(0).getPeProvisioner().getAvailableMips();
			for(int j = i+1;j<this.getHostList().size();j++)
			{
				if(this.getHostList().get(j).getPeList().get(0).getPeProvisioner().getAvailableMips() < cur_cpu)
				{
					cur_cpu = this.getHostList().get(j).getPeList().get(0).getPeProvisioner().getAvailableMips();
					min_index = j;
				}
			}
			Collections.swap(this.getHostList(),i,min_index);
		}
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

}
