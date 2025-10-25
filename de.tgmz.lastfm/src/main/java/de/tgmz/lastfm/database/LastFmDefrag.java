/*********************************************************************
* Copyright (c) 25.10.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.lastfm.database;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;

public class LastFmDefrag {
	private static final Logger LOG = LoggerFactory.getLogger(LastFmDefrag.class);
	
	public void run() {
        try (EntityManager em = LastFmDatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
        	em.runWithConnection((Connection conn) -> {
        		if (conn.getMetaData().getURL().startsWith("jdbc:h2:file")) {
        			LOG.info("Begin database defrag");
        			
        			conn.prepareCall("SHUTDOWN DEFRAG").execute();
        			
        			LOG.info("End database defrag");
        		}
        	});
        }
	}
}
