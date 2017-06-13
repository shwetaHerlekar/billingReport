package com.example;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import java.util.*;
import java.util.UUID;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
@SuppressWarnings("serial")
public class MailServlet extends HttpServlet {
        public void sendMail(String tdate, ArrayList<String> project_names, ArrayList<String> amount_spent)
        {
                        String to = "shweta_herlekar@persistent.com";
                        StringTokenizer st = new StringTokenizer(to,";");
                        String from = "gcp_billing_update@billing-167908.appspotmail.com";
                        String host = "localhost";
                        Properties properties = System.getProperties();
                        properties.setProperty("mail.smtp.host", host);
                        Session session = Session.getDefaultInstance(properties);
                        
                        StringBuilder msg = new StringBuilder();
						try {
         
                                MimeMessage message = new MimeMessage(session);
                                message.setFrom(new InternetAddress(from));
                                while(st.hasMoreTokens()) {
                                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(st.nextToken()));
                                }
                                message.setSubject("Billing status"+tdate);
                                
                                msg.append("Project Name        Total Amount spent($)\n");
                                for(int k=0; k< project_names.size();k++)
                                {
                                        msg.append(project_names.get(k)+"  ------>>  "+amount_spent.get(k)+"\n\n");
                                }
                                message.setText(String.valueOf(msg));
                                Transport.send(message);
                        }catch (Exception mex) {
                                
                        }
        }
    public QueryResult queryBigquery(BigQuery bigquery,String val) throws Exception
        {
                QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(val)
            // Use standard SQL syntax for queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();
                        // Create a job ID so that we can safely retry.
                        JobId jobId = JobId.of(UUID.randomUUID().toString());
                        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
   queryJob = queryJob.waitFor();
                        // Check for errors
                        if (queryJob == null) {
                                throw new RuntimeException("Job no longer exists");
                        } else if (queryJob.getStatus().getError() != null) {
                        // You can also look at queryJob.getStatus().getExecutionErrors() for all
                        // errors, not just the latest one.
                        throw new RuntimeException(queryJob.getStatus().getError().toString());
                        }
                        // Get the results.
                        QueryResponse response = bigquery.getQueryResults(jobId);
                        QueryResult result = response.getResult();
                         return result;
        }
        
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
        try{
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        String tdate = String.valueOf(cal.get(Calendar.YEAR))+"-"+String.valueOf(cal.get(Calendar.MONTH)+1)+"-"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        
        String query =  "SELECT "
                    + "sum(cost) as Todays_cost, project.name "
                    + "FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8` "
                                        + "WHERE _PARTITIONTIME = TIMESTAMP("+"'"+tdate+"'"+") "
                    + "group by project.name";
        QueryResult result = queryBigquery(bigquery, query);
        
        ArrayList<String> project_names=new ArrayList<String>();  
		ArrayList<String> cost_spent_today=new ArrayList<String>();  
        
        while (result != null) {
                for (List<FieldValue> row : result.iterateAll()) {
                                
                                cost_spent_today.add(row.get(0).getStringValue());
                                
                                project_names.add(row.get(1).getStringValue());
                                                                
                }
                result = result.getNextPage();
        }
        
        //out.println(project_names);
        //out.println(cost_spent_today);
        
        query =  "SELECT "
                    + "* "
                    + "FROM `billing-167908.billing_stats.Total_Billing_Status` order by project1 desc";
		result = queryBigquery(bigquery, query);
        
        ArrayList<String> project_names1=new ArrayList<String>();
        ArrayList<String> total_spent=new ArrayList<String>();
        int index=0;
        List<FieldValue> row1 = null;
        while (result != null) {
                for (List<FieldValue> row : result.iterateAll()) {
                                row1 = row;
                                //out.println(String.valueOf(row));
                                if(index==0)
                                {
                                        for(int k=0; k<row.size();k++)
                                        {
                                                project_names1.add(row.get(k).getStringValue());
                                        }
                                        index+=1;
                                        continue;
                                }
								 if(index==1)
                                {
                                        for(int k=0; k<row.size();k++)
                                        {
                                                total_spent.add(row.get(k).getStringValue());
                                        }
                                        index+=1;
                                }
                }
				      result = result.getNextPage();
        }
        //out.println(total_spent);
        //out.println(project_names1);
        
        ArrayList<String> project_names2=new ArrayList<String>();
        ArrayList<String> total_spent_till_today=new ArrayList<String>();
        
        boolean found = false;
        for(int j=0; j<project_names.size(); j++)
        {
                found = false;
                for(int t=0; t<project_names1.size(); t++)
                        {
                                if(project_names1.get(t).equals(project_names.get(j)))
                                {
                                        project_names2.add(project_names1.get(t));
                                        total_spent_till_today.add(String.valueOf(Float.parseFloat(cost_spent_today.get(j))+Float.parseFloat(total_spent.get(t))));
                                        found = true;
                                        break;
                                }
                        }
        }
		found = false;
		for(int j=0; j<project_names1.size(); j++)
        {
                found = false;
                for(int t=0; t<project_names.size(); t++)
                        {
                                if(project_names1.get(j).equals(project_names.get(t)))
                                {
                                        //project_names2.add(project_names1.get(t));
                                        //total_spent_till_today.add(String.valueOf(Float.parseFloat(cost_spent_today.get(j))+Float.parseFloat(total_spent.get(t))));
                                        found = true;
                                        break;
                                }
                        }
                if(!found)
                {
                        project_names2.add(project_names1.get(j));
                        total_spent_till_today.add(total_spent.get(j));
                }
		}
        
        //out.println(total_spent_till_today);
        //out.println(project_names2);
        
        StringBuilder t = new StringBuilder();
        StringBuilder t1 = new StringBuilder();
        for(int k =0; k<project_names2.size();k++)
        {
                        t.append("project");
                        t1.append("'"+total_spent_till_today.get(project_names2.indexOf(project_names1.get(k)))+"'");
                        t.append(k+1);
                        if(k!=project_names2.size()-1)
                        t.append(", ");
                        if(k!=total_spent_till_today.size()-1)
                        t1.append(", ");
        }
		
		for(int k =0;k<total_spent_till_today.size();k++)
		{
			double roundOff = Math.round(Double.parseDouble(total_spent_till_today.get(k))* 100.0) / 100.0;
			total_spent_till_today.set(k,String.valueOf(roundOff));
		}
		sendMail(tdate,project_names2,total_spent_till_today);
		
		//out.println(project_names1+"\n");
		//out.println(project_names+"\n");
		//out.println(project_names2+"\n");
		//out.println(total_spent_till_today+"\n");
        
        query =  "INSERT `billing-167908.billing_stats.Total_Billing_Status1` ("+String.valueOf(t)+") "
                    + "VALUES("+String.valueOf(t1)+"); ";
        result = queryBigquery(bigquery, query);
        }
        catch(Exception e)
        {
                out.println(String.valueOf(e));
        }
  }
}