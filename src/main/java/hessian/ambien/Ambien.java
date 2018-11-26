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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Ambien {
    private String version = "0.0.1";
    /*
    private String host = null;
    private int port = 9042;
    private String username = null;
    private String password = null;
    private String truststorePath = null;
    private String truststorePwd = null;
    private String keystorePath = null;
    private String keystorePwd = null;
    private String table_name = null;
    private String keyspace_name = null;
    private String output_dir = null;
    */
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
        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load((InputStream) new FileInputStream(new File(params.truststorePath)),
                params.truststorePwd.toCharArray());
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);

        KeyManagerFactory kmf = null;
        if (null != params.keystorePath) {
            KeyStore kks = KeyStore.getInstance("JKS");
            kks.load((InputStream) new FileInputStream(new File(params.keystorePath)),
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

    private void cleanup() {
        if (null != session)
            session.close();
        if (null != cluster)
            cluster.close();
    }
    
    public boolean run(String[] args) 
        throws IOException, ParseException, InterruptedException, ExecutionException,
               KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
               CertificateException, UnrecoverableKeyException {
        if (false == params.parseArgs(args)) {
            System.err.println("Bad arguments");
            System.err.println(usage());
            return false;
        }

        // Setup
        setup();
        File outFile = new File(params.output_dir);
        if (!outFile.isDirectory()) {
            System.err.println("Output directory must be a directory");
            return false;
        }
        if (!outFile.exists()) {
            System.err.println("Output directory does not exist");
            return false;
        }

        // Get Metadata for Table
        Metadata m = cluster.getMetadata();
        KeyspaceMetadata km = m.getKeyspace(params.keyspace_name);
        if (null == km) {
            System.err.println("Keyspace " + params.keyspace_name + " not found");
            return false;
        }
        TableMetadata tm = km.getTable(params.table_name);
        if (null == tm) {
            System.err.println("Table " + params.table_name + " not found");
            return false;
        }
        List<ColumnMetadata> clusteringCols = tm.getClusteringColumns();
        List<ColumnMetadata> partitionCols = tm.getPartitionKey();
        List<ColumnMetadata> regularCols = tm.getColumns();
        regularCols.removeAll(clusteringCols);
        regularCols.removeAll(partitionCols);
        CodecRegistry cr = cluster.getConfiguration().getCodecRegistry();

        // Produce Boilerplate (pom.xml, etc)
        AmbienBoilerplate ab = new AmbienBoilerplate(params);
        if (!ab.produceBoilerplate()) return false;

        // Produce Domain Classes
        AmbienDomain ad = new AmbienDomain(params, partitionCols, clusteringCols, regularCols, cr);
        if(!ad.produceDomainClasses()) return false;

        // Produce Repository Classes
        AmbienRepository ar = new AmbienRepository(params, partitionCols, clusteringCols, regularCols, cr);
        if(!ar.produceRepositoryClasses()) return false;

        // Produce Controller Classes
        AmbienController ac = new AmbienController(params, partitionCols, clusteringCols, regularCols, cr);
        if(!ac.produceControllerClasses()) return false;

        return true;
    }

    public static void main(String[] args) 
        throws IOException, ParseException, InterruptedException, ExecutionException,
               KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
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
        if (tfile.exists()) return false;
        try {
            tfile.createNewFile();
        } catch (IOException e) {
            System.err.println("Could not create file (" + path + ")");
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


}
