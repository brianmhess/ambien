package hessian.ambien;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AmbienDomain {
    private AmbienParams params = null;
    private String name = null;
    private String cap_name = null;
    private CodecRegistry cr = null;
    private String output_dir = null;
    private List<ColumnMetadata> partitionCols = null;
    private List<ColumnMetadata> clusteringCols = null;
    private List<ColumnMetadata> regularCols = null;

    public AmbienDomain(AmbienParams params, List<ColumnMetadata> partitionCols, List<ColumnMetadata> clusteringCols, List<ColumnMetadata> regularCols, CodecRegistry cr) {
        this.params = params;
        this.name = params.table_name;
        this.cap_name = Ambien.capName(name);
        this.output_dir = params.output_dir;
        this.partitionCols = partitionCols;
        this.clusteringCols = clusteringCols;
        this.regularCols = regularCols;
        this.cr = cr;
    }

    public boolean produceDomainClasses() {
        if(!produceKeyClass()) return false;
        if(!produceClass()) return false;

        return true;
    }

    public boolean produceKeyClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package hessian.ambien.domain;\n" +
                "\n" +
                "import org.springframework.data.cassandra.core.cql.PrimaryKeyType;\n" +
                "import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;\n" +
                "import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;\n" +
                "\n" +
                "import java.io.Serializable;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "@PrimaryKeyClass\n");
        sb.append("public class " + cap_name + "PrimaryKey implements Serializable {\n\n");

        // Private members
        List<String> primaryColName = new ArrayList<String>(partitionCols.size() + clusteringCols.size());
        List<String> primaryColType = new ArrayList<String>(partitionCols.size() + clusteringCols.size());
        for(int i = 0; i < partitionCols.size(); i++) {
            primaryColName.add(i, partitionCols.get(i).getName());
            primaryColType.add(i, cr.codecFor(partitionCols.get(i).getType()).getJavaType().getRawType().getName());
            sb.append("\t@PrimaryKeyColumn(name =\"" + primaryColName.get(i) + "\", ordinal = " + i + ", type = PrimaryKeyType.PARTITIONED)\n");
            sb.append("\tprivate " + primaryColType.get(i) + " " + primaryColName.get(i) + ";\n\n");
        }
        for(int i = 0; i < clusteringCols.size(); i++) {
            primaryColName.add(i + partitionCols.size(), clusteringCols.get(i).getName());
            primaryColType.add(i + partitionCols.size(), cr.codecFor(clusteringCols.get(i).getType()).getJavaType().getRawType().getName());
            sb.append("\t@PrimaryKeyColumn(name =\"" + primaryColName.get(i + partitionCols.size()) + "\", ordinal = " + (i + partitionCols.size()) + ", type = PrimaryKeyType.CLUSTERED)\n");
            sb.append("\tprivate " + primaryColType.get(i + partitionCols.size()) + " " + primaryColName.get(i + partitionCols.size()) + ";\n\n");
        }


        // Contructor
        sb.append("\tpublic " + cap_name + "PrimaryKey(" + primaryColType.get(0) + " " + primaryColName.get(0));
        for (int i = 1; i < primaryColName.size(); i++) {
            sb.append(", " + primaryColType.get(i) + " " + primaryColName.get(i));
        }
        sb.append(") {\n");
        for (int i = 0; i < primaryColName.size(); i++) {
            sb.append("\t\tthis." + primaryColName.get(i) + " = " + primaryColName.get(i) + ";\n");
        }
        sb.append("\t}\n\n");


        // Getters and Setters
        for (int i = 0; i < primaryColName.size(); i++) {
            String name = primaryColName.get(i);
            String camelname = Ambien.capName(name);
            String typename = primaryColType.get(i);

            // Getter
            sb.append("\tpublic " + typename + " get" + camelname + "() {\n\t\treturn " + name + ";\n\t}\n\n");

            // Setter
            sb.append("\tpublic void set" + camelname + "(" + typename + " " + name + ") {\n\t\tthis." + name + " = " + name + ";\n\t}\n\n");
        }


        // toString
        sb.append("\t@Override\n\tpublic String toString() {\n\t\treturn \"" + cap_name + "PrimaryKey{\" + \n");

        sb.append("\t\t\t\"" + primaryColName.get(0) + "='\" + " + primaryColName.get(0) + " + \"'\" +\n");
        for (int i = 1; i < primaryColName.size(); i++) {
            sb.append("\t\t\t\", " + primaryColName.get(i) + "='\" + " + primaryColName.get(i) + " + \"'\" +\n");
        }
        sb.append("\t\t\t'}';\n");
        sb.append("\t}\n\n");


        // equals
        sb.append("\t@Override\n\tpublic boolean equals(Object o) {\n");
        sb.append("\t\t if (this == o) return true;\n");
        sb.append("\t\tif (!(o instanceof " + cap_name + "PrimaryKey)) return false;\n\n");
        sb.append("\t\t" + cap_name + "PrimaryKey that = (" + cap_name + "PrimaryKey) o;\n\n");
        for (int i = 0; i < primaryColName.size(); i++) {
            sb.append("\t\tif (!get" + Ambien.capName(primaryColName.get(i)) + "().equals(that.get" + Ambien.capName(primaryColName.get(i)) + "())) return false;\n");
        }
        sb.append("\t\treturn true;\n\t}\n\n");

        // hashCode
        sb.append("\t@Override\n\tpublic int hashCode() {\n");
        sb.append("\t\tint result = get" + Ambien.capName(primaryColName.get(0)) + "().hashCode();\n");
        for (int i = 1; i < primaryColName.size(); i++) {
            sb.append("\t\tresult = 31 * result + get" + Ambien.capName(primaryColName.get(i)) + "().hashCode();\n");
        }
        sb.append("\t\treturn result;\n\t}\n\n");

        // Close class
        sb.append("}\n");


        // save file
        String fname = params.srcMainJavaHessianAmbienDomainDir + File.separator + cap_name + "PrimaryKey.java";

        return Ambien.writeFile(fname, sb.toString());
    }

    public boolean produceClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package hessian.ambien.domain;\n" +
                "\n" +
                "import org.springframework.data.cassandra.core.mapping.Column;\n" +
                "import org.springframework.data.cassandra.core.mapping.PrimaryKey;\n" +
                "import org.springframework.data.cassandra.core.mapping.Table;\n" +
                "\n" +
                "import java.io.Serializable;\n" +
                "\n" +
                "@Table(value=\"" + name + "\")\n");
        sb.append("public class " + cap_name + " implements Serializable {\n\n");

        // Primary Key
        sb.append("\t@PrimaryKey private " + cap_name + "PrimaryKey key;\n\n");

        // Columns
        List<String> regularColType = new ArrayList<String>(regularCols.size());
        for (int i = 0; i < regularCols.size(); i++) {
            regularColType.add(i, cr.codecFor(regularCols.get(i).getType()).getJavaType().getRawType().getName());
            sb.append("\t@Column(\"" + regularCols.get(i).getName() + "\") private " + regularColType.get(i) + " " + regularCols.get(i).getName() + ";\n\n");
        }

        // Contructor
        sb.append("\tpublic " + cap_name + "(");
        for (int i = 0; i < regularCols.size(); i++) {
            sb.append(regularColType.get(i) + " " + regularCols.get(i).getName() + ", ");
        }
        sb.append(cap_name + "PrimaryKey key) {\n");
        for (int i = 0; i < regularCols.size(); i++) {
            sb.append("\t\t this." + regularCols.get(i).getName() + " = " + regularCols.get(i).getName() + ";\n");
        }
        sb.append("\t\tthis.key = key;\n\t}\n\n");


        // Getters and Setters
        for (int i = 0; i < regularCols.size(); i++) {
            String name = regularCols.get(i).getName();
            String camelname = Ambien.capName(name);
            String typename = regularColType.get(i);

            // Getter
            sb.append("\tpublic " + typename + " get" + camelname + "() {\n\t\treturn " + name + ";\n\t}\n\n");

            // Setter
            sb.append("\tpublic void set" + camelname + "(" + typename + " " + name + ") {\n\t\tthis." + name + " = " + name + ";\n\t}\n\n");
        }
        sb.append("\tpublic " + cap_name + "PrimaryKey getKey() {\n\t\treturn key;\n\t}\n\n");
        sb.append("\tpublic void setKey(" + cap_name + "PrimaryKey key) {\n\t\tthis.key = key;\n\t}\n\n");


        // toString
        sb.append("\t@Override\n\tpublic String toString() {\n");
        sb.append("\t\treturn \"" + cap_name + "{\" +\n");
        sb.append("\t\t\t\"key='\" + key + \"'\" +\n");
        for (int i = 0; i < regularCols.size(); i++) {
            sb.append("\t\t\t\", " + regularCols.get(i).getName() + "='\" + " + regularCols.get(i).getName() + " + \"'\" +\n");
        }
        sb.append("\t\t\t\"}\";\n\t}\n\n");

        // Close class
        sb.append("}\n");


        // save file
        String fname = params.srcMainJavaHessianAmbienDomainDir + File.separator + cap_name + ".java";

        return Ambien.writeFile(fname, sb.toString());
    }
}
