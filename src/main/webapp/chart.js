var jresp,data,jresp1,query,start,end,p_name,grp,query1;
var amounts,s,e;
google.charts.load("current", {packages:["corechart"]});
var myDiv,myDiv1;

show = function(){
		myDiv1.style.display = "none";
	    myDiv.style.display = "block";
        setTimeout(hide, 5100); // 5 seconds
      },

      hide = function(){
		myDiv1.style.display = "block";
        myDiv.style.display = "none";
      };

window.onload = function(){
myDiv= document.getElementById("loading");
myDiv1= document.getElementById("main");
show();
//alert("in chart js");
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
createQuery();
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
				amounts=jresp.amounts.slice(0);
				//alert(amounts);
				jresp.amounts.sort(function(a, b){return b-a});
				/*for(var i=0;i<jresp.amounts.lenght;i++)
				{
					names.push(jresp.names[amounts.indexOf(jresp.amounts[i])]);
				}*/
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
				data.addRow([jresp.names[amounts.indexOf(jresp.amounts[i])].toString(),parseFloat(jresp.amounts[i]),"$"+jresp.amounts[i]]);
		}
		//alert(data)
		var options = {
		   bar: {groupWidth: "95%"},
          legend: { position: 'bottom' },
        };

        var chart = new google.visualization.BarChart(document.getElementById('curve_chart'));

        chart.draw(data, options);
		createQuery();
}

function clickMe(e){
	//alert(e.target.value);
	createQuery();
}

function radioChange(e){
	//alert(e.target.value);
	createQuery();
}

function setPicker(){
//alert("picker");
$('#from').datepicker({ dateFormat: 'dd-mm-yy'}).datepicker("setDate", new Date(2017, 4, 31));
$('#to').datepicker({ dateFormat: 'dd-mm-yy'}).datepicker("setDate", new Date(2017, 5, 15));
}


function createQuery(e)
{
	init();
	var radio_val;
	var radio1=document.getElementById("radio1");
	var radio2=document.getElementById("radio2");
	
	if(radio1.checked)
	radio_val=radio1.value;
	if(radio2.checked)
	radio_val=radio2.value;
	
	//alert(radio_val);
		
	if(radio_val=="overview")
	{
		init1();
	}
	else
	{
		init2();
	}

	loadData(radio_val);
}

function loadData(radio_val){
	//alert(query);
	var http = new XMLHttpRequest();
	http.open("GET", "query?query="+query, true);
	//Send the proper header information along with the request
	http.onreadystatechange = function() {//Call a function when the state changes.
		if(http.readyState == 4 && http.status == 200) {
				jresp1 = JSON.parse(http.responseText);
				drawChart1(radio_val);
		}
	}
	http.send();
}

function drawChart1(radio_val) {
        
		data = new google.visualization.DataTable();
		data.addColumn('string', 'Project Name');
		data.addColumn('number', 'Amount spent');
		data.addColumn({type:'string',role:'annotation'});
		
		var i,sum=0;
		for(i=0;i<jresp.amounts.length;i++)
		{
				if(!isNaN(parseFloat(jresp1.amounts[i])))
				{
					sum+=parseFloat(jresp1.amounts[i]);
				}
				data.addRow([jresp1.names[i],parseFloat(jresp1.amounts[i]),jresp1.amounts[i]]);
		}
		//alert(sum);
		document.getElementById('amount').innerHTML="Amount :$"+sum.toString();
		var options = {
          legend: { position: 'bottom' },
        };

        var chart;
		
		if(radio_val=="overview")
		{
			chart = new google.visualization.LineChart(document.getElementById('curve_chart1'));
		}
		else
		{
			chart = new google.visualization.ColumnChart(document.getElementById('curve_chart1'));
		}

        chart.draw(data, options);
}

function init(){
//alert("in init");
query = "SELECT sum(cost), product FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";

start = " WHERE _PARTITIONTIME > ";

end = " and _PARTITIONTIME < ";

p_name = " and project.name = ";

grp1 =" group by product";

grp2 = " group by t";

s = $("#from").val();
//alert(s);
s = s.substring(6,s.length)+"-"+s.substring(0,2)+"-"+s.substring(3,5);
e = $("#to").val();
e = e.substring(6,e.length)+"-"+e.substring(0,2)+"-"+e.substring(3,5);
start+="TIMESTAMP('"+s+"')";
end+="TIMESTAMP('"+e+"')";

return;
}

function init1()
{
	target_val=document.getElementById("target").value;
	
	var d1 = new Date(s);
	var d2 = new Date(e);
	
	//alert(s);
	
	diff = new Date(
    d2.getFullYear()-d1.getFullYear(), 
    d2.getMonth()-d1.getMonth(), 
    d2.getDate()-d1.getDate()
	);
	
	if(diff.getYear()<1)
	{
			if(diff.getMonth()<1)
			{
					if(diff.getDate()<7)
					{
						//alert("its days");
						query = "SELECT sum(cost) FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";
					}
					else
					{
						//alert("its weeks");
						query = "SELECT sum(cost), week(_PARTITIONTIME) as t FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";
					}
			}
			else
			{
				//alert("its months");
				query = "SELECT sum(cost), month(_PARTITIONTIME) as t FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";
			}
	}
	else
	{
		//alert("its years");
		query = "SELECT sum(cost), year(_PARTITIONTIME) as t FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";
	}
	
	if(target_val!="projects")
	{
		p_name+="'"+document.getElementById('target').value+"'";
		query+=start+end+p_name+grp2;
		alert(query);
	}
	else
	{
		query+=start+end+grp2;
		alert(query);
	}

return;
}

function init2()
{
	//alert("init 2");
	target_val=document.getElementById("target").value;
	if(target_val!="projects")
	{
		query = "SELECT sum(cost), product FROM `billing-167908.billing_stats.gcp_billing_export_00C10C_FC4CCD_E9F6D8`";
		p_name+="'"+document.getElementById('target').value+"'";
		query+=start+end+p_name+grp1;
		alert(query);
	}
	else
	{
		query+=start+end+grp1;
		alert(query);
	}
return;
}



