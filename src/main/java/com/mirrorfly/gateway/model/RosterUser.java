package com.mirrorfly.gateway.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Data;
import java.util.Objects;

@Entity
@Table(name = "rosterusers")
@Data
public class RosterUser extends AbstractEntity<RosterUserKey> {

	@EmbeddedId
	private RosterUserKey rosterUserKey;

	@Column(name = "nick")
	private String nick;

	@Column(name = "subscription")
	private Character subscription;

	@Column(name = "ask")
	private Character ask;

	@Column(name = "askmessage")
	private String askmessage;

	@Column(name = "server")
	private Character server;

	@Column(name = "subscribe")
	private String subscribe;

	@Column(name = "type")
	private String type;

	@Column(name = "jid_username")
	private String jidUsername;
	@Column(name = "license_key")
	private String licenseKey;

	public RosterUser() {
	}

	public RosterUser(RosterUserKey rosterUserKey, Character ask, Character server, String jidUsername, String nick,
					  Character subscription, String type, String licenseKey) {
		this.rosterUserKey = rosterUserKey;
		this.ask = ask;
		this.server = server;
		this.jidUsername = jidUsername;
		this.nick = nick;
		this.subscription = subscription;
		this.type = type;
		this.licenseKey = licenseKey;
	}

	@Override
	public RosterUserKey getId() {
		return rosterUserKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RosterUser that = (RosterUser) o;
		return Objects.equals(rosterUserKey, that.rosterUserKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rosterUserKey);
	}

}
