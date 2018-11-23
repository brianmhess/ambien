package hessian.ambien;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;

import java.io.File;
import java.util.List;

public class AmbienController {
    private String name = null;
    private CodecRegistry cr = null;
    private String output_dir = null;
    private List<ColumnMetadata> partitionCols = null;
    private List<ColumnMetadata> clusteringCols = null;
    private List<ColumnMetadata> regularCols = null;

    public AmbienController(String name, String output_dir, List<ColumnMetadata> partitionCols, List<ColumnMetadata> clusteringCols, List<ColumnMetadata> regularCols, CodecRegistry cr) {
        this.name = name;
        this.output_dir = output_dir;
        this.partitionCols = partitionCols;
        this.clusteringCols = clusteringCols;
        this.regularCols = regularCols;
        this.cr = cr;
    }

    public boolean produceControllerClasses() {
        if (!produceBaseControllerClass()) return false;

        return true;
    }

    public boolean produceBaseControllerClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package hessian.ambien.controller;\n" +
                "\n" +
                "import hessian.ambien.domain." + Ambien.capName(name) + ";\n" +
                "import hessian.ambien.repository." + Ambien.capName(name) + "Repository;\n" +
                "import hessian.ambien.repository." + Ambien.capName(name) + "SearchRepository;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@RestController\n" +
                "public class " + Ambien.capName(name) + "RestController {\n");
        sb.append("\t@Autowired\n\tprivate " + Ambien.capName(name) + "Repository " + name + "Repository;\n\n");

        // Hello
        sb.append("\t@RequestMapping(\"api/hello\")\n" +
                "\tpublic String hello() {\n" +
                "\t\treturn \"<html><body><H1>Hello World</H1></body></html>\";\n" +
                "\t}\n\n");

        // Add
        sb.append("\t@RequestMapping(value = \"api/add\", method = RequestMethod.POST)\n" +
                "\tpublic " + Ambien.capName(name) + " create" + Ambien.capName(name) + "(@RequestBody " + Ambien.capName(name) + " " + name + ") {\n" +
                "\t\t" + name + "Repository.save(" + name + ");\n" +
                "\t\treturn " + name + ";\n" +
                "\t}\n\n");

        // Partition Key
        sb.append("\t@RequestMapping(value = \"api");
        for (int i = 0; i < partitionCols.size(); i++) {
            sb.append("/{" + partitionCols.get(i).getName() + "}");
        }
        sb.append("\n\tpublic List<" + Ambien.capName(name) + "> " + partitionCols.get(0).getName());
        for (int i = 1; i < partitionCols.size(); i++) {
            sb.append(Ambien.capName(partitionCols.get(i).getName()));
        }
        sb.append("(@PathVariable " + cr.codecFor(partitionCols.get(0).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(0).getName());
        for (int i = 1; i < partitionCols.size(); i++) {
            sb.append(", @PathVariable " + cr.codecFor(partitionCols.get(i).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(i).getName());
        }
        sb.append(") {\n");
        sb.append("\t\treturn (ArrayList<" + Ambien.capName(name) + ">)" + name + "Repository.findBy");
        for (int i = 0; i < partitionCols.size(); i++) {
            sb.append("Key" + Ambien.capName(partitionCols.get(i).getName()));
        }
        sb.append("(");
        sb.append(", " + partitionCols.get(0).getName());
        for (int i = 1; i < partitionCols.size(); i++) {
            sb.append(", " + partitionCols.get(i).getName());
        }
        sb.append(");\n\t}\n\n");


        // Partition key and various clustering keys
        for (int j = 1; j < clusteringCols.size(); j++) {
            sb.append("\t@RequestMapping(value = \"api");
            for (int i = 0; i < partitionCols.size(); i++) {
                sb.append("/{" + partitionCols.get(i).getName() + "}");
            }
            for (int i = 0; i < j; i++) {
                sb.append("/{" + clusteringCols.get(i).getName() + "}");
            }
            sb.append("\n\tpublic List<" + Ambien.capName(name) + "> " + partitionCols.get(0).getName());
            for (int i = 1; i < partitionCols.size(); i++) {
                sb.append(Ambien.capName(partitionCols.get(i).getName()));
            }
            for (int i = 0; i < j; i++) {
                sb.append(Ambien.capName(clusteringCols.get(i).getName()));
            }
            sb.append("(@PathVariable " + cr.codecFor(partitionCols.get(0).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(0).getName());
            for (int i = 1; i < partitionCols.size(); i++) {
                sb.append(", @PathVariable " + cr.codecFor(partitionCols.get(i).getType()).getJavaType().getClass().getName() + " " + partitionCols.get(i).getName());
            }
            for (int i = 1; i < j; i++) {
                sb.append(", @PathVariable " + cr.codecFor(clusteringCols.get(i).getType()).getJavaType().getClass().getName() + " " + clusteringCols.get(i).getName());
            }
            sb.append(") {\n");
            sb.append("\t\treturn (ArrayList<" + Ambien.capName(name) + ">)" + name + "Repository.findBy");
            for (int i = 0; i < partitionCols.size(); i++) {
                sb.append("Key" + Ambien.capName(partitionCols.get(i).getName()));
            }
            for (int i = 0; i < j; i++) {
                sb.append("Key" + Ambien.capName(clusteringCols.get(i).getName()));
            }
            sb.append("(");
            sb.append(", " + partitionCols.get(0).getName());
            for (int i = 1; i < partitionCols.size(); i++) {
                sb.append(", " + partitionCols.get(i).getName());
            }
            for (int i = 1; i < j; i++) {
                sb.append(", " + clusteringCols.get(i).getName());
            }
            sb.append(");\n\t}\n\n");
        }

        sb.append("}\n");

        String fname = output_dir
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "java"
                + File.separator + "hessian"
                + File.separator + "ambien"
                + File.separator + "controller"
                + File.separator + Ambien.capName(name) + "RestController.java";
        return Ambien.writeFile(fname, sb.toString());
    }
}
