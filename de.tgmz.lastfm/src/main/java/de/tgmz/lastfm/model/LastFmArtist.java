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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class LastFmArtist implements Serializable {
	private static final long serialVersionUID = 7914215193257573284L;
	@Id
	private String name;
	
	public LastFmArtist(String key) {
		this();
		
		this.name = key;
	}
	
	public LastFmArtist() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "LastFmArtist [name=" + name + "]";
	}
}
