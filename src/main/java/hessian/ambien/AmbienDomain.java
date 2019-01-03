package hessian.ambien;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;
import javafx.util.Pair;

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
        if(!produceClass()) return false;

        return true;
    }

    public boolean produceClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package hessian.ambien.domain;\n" +
                "\n" +
                "import com.datastax.driver.mapping.annotations.Column;\n" +
                "import com.datastax.driver.mapping.annotations.PartitionKey;\n" +
                "import com.datastax.driver.mapping.annotations.ClusteringColumn;\n" +
                "import com.datastax.driver.mapping.annotations.Table;\n" +
                "\n" +
                "@Table(name=\"" + name + "\")\n");
        sb.append("public class " + cap_name + " {\n\n");

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
        String fname = params.srcMainJavaHessianAmbienDomainDir + File.separator + cap_name + ".java";

        return Ambien.writeFile(fname, sb.toString());
    }
}
