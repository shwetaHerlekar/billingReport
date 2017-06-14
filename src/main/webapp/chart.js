var data;
google.charts.load("current", {packages:["corechart"]});
window.onload = function(){
var http = new XMLHttpRequest();
http.open("GET", "report", true);

		//Send the proper header information along with the request
		http.onreadystatechange = function() {//Call a function when the state changes.
			if(http.readyState == 4 && http.status == 200) {
				data = JSON.parse(http.responseText);
				alert(data.credit);
				alert(data.amounts);
				google.charts.setOnLoadCallback(drawChart);
			}
		}
		
http.send();
}

function drawChart() {
        
		data = new google.visualization.DataTable();
		data.addColumn('string', 'Project Name');
		data.addColumn('number', 'Amount spent');
		data.addColumn('string', '{ role: "annotation" }');
		
		var i;
		for(i=0;i<data.amounts.length;i++)
		{
				data.addRow([data.names[i],parseFloat(data.amounts[i]),data.amounts[i]]);
		}
		
		var options = {
          title: 'Amount spent till today on each project',
          legend: { position: 'bottom' },
        };

        var chart = new google.visualization.BarChart(document.getElementById('curve_chart'));

        chart.draw(data, options);
      }
}