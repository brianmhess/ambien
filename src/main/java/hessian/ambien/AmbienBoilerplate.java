package hessian.ambien;

import java.io.*;

public class AmbienBoilerplate {
    String host = null;
    String keyspace = null;
    String output_dir = null;
    String srcDir = null;
    String srcMainDir = null;
    String srcMainJavaDir = null;
    String srcMainJavaHessianDir = null;
    String srcMainJavaHessianAmbienDir = null;
    String srcMainJavaHessianAmbienDomainDir = null;
    String srcMainJavaHessianAmbienRepositoryDir = null;
    String srcMainJavaHessianAmbienControllerDir = null;
    String srcMainResourcesDir = null;
    String srcMainResourcesStaticDir = null;
    String srcMainResourcesTemplateDir = null;

    public AmbienBoilerplate(String output_dir, String host, String keyspace) {
        this.host = host;
        this.keyspace = keyspace;
        this.output_dir = output_dir;
        this.srcDir = output_dir + File.separator + "src";
        this.srcMainDir = srcDir + File.separator + "main";
        this.srcMainJavaDir = srcMainDir + File.separator + "java";
        this.srcMainJavaHessianDir = srcMainJavaDir + File.separator + "hessian";
        this.srcMainJavaHessianAmbienDir = srcMainJavaHessianDir + File.separator + "ambien";
        this.srcMainJavaHessianAmbienDomainDir = srcMainJavaHessianAmbienDir + File.separator + "domain";
        this.srcMainJavaHessianAmbienRepositoryDir = srcMainJavaHessianAmbienDir + File.separator + "repository";
        this.srcMainJavaHessianAmbienControllerDir = srcMainJavaHessianAmbienDir + File.separator + "contoller";
        this.srcMainResourcesDir = srcMainDir + File.separator + "resources";
        this.srcMainResourcesStaticDir = srcMainResourcesDir + File.separator + "static";
        this.srcMainResourcesTemplateDir = srcMainResourcesDir + File.separator + "template";
    }

    public boolean produceBoilerplate() {
        if (!makeDirectoryStructure()) return false;
        if (!makePomXml()) return false;
        if (!makeApplicationProperties()) return false;

        return true;
    }

    public  boolean makeDirectoryStructure() {
        if (!createDirectory(srcMainJavaHessianAmbienDomainDir)) return false;
        if (!createDirectory(srcMainJavaHessianAmbienRepositoryDir)) return false;
        if (!createDirectory(srcMainJavaHessianAmbienControllerDir)) return false;
        if (!createDirectory(srcMainResourcesStaticDir)) return false;
        if (!createDirectory(srcMainResourcesTemplateDir)) return false;

        return true;
    }

    public boolean createDirectory(String dir) {
        File tfile = new File(dir);
        if (tfile.exists()) {
            if (tfile.isDirectory())
                return true;
            return false;
        }
        return tfile.mkdirs();
    }

    public boolean makePomXml() {
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
                "\t\t\t<artifactId>spring-data-cassandra</artifactId>\n" +
                "\t\t\t<version>${spring-data.version}</version>\n" +
                "\t\t\t<exclusions>\n" +
                "\t\t\t\t<exclusion>\n" +
                "\t\t\t\t\t<groupId>com.datastax.cassandra</groupId>\n" +
                "\t\t\t\t\t<artifactId>cassandra-driver-core</artifactId>\n" +
                "\t\t\t\t</exclusion>\n" +
                "\t\t\t</exclusions>\n" +
                "\t\t</dependency>\n" +
                "\t\t<dependency>\n" +
                "\t\t\t<groupId>org.springframework.data</groupId>\n" +
                "\t\t\t<artifactId>spring-data-commons</artifactId>\n" +
                "\t\t\t<version>${spring-data.version}</version>\n" +
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
                "</project>\n";
        return Ambien.writeFile(output_dir + File.separator + "pom.xml", contents);
    }

    public boolean makeApplicationProperties() {
        String contents = "# ------------------------------\n" +
                "# DataStax Enterprise parameters\n" +
                "# ------------------------------\n" +
                "dse.contactPoints=" + host + "\n" +
                "dse.port=9042\n" +
                "dse.keyspace=" + keyspace + "\n" +
                "\n" +
                "# ----------------------\n" +
                "# Spring Boot parameters\n" +
                "# ----------------------\n" +
                "spring.application.name=Ambien\n" +
                "server.port=8222\n" +
                "\n" +
                "springdata.basepackage=hessian.ambien.domain;";

        return Ambien.writeFile(srcMainResourcesDir + File.separator + "application.properties", contents);
    }

    public boolean makeApplication() {
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

        return Ambien.writeFile(srcMainJavaHessianAmbienDir + File.separator + "AmbienApplication.java", contents);
    }

    public boolean makeConfiguration() {
        String contents = "package hessian.ambien;\n" +
                "\n" +
                "import org.springframework.beans.factory.annotation.Value;\n" +
                "import org.springframework.context.annotation.Bean;\n" +
                "import org.springframework.context.annotation.Configuration;\n" +
                "import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;\n" +
                "import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;\n" +
                "import org.springframework.data.cassandra.core.mapping.BasicCassandraMappingContext;\n" +
                "import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;\n" +
                "\n" +
                "import javax.validation.constraints.NotNull;\n" +
                "\n" +
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
                "}\n";

        return Ambien.writeFile(srcMainJavaHessianAmbienDir + File.separator + "AmbienConfiguration.java", contents);
    }
}
