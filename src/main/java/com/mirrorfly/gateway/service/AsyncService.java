package com.mirrorfly.gateway.service;

import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.model.RosterUser;
import com.mirrorfly.gateway.model.RosterUserKey;
import com.mirrorfly.gateway.model.UserContact;
import com.mirrorfly.gateway.repo.RosterUserRepo;
import com.mirrorfly.gateway.repo.UserContactRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AsyncService {

    @Autowired
    UserContactRepo userContactRepo;

    @Autowired
    RosterUserRepo rosterUserRepo;

    @PersistenceContext
    EntityManager entityManager;

    @Async
    @SwitchDataSource("[0]")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRosters(String userdomain, String username, String contactNo, String xmppDomain) {

        AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

        log.info("Using DB connection URL: {}", (Object) entityManager.unwrap(Session.class).doReturningWork(conn -> conn.getMetaData().getURL()));

        List<UserContact> usersContacts = userContactRepo.findByMobileNumber(contactNo);

        Set<String> usernames = usersContacts.stream().map(UserContact::getUsername).collect(Collectors.toSet());

        List<String> rosters = rosterUserRepo.findAllRosters(usernames, username + "@" + xmppDomain);

        List<RosterUser> rosterUsers = usersContacts.stream()
                .filter(contact -> !rosters.contains(contact.getUsername()))
                .map(contact -> (new RosterUser(new RosterUserKey(contact.getUsername(), username + "@" + xmppDomain), 'N', 'N', username, contact.getName(), 'B', "item", contact.getLicenseKey())))
                .toList();

        rosterUserRepo.saveAll(rosterUsers);
        userContactRepo.deleteAll(usersContacts);

    }

}
