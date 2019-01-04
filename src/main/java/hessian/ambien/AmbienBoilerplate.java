package hessian.ambien;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;

public class AmbienBoilerplate {
    private AmbienParams params = null;
    private String host = null;
    private String keyspace = null;
    private String output_dir = null;
    private String hessianTypeparserDir = null;

    public AmbienBoilerplate(AmbienParams params) {
        this.params = params;
        this.host = params.host;
        this.keyspace = params.keyspace_name;
        this.output_dir = params.output_dir;
        this.hessianTypeparserDir = params.output_dir + File.separator + "repo" + File.separator + "hessian" + File.separator + "typeparser" + File.separator + "0.1";
    }

    public boolean produceBoilerplate() {
        if (!makeDirectoryStructure()) return false;
        if (!makePomXml()) return false;
        if (!makeApplicationProperties()) return false;
        if (!makeApplication()) return false;
        if (!makeConfiguration()) return false;
        if (!addKeystore()) return false;
        if (!addTruststore()) return false;
        if (!copyResources()) return false;

        return true;
    }

    private   boolean makeDirectoryStructure() {
        if (!createDirectory(params.srcMainJavaHessianAmbienDomainDir)) return false;
        if (!createDirectory(hessianTypeparserDir)) return false;
        if (!createDirectory(params.srcMainJavaHessianAmbienRepositoryDir)) return false;
        if (!createDirectory(params.srcMainJavaHessianAmbienControllerDir)) return false;
        if (!createDirectory(params.srcMainResourcesDir)) return false;
        //if (!createDirectory(params.srcMainResourcesStaticDir)) return false;
        if (!createDirectory(params.srcMainResourcesTemplatesDir)) return false;

        return true;
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

    private boolean makePomXml() {
        String contents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "\txsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "\t<modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "\t<groupId>hessian</groupId>\n" +
                "\t<artifactId>ambien</artifactId>\n" +
                "\t<version>0.0.1-SNAPSHOT</version>\n" +
                "\t<packaging>jar</packaging>\n" +
                "\n" +
                "\t<name>ambien</name>\n" +
                "\t<description>Demo project for Spring Boot</description>\n" +
                "\n" +
                "\t<parent>\n" +
                "\t\t<groupId>org.springframework.boot</groupId>\n" +
                "\t\t<artifactId>spring-boot-starter-parent</artifactId>\n" +
                "\t\t<version>2.0.6.RELEASE</version>\n" +
                "\t\t<relativePath/> <!-- lookup parent from repository -->\n" +
                "\t</parent>\n" +
                "\n" +
                "\t<properties>\n" +
                "\t\t<!-- DSE -->\n" +
                "\t\t<dse-java-driver.version>1.7.0</dse-java-driver.version>\n" +
                "\n" +
                "\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "\t\t<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>\n" +
                "\t\t<java.version>1.8</java.version>\n" +
                "\t\t<spring-data.version>2.0.7.RELEASE</spring-data.version>\n" +
                "\t</properties>\n" +
                "\n" +
                "\t<repositories>\n" +
                "\t\t<repository>\n" +
                "\t\t\t<id>data-local</id>\n" +
                "\t\t\t<name>data</name>\n" +
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
                "\t\t\t<artifactId>dse-java-driver-mapping</artifactId>\n" +
                "\t\t\t<version>${dse-java-driver.version}</version>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>com.datastax.dse</groupId>\n" +
                "\t\t\t<artifactId>dse-java-driver-extras</artifactId>\n" +
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
                "\t\t<!-- Spring Data, extras -->\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.data</groupId>\n" +
                "\t\t\t<artifactId>spring-data-commons</artifactId>\n" +
                "\t\t\t<version>${spring-data.version}</version>\n" +
                "\t\t</dependency>\n" +
                "\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>hessian</groupId>\n" +
                "\t\t\t<artifactId>typeparser</artifactId>\n" +
                "\t\t\t<version>0.1</version>\n" +
                "\t\t</dependency>\n" +
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
                "</project>\n";
        return Ambien.writeFile(output_dir + File.separator + "pom.xml", contents);
    }

    private boolean makeApplicationProperties() {
        String contents = "# ------------------------------\n" +
                "# DataStax Enterprise parameters\n" +
                "# ------------------------------\n" +
                "dse.contactPoints=" + host + "\n" +
                "dse.port=9042\n" +
                "dse.keyspace=" + keyspace + "\n" +
                ((null == params.username) ? "" : "dse.username=" + params.username + "\n") +
                ((null == params.password) ? "" : "dse.password=" + params.password + "\n") +
                ((null == params.truststorePwd) ? "" : "dse.truststorePwd=" + params.truststorePwd + "\n") +
                ((null == params.keystorePwd) ? "" : "dse.keystorePwd=" + params.keystorePwd + "\n") +
                "\n" +
                "# ----------------------\n" +
                "# Spring Boot parameters\n" +
                "# ----------------------\n" +
                "spring.application.name=Ambien\n" +
                "server.port=8222\n" +
                "\n" +
                "springdata.basepackage=hessian.ambien.domain;\n" +
                "management.endpoints.web.exposure.include=*\n" +
                "management.endpoint.health.show-details=always\n\n";
        if ((null != params.keystorePwd) || (null != params.truststorePwd)) {
            contents = contents + "\n# DSE Security parameters\n";
            if (null != params.keystorePwd) {
                contents = contents + "dse.keystorePwd=" + params.keystorePwd + "\n";
            }
            if (null != params.truststorePwd) {
                contents = contents + "dse.truststorePwd=" + params.truststorePwd + "\n";
            }
        }

        return Ambien.writeFile(params.srcMainResourcesDir + File.separator + "application.properties", contents);
    }

    private boolean makeApplication() {
        String contents = "package hessian.ambien;\n" +
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

        return Ambien.writeFile(params.srcMainJavaHessianAmbienDir + File.separator + "AmbienApplication.java", contents);
    }

    private boolean makeConfiguration() {
        String contents = "package hessian.ambien;\n" +
                "\n" +
                "import org.springframework.beans.factory.annotation.Value;\n" +
                "import org.springframework.context.annotation.Bean;\n" +
                "import org.springframework.context.annotation.Configuration;\n" +
                "import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;\n" +
                "\n" +
                "import javax.net.ssl.KeyManagerFactory;\n" +
                "import javax.net.ssl.SSLContext;\n" +
                "import javax.net.ssl.TrustManagerFactory;\n" +
                "import java.io.*;\n" +
                "import java.security.*;\n" +
                "import java.security.cert.CertificateException;\n" +
                "\n" +
                "import com.datastax.driver.core.*;\n" +
                "import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;\n" +
                "import com.datastax.driver.core.policies.TokenAwarePolicy;\n" +
                "import com.datastax.driver.dse.*;\n" +
                "import com.datastax.driver.mapping.*;\n" +
                "\n" +
                "@Configuration\n" +
                "public class AmbienConfiguration extends AbstractCassandraConfiguration {\n" +
                "    @Value(\"${dse.contactPoints}\")\n" +
                "    public String contactPoints;\n" +
                "\n" +
                "    //@Value(\"${dse.port}\")\n" +
                "    private int port = 9042;\n" +
                "\n" +
                "    //@Value(\"${dse.keyspace}\")\n" +
                "    private String keyspace = \"" + keyspace + "\";\n" +
                "\n" +
                "    public String getContactPoints() {\n" +
                "        return contactPoints;\n" +
                "    }\n" +
                "\n" +
                "    public String getKeyspaceName() {\n" +
                "        return keyspace;\n" +
                "    }\n" +
                "\n" +
                "    public int getPort() {\n" +
                "        return port;\n" +
                "    }\n" +
                "\n" +
                ((null == params.truststorePath) ? "" : "    public String truststorePath = " + params.srcMainResourcesDir + File.separator + "truststore" + ";\n\n") +
                ((null == params.keystorePath) ? "" : "    public String keystorePath = " + params.srcMainResourcesDir + File.separator + "keystore" + ";\n\n") +
                ((null == params.username) ? "" : "    @Value(\"${dse.username}\")\n    public String username;\n\n") +
                ((null == params.password) ? "" : "    @Value(\"${dse.password}\")\n    public String password;\n\n") +
                ((null == params.truststorePwd) ? "" : "    @Value(\"${dse.truststorePwd}\")\n    public String truststorePwd;\n\n") +
                ((null == params.keystorePwd) ? "" : "    @Value(\"${dse.keystorePwd}\")\n    public String keystorePwd;\n\n") +
                "    private SSLOptions createSSLOptions(String truststorePath, String truststorePwd, String keystorePath, String keystorePwd)\n" +
                "        throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,\n" +
                "            KeyManagementException, CertificateException, UnrecoverableKeyException {\n" +
                "        TrustManagerFactory tmf = null;\n" +
                "        if (null != truststorePath) {\n" +
                "            KeyStore tks = KeyStore.getInstance(\"JKS\");\n" +
                "            tks.load(new FileInputStream(new File(truststorePath)),\n" +
                "                     truststorePwd.toCharArray());\n" +
                "            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());\n" +
                "            tmf.init(tks);\n" +
                "        }\n" +
                "\n" +
                "        KeyManagerFactory kmf = null;\n" +
                "        if (null != keystorePath) {\n" +
                "            KeyStore kks = KeyStore.getInstance(\"JKS\");\n" +
                "            kks.load(new FileInputStream(new File(keystorePath)),\n" +
                "                     keystorePwd.toCharArray());\n" +
                "            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());\n" +
                "            kmf.init(kks, keystorePwd.toCharArray());\n" +
                "        }\n" +
                "\n" +
                "        SSLContext sslContext = SSLContext.getInstance(\"TLS\");\n" +
                "        sslContext.init(kmf != null? kmf.getKeyManagers() : null,\n" +
                "                        tmf != null ? tmf.getTrustManagers() : null,\n" +
                "                        new SecureRandom());\n" +
                "\n" +
                "        return RemoteEndpointAwareJdkSSLOptions.builder().withSSLContext(sslContext).build();\n" +
                "    }\n" +
                "\n" +
                "    @Bean\n" +
                "    public DseCluster dseCluster() {\n" +
                "        DseCluster.Builder dseClusterBuilder =\n" +
                "                DseCluster.builder()\n" +
                "                        .addContactPoints(contactPoints)\n" +
                "                        .withPort(port)\n" +
                "                        .withLoadBalancingPolicy(new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build() ) );\n" +
                ((null == params.username) ?  "" : "        if (null != username)\n" +
                "            clusterBuilder = clusterBuilder.withCredentials(username, password);\n") +
                ((null == params.truststorePwd) ?  "" : "        if (null != truststorePath)\n" +
                "            clusterBuilder = clusterBuilder.withSSL(createSSLOptions(truststorePath, truststorePwd, keystorePath, keystorePwd));\n") +
                "        return dseClusterBuilder.build();\n" +
                "    }\n" +
                "\n" +
                "    @Bean\n" +
                "    public DseSession dseSession(DseCluster dseCluster) {\n" +
                "\n" +
                "        return dseCluster.connect(keyspace);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Bean\n" +
                "    public MappingManager mappingManager(DseSession dseSession) {\n" +
                "\n" +
                "        return new MappingManager(dseSession);\n" +
                "    }"+
                "}\n";

        return Ambien.writeFile(params.srcMainJavaHessianAmbienDir + File.separator + "AmbienConfiguration.java", contents);
    }

    private boolean addKeystore() {
        if (null == params.keystorePath) return true;
        try {
            Files.copy(Paths.get(params.keystorePath), Paths.get(params.srcMainResourcesDir + File.separator + "keystore"));
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
            Files.copy(Paths.get(params.truststorePath), Paths.get(params.srcMainResourcesDir + File.separator + "truststore"));
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
