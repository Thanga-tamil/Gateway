package com.mirrorfly.gateway.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "rosterusers")
@Data
public class RosterUser {

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

}
