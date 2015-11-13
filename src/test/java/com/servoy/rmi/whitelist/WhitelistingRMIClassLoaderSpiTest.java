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

import static org.junit.Assert.assertSame;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for WhitelistingRMIClassLoaderSpi.
 */
@RunWith(value = MockitoJUnitRunner.class)
public class WhitelistingRMIClassLoaderSpiTest {

	@Mock
	private RMIClassLoaderSpi delegateMock;

	@After
	public void tearDown() {
		System.clearProperty(WhitelistingRMIClassLoaderSpi.CONFIG_PROPERTY);
	}

	@Test
	public void shouldAllowStandardClassesByDefault() throws Exception {

		WhitelistingRMIClassLoaderSpi underTest = new WhitelistingRMIClassLoaderSpi(
				delegateMock, null);

		underTest.loadClass(null, "java.lang.String", null);
		underTest.loadClass(null, "javax.management.Descriptor", null);

		Mockito.verify(delegateMock).loadClass(null, "java.lang.String", null);
		Mockito.verify(delegateMock).loadClass(null,
				"javax.management.Descriptor", null);

		Mockito.verifyNoMoreInteractions(delegateMock);
	}

	@Test
	public void shouldAllowArrays() throws Exception {

		new WhitelistingRMIClassLoaderSpi(delegateMock, null).loadClass(null,
				"[Lcom.servoy.Test;", null);

		Mockito.verify(delegateMock)
				.loadClass(null, "[Lcom.servoy.Test;", null);

		Mockito.verifyNoMoreInteractions(delegateMock);
	}

	@Test(expected = ClassNotFoundException.class)
	public void shouldBlockUnknown() throws Exception {

		try {
			new WhitelistingRMIClassLoaderSpi(delegateMock, null).loadClass(
					null, "com.servoy.Test", null);
		} finally {
			Mockito.verifyNoMoreInteractions(delegateMock);
		}
	}

	@Test(expected = ClassNotFoundException.class)
	public void shouldBlockBlacklisted() throws Exception {

		try {
			new WhitelistingRMIClassLoaderSpi(delegateMock,
					"com.servoy.:-javax.management.").loadClass(null,
					"javax.management.Descriptor", null);
		} finally {
			Mockito.verifyNoMoreInteractions(delegateMock);
		}
	}

	@Test
	public void shouldAllowWhitelisted() throws Exception {

		new WhitelistingRMIClassLoaderSpi(delegateMock, "com.servoy.:-javax.")
				.loadClass(null, "com.servoy.Test", null);

		Mockito.verify(delegateMock).loadClass(null, "com.servoy.Test", null);

		Mockito.verifyNoMoreInteractions(delegateMock);
	}

	@Test(expected = ClassNotFoundException.class)
	public void shouldBlockSpecifInWhitelisted() throws Exception {

		WhitelistingRMIClassLoaderSpi underTest = new WhitelistingRMIClassLoaderSpi(
				delegateMock, "com.a.b.:-com.a.b.c.");

		underTest.loadClass(null, "com.a.b.f.Test", null);

		try {
			underTest.loadClass(null, "com.a.b.c.Test", null);
		} finally {
			Mockito.verify(delegateMock)
					.loadClass(null, "com.a.b.f.Test", null);
			Mockito.verifyNoMoreInteractions(delegateMock);
		}
	}

	@Test
	public void shouldUseRMIDefaultProviderInstance() throws Exception {

		assertSame(RMIClassLoader.getDefaultProviderInstance(),
				new WhitelistingRMIClassLoaderSpi().delegate);
	}

	@Test(expected = ClassNotFoundException.class)
	public void shouldUseSystemPropertyForConfigure() throws Exception {

		System.setProperty(WhitelistingRMIClassLoaderSpi.CONFIG_PROPERTY,
				"-java.");

		try {
			new WhitelistingRMIClassLoaderSpi().loadClass(null,
					"java.lang.String", null);
		} finally {
			Mockito.verifyNoMoreInteractions(delegateMock);
		}
	}

	@Test
	public void shouldDeletegaGetClassAnnotation() throws Exception {

		new WhitelistingRMIClassLoaderSpi(delegateMock, null)
				.getClassAnnotation(java.lang.String.class);

		Mockito.verify(delegateMock).getClassAnnotation(java.lang.String.class);

		Mockito.verifyNoMoreInteractions(delegateMock);
	}

	@Test
	public void shoulDeletegaGetClassLoader() throws Exception {

		new WhitelistingRMIClassLoaderSpi(delegateMock, null)
				.getClassLoader("codebase");

		Mockito.verify(delegateMock).getClassLoader("codebase");

		Mockito.verifyNoMoreInteractions(delegateMock);
	}

	@Test
	public void shouldDeletegaLoadProxyClass() throws Exception {

		new WhitelistingRMIClassLoaderSpi(delegateMock, null).loadProxyClass(
				"codebase", new String[] { "java.util.Map" }, null);

		Mockito.verify(delegateMock).loadProxyClass("codebase",
				new String[] { "java.util.Map" }, null);

		Mockito.verifyNoMoreInteractions(delegateMock);
	}

}
