/*
 * Copyright 2015 Brian Hess
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hessian.ambien;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;


import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class Ambien {
    private String version = "0.0.1";
    private AmbienParams params = new AmbienParams();

    private Cluster cluster = null;
    private Session session = null;

    private String cqlSchema = null;
    private String filename = null;

    private String usage() {
        StringBuilder usage = new StringBuilder("version: ").append(version).append("\n");
        usage.append("Usage: ambien -host <hostname> -k <keyspaceName> -t <tableName> -o <outputDir> [options]\n");
        usage.append(AmbienParams.usage());
        return usage.toString();
    }

    private SSLOptions createSSLOptions()
        throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,
            KeyManagementException, CertificateException, UnrecoverableKeyException {
        TrustManagerFactory tmf = null;
        if (null != params.truststorePath) {
            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(new FileInputStream(new File(params.truststorePath)),
                    params.truststorePwd.toCharArray());
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(tks);
        }

        KeyManagerFactory kmf = null;
        if (null != params.keystorePath) {
            KeyStore kks = KeyStore.getInstance("JKS");
            kks.load(new FileInputStream(new File(params.keystorePath)),
                    params.keystorePwd.toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(kks, params.keystorePwd.toCharArray());
        }

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf != null? kmf.getKeyManagers() : null,
                        tmf != null ? tmf.getTrustManagers() : null,
                        new SecureRandom());

        return RemoteEndpointAwareJdkSSLOptions.builder().withSSLContext(sslContext).build();
    }

    private void setup()
        throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
               CertificateException, UnrecoverableKeyException  {
        // Connect to Cassandra
        Cluster.Builder clusterBuilder = Cluster.builder()
            .addContactPoint(params.host)
            .withPort(params.port)
            .withLoadBalancingPolicy(new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build()));
        if (null != params.username)
            clusterBuilder = clusterBuilder.withCredentials(params.username, params.password);
        if (null != params.truststorePath)
            clusterBuilder = clusterBuilder.withSSL(createSSLOptions());

        cluster = clusterBuilder.build();
        if (null == cluster) {
            throw new IOException("Could not create cluster");
        }
        session = cluster.connect();
    }

    private boolean cleanup(boolean retval) {
        cleanup();
        return retval;
    }

    private void cleanup() {
        if (null != session)
            session.close();
        if (null != cluster)
            cluster.close();
    }
    
    private boolean run(String[] args)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
               CertificateException, UnrecoverableKeyException {
        if (!params.parseArgs(args)) {
            System.err.println("Bad arguments");
            System.err.println(usage());
            return false;
        }
        System.err.println("Running with: " + params);

        // Setup
        setup();
        File outFile = new File(params.output_dir);
        if (!outFile.isDirectory()) {
            System.err.println("Output directory must be a directory");
            return cleanup(false);
        }
        if (!outFile.exists()) {
            System.err.println("Output directory does not exist");
            return cleanup(false);
        }

        // Produce Boilerplate (pom.xml, etc)
        AmbienBoilerplate ab = new AmbienBoilerplate(params);
        if (!ab.produceBoilerplate()) {
            System.err.println("Had trouble producing boilerplate");
            return cleanup(false);
        }

        List<String> restList = new ArrayList<String>();
        Metadata m = cluster.getMetadata();
        CodecRegistry cr = cluster.getConfiguration().getCodecRegistry();
        for (int idx = 0; idx < params.keyspace_name.size(); idx++) {
            // Get Metadata for Table
            String ksname = params.keyspace_name.get(idx);
            String tblname = params.table_name.get(idx);
            KeyspaceMetadata km = m.getKeyspace(ksname);
            if (null == km) {
                System.err.println("Keyspace " + ksname + " not found");
                return cleanup(false);
            }
            TableMetadata tm = km.getTable(tblname);
            if (null == tm) {
                System.err.println("Table " + tblname + " not found");
                return cleanup(false);
            }
            List<ColumnMetadata> clusteringCols = tm.getClusteringColumns();
            List<ColumnMetadata> partitionCols = tm.getPartitionKey();
            List<ColumnMetadata> regularCols = tm.getColumns();
            regularCols.removeAll(clusteringCols);
            regularCols.removeAll(partitionCols);

            // Produce Domain Classes
            AmbienDomain ad = new AmbienDomain(ksname, tblname, partitionCols, clusteringCols, regularCols, cr, params.srcDomainDir, params);
            if (!ad.produceDomainClasses()) {
                System.err.println("Had trouble producing domain classes");
                return cleanup(false);
            }

            // Produce Repository Classes
            AmbienRepository ar = new AmbienRepository(ksname, tblname, params, partitionCols, clusteringCols, regularCols, cr, restList);
            if (!ar.produceRepositoryClasses()) {
                System.err.println("Had trouble producing repository and controller classes");
                return cleanup(false);
            }
        }

        // Produce Index and Error pages
        if (!producePage("<h1>Welcome!</h1>\n<h2>This API brought to you by <i>Vested Interests</i></h2>\n",
                params.resourcesTemplatesDir + File.separator + "index.html", restList)) return false;
        if (!producePage("<h1>&#x1F627 Something went wrong...</h1>\n\n",
                params.resourcesTemplatesDir + File.separator + "error.html", restList)) return false;

        cleanup();
        return true;
    }

    public static void main(String[] args) 
        throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
               CertificateException, KeyManagementException {
        Ambien a = new Ambien();
        boolean success = a.run(args);
        if (success) {
            System.exit(0);
        } else {
            System.err.println("There was an error");
            System.exit(-1);
        }
    }


    public static boolean writeFile(String path, String contents) {
        File tfile = new File(path);
        if (tfile.exists()) {
            System.err.println("File " + path + " already exists");
            return false;
        }
        try {
            if (!tfile.createNewFile()) return false;
        } catch (IOException e) {
            System.err.println("Could not create file (" + path + ")");
            e.printStackTrace();
            return false;
        }

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(tfile));
            ps.print(contents);
        } catch (FileNotFoundException e) {
            System.err.println("Could not write contents (" + path + ")");
            return false;
        }

        return true;
    }

    public static String capName(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String restList(List<String> restEndpoints) {
        StringBuilder sb = new StringBuilder("<ul>\n");
        for (String s : restEndpoints) {
            sb.append("<li>" + s + "</li>\n");
        }
        sb.append("</ul>\n");
        return sb.toString();
    }

    private boolean producePage(String note, String fname, List<String> restList) {
        String page = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                note +
                "<h2>These are the supported REST endpoints</h2>\n" +
                restList(restList) +
                "<p><a href=\"/actuator/\">The Actuator</a></p>\n" +
                "</body>\n" +
                "</html>\n";
        return Ambien.writeFile(fname, page);
    }
}

