package idac.sm_rpi_based_sensors_v2.helpers;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MQTTConfig {

	/**
	 * The Properties instance where the properties file is loaded into memory.
	 */
	public static Properties prop;
	
	public static String server, user, password;
	public static int port;
	public static SSLSocketFactory socketFactory;
	public static TrustManagerFactory trustManagerFactory;
	
	static {
		try {
			InputStream is = null;

			// First try to load a custom properties file. This file is generated when a
			// property is modified. Also, the user can save the file manually in the same
			// folder as the JAR and add the desired values.
			String propertiesPath = "MQTT.properties";
			
			File f = new File(propertiesPath);
			if (f.exists() && !f.isDirectory()) {
				System.out.println("Attempting to find the parameters definition 'properties' file in " + propertiesPath);
				is = new FileInputStream(f);
			} else { // If there is no modified properties file, load the default properties file.
				is = ParameterConfig.class.getResourceAsStream("/MQTT.properties");
			}

			if (is == null) {
				System.out.println("ERROR: Could not find the parameters definition 'properties' file.");
			} else {
				prop = new Properties();
				prop.load(is); // Load properties file into memory
				
				// Close the input stream and release memory
				is.close();
				is = null;
				
				String activeConnection = prop.getProperty("active_connection");

				int connections = Integer.parseInt(prop.getProperty("connections"));
				String host = null;
				try (BufferedReader br = new BufferedReader(new FileReader("/home/pi/Desktop/working_directory/host.config"))) {
				    host = br.readLine();
				}
				if (host != null) {
					System.out.println("Host file found, host is: " + host + ". There are "+connections+" possible connections.");
					for (int i = 1; i <= connections; ++i) {
						if(prop.getProperty(i+".server").equals(host)) {
							activeConnection = Integer.toString(i);
							System.out.println("Hosts match");
							break;
						}
					}
				}
				else {
					System.out.println("Host file not found");					
				}

				server = prop.getProperty(activeConnection+".server");
				System.out.println("Connecting to server: " + server);
				user = prop.getProperty(activeConnection+".user");
				password = prop.getProperty(activeConnection+".password");
				port = Integer.parseInt(prop.getProperty(activeConnection+".port"));
				
				String certProperty = prop.getProperty(activeConnection+".cert_file");
				System.out.println(certProperty);
				
				if (!certProperty.equals("")) {
			        InputStream caCrtInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(certProperty);
					
			        Security.addProvider(new BouncyCastleProvider());
	
					// load CA certificate
					X509Certificate caCert = null;
	
					BufferedInputStream bis = new BufferedInputStream(caCrtInputStream);
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
	
					while (bis.available() > 0) {
						caCert = (X509Certificate) cf.generateCertificate(bis);
						// System.out.println(caCert.toString());
					}
	
					// CA certificate is used to authenticate server
					KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
					caKs.load(null, null);
					caKs.setCertificateEntry("ca-certificate", caCert);
					trustManagerFactory = TrustManagerFactory.getInstance("X509");
					trustManagerFactory.init(caKs);
	
					// finally, create SSL socket factory
					SSLContext context = SSLContext.getInstance("TLSv1.3");
					context.init(null, trustManagerFactory.getTrustManagers(), null);
					socketFactory = context.getSocketFactory();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
