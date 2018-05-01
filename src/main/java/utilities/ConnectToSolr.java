package utilities;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

public class ConnectToSolr {

	private static Logger logger = LogManager.getLogger(ConnectToSolr.class.getName());
	
	private StringBuilder uri;
	private SolrClient solrClient;
	private String solrHost;
	private int solrPort;
	private String coreName;
	
	public ConnectToSolr(String solrHost, int solrPort, String coreName) {
		this.solrHost = solrHost;
		this.solrPort = solrPort;
		this.coreName = coreName;
	}
	
	public void createSolrConnection() {	
		uri = new StringBuilder("http://");
		uri.append(solrHost).append(":").append(solrPort).append("/solr/").append(coreName);
		logger.info(uri.toString());
		solrClient = new HttpSolrClient.Builder(uri.toString()).build();
	}
	
	public void getResultSet() {
	}
}
