package com.johnson;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.reporting.ReportOutputFormat;
import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.core.operationresult.OperationResult;


public class RunJasper {

  public static void main(String[] args) {
     System.out.println("hello");
     Connection c = connect();
     String reportPath = "";
     Connection localCon = connect();
     Connection devCon = connectDev();
     if (devCon != null) {
       System.out.println("Got the connection!");
     }
     int num = getNumberOfMediaItems(devCon);
     System.out.println(num);
     
     // JRS-Rest-Java-Client Impl
     RestClientConfiguration configuration = new RestClientConfiguration("http://localhost:8081/jasperserver");
     JasperserverRestClient client = new JasperserverRestClient(configuration);
     Session session = client.authenticate("jasperadmin", "jasperadmin");
     //session.logout(); will end the session.
     OperationResult<InputStream> result = client
         .authenticate("jasperadmin", "jasperadmin")
         .reportingService()
         .report("/reports/DevUIDMediaList")
         .prepareForRun(ReportOutputFormat.HTML, 1)
         //.parameter("Cascading_name_single_select", "A & U Stalker Telecommunications, Inc")
         .run();
     InputStream report = result.getEntity();
     
     String strReport = convertStreamToString(report);
     System.out.println(strReport);
  }
  
  
  //Solution for converting an InputStream into a String using only Java libraries.
  static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
  
  private static Connection connect() {
    Connection con = null;
    try {
      Class.forName("org.postgresql.Driver");
      con = (Connection) DriverManager.getConnection("jdbc:postgresql:postgres", "postgres", "postgres");
      if (con == null) {
        System.out.println("Connection cannot be established");
        System.exit(0);
      }
    } catch (Exception ex) {
        System.out.println("Caught in DBConnect");
        ex.printStackTrace();
    }
    return con;
  }
  
  // Make sure to open the VPN before attempting this connection.
  private static Connection connectDev() {
    Connection con = null;
    try {
      Class.forName("org.postgresql.Driver");
      con = (Connection) DriverManager.getConnection("jdbc:postgresql://10.0.4.66:5433/burst",
          "samj", "hcwef64");
      if (con == null) {
        System.out.println("Connection cannot be established");
        System.exit(0);
      }
    } catch (Exception ex) {
        System.out.println("Caught in DBConnect");
        ex.printStackTrace();
    }
    return con;
  }
  
  private static int getNumberOfMediaItems(Connection con) {
    int num = 0;
    int uid = 966953716; // This is the uid of sam@burst.us on dev
    try {
      String query = "SELECT COUNT(*) FROM burst.media WHERE orig_owner_uid='"+uid+"';";
      Statement st = (Statement) con.createStatement();
      ResultSet rs = st.executeQuery(query);
      
      rs.next();
      String result = rs.getString(1);
      Integer temp = Integer.valueOf(result);
      num = temp.intValue();
    } 
    catch(Exception ex) {
      System.out.println("There's a wrench somewhere in the method 'getNumberOfMediaItems'!");
      ex.printStackTrace();
    }
    return num;
  }

}
