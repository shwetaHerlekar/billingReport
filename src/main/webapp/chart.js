window.onload = function(){
alert("In chart js");
var http = new XMLHttpRequest();
http.open("GET", "report", true);

		//Send the proper header information along with the request
		http.onreadystatechange = function() {//Call a function when the state changes.
			if(http.readyState == 4 && http.status == 200) {
				 var data = JSON.parse(http.responseText);
				 var i;
				 for(i=0;i<data.names.length;i++)
				 {
					createElement(data.names[i], data.amounts[i], i);
				 }
			}
		}
		
http.send();



}

function addProgressBar(var perc, var div_name)
{
	var div1 = document.createElement("div");
	div1.setAttribute("class", "progress");
	div1.style.width="300px";
	div1.style.height = "10px";

	var div = document.createElement("div");
	div.setAttribute("class", "progress-bar");
	div.setAttribute("role", "progressbar");
	div.setAttribute("aria-valuenow", "50");
	div.setAttribute("aria-valuemin", "0");
	div.setAttribute("area-valuemax", "100");
	div.style.width = "50%";
	div.style.height = "10px";
	div1.appendChild(div);

	document.getElementById(div_name).appendChild(div1);
}

function createElement(var p_name, var val, var i)
{
	var div1 = document.createElement("div");
	div1.setAttribute("id", "div"+i.toString());
	div1.innerHTML="Amount spent :"+val;
	document.getElementById("main").appendChild(div1);
	var am=parseFloat(val)/500.00*100;
	addProgressBar(am.toString(),"div"+i.toString());
}