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

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/**
 * The main contract of {@code DbService} is the creation of Session instances
 * and transaction management. {@code DbService} is a static class with no
 * state.
 * <p>
 * 
 * A typical usage should use the following idiom:
 * 
 * <pre>
 *	EntityManager em = DbService.getInstance().getEntityManagerFactory().createEntityManager();
 *
 *	em.getTransaction().begin();
 *	//do some work
 *	em.getTransaction().commit();
 *
 *	em.close();
 * </pre>
 * 
 * For short transactions use e.g.
 * 
 * <pre>
 *	DBService.getInstance().inTransaction((em) -> em.merge(o));
 * </pre>
 */
public final class LastFmDatabaseService {
	private static final Logger LOG = LoggerFactory.getLogger(LastFmDatabaseService.class);
	private static final LastFmDatabaseService INSTANCE = new LastFmDatabaseService();
	private static final String JDBC_URL_PROP = "jakarta.persistence.jdbc.url";
	private EntityManagerFactory emf;

	/**
	 * Private constructor for security reasons
	 */
	private LastFmDatabaseService() {
		long start = System.nanoTime();
		
		String jdbcUrl = System.getProperty(JDBC_URL_PROP, "jdbc:h2:file:~/Databases/lastfm;MODE=DB2;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE");
		
		emf = Persistence.createEntityManagerFactory("de.tgmz.lastfm", Map.of(JDBC_URL_PROP, jdbcUrl));
		
		LOG.info("Startuptime database service: {} ms", (System.nanoTime() - start) / 1000000.0);
	}

	public static LastFmDatabaseService getInstance() {
		return INSTANCE;
	}

	public void inTransaction(Consumer<EntityManager> work) {
		EntityManager entityManager = emf.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			work.accept(entityManager);
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return emf;
	}
}
