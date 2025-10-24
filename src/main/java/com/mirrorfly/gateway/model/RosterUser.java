package com.mirrorfly.gateway.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.Objects;

@Entity
@Table(name = "rosterusers")
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

    public RosterUserKey getRosterUserKey() {
        return rosterUserKey;
    }

    public void setRosterUserKey(RosterUserKey rosterUserKey) {
        this.rosterUserKey = rosterUserKey;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Character getSubscription() {
        return subscription;
    }

    public void setSubscription(Character subscription) {
        this.subscription = subscription;
    }

    public Character getAsk() {
        return ask;
    }

    public void setAsk(Character ask) {
        this.ask = ask;
    }

    public String getAskmessage() {
        return askmessage;
    }

    public void setAskmessage(String askmessage) {
        this.askmessage = askmessage;
    }

    public Character getServer() {
        return server;
    }

    public void setServer(Character server) {
        this.server = server;
    }

    public String getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJidUsername() {
        return jidUsername;
    }

    public void setJidUsername(String jidUsername) {
        this.jidUsername = jidUsername;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }
}
