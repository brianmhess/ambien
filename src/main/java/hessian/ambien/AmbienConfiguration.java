package hessian.ambien;

import java.io.File;

public class AmbienConfiguration {
    private AmbienParams params = null;

    public AmbienConfiguration(AmbienParams params) {
        this.params = params;
    }

    public boolean produceConfiguration() {
        StringBuilder sb = new StringBuilder("package " + params.package_name + ";\n" +
                "\n" +
                "import com.datastax.dse.driver.api.core.DseSession;\n" +
                "import com.datastax.dse.driver.api.core.DseSessionBuilder;\n" +
                "import org.springframework.beans.factory.annotation.Value;\n" +
                "import org.springframework.context.annotation.Bean;\n" +
                "import org.springframework.context.annotation.Configuration;\n" +
                "\n" +
                "import javax.net.ssl.KeyManagerFactory;\n" +
                "import javax.net.ssl.SSLContext;\n" +
                "import javax.net.ssl.TrustManagerFactory;\n" +
                "\n" +
                "import java.io.File;\n" +
                "import java.io.FileInputStream;\n" +
                "import java.io.FileNotFoundException;\n" +
                "import java.io.IOException;\n" +
                "import java.net.InetSocketAddress;\n" +
                "import java.security.*;\n" +
                "import java.security.cert.CertificateException;\n" +
                "\n" +
                "import " + params.package_name + ".dao.*;" +
                "\n" +
                "\n" +
                "@Configuration\n" +
                "public class AmbienConfiguration {\n" +
                "    @Value(\"${dse.contactPoints}\")\n" +
                "    public String contactPoints;\n" +
                "\n" +
                "    @Value(\"${dse.port}\")\n" +
                "    private int port;\n" +
                "\n" +
                "    @Value(\"${dse.localDc}\")\n" +
                "    private String localDatacenter;\n" +
                "\n" +
                "    @Value(\"${dse.truststorePath:#{null}}\")\n    public String truststorePath;\n\n" +
                "    @Value(\"${dse.keystorePath:#{null}}\")\n    public String keystorePath;\n\n" +
                "    @Value(\"${dse.username:#{null}}\")\n    public String username;\n\n" +
                "    @Value(\"${dse.password:#{null}}\")\n    public String password;\n\n" +
                "    @Value(\"${dse.truststorePwd:#{null}}\")\n    public String truststorePwd;\n\n" +
                "    @Value(\"${dse.keystorePwd:#{null}}\")\n    public String keystorePwd;\n\n" +
                "    public String getContactPoints() {\n" +
                "        return contactPoints;\n" +
                "    }\n" +
                "\n" +
                "    public int getPort() {\n" +
                "        return port;\n" +
                "    }\n" +
                "\n" +
                "    public String getLocalDatacenter() {\n" +
                "        return localDatacenter;\n" +
                "    }\n" +
                "\n" +
                "    private SSLContext createSSLOptions()\n" +
                "        throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,\n" +
                "            KeyManagementException, CertificateException, UnrecoverableKeyException {\n" +
                "        TrustManagerFactory tmf = null;\n" +
                "        if (null != truststorePath) {\n" +
                "            KeyStore tks = KeyStore.getInstance(\"JKS\");\n" +
                "            tks.load(this.getClass().getResourceAsStream(truststorePath),\n" +
                "                    truststorePwd.toCharArray());\n" +
                "            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());\n" +
                "            tmf.init(tks);\n" +
                "        }\n" +
                "\n" +
                "        KeyManagerFactory kmf = null;\n" +
                "        if (null != keystorePath) {\n" +
                "            KeyStore kks = KeyStore.getInstance(\"JKS\");\n" +
                "            kks.load(this.getClass().getResourceAsStream(keystorePath),\n" +
                "                    keystorePwd.toCharArray());\n" +
                "            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());\n" +
                "            kmf.init(kks, keystorePwd.toCharArray());\n" +
                "        }\n" +
                "\n" +
                "        SSLContext sslContext = SSLContext.getInstance(\"TLS\");\n" +
                "        sslContext.init(kmf != null? kmf.getKeyManagers() : null,\n" +
                "                        tmf != null ? tmf.getTrustManagers() : null,\n" +
                "                        new SecureRandom());\n" +
                "\n" +
                "        return sslContext;\n" +
                "    }\n" +
                "    @Bean\n" +
                "    public DseSession dseSession(LastUpdatedStateListener lastUpdatedStateListener, LastUpdatedSchemaListener lastUpdateSchemaListener)" +
                "            throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException,\n" +
                "               CertificateException, UnrecoverableKeyException {\n" +
                "        DseSessionBuilder builder = DseSession.builder()\n" +
                "                .addContactPoint(InetSocketAddress.createUnresolved(contactPoints, port))\n" +
                "                .withLocalDatacenter(localDatacenter);\n" +
                "        if (null != username)\n" +
                "            builder = builder.withAuthCredentials(username, password);\n" +
                "        if ((null != truststorePath) || (null != keystorePath))\n" +
                "            builder = builder.withSslContext(createSSLOptions());\n" +
                "        builder.withNodeStateListener(lastUpdatedStateListener);\n" +
                "        builder.withSchemaChangeListener(lastUpdateSchemaListener);\n" +
                "\n" +
                "        return builder.build();\n" +
                "    }\n" +
                "\n" +
                "    @Bean\n" +
                "    public LastUpdatedStateListener lastUpdatedStateListener() {\n" +
                "        return new LastUpdatedStateListener();\n" +
                "    }\n" +
                "\n" +
                "    @Bean\n" +
                "    public LastUpdatedSchemaListener lastUpdatedSchemaListener() {\n" +
                "        return new LastUpdatedSchemaListener();\n" +
                "    }\n" +
                "\n");
        for (int i = 0; i < params.keyspace_name.size(); i++) {
            String keyspace_name = params.keyspace_name.get(i);
            String table_name = params.table_name.get(i);
            String cap_name = Ambien.capName(keyspace_name) + Ambien.capName(table_name);
            String camel_name = keyspace_name + Ambien.capName(table_name);
            sb.append("    @Bean\n" +
                    "    public " + cap_name + "Mapper " + camel_name + "Mapper(DseSession dseSession) {\n" +
                    "        return new " + cap_name + "MapperBuilder(dseSession).build();\n" +
                    "    }\n" +
                    "\n" +
                    "    @Bean\n" +
                    "    public " + cap_name + "Dao " + camel_name + "Dao(" + cap_name + "Mapper " + camel_name + "Mapper) {\n" +
                    "        return " + camel_name + "Mapper." + camel_name + "Dao(\"" + keyspace_name + "\", \"" + table_name + "\");\n" +
                    "    }\n" +
                    "\n");
        }
        sb.append("}\n\n");

        return Ambien.writeFile(params.javaSrcDir + File.separator + "AmbienConfiguration.java", sb.toString());
    }
}
