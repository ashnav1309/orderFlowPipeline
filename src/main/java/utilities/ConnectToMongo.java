package utilities;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ConnectToMongo {

	private MongoClient mongoClient 				= null;
	private MongoCollection<Document> collection 	= null;
	private String mongoHost 						= null;
	private String mongoDatabase        			= null;
	private String mongoCollection					= null;
	private int mongoPort 							= 0;

	public ConnectToMongo(String mongoHost, int mongoPort, String mongoDatabase, String mongoCollection) {
		this.mongoHost 			= mongoHost;
		this.mongoPort 			= mongoPort;
		this.mongoDatabase		= mongoDatabase;
		this.mongoCollection	= mongoCollection;
	}

	public void createMongoConnection() throws MongoClientException{
		String url = "mongodb://"+mongoHost+":"+mongoPort;
		mongoClient = new MongoClient(new MongoClientURI(url));
		MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
		collection = database.getCollection(mongoCollection);
		if(collection.count() < 1) {
			destroyMongoConnection();
			throw new MongoClientException("Mongo Collection has no data");
		}
		System.out.printf("========================================================================\n");
		System.out.printf("Mongo Connection to %s established\n", url);
		System.out.printf("========================================================================\n\n");
	}

	public void destroyMongoConnection() {
		if(mongoClient != null) {
			mongoClient.close();
		}
		System.out.println("Mongo Connection destroyed");
	}

	public void getResultSet() {
//		Filters.
//		Bson query = new BasicDBObject();
//		query.("grn_code", "LKG1003092");
		FindIterable<Document> documents = collection.find();
		for(Document document : documents) {
			System.out.println(document);
			// System.out.println(document.toJson());
		}
	}

	public static void main(String []args) {
		ConnectToServer connectToSSH = new ConnectToServer("Preprod", "ashnavsaxena", "bastion.lenskartserver.net", "ashnav1309", 22, 29180, "mongo-scm.preprod.internal", 27017);
		ConnectToMongo connectToMongo = new ConnectToMongo("localhost", 29180, "inventory", "barcode_details");
		if(connectToSSH.createSSHSession()) {
			try {
			connectToMongo.createMongoConnection();
			connectToMongo.getResultSet();
			}
			catch(MongoClientException e) {
				e.printStackTrace();
			}
		}
		connectToMongo.destroyMongoConnection();
		connectToSSH.destroyLPort();
		connectToSSH.destroySSHSession();
	}
}
