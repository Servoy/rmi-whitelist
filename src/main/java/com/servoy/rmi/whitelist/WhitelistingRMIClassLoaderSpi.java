/*
 * Copyright (C) 2015 Servoy BV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.servoy.rmi.whitelist;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rob Gansevles
 *
 */
public class WhitelistingRMIClassLoaderSpi extends RMIClassLoaderSpi {

	public static final String CONFIG_PROPERTY = "rmi.whitelist.config";
	
	static final Logger LOGGER = LoggerFactory
			.getLogger(WhitelistingRMIClassLoaderSpi.class.getName());

	final RMIClassLoaderSpi delegate;

	private final List<String> whiteList = new ArrayList<String>();
	// Some defaults
	{
		whiteList.add("["); // array, element type will be loaded separately
		whiteList.add("java.");
		whiteList.add("javax.management."); // jmx
		whiteList.add("sun.");
	}

	private final List<String> blackList = new ArrayList<String>();

	public WhitelistingRMIClassLoaderSpi() {
		this(RMIClassLoader.getDefaultProviderInstance(), System
				.getProperty(CONFIG_PROPERTY));
	}

	public WhitelistingRMIClassLoaderSpi(RMIClassLoaderSpi delegate,
			String config) {
		this.delegate = delegate;

		// parse config
		if (config != null) {
			for (String str : config.split(":")) {
				String trimmed = str.trim();

				if (trimmed.length() > 0) {
					if (trimmed.startsWith("-") && trimmed.length() > 1) {
						blackList.add(trimmed.substring(1));
					} else {
						whiteList.add(trimmed);
					}
				}
			}
		}
	}

	private void checkWhitelist(String name) throws ClassNotFoundException {

		for (String blacklisted : blackList) {
			if (name.startsWith(blacklisted)) {
				block("Class blacklisted for RMI: " + name);
			}
		}

		for (String whiteListed : whiteList) {
			if (name.startsWith(whiteListed)) {
				return;
			}
		}

		block("Class not whitelisted for RMI: " + name);
	}

	private static void block(String message) throws ClassNotFoundException {
		LOGGER.warn(message);
		throw new ClassNotFoundException(message);
	}

	@Override
	public Class<?> loadClass(String codebase, String name,
			ClassLoader defaultLoader) throws MalformedURLException,
			ClassNotFoundException {

		checkWhitelist(name);

		return delegate.loadClass(codebase, name, defaultLoader);
	}

	@Override
	public Class<?> loadProxyClass(String codebase, String[] interfaces,
			ClassLoader defaultLoader) throws MalformedURLException,
			ClassNotFoundException {

		return delegate.loadProxyClass(codebase, interfaces, defaultLoader);
	}

	@Override
	public ClassLoader getClassLoader(String codebase)
			throws MalformedURLException {

		return delegate.getClassLoader(codebase);
	}

	@Override
	public String getClassAnnotation(Class<?> cl) {

		return delegate.getClassAnnotation(cl);
	}

}
