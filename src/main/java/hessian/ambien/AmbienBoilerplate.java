package hessian.ambien;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class AmbienBoilerplate {
    private AmbienParams params = null;
    private String host = null;
    private String keyspace = null;
    private String output_dir = null;
    private String hessianTypeparserDir = null;

    public AmbienBoilerplate(AmbienParams params) {
        this.params = params;
        this.host = params.host;
        this.keyspace = params.keyspace_name.get(0);
        this.output_dir = params.output_dir;
        this.hessianTypeparserDir = params.output_dir + File.separator + "repo" + File.separator + "hessian" + File.separator + "typeparser" + File.separator + "0.1";
    }

    public boolean produceBoilerplate() {
        return makeDirectoryStructure()
                && makePomXml()
                && makeReadme()
                && makeApplicationProperties()
                && makeApplication()
                && makeLastUpdatedStateListener()
                && makeLastUpdatedSchemaListener()
                && makeStateListeningHealthCheck()
                && makeAmbienHealthCheck()
                && addKeystore()
                && addTruststore()
                && copyResources();
    }

    private   boolean makeDirectoryStructure() {
        return createDirectory(params.srcDomainDir)
                && createDirectory(hessianTypeparserDir)
                && createDirectory(params.srcRepositoryDir)
                && createDirectory(params.srcControllerDir)
                && createDirectory(params.resourcesTemplatesDir);
    }

    private boolean createDirectory(String dir) {
        File tfile = new File(dir);
        if (tfile.exists()) {
            if (tfile.isDirectory())
                return true;
            return false;
        }
        return tfile.mkdirs();
    }

    private boolean makeReadme() {
        String contents = "# Brought to you by Ambien\n" +
                "\n" +
                "## Contents\n" +
                "This directory contains source code for a Spring Boot applicaion\n" +
                "that is a REST API for tables in a DSE cluster, including create,\n" +
                "update, delete, and read operations.  The various read operations are\n" +
                "generated based on the schema of the tables.\n" +
                "\n" +
                "The intention is that you can modify the generated code to suit your needs \n" +
                "(tweak some code, add some endpoints, remove unneeded ones, etc).\n" +
                "\n" +
                "## Building\n" +
                "To build the code, simply run:\n" +
                "```\n" +
                "mvn clean package\n" +
                "```\n" +
                "\n" +
                "## Running\n" +
                "To run the generated code, simply run:\n" +
                "```\n" +
                "java -jar target/*.jar\n" +
                "```\n" +
                "\n" +
                "## Enjoy\n" +
                "Now, REST up and Enjoy!\n";

        return Ambien.writeFile(output_dir + File.separator + "README.md", contents);
    }

    private boolean makePomXml() {
        String[] elems = params.package_name.split("\\.");
        StringBuilder groupId = new StringBuilder(elems[0]);
        for (int i = 1; i < elems.length - 1; i++)
            groupId.append("." + elems[i]);
        String artifactId = elems[elems.length - 1];
        String contents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "\txsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "\t<modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "\t<groupId>" + groupId.toString() + "</groupId>\n" +
                "\t<artifactId>" + artifactId + "</artifactId>\n" +
                "\t<version>0.0.1-SNAPSHOT</version>\n" +
                "\t<packaging>jar</packaging>\n" +
                "\n" +
                "\t<name>expensivest</name>\n" +
                "\t<description>Demo project for Spring Boot</description>\n" +
                "\n" +
                "\t<parent>\n" +
                "\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t<artifactId>spring-boot-starter-parent</artifactId>\n" +
                "\t\t<version>2.1.4.RELEASE</version>\n" +
                "\t\t<relativePath/> <!-- lookup parent from repository -->\n" +
                "\t</parent>\n" +
                "\n" +
                "\t<properties>\n" +
                "\t\t<!-- DSE -->\n" +
                "\t\t<dse-java-driver.version>2.2.0</dse-java-driver.version>\n" +
                "\n" +
                "\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "\t\t<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>\n" +
                "\t\t<java.version>1.8</java.version>\n" +
                "\t\t<spring-data.version>2.0.7.RELEASE</spring-data.version>\n" +
                "\t</properties>\n" +
                "\n" +
                "\t<dependencyManagement>\n" +
                "\t\t<dependencies>\n" +
                "\t\t\t<dependency>\n" +
                "\t\t\t\t<groupId>io.projectreactor</groupId>\n" +
                "\t\t\t\t<artifactId>reactor-bom</artifactId>\n" +
                "\t\t\t\t<version>Bismuth-RELEASE</version>\n" +
                "\t\t\t\t<type>pom</type>\n" +
                "\t\t\t\t<scope>import</scope>\n" +
                "\t\t\t</dependency>\n" +
                "\t\t</dependencies>\n" +
                "\t</dependencyManagement>\n" +
                "\n" +
                "\n" +
                "\t<repositories>\n" +
                "\t\t<repository>\n" +
                "\t\t\t<id>1-data-local</id>\n" +
                "\t\t\t<name>datat2</name>\n" +
                "\t\t\t<url>file://${project.basedir}/repo</url>\n" +
                "\t\t</repository>\n" +
                "\t</repositories>\n" +
                "\n" +
                "\t<dependencies>\n" +
                "\t\t<!-- DSE -->\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>com.datastax.dse</groupId>\n" +
                "\t\t\t<artifactId>dse-java-driver-core</artifactId>\n" +
                "\t\t\t<version>${dse-java-driver.version}</version>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>com.datastax.dse</groupId>\n" +
                "\t\t\t<artifactId>dse-java-driver-query-builder</artifactId>\n" +
                "\t\t\t<version>${dse-java-driver.version}</version>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>com.datastax.dse</groupId>\n" +
                "\t\t\t<artifactId>dse-java-driver-mapper-processor</artifactId>\n" +
                "\t\t\t<version>${dse-java-driver.version}</version>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>com.datastax.dse</groupId>\n" +
                "\t\t\t<artifactId>dse-java-driver-mapper-runtime</artifactId>\n" +
                "\t\t\t<version>${dse-java-driver.version}</version>\n" +
                "\t\t</dependency>\n" +
                "\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t\t<artifactId>spring-boot-starter-actuator</artifactId>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t\t<artifactId>spring-boot-starter-data-cassandra</artifactId>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t\t<artifactId>spring-boot-starter-thymeleaf</artifactId>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t\t<artifactId>spring-boot-starter-web</artifactId>\n" +
                "\t\t</dependency>\n" +
                "\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t\t<artifactId>spring-boot-starter-test</artifactId>\n" +
                "\t\t\t<scope>test</scope>\n" +
                "\t\t</dependency>\n" +
                "\n" +
                "\t\t<!-- Reactor -->\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>io.projectreactor</groupId>\n" +
                "\t\t\t<artifactId>reactor-test</artifactId>\n" +
                "\t\t\t<scope>test</scope>\n" +
                "\t\t</dependency>\n" +
                "\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>hessian</groupId>\n" +
                "\t\t\t<artifactId>typeparser</artifactId>\n" +
                "\t\t\t<version>0.1</version>\n" +
                "\t\t</dependency>\n" +
                "\n" +
                "\t</dependencies>\n" +
                "\n" +
                "\t<build>\n" +
                "\t\t<plugins>\n" +
                "\t\t\t<plugin>\n" +
                "\t\t\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t\t\t<artifactId>spring-boot-maven-plugin</artifactId>\n" +
                "\t\t\t</plugin>\n" +
                "\t\t</plugins>\n" +
                "\t</build>\n" +
                "\n" +
                "\n" +
                "</project>";
        return Ambien.writeFile(output_dir + File.separator + "pom.xml", contents);
    }

    private boolean makeApplicationProperties() {
        String contents = "# ------------------------------\n" +
                "# DataStax Enterprise parameters\n" +
                "# ------------------------------\n" +
                "dse.contactPoints=" + host + "\n" +
                "dse.port=9042\n" +
                "dse.localDc=" + params.dataCenter + "\n" +
                "dse.keyspace=" + params.keyspace_name.get(0) + "\n" +
                ((null == params.username) ? "" : "dse.username=" + params.username + "\n") +
                ((null == params.password) ? "" : "dse.password=" + params.password + "\n") +
                ((null == params.truststorePwd) ? "" : "dse.truststorePwd=" + params.truststorePwd + "\n") +
                ((null == params.keystorePwd) ? "" : "dse.keystorePwd=" + params.keystorePwd + "\n") +
                ((null == params.truststorePath) ? "" : "dse.truststorePath=truststore" + "\n") +
                ((null == params.keystorePath) ? "" : "dse.keystorePath=keystore" + "\n") +
                "\n" +
                "# ----------------------\n" +
                "# Spring Boot parameters\n" +
                "# ----------------------\n" +
                "spring.application.name=Ambien\n" +
                "server.port=" + params.httpPort + "\n" +
                "\n" +
                "springdata.basepackage=" + params.package_name + ".domain;\n" +
                "management.endpoints.web.exposure.include=*\n" +
                "management.endpoint.health.show-details=always\n" +
                "management.health.cassandra.enabled=false\n";
        if ((null != params.keystorePwd) || (null != params.truststorePwd)) {
            contents = contents + "\n# DSE Security parameters\n";
            if (null != params.keystorePwd) {
                contents = contents + "dse.keystorePwd=" + params.keystorePwd + "\n";
            }
            if (null != params.truststorePwd) {
                contents = contents + "dse.truststorePwd=" + params.truststorePwd + "\n";
            }
        }

        return Ambien.writeFile(params.resourcesDir + File.separator + "application.properties", contents);
    }

    private boolean makeApplication() {
        String contents = "package " + params.package_name + ";\n" +
                "\n" +
                "import org.springframework.boot.SpringApplication;\n" +
                "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
                "import org.springframework.boot.builder.SpringApplicationBuilder;\n" +
                "import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;\n" +
                "\n" +
                "@SpringBootApplication\n" +
                "public class AmbienApplication extends SpringBootServletInitializer {\n" +
                "\t@Override\n" +
                "\tprotected SpringApplicationBuilder configure(SpringApplicationBuilder application) {\n" +
                "\t\treturn application.sources(AmbienApplication.class);\n" +
                "\t}\n" +
                "\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tSpringApplication.run(AmbienApplication.class, args);\n" +
                "\t}\n" +
                "}\n";

        return Ambien.writeFile(params.javaSrcDir + File.separator + "AmbienApplication.java", contents);
    }

    private boolean makeLastUpdatedStateListener() {
        String contents = "package " + params.package_name + ";\n" +
                "\n" +
                "import com.datastax.oss.driver.api.core.metadata.Node;\n" +
                "import com.datastax.oss.driver.api.core.metadata.NodeStateListener;\n" +
                "\n" +
                "public class LastUpdatedStateListener implements NodeStateListener {\n" +
                "    private long lastUpdated = System.currentTimeMillis();\n" +
                "    private long lastChecked = -1;\n" +
                "\n" +
                "    @Override\n" +
                "    public void onAdd(Node host) {\n" +
                "        this.lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onUp(Node host) {\n" +
                "        this.lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onDown(Node host) {\n" +
                "        this.lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onRemove(Node host) {\n" +
                "        this.lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void close() {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    public long getLastUpdated() {\n" +
                "        return this.lastUpdated;\n" +
                "    }\n" +
                "\n" +
                "    public long getLastChecked() {\n" +
                "        long retval = this.lastChecked;\n" +
                "        this.lastChecked = System.currentTimeMillis();\n" +
                "        return retval;\n" +
                "    }\n" +
                "}\n";
        return Ambien.writeFile(params.javaSrcDir + File.separator + "LastUpdatedStateListener.java", contents);
    }

    private boolean makeLastUpdatedSchemaListener() {
        String contents = "package " + params.package_name + ";\n" +
                "\n" +
                "import com.datastax.oss.driver.api.core.metadata.schema.*;\n" +
                "import com.datastax.oss.driver.api.core.type.UserDefinedType;\n" +
                "import edu.umd.cs.findbugs.annotations.NonNull;\n" +
                "\n" +
                "public class LastUpdatedSchemaListener implements SchemaChangeListener {\n" +
                "    private long lastUpdated = System.currentTimeMillis();\n" +
                "    private long lastChecked = -1;\n" +
                "\n" +
                "    public long getLastUpdated() {\n" +
                "        return this.lastUpdated;\n" +
                "    }\n" +
                "\n" +
                "    public long getLastChecked() {\n" +
                "        long retval = this.lastChecked;\n" +
                "        this.lastChecked = System.currentTimeMillis();\n" +
                "        return retval;\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Override\n" +
                "    public void onKeyspaceCreated(@NonNull KeyspaceMetadata keyspaceMetadata) {\n" +
                "        lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onKeyspaceDropped(@NonNull KeyspaceMetadata keyspaceMetadata) {\n" +
                "        lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onKeyspaceUpdated(@NonNull KeyspaceMetadata keyspaceMetadata, @NonNull KeyspaceMetadata keyspaceMetadata1) {\n" +
                "        lastUpdated = System.currentTimeMillis();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onTableCreated(@NonNull TableMetadata tableMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onTableDropped(@NonNull TableMetadata tableMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onTableUpdated(@NonNull TableMetadata tableMetadata, @NonNull TableMetadata tableMetadata1) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onUserDefinedTypeCreated(@NonNull UserDefinedType userDefinedType) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onUserDefinedTypeDropped(@NonNull UserDefinedType userDefinedType) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onUserDefinedTypeUpdated(@NonNull UserDefinedType userDefinedType, @NonNull UserDefinedType userDefinedType1) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onFunctionCreated(@NonNull FunctionMetadata functionMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onFunctionDropped(@NonNull FunctionMetadata functionMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onFunctionUpdated(@NonNull FunctionMetadata functionMetadata, @NonNull FunctionMetadata functionMetadata1) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onAggregateCreated(@NonNull AggregateMetadata aggregateMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onAggregateDropped(@NonNull AggregateMetadata aggregateMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onAggregateUpdated(@NonNull AggregateMetadata aggregateMetadata, @NonNull AggregateMetadata aggregateMetadata1) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onViewCreated(@NonNull ViewMetadata viewMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onViewDropped(@NonNull ViewMetadata viewMetadata) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onViewUpdated(@NonNull ViewMetadata viewMetadata, @NonNull ViewMetadata viewMetadata1) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void close() throws Exception {\n" +
                "\n" +
                "    }\n" +
                "}\n";

        return Ambien.writeFile(params.javaSrcDir + File.separator + "LastUpdatedSchemaListener.java", contents);
    }

    private boolean makeStateListeningHealthCheck() {
        String contents = "package " + params.package_name + ";\n" +
                "\n" +
                "import com.datastax.dse.driver.api.core.DseSession;\n" +
                "import com.datastax.oss.driver.api.core.CqlIdentifier;\n" +
                "import com.datastax.oss.driver.api.core.metadata.*;\n" +
                "import com.datastax.oss.driver.api.core.metadata.token.TokenRange;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.beans.factory.annotation.Value;\n" +
                "import org.springframework.boot.actuate.health.Health;\n" +
                "import org.springframework.boot.actuate.health.HealthIndicator;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import java.util.stream.Collectors;\n" +
                "\n" +
                "public class StateListeningHealthCheck implements HealthIndicator {\n" +
                "    private Health lastHealth = Health.unknown().build();\n" +
                "\n" +
                "    @Autowired\n" +
                "    private DseSession session;\n" +
                "    @Autowired\n" +
                "    LastUpdatedStateListener lastUpdatedStateListener;\n" +
                "    @Autowired\n" +
                "    LastUpdatedSchemaListener lastUpdatedSchemaListener;\n" +
                "\n" +
                "    @Value(\"${dse.localDc}\")\n" +
                "    private String datacenter;\n" +
                "\n" +
                "    @Value(\"${dse.keyspace}\")\n" +
                "    private String keyspace;\n" +
                "\n" +
                "    private List<String> findKeyspacesForDataCenter(String datacenter, Metadata metadata, TokenMap tokenMap) {\n" +
                "        List<String> keyspaces = new ArrayList<String>();\n" +
                "        Node oneNode = metadata.getNodes().values().stream().filter(n -> (0 == datacenter.compareTo(n.getDatacenter()))).findAny().orElse(null);\n" +
                "        if (null == oneNode)\n" +
                "            throw new IllegalArgumentException(\"No nodes found for the data center (\" + datacenter + \")\");\n" +
                "        for (CqlIdentifier ks : metadata.getKeyspaces().keySet()) {\n" +
                "            if (0 <= tokenMap.getTokenRanges(ks, oneNode).size()) {\n" +
                "                keyspaces.add(ks.asInternal());\n" +
                "            }\n" +
                "        }\n" +
                "        if (0 == keyspaces.size())\n" +
                "            throw new IllegalArgumentException(\"No keyspaces replicated to this data center (\" + datacenter + \")\");\n" +
                "        return keyspaces;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public Health health() {\n" +
                "        if ((lastUpdatedStateListener.getLastUpdated() < lastUpdatedStateListener.getLastChecked())\n" +
                "            && (lastUpdatedSchemaListener.getLastUpdated() < lastUpdatedSchemaListener.getLastChecked()))\n" +
                "            return lastHealth;\n" +
                "        Metadata metadata = session.getMetadata();\n" +
                "        List<TokenRange> badTokenRanges = new ArrayList<TokenRange>();\n" +
                "        if (!metadata.getTokenMap().isPresent())\n" +
                "            return Health.unknown().build();\n" +
                "        TokenMap tokenMap = metadata.getTokenMap().get();\n" +
                "        if (null == keyspace) {\n" +
                "            List<String> keyspaces = findKeyspacesForDataCenter(datacenter, metadata, tokenMap);\n" +
                "            if (0 == keyspaces.size())\n" +
                "                return Health.unknown().build();\n" +
                "            keyspace = keyspaces.get(0);\n" +
                "        }\n" +
                "        for (TokenRange tr : tokenMap.getTokenRanges()) {\n" +
                "            long numReplicas = tokenMap.getReplicas(keyspace, tr).size();\n" +
                "            long numReplicasUp = tokenMap.getReplicas(keyspace, tr)\n" +
                "                    .stream()\n" +
                "                    .filter(h -> (0 == h.getDatacenter().compareTo(datacenter)))\n" +
                "                    .filter(h -> (h.getState() == NodeState.UP))\n" +
                "                    .count();\n" +
                "            if (numReplicasUp <= (numReplicas + 1)/2)\n" +
                "                badTokenRanges.add(tr);\n" +
                "        }\n" +
                "        List<Node> badHosts = metadata.getNodes().values()\n" +
                "                .stream()\n" +
                "                .filter(h -> (0 == h.getDatacenter().compareTo(datacenter)))\n" +
                "                .filter(h -> (h.getState() != NodeState.UP))\n" +
                "                .collect(Collectors.toList());\n" +
                "\n" +
                "        Health.Builder hbuilder = (badTokenRanges.isEmpty()) ? Health.up() : Health.down();\n" +
                "        hbuilder.withDetail(\"BadTokenRanges\", badTokenRanges);\n" +
                "        hbuilder.withDetail(\"DownHosts\", badHosts);\n" +
                "        hbuilder.withDetail(\"NumTokenRanges\", tokenMap.getTokenRanges().size());\n" +
                "        hbuilder.withDetail(\"NumHosts\", metadata.getNodes().values().size());\n" +
                "        hbuilder.withDetail(\"DataCenter\", datacenter);\n" +
                "        hbuilder.withDetail(\"Keyspace Checked\", keyspace);\n" +
                "\n" +
                "        lastHealth = hbuilder.build();\n" +
                "        return lastHealth;\n" +
                "    }\n" +
                "}\n";

        return Ambien.writeFile(params.javaSrcDir + File.separator + "StateListeningHealthCheck.java", contents);
    }

    private boolean makeAmbienHealthCheck() {
        String contents = "package " + params.package_name + ";\n" +
                "\n" +
                "import org.springframework.stereotype.Component;\n" +
                "\n" +
                "@Component\n" +
                "public class AmbienHealthCheck extends StateListeningHealthCheck {\n" +
                "}\n";
        return Ambien.writeFile(params.javaSrcDir + File.separator + "AmbienHealthCheck.java", contents);
    }

    private boolean addKeystore() {
        if (null == params.keystorePath) return true;
        try {
            Files.copy(Paths.get(params.keystorePath), Paths.get(params.resourcesDir + File.separator + "keystore"));
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean addTruststore() {
        if (null == params.truststorePath) return true;
        try {
            Files.copy(Paths.get(params.truststorePath), Paths.get(params.resourcesDir + File.separator + "truststore"));
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean copyResources() {
        InputStream is = this.getClass().getResourceAsStream("/typeparser-0.1.jar");
        try {
            Files.copy(is, Paths.get(hessianTypeparserDir + File.separator + "typeparser-0.1.jar"), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
