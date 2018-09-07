package ch.icarosdev.basisrauschmeteo.regtherm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.os.Environment;
import android.util.Log;
import ch.icarosdev.webviewloadlib.xml.IDefinitionDownloadedListener;

public class RegthermDataWorkspace {
	private static final String TAG = "Regtherm xml Serializer";

	public static RegthermDataWorkspace getInstance() {
		return new RegthermDataWorkspace();
	}

	private IDefinitionDownloadedListener definitionLoaderlistener;

	private String downloadUrl;
	private static String postUrlToOpenBefore;
	private String postArguments;

	private static String applicationName;
	private boolean isInitialized;

	private static HttpClient client;
	private HttpResponse finalHttpResponse;

	public RegthermDataWorkspace initialize(String applicationNamen,
			String downloadUrl, IDefinitionDownloadedListener listener,
			String postUrlToOpenBeforen, String postArguments) {
		this.definitionLoaderlistener = listener;
		this.downloadUrl = downloadUrl;
		applicationName = applicationNamen;
		this.isInitialized = true;
		postUrlToOpenBefore = postUrlToOpenBeforen;
		this.postArguments = postArguments;
		return this;
	}

	public RegthermData loadRegthermData() {
		RegthermData data = readFile();
	
		if (data == null) {
			// download file
			data = new RegthermData();
			this.downloadFileFromUrl(data);
		} else {
			RegthermPageData page = data.findPageData(downloadUrl);
			if (page == null) {
				this.downloadFileFromUrl(data);
			}
		}
		
		this.asyncRefreshRegthermData(data);
	
		return data;
	}

	public static String getPath() {
		return Environment.getExternalStorageDirectory() + File.separator
				+ applicationName + File.separator + "RegthermData.xml";
	}
	
	public static void resetHttpClient()
	{
		client = null;
	}

	private void asyncRefreshRegthermData(final RegthermData data) {
	
		new Thread(new Runnable() { 
	        public void run(){
	        	try{
	        	downloadFileFromUrl(data);
	        	definitionLoaderlistener.definitionLoaded();
	        	} catch (Exception e) {
	    			Log.e(TAG, e.toString(), e);
	    		}
	        }
	}).start();
		
	}

	private static synchronized void initializeHttpClient() {
		
		if(client == null){
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
	
			DefaultHttpClient clienttemp = new DefaultHttpClient();
			BasicHttpParams sHttpParams = new BasicHttpParams();
			SchemeRegistry sSupportedSchemes = new SchemeRegistry();
	
			sHttpParams.setParameter("http.socket.timeout", 5000);
			sHttpParams.setParameter("http.connection.timeout", 5000);
			sSupportedSchemes.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			sSupportedSchemes.register(new Scheme("https", TrustAllSSLSocketFactory.getSocketFactory(), 443));
	
			SingleClientConnManager mgr = new SingleClientConnManager(clienttemp.getParams(), sSupportedSchemes);
			client = new DefaultHttpClient(mgr, clienttemp.getParams());
	
			// Set verifier
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			
			executeLoginProcedure();
		}
	}

	private synchronized void downloadFileFromUrl(RegthermData regthermData) {

		try {
			initializeHttpClient();
			HttpGet request = new HttpGet(downloadUrl);
			finalHttpResponse = client.execute(request);

			RegthermPageData pageData = regthermData.findPageData(downloadUrl);

			if (pageData == null) {
				pageData = new RegthermPageData();
				pageData.setPageUrl(downloadUrl);
				regthermData.regthermPages.add(pageData);
			}

			downloadRegthermDocument(pageData);

			persistRegthermData(regthermData);
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString(), e);
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	private void downloadRegthermDocument(RegthermPageData pageData) {
		InputStream in;
		pageData.resetRows();
		try {
			in = finalHttpResponse.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				parseRow(line, i, pageData);	
				i++;
			}
			in.close();
		} catch (IllegalStateException e) {
			Log.e(TAG, e.toString(), e);
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	private void parseRow(String line, int i, RegthermPageData pageData) {
		if (i == 0) {
			pageData.headerRow = line.substring(0, line.indexOf("["));
		}
		if (i > 2) {
			RegthermRow row = new RegthermRow();

			row.time = line.substring(0, line.indexOf("  "));
			line = line.substring(line.indexOf("  ") + 2, line.length());

			row.temperatur = line.substring(0, line.indexOf("  "));
			line = line.substring(line.indexOf("  ") + 2, line.length());

			row.temperaturTaupunkt = line.substring(0, line.indexOf(" "));
			line = line.substring(line.indexOf(" ") + 1, line.length());

			row.parseRegthermData(line.substring(0, line.indexOf("  ")));
			line = line.substring(line.indexOf("  ") + 2, line.length());

			pageData.regthermRows.add(row);
		}
	}

	private static synchronized void executeLoginProcedure() {
		if (postUrlToOpenBefore != null && postUrlToOpenBefore.length() > 0) {
			String[] urlsExecuteBefore = postUrlToOpenBefore.split(":#_#:");

			for (String urlToExecute : urlsExecuteBefore) {

				try {
					HttpGet request = new HttpGet(urlToExecute);
					client.execute(request);
				} catch (ClientProtocolException e) {
					Log.e(TAG, e.toString(), e);
				} catch (IOException e) {
					Log.e(TAG, e.toString(), e);
				}

				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					Log.e(TAG, e.toString(), e);
				}
			}
		}
	}

	private static synchronized void persistRegthermData(RegthermData regthermData) {
		try {


			File xmlFile = new File(getPath());

			if (xmlFile.exists()) {
				xmlFile.delete();
			}

			initializeDir();

			Serializer serializer = new Persister();
			serializer.write(regthermData, xmlFile);
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	private RegthermData readFile() {
		try {
			if (!isInitialized) {
				return null;
			}

			File xmlFile = new File(getPath());
			if (!xmlFile.exists()) {
				return null;
			}

			Serializer serializer = new Persister();
			RegthermData regthermData = (RegthermData) serializer.read(
					RegthermData.class, xmlFile);
			return regthermData;

		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}

		return null;
	}

	private static void initializeDir() {
		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + applicationName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	// ------------------------------------------------- SSL STUFF

	private static final class TrustAllSSLSocketFactory implements
			LayeredSocketFactory {

		private static final TrustAllSSLSocketFactory DEFAULT_FACTORY = new TrustAllSSLSocketFactory();

		public static TrustAllSSLSocketFactory getSocketFactory() {
			return DEFAULT_FACTORY;
		}

		private SSLContext sslcontext;
		private javax.net.ssl.SSLSocketFactory socketfactory;

		private TrustAllSSLSocketFactory() {
			super();
			TrustManager[] tm = new TrustManager[] { new X509TrustManager() {

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return new java.security.cert.X509Certificate[0];
				}

			} };
			try {
				this.sslcontext = SSLContext.getInstance(SSLSocketFactory.TLS);
				this.sslcontext.init(null, tm, new SecureRandom());
				this.socketfactory = this.sslcontext.getSocketFactory();
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, "Failed to instantiate TrustAllSSLSocketFactory!", e);
			} catch (KeyManagementException e) {
				Log.e(TAG, "Failed to instantiate TrustAllSSLSocketFactory!", e);
			}
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			SSLSocket sslSocket = (SSLSocket) this.socketfactory.createSocket(
					socket, host, port, autoClose);
			return sslSocket;
		}

		@Override
		public Socket connectSocket(Socket sock, String host, int port,
				InetAddress localAddress, int localPort, HttpParams params)
				throws IOException, UnknownHostException,
				ConnectTimeoutException {
			if (host == null) {
				throw new IllegalArgumentException(
						"Target host may not be null.");
			}
			if (params == null) {
				throw new IllegalArgumentException(
						"Parameters may not be null.");
			}

			SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock
					: createSocket());

			if ((localAddress != null) || (localPort > 0)) {

				// we need to bind explicitly
				if (localPort < 0) {
					localPort = 0; // indicates "any"
				}

				InetSocketAddress isa = new InetSocketAddress(localAddress,
						localPort);
				sslsock.bind(isa);
			}

			int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
			int soTimeout = HttpConnectionParams.getSoTimeout(params);

			InetSocketAddress remoteAddress;
			remoteAddress = new InetSocketAddress(host, port);

			sslsock.connect(remoteAddress, connTimeout);

			sslsock.setSoTimeout(soTimeout);

			return sslsock;
		}

		@Override
		public Socket createSocket() throws IOException {
			// the cast makes sure that the factory is working as expected
			return (SSLSocket) this.socketfactory.createSocket();
		}

		@Override
		public boolean isSecure(Socket sock) throws IllegalArgumentException {
			return true;
		}

	}

}
