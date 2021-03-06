/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import java.io.IOException;
import java.net.Socket;

import org.jacoco.agent.rt.IExceptionLogger;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * Controller that connects to a TCP port. This controller uses the following
 * agent options:
 * <ul>
 * <li>address</li>
 * <li>port</li>
 * </ul>
 */
public class TcpClientController implements IAgentController {

	private final IExceptionLogger logger;

	private TcpConnection connection;

	private Thread worker;

	/**
	 * New controller instance.
	 * 
	 * @param logger
	 *            logger to use in case of exceptions is spawned threads
	 */
	public TcpClientController(final IExceptionLogger logger) {
		this.logger = logger;
	}

	public void startup(final AgentOptions options, final IRuntime runtime)
			throws IOException {
		final Socket socket = createSocket(options);
		connection = new TcpConnection(socket, runtime);
		connection.init();
		worker = new Thread(new Runnable() {
			public void run() {
				try {
					connection.run();
				} catch (final IOException e) {
					logger.logExeption(e);
				}
			}
		});
		worker.setName(getClass().getName());
		worker.setDaemon(true);
		worker.start();
	}

	public void shutdown() throws Exception {
		connection.close();
		worker.join();
	}

	public void writeExecutionData() throws IOException {
		connection.writeExecutionData();
	}

	/**
	 * Open a socket based on the given configuration.
	 * 
	 * @param options
	 *            address and port configuration
	 * @return opened socket
	 * @throws IOException
	 */
	protected Socket createSocket(final AgentOptions options)
			throws IOException {
		return new Socket(options.getAddress(), options.getPort());
	}

}
