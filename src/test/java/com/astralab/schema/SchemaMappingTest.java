package com.astralab.schema;

import com.astralab.schema.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AstraLabApplication.class)
@ActiveProfiles("test")
public class SchemaMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired
    private UserGroupMemberRepository userGroupMemberRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private DatasetVersionRepository datasetVersionRepository;
    @Autowired
    private DatasetFileRepository datasetFileRepository;
    @Autowired
    private DatasetColumnRepository datasetColumnRepository;
    @Autowired
    private IngestionJobRepository ingestionJobRepository;
    @Autowired
    private IngestionMetricsRepository ingestionMetricsRepository;
    @Autowired
    private IngestionStatusHistoryRepository ingestionStatusHistoryRepository;
    @Autowired
    private ColumnProfileRepository columnProfileRepository;
    @Autowired
    private DatasetUserAccessRepository datasetUserAccessRepository;
    @Autowired
    private DatasetTeamAccessRepository datasetTeamAccessRepository;
    @Autowired
    private DatasetGroupAccessRepository datasetGroupAccessRepository;
    @Autowired
    private DatasetOwnerHistoryRepository datasetOwnerHistoryRepository;
    @Autowired
    private DatasetTagRepository datasetTagRepository;
    @Autowired
    private DatasetTagMappingRepository datasetTagMappingRepository;
    @Autowired
    private UserDatasetFavoriteRepository userDatasetFavoriteRepository;

    @Test
    @DisplayName("Verify that all 22 JPA entities are registered in Metamodel")
    void verifyAllEntitiesRegistered() {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        Set<String> entityNames = entities.stream()
                .map(EntityType::getName)
                .collect(Collectors.toSet());

        assertThat(entityNames).containsExactlyInAnyOrder(
                "Tenant",
                "Team",
                "AppUser",
                "UserGroup",
                "UserGroupMember",
                "Workspace",
                "WorkspaceMember",
                "Dataset",
                "DatasetVersion",
                "DatasetFile",
                "DatasetColumn",
                "IngestionJob",
                "IngestionMetrics",
                "IngestionStatusHistory",
                "ColumnProfile",
                "DatasetUserAccess",
                "DatasetTeamAccess",
                "DatasetGroupAccess",
                "DatasetOwnerHistory",
                "DatasetTag",
                "DatasetTagMapping",
                "UserDatasetFavorite"
        );
    }

    @Test
    @DisplayName("Verify all 22 repositories are successfully wired")
    void verifyAllRepositoriesInjected() {
        assertThat(tenantRepository).isNotNull();
        assertThat(teamRepository).isNotNull();
        assertThat(appUserRepository).isNotNull();
        assertThat(userGroupRepository).isNotNull();
        assertThat(userGroupMemberRepository).isNotNull();
        assertThat(workspaceRepository).isNotNull();
        assertThat(workspaceMemberRepository).isNotNull();
        assertThat(datasetRepository).isNotNull();
        assertThat(datasetVersionRepository).isNotNull();
        assertThat(datasetFileRepository).isNotNull();
        assertThat(datasetColumnRepository).isNotNull();
        assertThat(ingestionJobRepository).isNotNull();
        assertThat(ingestionMetricsRepository).isNotNull();
        assertThat(ingestionStatusHistoryRepository).isNotNull();
        assertThat(columnProfileRepository).isNotNull();
        assertThat(datasetUserAccessRepository).isNotNull();
        assertThat(datasetTeamAccessRepository).isNotNull();
        assertThat(datasetGroupAccessRepository).isNotNull();
        assertThat(datasetOwnerHistoryRepository).isNotNull();
        assertThat(datasetTagRepository).isNotNull();
        assertThat(datasetTagMappingRepository).isNotNull();
        assertThat(userDatasetFavoriteRepository).isNotNull();
    }
}
