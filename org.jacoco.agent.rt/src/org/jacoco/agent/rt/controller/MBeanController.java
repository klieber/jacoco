/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * Controller that registers MBean. This controller does not use agent options.
 */
public class MBeanController implements IAgentController, IRuntimeMBean {

	private static final String OBJECT_NAME = "org.jacoco:type=Runtime";

	private IRuntime runtime;

	public void startup(final AgentOptions options, final IRuntime runtime)
			throws Exception {
		this.runtime = runtime;
		ManagementFactory.getPlatformMBeanServer().registerMBean(
				new StandardMBean(this, IRuntimeMBean.class),
				new ObjectName(OBJECT_NAME));
	}

	public void shutdown() throws Exception {
		ManagementFactory.getPlatformMBeanServer().unregisterMBean(
				new ObjectName(OBJECT_NAME));
	}

	public void writeExecutionData() throws Exception {
		// nothing to do
	}

	// === IRuntimeMBean ===

	public String getVersion() {
		return JaCoCo.VERSION;
	}

	public String getSessionId() {
		return runtime.getSessionId();
	}

	public void setSessionId(final String id) {
		runtime.setSessionId(id);
	}

	public byte[] dump(final boolean reset) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final ExecutionDataWriter writer = new ExecutionDataWriter(output);
		runtime.collect(writer, writer, reset);
		return output.toByteArray();
	}

	public void reset() {
		runtime.reset();
	}

}
