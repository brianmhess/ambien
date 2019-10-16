package hessian.ambien;

import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AmbienDomain {
    private String tableName = null;
    private String keyspaceName = null;
    private String cap_name = null;
    private CodecRegistry cr = null;
    private List<ColumnMetadata> partitionCols = null;
    private List<ColumnMetadata> clusteringCols = null;
    private List<ColumnMetadata> regularCols = null;
    private String outputDir = null;
    private AmbienParams params = null;

    public AmbienDomain(String keyspaceName, String tableName, List<ColumnMetadata> partitionCols,
                        List<ColumnMetadata> clusteringCols, List<ColumnMetadata> regularCols, CodecRegistry cr,
                        String outputDir, AmbienParams params) {
        this.tableName = tableName;
        this.keyspaceName = keyspaceName;
        this.cap_name = Ambien.capName(keyspaceName) + Ambien.capName(tableName);
        this.partitionCols = partitionCols;
        this.clusteringCols = clusteringCols;
        this.regularCols = regularCols;
        this.cr = cr;
        this.outputDir = outputDir;
        this.params = params;
    }

    public boolean produceDomainClasses() {
        return produceClass();
    }

    private boolean produceClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + params.package_name + ".domain;\n" +
                "\n" +
                "import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;\n" +
                "import com.datastax.oss.driver.api.mapper.annotations.Entity;\n" +
                "import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;\n" +
                "import hessian.typeparser.AnyParser;\n" +
                "\n" +
                "import java.text.ParseException;\n" +
                "import java.time.Instant;\n" +
                "import java.util.Objects;\n" +
                "\n" +
                "@Entity\n");
        sb.append("public class " + cap_name + " {\n\n");

        List<Pair<String,String>> cols = new ArrayList<Pair<String,String>>(partitionCols.size() + clusteringCols.size() + regularCols.size());

        // Partition Keys
        for (int i = 0; i < partitionCols.size(); i++) {
            String name = partitionCols.get(i).getName().asInternal();
            String type = cr.codecFor(partitionCols.get(i).getType()).getJavaType().getRawType().getName();
            sb.append("\t@PartitionKey(" + i + ")\n\tprivate " + type + " " + name + ";\n\n");
            cols.add(new Pair<String,String>(name, type));
        }

        // Clustering Columns
        for (int i = 0; i < clusteringCols.size(); i++) {
            String name = clusteringCols.get(i).getName().asInternal();
            String type = cr.codecFor(clusteringCols.get(i).getType()).getJavaType().getRawType().getName();
            sb.append("\t@ClusteringColumn(" + i + ")\n\t private " + type + " " + name + ";\n\n");
            cols.add(new Pair<String,String>(name, type));
        }

        // Columns
        for (int i = 0; i < regularCols.size(); i++) {
            String name = regularCols.get(i).getName().asInternal();
            String type = cr.codecFor(regularCols.get(i).getType()).getJavaType().getRawType().getName();
            sb.append("\tprivate " + type + " " + name + ";\n\n");
            cols.add(new Pair<String,String>(name, type));
        }

        // Contructor
        sb.append("\tpublic " + cap_name + "() { }\n\n");
        sb.append("\tpublic " + cap_name + "(");
        sb.append(cols.get(0).getValue() + " " + cols.get(0).getKey());
        for (int i = 1; i < cols.size(); i++) {
            sb.append(", " + cols.get(i).getValue() + " " + cols.get(i).getKey());
        }
        sb.append(") {\n");
        for (int i = 0; i < cols.size(); i++) {
            sb.append("\t\tthis." + cols.get(i).getKey() + " = " + cols.get(i).getKey() + ";\n");
        }
        sb.append("\t}\n\n");


        // Getters and Setters
        for (int i = 0; i < cols.size(); i++) {
            String name = cols.get(i).getKey();
            String camelName = Ambien.capName(name);
            String typename = cols.get(i).getValue();

            // Getter
            sb.append("\tpublic " + typename + " get" + camelName + "() {\n\t\treturn " + name + ";\n\t}\n\n");

            // Setter
            sb.append("\tpublic void set" + camelName + "(" + typename + " " + name + ") {\n\t\tthis." + name + " = " + name + ";\n\t}\n\n");

        }

        // toString
        sb.append("\t@Override\n\tpublic String toString() {\n");
        sb.append("\t\treturn \"" + cap_name + "{\" +\n");
        sb.append("\t\t\t\"" + cols.get(0).getKey() + "='\" + " + cols.get(0).getKey() + " + \"'\" +\n");
        for (int i = 1; i < cols.size(); i++) {
            sb.append("\t\t\t\", " + cols.get(i).getKey() + "='\" + " + cols.get(i).getKey() + " + \"'\" +\n");
        }
        sb.append("\t\t\t\"}\";\n\t}\n\n");

        // Close class
        sb.append("}\n");


        // save file
        String fname = outputDir + File.separator + cap_name + ".java";

        return Ambien.writeFile(fname, sb.toString());
    }
}
