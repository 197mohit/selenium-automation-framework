package com.paytm.apphelpers;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.paytm.LocalConfig;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class MongoDBHelper {

  private static MongoClient mongoClient;

  static {
    Logger logger = (Logger) LoggerFactory.getLogger("org.mongodb.driver.cluster");
    logger.setLevel(Level.ERROR);
    mongoClient = new MongoClient(new MongoClientURI(LocalConfig.MONGO_DB_URI));
  }

  public static MongoCollection<Document> getCollection(String collectionName) {
    MongoDatabase database = mongoClient.getDatabase(LocalConfig.MONGO_DATABSE_NAME);
    return database.getCollection(collectionName);
  }

  public static Document findDocumentById(MongoCollection<Document> dbCollection, String id, String fieldToBeUpdated) {
    Document query = new Document();
    System.out.println("ID is: " + id);
    if (ObjectId.isValid(id)) {
      System.out.println("Object ID is valid Id hence inside this");
      query.put("_id", new ObjectId(id));
    } else {
      System.out.println(id + " is Not a valid ObjectId");
      query.put(fieldToBeUpdated, id);
    }
    return dbCollection.find(query).first();
  }

  public static void updateDocumentByObjectId(MongoCollection<Document> dbCollection, String objectIdToBeUpdated, String key, String value) {
    Document query = new Document("_id", new ObjectId(objectIdToBeUpdated));
    Document setData = new Document(key, value);
    dbCollection.updateOne(query, new Document("$set", setData));
  }

  public static void updateDocumentByAnyField(MongoCollection<Document> dbCollection, String fieldName, String fieldValue, String key, String value, Boolean isUpdateMany) {
    Document query = new Document(fieldName, fieldValue);
    Document setData = new Document(key, value);
    System.out.println("Query for updating document is:" + query.toString());
    System.out.println("Update statement is: " + setData.toString());
    if (isUpdateMany) {
      dbCollection.updateMany(query, new Document("$set", setData));
    } else {
      dbCollection.updateOne(query, new Document("$set", setData));
    }
  }

  public static void deleteDeadTokens(MongoCollection<Document> dbCollection) {
    System.out.println("Deleting Dead Tokens");
    dbCollection.deleteMany(Filters.in("tokenState", "DEAD", "FAILED", "INIT"));
  }
}