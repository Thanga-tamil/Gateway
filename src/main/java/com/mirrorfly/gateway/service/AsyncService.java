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

        List<RosterUser> rosterUsers = usersContacts.stream()
                .map(contact -> {
                    RosterUserKey key = new RosterUserKey(contact.getUsername(), username + "@" + xmppDomain);
                    // Exclude Existing Records Using "UNIQUE KEY i_rosteru_sh_user_jid".. ( RosterUserKey )
                    return rosterUserRepo.findById(key).orElse(new RosterUser(key, 'N', 'N', username, contact.getName(), 'B', "item", contact.getLicenseKey()));
                }).toList();

        rosterUserRepo.saveAll(rosterUsers);
        userContactRepo.deleteAll(usersContacts);

    }

}
