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
    private String host = null;
    private int port = 9042;
    private String username = null;
    private String password = null;
    private String truststorePath = null;
    private String truststorePwd = null;
    private String keystorePath = null;
    private String keystorePwd = null;
    private Cluster cluster = null;
    private Session session = null;
    private String table_name = null;
    private String keyspace_name = null;
    private String output_dir = null;

    private String cqlSchema = null;
    private String filename = null;

    private String usage() {
        StringBuilder usage = new StringBuilder("version: ").append(version).append("\n");
        return usage.toString();
    }
    
    private boolean validateArgs() {
        if (null == host) {
            System.err.println("No host provided.");
            return false;
        }
        if (null == table_name) {
            System.err.println("No table name provided.");
            return false;
        }

        if (null == keyspace_name) {
            System.err.println("No keyspace name provided.");
            return false;
        }

        if (null == output_dir) {
            System.err.println("No output directory provided.");
            return false;
        }

        return true;
    }
    
    private boolean processConfigFile(String fname, Map<String, String> amap)
        throws IOException, FileNotFoundException {
        File cFile = new File(fname);
        if (!cFile.isFile()) {
            System.err.println("Configuration File must be a file");
            return false;
        }

        BufferedReader cReader = new BufferedReader(new FileReader(cFile));
        String line;
        while ((line = cReader.readLine()) != null) {
            String[] fields = line.trim().split("\\s+");
            if (2 != fields.length) {
                System.err.println("Bad line in config file: " + line);
                return false;
            }
            if (null == amap.get(fields[0])) {
                amap.put(fields[0], fields[1]);
            }
        }
        return true;
    }

    private boolean parseArgs(String[] args)
        throws IOException, FileNotFoundException {
        String tkey;
        if (args.length == 0) {
            System.err.println("No arguments specified");
            return false;
        }
        if (0 != args.length % 2)
            return false;

        Map<String, String> amap = new HashMap<String,String>();
        for (int i = 0; i < args.length; i+=2) {
            amap.put(args[i], args[i+1]);
        }

        if (null != (tkey = amap.remove("-configFile")))
            if (!processConfigFile(tkey, amap))
                return false;

        host = amap.remove("-host");
        if (null == host) { // host is required
            System.err.println("Must provide a host");
            return false;
        }

        if (null != (tkey = amap.remove("-port")))          port = Integer.parseInt(tkey);
        if (null != (tkey = amap.remove("-user")))          username = tkey;
        if (null != (tkey = amap.remove("-pw")))            password = tkey;
        if (null != (tkey = amap.remove("-ssl-truststore-path"))) truststorePath = tkey;
        if (null != (tkey = amap.remove("-ssl-truststore-pw")))  truststorePwd =  tkey;
        if (null != (tkey = amap.remove("-ssl-keystore-path")))   keystorePath = tkey;
        if (null != (tkey = amap.remove("-ssl-keystore-pw")))    keystorePwd = tkey;
        if (null != (tkey = amap.remove(("-t"))))               table_name = tkey;
        if (null != (tkey = amap.remove(("-k"))))               keyspace_name = tkey;
        if (null != (tkey = amap.remove(("-o"))))               output_dir = tkey;


        return validateArgs();
    }

    /*
    private SSLOptions createSSLOptions()
        throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException,
            KeyManagementException, CertificateException, UnrecoverableKeyException {
        TrustManagerFactory tmf = null;
        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load((InputStream) new FileInputStream(new File(truststorePath)),
                 truststorePwd.toCharArray());
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);

        KeyManagerFactory kmf = null;
        if (null != keystorePath) {
            KeyStore kks = KeyStore.getInstance("JKS");
            kks.load((InputStream) new FileInputStream(new File(keystorePath)),
                     keystorePwd.toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(kks, keystorePwd.toCharArray());
        }

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf != null? kmf.getKeyManagers() : null,
                        tmf != null ? tmf.getTrustManagers() : null,
                        new SecureRandom());

        return RemoteEndpointAwareJdkSSLOptions.builder().withSSLContext(sslContext).build();
    }
    */

    private void setup()
        throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
               CertificateException, UnrecoverableKeyException  {
        // Connect to Cassandra
        Cluster.Builder clusterBuilder = Cluster.builder()
            .addContactPoint(host)
            .withPort(port)
            .withLoadBalancingPolicy(new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build()));
        if (null != username)
            clusterBuilder = clusterBuilder.withCredentials(username, password);
        //if (null != truststorePath)
          //  clusterBuilder = clusterBuilder.withSSL(createSSLOptions());

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
        if (false == parseArgs(args)) {
            System.err.println("Bad arguments");
            System.err.println(usage());
            return false;
        }

        // Setup
        setup();
        File outFile = new File(output_dir);
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
        KeyspaceMetadata km = m.getKeyspace(keyspace_name);
        if (null == km) {
            System.err.println("Keyspace " + keyspace_name + " not found");
            return false;
        }
        TableMetadata tm = km.getTable(table_name);
        if (null == tm) {
            System.err.println("Table " + table_name + " not found");
            return false;
        }
        List<ColumnMetadata> clusteringCols = tm.getClusteringColumns();
        List<ColumnMetadata> partitionCols = tm.getPartitionKey();
        List<ColumnMetadata> regularCols = tm.getColumns();
        regularCols.removeAll(clusteringCols);
        regularCols.removeAll(partitionCols);
        CodecRegistry cr = cluster.getConfiguration().getCodecRegistry();

        // Produce Boilerplate (pom.xml, etc)
        AmbienBoilerplate ab = new AmbienBoilerplate(output_dir, host, keyspace_name);
        if (!ab.produceBoilerplate()) return false;

        // Produce Domain Classes
        AmbienDomain ad = new AmbienDomain(table_name, output_dir, partitionCols, clusteringCols, regularCols, cr);
        if(!ad.produceDomainClasses()) return false;

        // Produce Repository Classes
        AmbienRepository ar = new AmbienRepository(table_name, output_dir, partitionCols, clusteringCols, regularCols, cr);
        if(!ar.produceRepositoryClasses()) return false;

        // Produce Controller Classes
        AmbienController ac = new AmbienController(table_name, output_dir, partitionCols, clusteringCols, regularCols, cr);
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

