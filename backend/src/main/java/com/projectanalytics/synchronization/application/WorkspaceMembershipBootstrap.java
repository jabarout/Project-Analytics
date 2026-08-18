package com.projectanalytics.synchronization.application;

import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceMembershipEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceMembershipRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the seed platform admin retains access to workspaces created before membership existed.
 */
@Component
public class WorkspaceMembershipBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceMembershipBootstrap.class);

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository membershipRepository;

    public WorkspaceMembershipBootstrap(
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMembershipRepository membershipRepository
    ) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findByUsername("admin").ifPresent(admin -> {
            int created = 0;
            for (var workspace : workspaceRepository.findAll()) {
                if (membershipRepository.findByWorkspaceIdAndUserId(workspace.getId(), admin.getId()).isEmpty()) {
                    membershipRepository.save(new WorkspaceMembershipEntity(
                            workspace.getId(),
                            admin.getId(),
                            true,
                            true
                    ));
                    created++;
                }
            }
            if (created > 0) {
                log.info("Backfilled workspace membership for seed admin on {} workspace(s)", created);
            }
        });
    }
}
