package com.yeeframework.automate;

/**
 * All menu should be implemented by this class if want to be managed by the system
 * 
 * @author ari.patriana
 *
 */
public interface Menu {

	public String getMenuLevel1();
	
	public String getMenuLevel2();
	
	public String getMenuLevel3();
	
	public String getId();
	
	public String getModuleId();
}
