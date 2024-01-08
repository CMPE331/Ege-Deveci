package cmpe331;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class Mongo {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private String connectionString;
    private String dbNameString;

    /**
     * Constructor to initialize the MongoDB connector.
     *
     * @param connectionString MongoDB connection string.
     * @param dbName           Name of the MongoDB database.
     */
    public Mongo(String connectionString, String dbName) {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        // Create a new client and connect to the server
        this.mongoClient = MongoClients.create(settings);
        this.connectionString = connectionString;
        this.dbNameString = dbName;

        try {
            // Send a ping to confirm a successful connection
            this.database = mongoClient.getDatabase(dbName);
            database.runCommand(new Document("ping", 1));
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");

            //MongoCollection<Document> collection = database.getCollection("Patients");

//            try (MongoCursor<Document> cursor = collection.find().iterator()) {
//                while (cursor.hasNext()) {
//                    Document document = cursor.next();
//                    String name = document.getString("name");
//                    String uid = document.getString("uid");
//
//                    System.out.println("Name: " + name + ", UID: " + uid);
//                }
//            }
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts a new document with a specified value into the given collection.
     *
     * @param collectionName Name of the MongoDB collection.
     * @param value          Value to be inserted.
     */
    public void insert(String collectionName, String value) {
        Document document = new Document("value", value);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
    }

    /**
     * Retrieves all documents from the specified collection.
     *
     * @param collectionName Name of the MongoDB collection.
     * @return List of documents.
     */
    public List<Document> getAll(String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.find().into(new ArrayList<>());
    }

    /**
     * Retrieves the document with the specified ID from the given collection.
     *
     * @param collectionName Name of the MongoDB collection.
     * @param id             ID of the document.
     * @return Document with the specified ID.
     */
    public Document getById(String collectionName, String id) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.find(new Document("_id", id)).first();
    }

    /**
     * Updates a document with a new value based on the specified ID.
     *
     * @param collectionName Name of the MongoDB collection.
     * @param id             ID of the document to be updated.
     * @param newValue       New value to be set.
     */
    public void update(String collectionName, String id, String newValue) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Bson filter = new Document("_id", id);
        Bson update = new Document("$set", new Document("value", newValue));
        collection.updateOne(filter, update);
    }

    /**
     * Deletes the first document in the specified collection.
     *
     * @param collectionName Name of the MongoDB collection.
     */
    public void deleteFirst(String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(new Document());
    }

    /**
     * Retrieves the top 6 documents from the specified collection.
     *
     * @param collectionName Name of the MongoDB collection.
     * @return List of top 6 documents.
     */
    public String[][] getTop8(String collectionName) {
    	try {
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Sort by your preferred field and limit to 8 entries
            FindIterable<Document> result = collection.find().limit(8).sort(new Document("dateTime", 1));

            long count = collection.countDocuments(); // Get the total number of documents

            int resultSize = (int) Math.min(count, 8); // Determine the size of the result array

            String[][] topEntries = new String[resultSize][3];
            int index = 0;

            for (Document document : result) {
                // Assuming your document has three fields with names "field1", "field2", "field3"
                topEntries[index][0] = document.getString("name");
                topEntries[index][1] = document.getString("uid");
                topEntries[index][2] = document.getString("dateTime");

                index++;
            }
            

            return topEntries;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    /**
     * Checks the MongoDB connection status.
     *
     * @return True if connected, false otherwise.
     */
    public boolean checkConnection() {
        try {
            mongoClient.listDatabaseNames();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void reconnect() {
        if (!checkConnection()) {
            // Close the existing client if it's open
            closeConnection();

            // Reinitialize the MongoClient and database
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .serverApi(serverApi)
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(dbNameString);
        }
    }

    /**
     * Closes the MongoDB connection.
     */
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
