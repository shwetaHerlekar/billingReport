window.onload = function(){
alert("Hello");
$('#from').datepicker({ dateFormat: 'dd-mm-yy'}).datepicker("setDate", new Date(2016, 4, 31));
$('#to').datepicker({ dateFormat: 'dd-mm-yy'}).datepicker("setDate", new Date(2017, 3, 1));
$( "#from" ).datepicker({
      defaultDate: "+1w",
      changeMonth: true,
      numberOfMonths: 1,
      onClose: function( selectedDate ) {
        $( "#to" ).datepicker( "option", "minDate", selectedDate );
      }
    });
    $( "#to" ).datepicker({
      defaultDate: "+1w",
      changeMonth: true,
      numberOfMonths: 1,
      onClose: function( selectedDate ) {
        $( "#from" ).datepicker( "option", "maxDate", selectedDate );
      }
});
var sdate = $('#from').val()
var edate = $('#to').val()
var url = "report?sdate="+sdate+"&edate="+edate;
alert(url);
}