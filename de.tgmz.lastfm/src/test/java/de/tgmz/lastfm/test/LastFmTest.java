/*********************************************************************
* Copyright (c) 02.02.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.lastfm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tgmz.lastfm.database.LastFmDatabaseService;
import de.tgmz.lastfm.load.LastFmLoad;
import de.tgmz.lastfm.model.LastFm;
import de.tgmz.lastfm.model.LastFmTag;
import jakarta.persistence.EntityManager;

public class LastFmTest {
	private static final String JDBC_DATA_DIR = System.getProperty("java.io.tmpdir") + File.separatorChar + "lastfm_test";
	private static final String JDBC_DATA_FILE = JDBC_DATA_DIR + File.separatorChar + "lastfm";
	private static final String JDBC_PROTOCOL = "jdbc:h2:file:";
	private static final String JDBC_PROPERTIES = ";MODE=DB2;DEFAULT_NULL_ORDERING=HIGH";
	private static final String JDBC_URL = JDBC_PROTOCOL + JDBC_DATA_FILE + JDBC_PROPERTIES;

	@BeforeClass
	public static void setupOnce() {
		FileUtils.deleteQuietly(new File(JDBC_DATA_DIR));
		
		System.setProperty("jakarta.persistence.jdbc.url", JDBC_URL);
	}
	
	@AfterClass
	public static void teardownOnce() {
		FileUtils.deleteQuietly(new File(JDBC_DATA_DIR));
	}
	
	@Before
	public void testRun() throws IOException {
		LastFmLoad lfl = new LastFmLoad();
		lfl.setLogThreshold(1);
		
		URL url = this.getClass().getClassLoader().getResource("lastfm_minimal.zip");

		lfl.run(url.getPath());
	}
	
	@Test
	public void testTRAAAAW128F429D538() {
		try (EntityManager em = LastFmDatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
			LastFm lfm = em.find(LastFm.class, "TRAAAAW128F429D538");
		
			assertEquals("I Didn't Mean To", lfm.getTitle());
			assertTrue(lfm.getArtists().stream().anyMatch(a -> "Casual".equals(a.getName())));

			LastFm similar = em.find(LastFm.class, "TRABACN128F425B784");
		
			assertEquals(0.871737, lfm.getSimilars().get(similar).floatValue(), 0.000001f);
		
			LastFmTag lft = em.find(LastFmTag.class, "Hip-Hop");
		
			assertEquals(50, lfm.getTags().get(lft).intValue());
		}
	}
}
