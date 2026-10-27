package com.astralab.schema.config;

import com.astralab.schema.dto.ColumnSchemaDto;
import com.astralab.schema.dto.InferredSchemaResult;
import com.astralab.schema.entity.*;
import com.astralab.schema.repository.*;
import com.astralab.schema.service.SchemaInferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.dummy-data.enabled", havingValue = "true", matchIfMissing = true)
public class DatasetDummyDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatasetDummyDataInitializer.class);

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final DatasetColumnRepository datasetColumnRepository;
    private final AppUserRepository appUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TenantRepository tenantRepository;
    private final SchemaInferenceService schemaInferenceService;
    private final Path storageDirectory;

    public DatasetDummyDataInitializer(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetFileRepository datasetFileRepository,
            DatasetColumnRepository datasetColumnRepository,
            AppUserRepository appUserRepository,
            WorkspaceRepository workspaceRepository,
            TenantRepository tenantRepository,
            SchemaInferenceService schemaInferenceService,
            @Value("${app.storage.upload-dir:./data/uploads}") String uploadDir) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.datasetColumnRepository = datasetColumnRepository;
        this.appUserRepository = appUserRepository;
        this.workspaceRepository = workspaceRepository;
        this.tenantRepository = tenantRepository;
        this.schemaInferenceService = schemaInferenceService;
        this.storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (datasetRepository.count() > 0) {
            log.info("Datasets already exist in database (count: {}). Skipping dummy data initialization.", datasetRepository.count());
            return;
        }

        log.info("No datasets detected in database. Initializing AstraLab dummy datasets and sample files for Insomnia testing...");

        try {
            Files.createDirectories(this.storageDirectory);

            OffsetDateTime now = OffsetDateTime.now();

            // 1. Seed Tenant
            Tenant tenant = tenantRepository.findById("tenant_astralab_global").orElseGet(() -> {
                Tenant t = new Tenant("tenant_astralab_global");
                t.setName("AstraLab Global Corporation");
                t.setPlan("ENTERPRISE");
                t.setEnvironment("PRODUCTION");
                t.setCreatedAt(now);
                return tenantRepository.save(t);
            });

            // 2. Seed Users
            AppUser sarahUser = createOrGetUser("user_sarah", "sarah_analyst", "kc_sarah_analyst", "sarah.connor@astralab.io", "Sarah Connor", "Senior Data Analyst", "Analytics", tenant, now);
            AppUser alexUser = createOrGetUser("user_alex", "alex_engineer", "kc_alex_engineer", "alex.rivera@astralab.io", "Alex Rivera", "Lead Data Engineer", "Data Platform", tenant, now);
            AppUser davidUser = createOrGetUser("user_david", "david_finance", "kc_david_finance", "david.kim@astralab.io", "David Kim", "Financial Analyst", "Finance", tenant, now);

            // 3. Seed Workspaces
            Workspace wsAnalytics = createOrGetWorkspace("ws_analytics", "Marketing & Customer Analytics", "Enterprise business analytics workspace", tenant, sarahUser, now);
            Workspace wsFinance = createOrGetWorkspace("ws_finance", "Finance & Settlements", "Transactional audits and ledger reconciliation", tenant, davidUser, now);
            Workspace wsIot = createOrGetWorkspace("ws_iot_platform", "IoT Smart Platform", "Real-time edge sensor telemetry and ingestion", tenant, alexUser, now);

            // 4. Seed Dataset 1: Customer Demographics (Zone: gold, Owner: sarah_analyst)
            String customersCsv = """
                    customer_id,first_name,last_name,email,country,tier,loyalty_points,account_balance,created_date,is_active
                    1001,John,Doe,john.doe@example.com,USA,PLATINUM,4520,12450.50,2023-01-15,true
                    1002,Jane,Smith,jane.smith@example.co.uk,UK,GOLD,3210,8750.25,2023-02-20,true
                    1003,Carlos,Santana,carlos.s@example.es,Spain,SILVER,1850,3420.00,2023-03-12,true
                    1004,Aki,Tanaka,aki.tanaka@example.jp,Japan,PLATINUM,5100,15800.75,2023-04-05,true
                    1005,Elena,Rostova,elena.r@example.de,Germany,BRONZE,650,980.10,2023-05-18,false
                    1006,Marcus,Vance,marcus.v@example.com,USA,GOLD,2980,6420.30,2023-06-22,true
                    1007,Priya,Sharma,priya.sharma@example.in,India,PLATINUM,6200,21050.00,2023-07-09,true
                    1008,Lucas,Dubois,lucas.d@example.fr,France,SILVER,1400,2890.45,2023-08-14,true
                    1009,Chloe,Martin,chloe.m@example.ca,Canada,BRONZE,820,1150.00,2023-09-30,true
                    1010,Sven,Lindqvist,sven.l@example.se,Sweden,GOLD,3450,9200.80,2023-10-11,true
                    1011,Amara,Okonkwo,amara.o@example.ng,Nigeria,SILVER,1990,4120.60,2023-11-04,true
                    1012,Mateo,Hernandez,mateo.h@example.mx,Mexico,GOLD,2750,5600.00,2023-12-19,true
                    """;
            seedDataset(
                    "ds_customers_demo",
                    "Global Customer Demographics",
                    "Enterprise demographic profile dataset with customer tiers, lifetime value, and regions.",
                    "gold",
                    sarahUser,
                    wsAnalytics,
                    "customers_demo.csv",
                    customersCsv,
                    now
            );

            // 5. Seed Dataset 2: E-Commerce Transactions (Zone: silver, Owner: david_finance)
            String ordersCsv = """
                    order_id,customer_id,customer_name,product_category,unit_price,quantity,total_amount,order_status,payment_method,order_date
                    ORD-5001,1001,John Doe,Electronics,899.99,1,899.99,DELIVERED,CREDIT_CARD,2026-04-01
                    ORD-5002,1002,Jane Smith,Home & Kitchen,149.50,2,299.00,SHIPPED,PAYPAL,2026-04-02
                    ORD-5003,1003,Carlos Santana,Books,29.99,3,89.97,DELIVERED,DEBIT_CARD,2026-04-02
                    ORD-5004,1004,Aki Tanaka,Fashion,220.00,1,220.00,PROCESSING,APPLE_PAY,2026-04-03
                    ORD-5005,1006,Marcus Vance,Sports,85.00,2,170.00,DELIVERED,CREDIT_CARD,2026-04-03
                    ORD-5006,1007,Priya Sharma,Electronics,1299.00,1,1299.00,DELIVERED,WIRE_TRANSFER,2026-04-04
                    ORD-5007,1008,Lucas Dubois,Automotive,45.50,4,182.00,CANCELLED,CREDIT_CARD,2026-04-04
                    ORD-5008,1010,Sven Lindqvist,Home & Kitchen,310.00,1,310.00,DELIVERED,KLARNA,2026-04-05
                    ORD-5009,1011,Amara Okonkwo,Fashion,175.25,2,350.50,SHIPPED,DEBIT_CARD,2026-04-05
                    ORD-5010,1012,Mateo Hernandez,Sports,65.00,3,195.00,DELIVERED,CASH_ON_DELIVERY,2026-04-06
                    ORD-5011,1001,John Doe,Books,19.99,5,99.95,DELIVERED,CREDIT_CARD,2026-04-06
                    ORD-5012,1007,Priya Sharma,Fashion,450.00,1,450.00,PROCESSING,CREDIT_CARD,2026-04-07
                    """;
            seedDataset(
                    "ds_ecommerce_orders",
                    "E-Commerce Transaction Stream",
                    "Daily transaction logs including product categories, order values, and fulfillment states.",
                    "silver",
                    davidUser,
                    wsFinance,
                    "ecommerce_orders.csv",
                    ordersCsv,
                    now
            );

            // 6. Seed Dataset 3: Factory IoT Telemetry (Zone: landing, Owner: alex_engineer)
            String telemetryCsv = """
                    sensor_id,device_model,facility,temperature_celsius,humidity_pct,vibration_hz,pressure_bar,status_code,timestamp
                    SN-901,AstraSense-X1,Facility-North,24.5,45.2,12.4,1.013,200,2026-04-10 08:00:00
                    SN-902,AstraSense-X1,Facility-North,25.1,46.0,14.1,1.015,200,2026-04-10 08:05:00
                    SN-903,AstraSense-X2,Facility-East,68.4,15.8,42.8,2.350,301,2026-04-10 08:10:00
                    SN-901,AstraSense-X1,Facility-North,24.8,45.5,12.9,1.014,200,2026-04-10 08:15:00
                    SN-904,AstraSense-Pro,Facility-West,31.2,52.3,18.6,1.050,200,2026-04-10 08:20:00
                    SN-902,AstraSense-X1,Facility-North,26.0,46.8,15.0,1.018,200,2026-04-10 08:25:00
                    SN-903,AstraSense-X2,Facility-East,72.1,14.2,49.2,2.410,500,2026-04-10 08:30:00
                    SN-905,AstraSense-Pro,Facility-South,22.9,40.1,9.8,1.008,200,2026-04-10 08:35:00
                    SN-904,AstraSense-Pro,Facility-West,31.7,53.0,19.1,1.052,200,2026-04-10 08:40:00
                    SN-901,AstraSense-X1,Facility-North,25.3,45.8,13.2,1.014,200,2026-04-10 08:45:00
                    """;
            seedDataset(
                    "ds_sensor_telemetry",
                    "Factory IoT Edge Telemetry",
                    "High-frequency streaming IoT sensor readings: temperature, humidity, pressure, and vibration.",
                    "landing",
                    alexUser,
                    wsIot,
                    "sensor_telemetry.csv",
                    telemetryCsv,
                    now
            );

            log.info("Successfully seeded AstraLab dummy datasets! Ready for testing via Insomnia.");
            log.info("Available Dataset IDs: [ds_customers_demo, ds_ecommerce_orders, ds_sensor_telemetry]");

        } catch (Exception e) {
            log.error("Failed to seed dummy data: {}", e.getMessage(), e);
        }
    }

    private void seedDataset(String datasetId,
                             String name,
                             String description,
                             String zone,
                             AppUser owner,
                             Workspace workspace,
                             String filename,
                             String csvContent,
                             OffsetDateTime now) throws IOException {

        // 1. Create target folder and file on disk
        Path datasetDir = this.storageDirectory.resolve(datasetId).normalize();
        Files.createDirectories(datasetDir);
        Path targetFile = datasetDir.resolve(filename).normalize();
        Files.writeString(targetFile, csvContent);

        long sizeBytes = Files.size(targetFile);
        String checksum = computeSha256(targetFile);
        String relativePath = this.storageDirectory.relativize(targetFile).toString().replace('\\', '/');

        // 2. Infer Schema automatically via SchemaInferenceService
        InferredSchemaResult inferred = schemaInferenceService.inferSchemaAndSample(targetFile, "CSV", 10);

        // 3. Persist Dataset
        Dataset dataset = new Dataset(datasetId);
        dataset.setName(name);
        dataset.setDescription(description);
        dataset.setZone(zone);
        dataset.setOwnerUser(owner);
        dataset.setWorkspace(workspace);
        dataset.setStatus("ACTIVE");
        dataset.setVisibility("INTERNAL");
        dataset.setCurrentVersion(1);
        dataset.setCreatedAt(now);
        dataset.setUpdatedAt(now);
        datasetRepository.save(dataset);

        // 4. Persist DatasetVersion
        String versionId = "ver_" + datasetId.replace("ds_", "");
        DatasetVersion version = new DatasetVersion(versionId);
        version.setDataset(dataset);
        version.setVersionNumber(1);
        version.setStatus("AVAILABLE");
        version.setSchemaHash(checksum);
        version.setCreatedByUser(owner);
        version.setCreatedAt(now);
        datasetVersionRepository.save(version);

        // 5. Persist DatasetFile
        String fileId = "file_" + datasetId.replace("ds_", "");
        DatasetFile datasetFile = new DatasetFile(fileId);
        datasetFile.setDatasetVersion(version);
        datasetFile.setUploadedByUser(owner);
        datasetFile.setOriginalFilename(filename);
        datasetFile.setFileFormat("CSV");
        datasetFile.setMimeType("text/csv");
        datasetFile.setFileSizeBytes(sizeBytes);
        datasetFile.setStorageBucket("local");
        datasetFile.setObjectKey(relativePath);
        datasetFile.setChecksum(checksum);
        datasetFile.setChecksumAlgorithm("SHA-256");
        datasetFile.setUploadedAt(now);
        datasetFileRepository.save(datasetFile);

        // 6. Persist DatasetColumns
        List<DatasetColumn> columns = new ArrayList<>();
        int colIndex = 1;
        for (ColumnSchemaDto colDto : inferred.getColumns()) {
            String colId = "col_" + datasetId.replace("ds_", "") + "_" + (colIndex++);
            DatasetColumn col = new DatasetColumn(colId);
            col.setDatasetVersion(version);
            col.setColumnName(colDto.getColumnName());
            col.setLogicalDataType(colDto.getLogicalDataType());
            col.setSourceDataType(colDto.getSourceDataType());
            col.setOrdinalPosition(colDto.getOrdinalPosition());
            col.setNullable(colDto.getNullable());
            col.setFormatPattern(colDto.getFormatPattern());
            col.setCreatedAt(now);
            columns.add(col);
        }
        datasetColumnRepository.saveAll(columns);
    }

    private AppUser createOrGetUser(String id, String username, String keycloakId, String email, String displayName, String designation, String department, Tenant tenant, OffsetDateTime now) {
        return appUserRepository.findByUsername(username).orElseGet(() -> {
            AppUser u = new AppUser(id);
            u.setUsername(username);
            u.setKeycloakId(keycloakId);
            u.setEmail(email);
            u.setDisplayName(displayName);
            u.setDesignation(designation);
            u.setDepartment(department);
            u.setStatus("ACTIVE");
            u.setTenant(tenant);
            u.setCreatedAt(now);
            return appUserRepository.save(u);
        });
    }

    private Workspace createOrGetWorkspace(String id, String name, String description, Tenant tenant, AppUser createdBy, OffsetDateTime now) {
        return workspaceRepository.findById(id).orElseGet(() -> {
            Workspace ws = new Workspace(id);
            ws.setName(name);
            ws.setDescription(description);
            ws.setStatus("ACTIVE");
            ws.setTenant(tenant);
            ws.setCreatedByUser(createdBy);
            ws.setCreatedAt(now);
            return workspaceRepository.save(ws);
        });
    }

    private String computeSha256(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
