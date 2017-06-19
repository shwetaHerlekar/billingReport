var jresp,data,jresp1,query,start,end,p_name,grp;
google.charts.load("current", {packages:["corechart"]});



window.onload = function(){
//alert("in chart js");
//createQuery();
$( "#from" ).datepicker({
      defaultDate: "+1w",
      changeMonth: true,
      numberOfMonths: 1,
      onClose: function( selectedDate ) {
		//alert("changed");
		createQuery();
        $( "#to" ).datepicker( "option", "minDate", selectedDate );
      }
    });
$( "#to" ).datepicker({
      defaultDate: "+1w",
      changeMonth: true,
      numberOfMonths: 1,
      onClose: function( selectedDate ) {
		//alert("changed");
		createQuery();
        $( "#from" ).datepicker( "option", "maxDate", selectedDate );
      }
});
setPicker();
console.log("back");
var http = new XMLHttpRequest();
http.open("GET", "report", true);

		//Send the proper header information along with the request
		http.onreadystatechange = function() {//Call a function when the state changes.
			if(http.readyState == 4 && http.status == 200) {
				jresp = JSON.parse(http.responseText);
				select = document.getElementById("target");
				for (var i = 0; i<jresp.names.length; i++){
					var opt = document.createElement('option');
					opt.setAttribute("value", jresp.names[i]);
					opt.innerHTML = jresp.names[i];
					select.appendChild(opt);
				}
				if(parseFloat(jresp.credit)<0)
				{
					document.getElementById('credit').innerHTML="0";
					document.getElementById('due').innerHTML=(parseFloat(jresp.credit)*-1).toString();
				}
				else
				{
					document.getElementById('credit').innerHTML=jresp.credit;
					document.getElementById('due').innerHTML="0";
				}
				google.charts.setOnLoadCallback(drawChart);
			}
		}
		
http.send();
}

function drawChart() {
        
		data = new google.visualization.DataTable();
		data.addColumn('string', 'Project Name');
		data.addColumn('number', 'Amount spent');
		data.addColumn({type:'string',role:'annotation'});
		
		var i;
		for(i=0;i<jresp.amounts.length;i++)
		{
				//alert(jresp.names[i]);
				//alert(parseFloat(jresp.amounts[i]));
				data.addRow([jresp.names[i].toString(),parseFloat(jresp.amounts[i]),jresp.amounts[i]]);
		}
		//alert(data)
		var options = {
          title: 'Amount spent till today on each project',
          legend: { position: 'bottom' },
        };

        var chart = new google.visualization.BarChart(document.getElementById('curve_chart'));

        chart.draw(data, options);
		createQuery();
}

function clickMe(){
	createQuery();
}

function setPicker(){
//alert("picker");
$('#from').datepicker({ dateFormat: 'dd-mm-yy'}).datepicker("setDate", new Date(2016, 4, 31));
$('#to').datepicker({ dateFormat: 'dd-mm-yy'}).datepicker("setDate", new Date(2017, 5, 15));
return;
}


function createQuery()
{
	init();
	//alert("query");
	var s = $("#from").val();
	s = s.substring(6,s.length)+"-"+s.substring(3,5)+"-"+s.substring(0,2);
	//alert(s);
	var e = $("#to").val();
	e = e.substring(6,e.length)+"-"+e.substring(3,5)+"-"+e.substring(0,2);
	start+="TIMESTAMP('"+s+"')";
	end+="TIMESTAMP('"+e+"')";
	p_name+="'"+document.getElementById('target').value+"'";
	query+=start+end+p_name+grp;
	loadData();
}

function loadData(){
	//alert(query);
	var http = new XMLHttpRequest();
	http.open("GET", "query?query="+query, true);
	//Send the proper header information along with the request
	http.onreadystatechange = function() {//Call a function when the state changes.
		if(http.readyState == 4 && http.status == 200) {
				jresp1 = JSON.parse(http.responseText);
				drawChart1();
		}
	}
	http.send();
}

function drawChart1() {
        
		data = new google.visualization.DataTable();
		data.addColumn('string', 'Project Name');
		data.addColumn('number', 'Amount spent');
		data.addColumn({type:'string',role:'annotation'});
		
		var i,sum=0;
		for(i=0;i<jresp.amounts.length;i++)
		{
				sum+=parseFloat(jresp1.amounts[i]);
				data.addRow([jresp1.names[i],parseFloat(jresp1.amounts[i]),jresp1.amounts[i]]);
		}
		alert(sum);
		document.getElementById('amount').innerHTML="Amount :"+sum.toString();
		var options = {
          title: 'Amount spent different products',
          legend: { position: 'bottom' },
        };

        var chart = new google.visualization.ColumnChart(document.getElementById('curve_chart1'));

        chart.draw(data, options);
}

function init(){
//alert("in init");
query = "SELECT sum(cost), product FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";

start = " WHERE _PARTITIONTIME > ";

end = " and _PARTITIONTIME < ";

p_name = " and project.name = ";

grp =" group by product"
return;
}

