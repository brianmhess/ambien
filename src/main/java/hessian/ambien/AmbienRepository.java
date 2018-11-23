package hessian.ambien;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;

import java.io.File;
import java.util.List;

public class AmbienRepository {
    private String name = null;
    private String cap_name = null;
    private CodecRegistry cr = null;
    private String output_dir = null;
    private List<ColumnMetadata> partitionCols = null;
    private List<ColumnMetadata> clusteringCols = null;
    private List<ColumnMetadata> regularCols = null;

    public AmbienRepository(String name, String output_dir, List<ColumnMetadata> partitionCols, List<ColumnMetadata> clusteringCols, List<ColumnMetadata> regularCols, CodecRegistry cr) {
        this.name = name;
        this.cap_name = Ambien.capName(name);
        this.output_dir = output_dir;
        this.partitionCols = partitionCols;
        this.clusteringCols = clusteringCols;
        this.regularCols = regularCols;
        this.cr = cr;
    }

    public boolean produceRepositoryClasses() {
        if (!produceBaseRepositoryClass()) return false;

        return true;
    }

    public boolean produceBaseRepositoryClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package hessian.ambien.repository;\n" +
                "\n" +
                "import hessian.ambien.domain." + cap_name + ";\n" +
                "import hessian.ambien.domain." + cap_name + "PrimaryKey;\n" +
                "import org.springframework.data.cassandra.repository.AllowFiltering;\n" +
                "import org.springframework.data.cassandra.repository.CassandraRepository;\n" +
                "import org.springframework.data.cassandra.repository.Query;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public interface " + cap_name + "Repository extends CassandraRepository<" + cap_name + ", " + cap_name + "PrimaryKey> {\n");
        // Find All
        sb.append("\tList<" + cap_name + "> findAll();\n");

        // Save
        sb.append("\t" + cap_name + " save(" + cap_name + " " + name + ");");

        // Find By Partition Key
        StringBuilder partkeysb = new StringBuilder();
        partkeysb.append("Key" + Ambien.capName(partitionCols.get(0).getName()));
        for (int i = 0; i < partitionCols.size(); i++) {
            partkeysb.append("AndKey" + Ambien.capName(partitionCols.get(i).getName()));
        }
        String partkeys = partkeysb.toString();
        sb.append("\tList<" + cap_name + "> findBy" + partkeys + "(");
        sb.append(", " + cr.codecFor(partitionCols.get(0).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(0).getName());
        for (int i = 1; i < partitionCols.size(); i++) {
            sb.append(", " + cr.codecFor(partitionCols.get(i).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(i).getName());
        }
        sb.append(");\n\n");

        // Find By Partition Key and some Clustering Keys
        for (int j = 1; j < clusteringCols.size(); j++) {
            sb.append("\tList<" + cap_name + "> findBy" + partkeys);
            for (int i = 0; i < j; i++) {
                sb.append("And" + Ambien.capName(clusteringCols.get(i).getName()));
            }
            sb.append("(");
            sb.append(", " + cr.codecFor(partitionCols.get(0).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(0).getName());
            for (int i = 1; i < partitionCols.size(); i++) {
                sb.append(", " + cr.codecFor(partitionCols.get(i).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(i).getName());
            }
            for (int i = 0; i < j; i++) {
                sb.append(", " + cr.codecFor(clusteringCols.get(i).getType()).getJavaType().getClass().getName() + " " + clusteringCols.get(i).getName());
            }
            sb.append(");\n\n");
        }

        sb.append("}\n");

        String fname = output_dir
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "java"
                + File.separator + "hessian"
                + File.separator + "ambien"
                + File.separator + "repository"
                + File.separator + cap_name + "Repository.java";
        return Ambien.writeFile(fname, sb.toString());
    }
}
