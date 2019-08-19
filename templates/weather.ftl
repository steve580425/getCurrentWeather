<script src="https://libs.baidu.com/jquery/1.7.0/jquery.min.js"></script>

<style> 
.bor{border:1px dashed #000000;width:150px;height:30px;margin:1px} 
</style>

<select id="weatherSelection" onchange="transSelChange()">
	<option>city selection</option>
 	<#list weather as weather>
 		<option value="${weather.id?c}">${weather.name}</option>
	</#list>
</select>
<table></table>
<table>
	<tr><td class="bor">City:</td><td class="bor" id="city"></td></tr>
	<tr><td class="bor">Updated time:</td><td class="bor" id="updatetime"></td></tr>
	<tr><td class="bor">Weather:</td><td class="bor" id="weather"></td></tr>
	<tr><td class="bor">Temperature:</td><td class="bor" id="temperature"></td></tr>
	<tr><td class="bor">Wind:</td><td class="bor" id="wind"></td></tr>
</table>
<script>
	var element = $("#weatherSelection");
	var city= $("#city");
	var updatetime= $("#updatetime");
	var weather= $("#weather");
	var temperature= $("#temperature");
	var wind= $("#wind");
	
	function transSelChange() {
	    var transSelValue = element.val();
	  	$.ajax({
			url:"${weatherUrl}" + transSelValue,    
			dataType:"json",
			type:"get",
			data:{},
			beforeSend:function(data,status,g){
				element.attr("disabled",true);
			},	  
			success:function(data){
				city.html(data.cityName);
				updatetime.html(data.updatedTime);
				weather.html(data.weatherDesc);
				temperature.html(data.temperatureDesc);
				wind.html(data.wind);
			},
			error:function(data,status,g){
				//alert(status);
				city.html('');
				updatetime.html('');
				weather.html('');
				temperature.html('');
				wind.html('');
			},
			complete:function(data,status,g){	
				element.attr("disabled",false);
			}	
		});	
	}

	$(document).ready(function(){
    	setInterval(transSelChange, ${retrieveInterval?c});
   	});
</script>