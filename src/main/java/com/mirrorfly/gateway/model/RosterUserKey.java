package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RosterUserKey implements Serializable {

	@Column(name = "username")
	private String username;

	@Column(name = "jid")
	private String jid;

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RosterUserKey that = (RosterUserKey) obj;
        return username.equals(that.username) && jid.equals(that.jid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, jid);
    }

}
