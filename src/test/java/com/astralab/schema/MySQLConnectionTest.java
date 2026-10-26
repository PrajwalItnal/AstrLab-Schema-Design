package com.astralab.schema;

import com.astralab.schema.entity.*;
import com.astralab.schema.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AstraLabApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:mysql://localhost:3306/astralab?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
    "spring.datasource.username=root",
    "spring.datasource.password=Prajwal@2005",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
    "spring.jpa.hibernate.ddl-auto=update"
})
public class MySQLConnectionTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Test
    @DisplayName("Verify connection to MySQL and verify all 22 tables exist in astralab database")
    void testMySQLConnectionAndTableCreation() {
        @SuppressWarnings("unchecked")
        List<String> tableNames = entityManager.createNativeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'astralab'"
        ).getResultList();

        System.out.println("Discovered " + tableNames.size() + " tables in MySQL astralab database: " + tableNames);

        assertThat(tableNames).contains(
                "tenant",
                "team",
                "app_user",
                "user_group",
                "user_group_member",
                "workspace",
                "workspace_member",
                "dataset",
                "dataset_version",
                "dataset_file",
                "dataset_column",
                "ingestion_job",
                "ingestion_metrics",
                "ingestion_status_history",
                "column_profile",
                "dataset_user_access",
                "dataset_team_access",
                "dataset_group_access",
                "dataset_owner_history",
                "dataset_tag",
                "dataset_tag_mapping",
                "user_dataset_favorite"
        );
    }

    @Test
    @DisplayName("Verify basic CRUD operations on MySQL")
    void testBasicCRUDOnMySQL() {
        // 1. Create Tenant
        Tenant tenant = new Tenant("tenant-test-001");
        tenant.setName("Test Tenant");
        tenant.setPlan("Enterprise");
        tenant.setEnvironment("Staging");
        tenant.setCreatedAt(OffsetDateTime.now());
        tenantRepository.save(tenant);

        assertThat(tenantRepository.existsById("tenant-test-001")).isTrue();

        // Clean up
        tenantRepository.delete(tenant);
        assertThat(tenantRepository.existsById("tenant-test-001")).isFalse();
    }
}
