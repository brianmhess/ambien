package hessian.ambien;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnMetadata;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmbienRepository {
    private AmbienParams params;
    private String table_name;
    private String keyspace_name;
    private String cap_name;
    private String camel_name;
    private CodecRegistry cr;
    private List<ColumnMetadata> partitionCols;
    private List<ColumnMetadata> clusteringCols;
    private List<ColumnMetadata> regularCols;
    private boolean allowAllowFiltering;
    private String queryBuilderBase;
    private String[] ineq = {"lt", "lte", "gt", "gte"};
    private List<String> bases;
    private List<String> restEndpoints;
    private String endpointPrefix;


    public AmbienRepository(String keyspace_name, String table_name, AmbienParams params,
                            List<ColumnMetadata> partitionCols, List<ColumnMetadata> clusteringCols,
                            List<ColumnMetadata> regularCols, CodecRegistry cr, List<String> restEndpoints) {
        this(keyspace_name, table_name, params, partitionCols, clusteringCols, regularCols, cr, false, restEndpoints);
    }

    public AmbienRepository(String keyspace_name, String table_name, AmbienParams params,
                            List<ColumnMetadata> partitionCols, List<ColumnMetadata> clusteringCols,
                            List<ColumnMetadata> regularCols, CodecRegistry cr, boolean allowAllowFiltering, List<String> restEndpoints) {
        this.params = params;
        this.partitionCols = partitionCols;
        this.clusteringCols = clusteringCols;
        this.regularCols = regularCols;
        this.cr = cr;
        this.allowAllowFiltering = allowAllowFiltering;
        this.table_name = table_name;
        this.keyspace_name = keyspace_name;
        this.cap_name = Ambien.capName(keyspace_name) + Ambien.capName(table_name);
        this.camel_name = keyspace_name + Ambien.capName(table_name);
        queryBuilderBase = "QueryBuilder.select().all().from(\"" + keyspace_name + "\", \"" + table_name + "\")";
        bases = new ArrayList<>();
        this.restEndpoints = restEndpoints;
    }

    public boolean produceRepositoryClasses() {
        if (!produceBaseRepositoryClass()) return false;

        return true;
    }

    private String typeFor(ColumnMetadata cm) {
        return cr.codecFor(cm.getType()).getJavaType().getRawType().getName();
    }

    private boolean produceBaseRepositoryClass() {
        StringBuilder sbr = new StringBuilder();
        StringBuilder sbc = new StringBuilder();
        int i, j;
        genRepositoryHeader(sbr);
        genControllerHeader(sbc);

        // Save
        sbr.append("\t// Save\n");
        sbr.append("\tpublic " + cap_name + " save(" + cap_name + " x) {\n");
        sbr.append("\t\tmapper.save(x);\n");
        sbr.append("\t\treturn x;\n");
        sbr.append("\t}\n\n");

        endpointPrefix = params.endpointRoot(keyspace_name, table_name) + "/";
        String endpoint = endpointPrefix + "add";
        restEndpoints.add(endpoint);
        sbc.append("\t// Add new\n");
        sbc.append("\t@RequestMapping(value = \"" + endpoint + "\", method = RequestMethod.POST)\n" +
                "\tpublic " + cap_name + " save(");
        sbc.append("@RequestParam String " + partitionCols.get(0).getName());
        for (i = 1; i < partitionCols.size(); i++)
            sbc.append(", @RequestParam String " + partitionCols.get(i).getName());
        for (i = 0; i < clusteringCols.size(); i++)
            sbc.append(", @RequestParam String " + clusteringCols.get(i).getName());
        for (i = 0; i < regularCols.size(); i++)
            sbc.append(", @RequestParam(required = false) String " + regularCols.get(i).getName());
        sbc.append(") throws ParseException {\n");
        sbc.append("\t\t" + cap_name + " " + camel_name + " = new " + cap_name + "(anyParser.parse(" + partitionCols.get(0).getName() + ", " + typeFor(partitionCols.get(0)) + ".class)");
        for (i = 1; i < partitionCols.size(); i++)
            sbc.append(", anyParser.parse(" + partitionCols.get(i).getName() + ", " + typeFor(partitionCols.get(i)) + ".class)");
        for (i = 0; i < clusteringCols.size(); i++)
            sbc.append(", anyParser.parse(" + clusteringCols.get(i).getName() + ", " + typeFor(clusteringCols.get(i)) + ".class)");
        for (i = 0; i < regularCols.size(); i++)
            sbc.append(", anyParser.parse(" + regularCols.get(i).getName() + ", " + typeFor(regularCols.get(i)) + ".class)");
        sbc.append(");\n");
        sbc.append("\t\t" + camel_name + "Repository.save(" + camel_name + ");\n");
        sbc.append("\t\treturn " + camel_name + ";\n");
        sbc.append("\t}\n\n");

        // Delete
        sbr.append("\tpublic void delete(");
        sbr.append(typeFor(partitionCols.get(0)) + " " + partitionCols.get(0).getName());
        for (i = 1; i < partitionCols.size(); i++)
            sbr.append(", " + typeFor(partitionCols.get(i)) + " " + partitionCols.get(i).getName());
        for (i = 0; i < clusteringCols.size(); i++)
            sbr.append(", " + typeFor(clusteringCols.get(i)) + " " + clusteringCols.get(i).getName());
        sbr.append(") {\n");
        sbr.append("\t\tmapper.delete(");
        sbr.append(partitionCols.get(0).getName());
        for (i = 1; i < partitionCols.size(); i++)
            sbr.append(", " + partitionCols.get(i).getName());
        for (i = 0; i < clusteringCols.size(); i++)
            sbr.append(", " + clusteringCols.get(i).getName());
        sbr.append(");\n");
        sbr.append("\t}\n\n");

        endpoint = endpointPrefix + "delete";
        restEndpoints.add(endpoint);
        sbc.append("\t// Delete\n");
        sbc.append("\t@RequestMapping(value = \"" + endpoint + "\", method = RequestMethod.POST)\n" +
                "\tpublic void delete(");
        sbc.append("@RequestParam String " + partitionCols.get(0).getName());
        for (i = 1; i < partitionCols.size(); i++)
            sbc.append(", @RequestParam String " + partitionCols.get(i).getName());
        for (i = 0; i < clusteringCols.size(); i++)
            sbc.append(", @RequestParam String " + clusteringCols.get(i).getName());
        sbc.append(") throws ParseException {\n");
        sbc.append("\t\t" + camel_name + "Repository.delete(");
        sbc.append("anyParser.parse(" + partitionCols.get(0).getName() + ", " + typeFor(partitionCols.get(0)) + ".class)");
        for (i = 1; i < partitionCols.size(); i++)
            sbc.append(", anyParser.parse(" + partitionCols.get(i).getName() + ", " + typeFor(partitionCols.get(i)) + ".class)");
        for (i = 0; i < clusteringCols.size(); i++)
            sbc.append(", anyParser.parse(" + clusteringCols.get(i).getName() + ", " + typeFor(clusteringCols.get(i)) + ".class)");
        sbc.append(");\n");
        sbc.append("\t}\n\n");

        /*
        sbc.append("@RequestBody " + cap_name + " " + name + ") {\n" +
                "\t\t" + name + "Repository.delete(" + name + ");\n" +
                "\t}\n\n");
                */


        // Selects....
        String base;
        String comment;
        String path;
        String pathvars;

        // Find All
        base = "findAll";
        bases.add(base);
        sbr.append("\t// Find All\n");
        sbr.append("\tprivate static BuiltStatement built_" + base + " = " + queryBuilderBase + ";\n");
        sbr.append("\tprivate PreparedStatement ps_" + base + ";\n");
        sbr.append("\tpublic List<" + cap_name + "> " + base + "() {\n");
        sbr.append("\t\tBoundStatement bs = ps_" + base + ".bind();\n");
        sbr.append("\t\treturn mapper.map(session.execute(bs)).all();\n");
        sbr.append("\t}\n\n");

        //    Controller
        endpoint = endpointPrefix + "all";
        restEndpoints.add(endpoint);
        sbc.append("\t// Find all\n");
        sbc.append("\t@RequestMapping(\"" + endpoint + "\")\n\tpublic List<" + cap_name + "> all() {\n");
        sbc.append("\t\treturn (ArrayList<" + cap_name + ">)" + camel_name + "Repository.findAll();\n\t}\n\n");

        // Find Some
        base = "findSome";
        bases.add(base);
        sbr.append("\t// Find Some\n");
        sbr.append("\tprivate static BuiltStatement built_" + base + " = " + queryBuilderBase + ".limit(bindMarker(\"lmt\"));\n");
        sbr.append("\tprivate PreparedStatement ps_" + base + ";\n");
        sbr.append("\tpublic List<" + cap_name + "> " + base + "(Integer some) {\n");
        sbr.append("\t\tBoundStatement bs = ps_" + base + ".bind();\n");
        sbr.append("\t\tbs.set(\"lmt\", some, Integer.class);\n");
        sbr.append("\t\treturn mapper.map(session.execute(bs)).all();\n");
        sbr.append("\t}\n\n");

        //    Controller
        endpoint = endpointPrefix + "some";
        sbc.append("\t// Find Some\n");
        restEndpoints.add(endpoint + "/{some}");
        sbc.append("\t@RequestMapping(\"" + endpoint + "/{some}\")\n\tpublic List<" + cap_name + "> someGet(@PathVariable int some) {\n");
        sbc.append("\t\treturn (ArrayList<" + cap_name + ">)" + camel_name + "Repository.findSome(some);\n\t}\n\n");
        restEndpoints.add(endpoint + "?some={some}");
        sbc.append("\t@RequestMapping(value = \"" + endpoint + "\", method = {RequestMethod.POST, RequestMethod.GET})\n\tpublic List<" + cap_name + "> somePost(@RequestParam int some) {\n");
        sbc.append("\t\treturn (ArrayList<" + cap_name + ">)" + camel_name + "Repository.findSome(some);\n\t}\n\n");


        // Find By Partition Key
        List<Pair<String,String>> cols = new ArrayList<>();
        base = "findBy";
        comment = "\t// Find By";
        path = "";
        pathvars = "";

        for (i = 0; i < partitionCols.size(); i++) {
            String name = partitionCols.get(i).getName();
            String type = typeFor(partitionCols.get(i));
            base = base + ((0 == i) ? "" : "And") + Ambien.capName(name);
            comment = comment + ((0 == i) ? " " : " and") + name;
            path = path + ((0 == i) ? "" : "_") + name;
            pathvars = pathvars + "/{" + name + "}";
            cols.add(new Pair<>(name, type));
        }
        bases.add(base);
        sbr.append("\t// Find By Partition Key\n");
        sbr.append(comment + "\n");

        //    Controller
        sbc.append("\t// Find By Partition Key\n");
        genFunction(sbr, sbc, base, cols, path, pathvars, false);

        if (allowAllowFiltering) {
            sbr.append("\t// Regular Columns with ALLOW FILTERING\n");
            genRegularColumns(sbr, sbc, base, cols, regularCols, path, pathvars);
        }

        // Find By Partition Key and some Clustering Columns
        sbr.append("\t// Find By Partition Key and some Clustering Columns\n");
        sbc.append("\t// Find By Partition Key and some Clustering Columns\n");
        for (j = 0; j < clusteringCols.size(); j++) {
            String name = clusteringCols.get(j).getName();
            String type = typeFor(clusteringCols.get(j));
            base = base + "And" + Ambien.capName(name);
            comment = comment + " and " + name;
            path = path + "_" + name;
            pathvars = pathvars + "/{" + name + "}";
            cols.add(new Pair<>(name, type));
            bases.add(base);
            sbr.append(comment + "\n");
            genFunction(sbr, sbc, base, cols, path, pathvars, false);
            sbr.append("\t// With Inequality\n");
            sbc.append("\t// With Inequality\n");
            genFunctionInequality(sbr, sbc, base, cols, path, pathvars);

            if (allowAllowFiltering) {
                sbr.append("\t// Regular Columns with ALLOW FILTERING\n");
                sbc.append("\t// Regular Columns with ALLOW FILTERING\n");
                genRegularColumns(sbr, sbc, base, cols, regularCols, path, pathvars);
            }
        }

        // Post Construct Init
        sbr.append("\t@PostConstruct\n");
        sbr.append("\tprivate void init() {\n");
        sbr.append("\t\tthis.mapper = mappingManager.mapper(" + cap_name + ".class);\n");
        sbr.append("\t\tthis.mapper.setDefaultSaveOptions(Mapper.Option.saveNullFields(false));\n");
        sbr.append("\t\tthis.session = mappingManager.getSession();\n");
        //    Prepare all queries
        for (String b : bases) {
            sbr.append("\t\tps_" + b + " = session.prepare(built_" + b + ");\n");
        }
        sbr.append("\t}\n\n");

        sbr.append("}\n");
        sbc.append("}\n");

        String fnamer = params.srcRepositoryDir + File.separator + cap_name + "Repository.java";
        String fnamec = params.srcControllerDir + File.separator + cap_name + "RestController.java";
        return Ambien.writeFile(fnamer, sbr.toString()) && Ambien.writeFile(fnamec, sbc.toString());
    }

    private void genRepositoryHeader(StringBuilder sbr) {
        sbr.append("package " + params.package_name + ".repository;\n" +
                "\n" +
                "import " + params.package_name + ".domain." + cap_name + ";\n" +
                "\n" +
                "import com.datastax.driver.core.BoundStatement;\n" +
                "import com.datastax.driver.core.Session;\n" +
                "import com.datastax.driver.core.PreparedStatement;\n" +
                "import com.datastax.driver.core.querybuilder.BuiltStatement;\n" +
                "import com.datastax.driver.core.querybuilder.QueryBuilder;\n" +
                "import com.datastax.driver.mapping.MappingManager;\n" +
                "import com.datastax.driver.mapping.Mapper;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "import javax.annotation.PostConstruct;\n" +
                "import java.util.List;\n" +
                "\n" +
                "import static com.datastax.driver.core.querybuilder.QueryBuilder.*;\n" +
                "\n" +
                "@Repository\n" +
                "public class " + cap_name + "Repository{\n");

        // Member Variables
        sbr.append("\tprivate MappingManager mappingManager;\n");
        sbr.append("\tprivate Mapper<" + cap_name + "> mapper;\n");
        sbr.append("\tprivate Session session;\n");
        sbr.append("\n");

        // Constructor
        sbr.append("\t@Autowired\n");
        sbr.append("\tpublic " + cap_name + "Repository(MappingManager mappingManager) {\n");
        sbr.append("\t\tthis.mappingManager = mappingManager;\n");
        sbr.append("\t}\n\n");
    }

    private void genControllerHeader(StringBuilder sbc) {
        sbc.append("package " + params.package_name + ".controller;\n" +
                "\n" +
                "import " + params.package_name + ".domain." + cap_name + ";\n" +
                "import " + params.package_name + ".repository." + cap_name + "Repository;\n" +
                "import hessian.typeparser.AnyParser;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import java.text.ParseException;\n" +
                "\n" +
                "@RestController\n" +
                "public class " + cap_name + "RestController {\n");
        sbc.append("\t@Autowired\n\tprivate " + cap_name + "Repository " + camel_name + "Repository;\n\n");
        sbc.append("\tprivate AnyParser anyParser = new AnyParser();\n\n");

        // Hello
        sbc.append("\t@RequestMapping(\"" + params.endpointRoot(keyspace_name,table_name) + "/hello\")\n" +
                "\tpublic String hello() {\n" +
                "\t\treturn \"<html><body><H1>Hello World</H1></body></html>\";\n" +
                "\t}\n\n");
    }

    private void genFunction(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                             String path, String pathvars, boolean allowFiltering) {
        genFunction(sbr, sbc, base, cols, path, pathvars, allowFiltering, "eq");
    }

    private void genFunction(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                             String path, String pathvars, boolean allowFiltering, String ineq) {
        genFunctionPostAndGet(sbr, sbc, base, cols, path, pathvars, allowFiltering, ineq);
    }

    private void genFunctionGet(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                               String path, String pathvars, boolean allowFiltering, String ineq) {
        genFunction(sbr, sbc, base, cols, path, pathvars, allowFiltering, ineq, "@PathVariable", "RequestMethod.GET");
    }

    private void genFunctionPost(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                                String path, String pathvars, boolean allowFiltering, String ineq) {
        genFunction(sbr, sbc, base, cols, path, "", allowFiltering, ineq, "@RequestParam", "RequestMethod.POST");
    }

    private void genFunctionPostAndGet(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                                 String path, String pathvars, boolean allowFiltering, String ineq) {
        genFunction(sbr, sbc, base, cols, path, "", allowFiltering, ineq, "@RequestParam", "{RequestMethod.POST, RequestMethod.GET}");
    }

    private void genFunction(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                                String path, String pathvars, boolean allowFiltering, String ineq, String varPrefix,
                                String requestMethod) {
        // Repository
        sbr.append("\tprivate static BuiltStatement built_" + base + " = " + queryBuilderBase);
        if (1 == cols.size()) {
            sbr.append(".where(" + ineq + "(\"" + cols.get(0).getKey() + "\", bindMarker(\"" + cols.get(0).getKey() + "\")))");
        }
        else {
            sbr.append(".where(eq(\"" + cols.get(0).getKey() + "\", bindMarker(\"" + cols.get(0).getKey() + "\")))");
            for (int i = 1; i < cols.size() - 1; i++) {
                sbr.append(".and(eq(\"" + cols.get(i).getKey() + "\", bindMarker(\"" + cols.get(i).getKey() + "\")))");
            }
            sbr.append(".and(" + ineq + "(\"" + cols.get(cols.size() - 1).getKey() + "\", bindMarker(\"" + cols.get(cols.size() - 1).getKey() + "\")))");
        }
        sbr.append(allowFiltering ? ".allowFiltering()" : "");
        sbr.append(";\n");
        sbr.append("\tprivate PreparedStatement ps_" + base + ";\n");
        sbr.append("\tpublic List<" + cap_name + "> " + base + " (");
        sbr.append(cols.get(0).getValue() + " " + cols.get(0).getKey());
        for (int i = 1; i < cols.size(); i++) {
            sbr.append(", " + cols.get(i).getValue() + " " + cols.get(i).getKey());
        }
        sbr.append(") {\n");
        sbr.append("\t\tBoundStatement bs = ps_" + base + ".bind();\n");
        for (int i = 0; i < cols.size(); i++) {
            sbr.append("\t\tbs.set(\"" + cols.get(i).getKey() + "\", " + cols.get(i).getKey() + ", " + cols.get(i).getValue() + ".class);\n");
        }
        sbr.append("\t\treturn mapper.map(session.execute(bs)).all();\n");
        sbr.append("\t}\n\n");


        // Controller
        String endpoint = endpointPrefix + path + pathvars;
        restEndpoints.add(endpoint);
        sbc.append("\t@RequestMapping(value = \"" + endpoint + "\", method = " + requestMethod + ")\n");
        sbc.append("\tpublic List<" + cap_name + "> " + base);
        sbc.append("(" + varPrefix + " String " + cols.get(0).getKey());
        for (int i = 1; i < cols.size(); i++) {
            sbc.append(", " + varPrefix + " String " + cols.get(i).getKey());
        }
        sbc.append(") throws ParseException  {\n");
        sbc.append("\t\treturn (ArrayList<" + cap_name + ">)" + camel_name + "Repository." + base + "(");
        sbc.append("anyParser.parse(" + cols.get(0).getKey() + ", " + cols.get(0).getValue() + ".class)");
        for (int i = 1; i < cols.size(); i++) {
            sbc.append(", anyParser.parse(" + cols.get(i).getKey() + ", " + cols.get(i).getValue() + ".class)");
        }
        sbc.append(");\n\t}\n\n");
    }

    private void genFunctionInequality(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                                       String path, String pathvars) {
        for (String s : ineq) {
            sbr.append("// " + s + " " + cols.get(cols.size()-1).getKey() + "\n");
            bases.add(base + "_" + s);
            genFunction(sbr, sbc, base + "_" + s, cols, path + "_" + s, pathvars, false, s);
        }
    }

    private void genRegularColumns(StringBuilder sbr, StringBuilder sbc, String base, List<Pair<String,String>> cols,
                                   List<ColumnMetadata> regCols, String path, String pathvars) {
        String jbase;
        String jpath;
        String jpathvars;
        List<Pair<String,String>> jcols = new ArrayList<>();
        for (int j = 0; j < regCols.size(); j++) {
            String jname = regCols.get(j).getName();
            String jtype = typeFor(regCols.get(j));
            jcols.add(new Pair<>(jname, jtype));
        }
        for (int j = 1; j <= regCols.size(); j++) {
            for (List<Pair<String, String>> tjcol : Lists.partition(jcols, j)) {
                jbase = base;
                jpath = path;
                jpathvars = pathvars;
                for (int k = 0; k < tjcol.size(); k++) {
                    String name = tjcol.get(k).getKey();
                    jbase = jbase + "And" + Ambien.capName(name);
                    jpath = jpath + "_" + name;
                    jpathvars = jpathvars + "/{" + name + "}";
                }
                bases.add(jbase);
                genFunction(sbr, sbc, jbase, Stream.concat(cols.stream(), tjcol.stream()).collect(Collectors.toList()),
                        jpath, jpathvars, true);
            }
        }
    }
}
