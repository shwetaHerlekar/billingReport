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
public class QueryServlet extends HttpServlet {
        
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
	resp.setContentType("application/json");
	String query = req.getParameter();
    PrintWriter out = resp.getWriter();
        try{
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
		
		QueryResult result = queryBigquery(bigquery, query);
        
        ArrayList<String> product=new ArrayList<String>();
        ArrayList<String> spent=new ArrayList<String>();
		
		while (result != null) {
                for (List<FieldValue> row : result.iterateAll()) {
                                
                                spent.add(row.get(0).getStringValue());
					
                                product.add(row.get(1).getStringValue());
                                                                
                }
                result = result.getNextPage();
        }
		
		for(int k =0;k<spent.size();k++)
		{
			double roundOff = Math.round(Double.parseDouble(spent.get(k))* 100.0) / 100.0;
			spent.set(k,String.valueOf(roundOff));
			
		}
		
		JSONObject mobj = new JSONObject();
		JSONArray p_names = new JSONArray();
		JSONArray am_spent = new JSONArray();
        for(int i=0;i<spent.size();i++)
		{
			p_names.add(product.get(i));
			am_spent.add(spent.get(i));
		}
		mobj.put("names", p_names);
		mobj.put("amounts", am_spent);
		out.println(mobj);
		
		out.close();
        }
        catch(Exception e)
        {
                out.println(String.valueOf(e));
        }
  }
}