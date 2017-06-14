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
	resp.setContentType("application/json");
    PrintWriter out = resp.getWriter();
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
		
		out.close();
        }
        catch(Exception e)
        {
                out.println(String.valueOf(e));
        }
  }
}