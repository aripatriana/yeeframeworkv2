package com.yeeframework.automate;

import com.yeeframework.automate.entry.TestCasePath;

/**
 * All of the implementation of workflow should be implemented by this class in order to test 
 * 
 * @author ari.patriana
 *
 */
public interface RunTestWorkflow {

	public void testWorkflow(TestCasePath testCasePath);
	
	public void testWorkflow();
}
