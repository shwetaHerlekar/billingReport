window.onload = function(){
alert("In chart js");
var data;
var http = new XMLHttpRequest();
http.open("GET", "report", true);

		//Send the proper header information along with the request
		http.onreadystatechange = function() {//Call a function when the state changes.
			if(http.readyState == 4 && http.status == 200) {
				data = JSON.parse(http.responseText);
				alert(data.credit);
				alert(data.amounts);
			}
		}
		
http.send();
}