import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import com.opencsv.CSVWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Client {
	public static void main(String[] args) throws Exception {
		DataGetter getter = new DataGetter();
		String creatives = getter.getResponse("http://exApi.com/api/creatives");
		String campaigns = getter.getResponse("http://exApi.com/api/campaigns");
		JSONParser parser = new JSONParser();
		Hashtable<Integer, Campaign> table = new Hashtable<Integer, Campaign>();

		//parse campaign data into campaign objects and store in table
		JSONArray arr = (JSONArray)parser.parse(campaigns);
		for  (Object i : arr) {
			String cpmStr = ((String)((JSONObject)i).get("cpm")).substring(1);
			float cpm = Float.parseFloat(cpmStr);
			int id = ((Long)((JSONObject)i).get("id")).intValue();
			String name = (String)((JSONObject)i).get("name");
			String start_date = (String)((JSONObject)i).get("startDate");
			Campaign campaign = new Campaign(cpm, id, name, start_date);
			table.put(id, campaign);	
		}

		//parse creative data and add to parent campaign by table lookup
		arr = (JSONArray)parser.parse(creatives);
		for (Object i : arr) {
			int parentId = ((Long)((JSONObject)i).get("parentId")).intValue();
			int clicks = ((Long)((JSONObject)i).get("clicks")).intValue();
			int views =  ((Long)((JSONObject)i).get("views")).intValue();
			Campaign parent = table.get(parentId);
			parent.clicks += clicks;
			parent.views += views;
		}

    //donnect to local db and create table
		String dbURL = "jdbc:sqlite:client.db";
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection(dbURL);
		Statement s = conn.createStatement();
		String sqlStr = "CREATE TABLE IF NOT EXISTS CAMPAIGNS (" +
				"ID INT PRIMARY KEY," +
				"NAME VARCHAR(255) NOT NULL UNIQUE," +
				"CPM DECIMAL(10, 5) NOT NULL," +
				"START_DATE VARCHAR(255) NOT NULL," +
				"CLICKS INT NOT NULL," +
				"VIEWS INT NOT NULL," +
				"REVENUE DECIMAL(10,5) NOT NULL)";
		s.executeUpdate(sqlStr);

    //store campaign info in table
		for (int id : table.keySet()) {
			Campaign campaign = table.get(id);
			sqlStr = "INSERT INTO CAMPAIGNS (ID, NAME, CPM, START_DATE, CLICKS, VIEWS, REVENUE) VALUES (" +
					campaign.id + ", '" + (campaign.name).replaceAll("'", "''") + "', " + campaign.cpm + ", '" +
					campaign.start_date + "', " + campaign.clicks + ", " + campaign.views + ", " + campaign.getRevenueStr() + ")";
			try {
				s.executeUpdate(sqlStr);
			}
			catch ( Exception e ) {
				System.err.println( e.getClass().getName() + ": " + e.getMessage() );
				System.exit(0);
			}
		}
		
		java.sql.ResultSet rs = s.executeQuery("SELECT ID, NAME, VIEWS, CLICKS, REVENUE FROM CAMPAIGNS");
		CSVWriter writer = new CSVWriter(new FileWriter("campaigns.csv"));
		writer.writeAll(rs, true);
		s.close();
		conn.close();
		writer.close();
		System.out.println("campaigns.csv created successfully.");
	}
}
