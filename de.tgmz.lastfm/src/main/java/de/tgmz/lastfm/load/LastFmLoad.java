/*********************************************************************
* Copyright (c) 25.10.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.lastfm.load;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tgmz.lastfm.database.LastFmDefrag;
import de.tgmz.lastfm.database.LastFmDatabaseService;
import de.tgmz.lastfm.model.LastFm;
import de.tgmz.lastfm.model.LastFmArtist;
import de.tgmz.lastfm.model.LastFmTag;
import jakarta.persistence.EntityManager;

public class LastFmLoad {
	private static final Logger LOG = LoggerFactory.getLogger(LastFmLoad.class);
	
	private static final int DEFRAG_THRESHOLD = 100_000;

	private static final class JSONLFM {
		public String artist;
		public String timestamp;
		public String track_id;
		public String title;
		public String[][] tags;
		public String[][] similars;
	}

	private ObjectMapper mapper;
	
	private LastFmDefrag defrag;
	
	private int logThreshold = 10_000;
	
	public LastFmLoad() {
		mapper = new ObjectMapper();
		
		defrag = new LastFmDefrag();
	}
	
	public static void main(String[] args) throws IOException {
		LastFmLoad load = new LastFmLoad();

		load.run(args[0]);
	}

	public void run(String zip) throws IOException {
		this.load(zip);
		
		this.defrag();
		
		this.loadSimilars(zip);
		
		this.defrag();
	}
	
	private void load(String zip) throws IOException {
		long count = 0;
		
		try (ZipFile zipFile = new ZipFile(zip);
			EntityManager em = LastFmDatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    
		    while (entries.hasMoreElements()) {
		        ZipEntry entry = entries.nextElement();
		        // Check if entry is a directory
		        if (!entry.isDirectory()) {
		            try (InputStream is = zipFile.getInputStream(entry)) {
		    			JSONLFM jlfm = mapper.readValue(is, JSONLFM.class);

		    			LastFm lfm = new LastFm();

		    			Arrays.stream(jlfm.artist.split(";")).forEach(x -> lfm.getArtists().add(getArtist(em, StringUtils.left(x, 255))));
		    			
		    			lfm.setId(jlfm.track_id);
		    			lfm.setTimestamp(LocalDateTime.ofInstant(Timestamp.valueOf(jlfm.timestamp).toInstant(), ZoneId.systemDefault()));
		    			lfm.setTitle(jlfm.title);
		    			
		    			Arrays.stream(jlfm.tags).forEach(s -> lfm.getTags().put(getTag(em, StringUtils.left(capitalize(s[0]), 255)), Integer.parseInt(s[1])));
		    		
		    			LastFmDatabaseService.getInstance().inTransaction(x -> x.merge(lfm));
		    			
		    			if (++count % logThreshold == 0 && LOG.isErrorEnabled()) {
		    				LOG.info("Processed {}. {}", String.format("%,d", count), lfm);
		    			}
		            }
		        }
		    }
		}
		
		if (LOG.isErrorEnabled()) {
			LOG.info("{} entities saved", String.format("%,d", count));
		}
	}
	
	private void loadSimilars(String zip) throws IOException {
		long count = 0;
		
		try (ZipFile zipFile = new ZipFile(zip);
				EntityManager em = LastFmDatabaseService.getInstance().getEntityManagerFactory().createEntityManager()) {
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    
		    while (entries.hasMoreElements()) {
		        ZipEntry entry = entries.nextElement();
		        // Check if entry is a directory
		        if (!entry.isDirectory()) {
		            try (InputStream is = zipFile.getInputStream(entry)) {
		            	JSONLFM jlfm = mapper.readValue(is, JSONLFM.class);

		    			LastFm lfm = em.find(LastFm.class, jlfm.track_id);

		    			Arrays.stream(jlfm.similars).forEach(s -> lfm.getSimilars().put(em.find(LastFm.class, s[0]), Float.parseFloat(s[1])));
		    			
		    			lfm.getSimilars().entrySet().removeIf(e -> e.getKey() == null);
		    			
		    			LastFmDatabaseService.getInstance().inTransaction(x -> x.merge(lfm));
	    				
		    			if (++count % logThreshold == 0 && LOG.isErrorEnabled()) {
		    				LOG.info("Processed {}. {}", String.format("%,d", count), lfm);
		    			}
		    			
		    			if (count % DEFRAG_THRESHOLD == DEFRAG_THRESHOLD - 1) {
		    				defrag();
		    			}
		            }
		        }
		    }
		}
		
		if (LOG.isErrorEnabled()) {
			LOG.info(" {} entities saved", String.format("%,d", count));
		}
	}
	
	private void defrag() {
		defrag.run();
	}

	private LastFmArtist getArtist(EntityManager em, String key) {
		LastFmArtist lfa = em.find(LastFmArtist.class, key);
		
		if (lfa == null) {
			lfa = new LastFmArtist(key);
		}
		
		return lfa;
	}
	
	private LastFmTag getTag(EntityManager em, String key) {
		LastFmTag[] lft = new LastFmTag[] {em.find(LastFmTag.class, key)};
		
		if (lft[0] == null) {
			lft[0] = new LastFmTag(key);
			
			LastFmDatabaseService.getInstance().inTransaction(x -> x.persist(lft[0]));
		}
		
		return lft[0];
	}
	
	private static String capitalize(String s) {
		StringJoiner sj0 = new StringJoiner(" ");
		
		Arrays.stream(s.split("\\s")).forEach(x -> sj0.add(StringUtils.capitalize(x)));
		
		StringJoiner sj1 = new StringJoiner("-");
		
		Arrays.stream(sj0.toString().split("\\-")).forEach(x -> sj1.add(StringUtils.capitalize(x)));
		
		return sj1.toString();
	}

	public void setLogThreshold(int logThreshold) {
		this.logThreshold = logThreshold;
	}
}
