package hessian.ambien;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AmbienDomain {
    private String tableName = null;
    private String keyspaceName = null;
    private String className = null;
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
        this.className = Ambien.capName(keyspaceName) + Ambien.capName(tableName);
        this.partitionCols = partitionCols;
        this.clusteringCols = clusteringCols;
        this.regularCols = regularCols;
        this.cr = cr;
        this.outputDir = outputDir;
        this.params = params;
    }

    public boolean produceDomainClasses() {
        if(!produceClass()) return false;

        return true;
    }

    private boolean produceClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + params.package_name + ".domain;\n" +
                "\n" +
                "import com.datastax.driver.mapping.annotations.Column;\n" +
                "import com.datastax.driver.mapping.annotations.PartitionKey;\n" +
                "import com.datastax.driver.mapping.annotations.ClusteringColumn;\n" +
                "import com.datastax.driver.mapping.annotations.Table;\n" +
                "\n" +
                "@Table(name=\"" + tableName + "\", keyspace = \"" + keyspaceName + "\")\n");
        sb.append("public class " + className + " {\n\n");

        List<Pair<String,String>> cols = new ArrayList<Pair<String,String>>(partitionCols.size() + clusteringCols.size() + regularCols.size());

        // Partition Keys
        for (int i = 0; i < partitionCols.size(); i++) {
            String name = partitionCols.get(i).getName();
            String type = cr.codecFor(partitionCols.get(i).getType()).getJavaType().getRawType().getName();
            sb.append("\t@PartitionKey(" + i + ")\n\t@Column\n\tprivate " + type + " " + name + ";\n\n");
            cols.add(new Pair<String,String>(name, type));
        }

        // Clustering Columns
        for (int i = 0; i < clusteringCols.size(); i++) {
            String name = clusteringCols.get(i).getName();
            String type = cr.codecFor(clusteringCols.get(i).getType()).getJavaType().getRawType().getName();
            sb.append("\t@ClusteringColumn(" + i + ")\n\t@Column\n\t private " + type + " " + name + ";\n\n");
            cols.add(new Pair<String,String>(name, type));
        }

        // Columns
        for (int i = 0; i < regularCols.size(); i++) {
            String name = regularCols.get(i).getName();
            String type = cr.codecFor(regularCols.get(i).getType()).getJavaType().getRawType().getName();
            sb.append("\t@Column\n\tprivate " + type + " " + name + ";\n\n");
            cols.add(new Pair<String,String>(name, type));
        }

        // Contructor
        sb.append("\tpublic " + className + "() { }\n\n");
        sb.append("\tpublic " + className + "(");
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
        sb.append("\t\treturn \"" + className + "{\" +\n");
        sb.append("\t\t\t\"" + cols.get(0).getKey() + "='\" + " + cols.get(0).getKey() + " + \"'\" +\n");
        for (int i = 1; i < cols.size(); i++) {
            sb.append("\t\t\t\", " + cols.get(i).getKey() + "='\" + " + cols.get(i).getKey() + " + \"'\" +\n");
        }
        sb.append("\t\t\t\"}\";\n\t}\n\n");

        // Close class
        sb.append("}\n");


        // save file
        String fname = outputDir + File.separator + className + ".java";

        return Ambien.writeFile(fname, sb.toString());
    }
}
