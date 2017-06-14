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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class ReportServlet extends HttpServlet {
        
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
	//resp.setContentType("application/json");
	PrintWriter out=res.getWriter();
	String n1=req.getParameter("sdate");
	String n2=req.getParameter("edate");
	out.println(n1);
	out.println(n2);
        try{
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
		String query =  "SELECT "
                    + "* "
                    + "FROM `billing-167908.billing_stats.Total_Billing_Status1` order by project1 desc";
		QueryResult result = queryBigquery(bigquery, query);
        
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
		
		query =  "SELECT "
                    + "sum(cost), project.name "
                    + "FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8` "
                                        + "WHERE _PARTITIONTIME > TIMESTAMP('2017-05-31') "
                    + "group by project.name";
        result = queryBigquery(bigquery, query);
        
        ArrayList<String> project_names=new ArrayList<String>();  
		ArrayList<String> cost_spent_today=new ArrayList<String>();  
		
		double sum = 0;
        
        while (result != null) {
                for (List<FieldValue> row : result.iterateAll()) {
                                
                                cost_spent_today.add(row.get(0).getStringValue());
								
								sum+=Double.parseDouble(row.get(0).getStringValue());
                                
                                project_names.add(row.get(1).getStringValue());
                                                                
                }
                result = result.getNextPage();
        }
		//out.println(cost_spent_today);
		//out.println(project_names);
		ArrayList<String> project_names2=new ArrayList<String>();
        ArrayList<String> total_spent_till_today=new ArrayList<String>();
        
        boolean found = false;
		for(int j=0; j<project_names1.size(); j++)
        {
                found = false;
                for(int t=0; t<project_names.size(); t++)
                        {
                                if(project_names1.get(j).equals(project_names.get(t)))
                                {
                                        project_names2.add(project_names1.get(j));
                                        total_spent_till_today.add(String.valueOf(Float.parseFloat(cost_spent_today.get(t))+Float.parseFloat(total_spent.get(j))));
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
		
		query =  "SELECT "
                    + "* "
                    + "FROM `billing-167908.billing_stats.Credit` ";
		result = queryBigquery(bigquery, query);
        
        double credit=0;
        
        while (result != null) {
                for (List<FieldValue> row : result.iterateAll()) {
                                
                                credit = Math.round(Double.parseDouble(row.get(0).getStringValue())* 100.0) / 100.0;                               
                }
                result = result.getNextPage();
        }
		for(int k =0;k<total_spent_till_today.size();k++)
		{
			double roundOff = Math.round(Double.parseDouble(total_spent_till_today.get(k))* 100.0) / 100.0;
			total_spent_till_today.set(k,String.valueOf(roundOff));
			
		}
		
		JSONObject mobj = new JSONObject();
		JSONArray p_names = new JSONArray();
		JSONArray am_spent = new JSONArray();
        for(int i=0;i<total_spent_till_today.size();i++)
		{
			p_names.add(project_names2.get(i));
			am_spent.add(total_spent_till_today.get(i));
		}
		mobj.put("names", p_names);
		mobj.put("amounts", am_spent);
		mobj.put("credit", credit-sum);
		out.println(mobj);
		out.close();
        }
        catch(Exception e)
        {
                out.println(String.valueOf(e));
        }
  }
}