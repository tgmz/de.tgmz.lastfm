/*********************************************************************
* Copyright (c) 25.10.2025 Thomas Zierer
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package de.tgmz.lastfm.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
public class LastFm implements Serializable {
	private static final long serialVersionUID = 4646185258762419483L;
	@Id
	@Column(length = 18)
	private String id;
	private LocalDateTime timestamp;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	private Set<LastFmArtist> artists;
	@Column(length = 2048)
	private String title;
	@ElementCollection
	private Map<LastFmTag, Integer> tags;
	@ElementCollection
	private Map<LastFm, Float> similars;
	
	public LastFm() {
		artists = new HashSet<>();
		tags = new HashMap<>();
		similars = new HashMap<>();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public Set<LastFmArtist> getArtists() {
		return artists;
	}
	public void setArtists(Set<LastFmArtist> artists) {
		this.artists = artists;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Map<LastFmTag, Integer> getTags() {
		return tags;
	}
	public void setTags(Map<LastFmTag, Integer> tags) {
		this.tags = tags;
	}
	public Map<LastFm, Float> getSimilars() {
		return similars;
	}
	public void setSimilars(Map<LastFm, Float> similars) {
		this.similars = similars;
	}

	@Override
	public String toString() {
		return "LastFm [id=" + id + ", artists=" + artists + ", title=" + title + ", tags=" + tags + "]";
	}
}
