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
                "import " + params.package_name + ".dao.*;" +
                "\n" +
                "\n" +
                "@Configuration\n" +
                "public class AmbienConfiguration {\n" +
                "    @Value(\"${apollo.credentials}\")\n" +
                "    private String apolloCredentials;\n" +
                "\n" +
                "    @Value(\"${dse.username}\")\n" +
                "    private String username;\n" +
                "\n" +
                "    @Value(\"${dse.password}\")\n" +
                "    private String password;\n" +
                "\n" +
                "    public String getApolloCredentials() {\n" +
                "        return this.apolloCredentials;\n" +
                "    }\n" +
                "\n" +
                "    public String getUsername() {\n" +
                "        return this.username;\n" +
                "    }\n" +
                "\n" +
                "    public String getPassword() {\n" +
                "        return this.password;\n" +
                "    }\n" +
                "\n" +
                "    @Bean\n" +
                "    public DseSession dseSession(LastUpdatedStateListener lastUpdatedStateListener, LastUpdatedSchemaListener lastUpdateSchemaListener) {\n" +
                "        DseSessionBuilder dseSessionBuilder = DseSession.builder()\n" +
                "                .withCloudSecureConnectBundle(this.getClass().getResourceAsStream(this.apolloCredentials))\n" +
                "                .withAuthCredentials(this.username, this.password);\n" +
                "        dseSessionBuilder.withNodeStateListener(lastUpdatedStateListener);\n" +
                "        dseSessionBuilder.withSchemaChangeListener(lastUpdateSchemaListener);\n" +
                "\n" +
                "        return dseSessionBuilder.build();\n" +
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
