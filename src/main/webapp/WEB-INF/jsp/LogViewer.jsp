<%@ page language ="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/frame/header.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>

  <style>
    fieldset {
      border: 0;
    }
    label {
      display: block;
      margin: 30px 0 0 0;
    }
    .overflow {
      height: 200px;
    }
    
	.container {
	  display: inline-table;
	  border:1px solid #cccccc;
	  width:100%;
	  padding-right: 14px;
	  margin-top:5px;
	  margin-bottom:5px;
	  
	}
	.fieldName {
	  display: table-cell;
	  padding-right: 4px;
	  padding-left:30px;
	  font-family:Sans-serif; 
	  font-size:0.9em;
	  vertical-align:text-top;
	  padding-bottom:10px;
	}
	.data {
	  display: table-cell;
	  padding-left:20px;
	  font-family:Sans-serif; 
	  font-size:0.9em;
	  padding-bottom:15px;
	}
	.select {
	  display: table-cell;
	  padding-left:20px;
	  font-family:Sans-serif; 
	  font-size:0.9em;
	  padding-bottom:15px;
	}
	
	.logbutton {
	  display: table-cell;
	  padding-left:50px;
	  font-family:Sans-serif; 
	  font-size:0.9em;
	  padding-bottom:70px;
	  border:none;
	  overflow:auto;
	  margin:0px auto;
	  padding-bottom:15px;
	}
	    
  </style>

<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css"></link>
<script src="https://code.jquery.com/jquery-1.12.4.js"></script>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  
<!-- %@ include file="/WEB-INF/jsp/common/include/ruiCommon.jspf" %-->
<script type="text/javascript">

	$(document).ready(function() {
		
		var height = 600;

		$('#logSize').val(100);
		$('#logText').attr('readonly','readonly');
		$('#logText').prop('readonly', true);
		$('#logText').css('height', height);
		var sendData="logType=" + $('#logType').val();

		/**
		* When clicked Log search button, call url with selected option value.
		**/
		$("#btnSearch").click(function(e) {
		    e.preventDefault();
			debugger;
	    
			$('#logText').val("");

			var targetURL = $("#system option:selected").val();
			
			var logSize = $('#logSize').val();
			if (logSize > 1000) {
				alert("Log size should be less than 1000");
				return false;
			}
			var sendData = {"logType" : $('#logType').val(), "logSize" : $('#logSize').val()};
			//var sendData = "logType=" + $('#logType').val() + "&logSize=" + $('#logSize').val();
		    $.ajax({
		        type: "POST",
		        url: targetURL,
		        crossDomain : true,
		        data : sendData,
		        jsonpCallback : "logCallBack",
		        dataType : "jsonp",  //Cross domain
		        contentType: "application/json"
//		        success: function(result) {
//		        	alert(result);
//		        	debugger;
//		        	$.each(result, function (key, value) {
//		        		$("#logText").val(value);	
//		        	});
//		        },
//		        error: function(result) {
//		        	debugger;
//		        	alert(result.log)
//		        	$("#logText").val(result.log);
//		        }
		    });
		});
		
		getSystemInfo();
	});
	
	function logCallBack(result) {
		debugger;
		var lastIdx = result.string.lastIndexOf("}") - 2;
		var firstIdx = result.string.indexOf(":") + 2;
		var logText = result.string.substring(firstIdx, lastIdx);
		alert(logText);

		$("#logText").val(logText);
		
	}
	/**
	* This function read url from ibm server
	* And generate select options
	**/
	function getSystemInfo() {
		$('#system').empty();
		debugger;
		var len = document.URL.indexOf("ibm");
		var curURL = document.URL.substring(0,len) + "ibm";
		var targetURL =  curURL + "/log/systemInfo.do";
		$.ajax({
	        type: "POST",
	        url: targetURL,
	        crossDomain : true,
	        dataType : "json",
	        success: function(result) {
	        	$.each(result, function (key, value) {
	        	    $('#system').append($('<option>', { 
	        	        value: value,
	        	        text : key 
	        	    }));
	        	});
	        },
	        error: function(result) {
	        	$("#logText").val(result.log);
	        }
	    });
		
	}

</script>
</head>
<body>
<div id="LblockBodyScroll">
<div id="LblockBody">
	<div class="LblockPageHeader">
        <div class="LblockPageTitle">
            <h1 id="headTitle"></h1>
        </div>
        <div class="LblockPageLocation">
            <ul>
                <li class="Lfirst" id="menuPath"></li>
            </ul>
        </div>
	</div>
	<div class="container">
        <form action="">
        	<div class="fieldName">
	       		<label>SYSTEM</label>
        	</div>
        	<div class="select">
	            <select name="system" id="system">
	            <!-- 
	        		<option value="http://localhost:7070/ibm/log/viewer.do">PORTAL-WAS1</option>
					<option value="http://localhost:7070/ibm/log/viewer.do">IBM-WAS1</option>
					<option value="http://localhost:7070/ibm/log/viewer.do">DSC-WAS1</option>
					<option value="http://localhost:7070/ibm/log/viewer.do">INF-WAS1</option>
				-->
				</select>
        	</div>
        	<div class="fieldName">
	           	<label>LOG TYPE</label>
 			</div>
        	<div class="select">
	            <select name="logType" id="logType">
			        <option value="app">APP</option>
			        <option value="error">ERROR</option>
			        <option value="sql">SQL</option>
			    </select>
			</div>
        	<div class="fieldName">
	           	<label>LOG SIZE</label>
 			</div>
			<div class="data">
				<input type="text" id="logSize" title="Log size should be less than 1000" size="10"></input>
			</div>
        	<div class="logbutton">
	           	<button  id="btnSearch">Log Search</button>
	        </div>
 		</form>
	</div>

	<div><H2 style="height:100%;padding:5px; margin-top:10px; font-family:Sans-serif; font-size:1.2em;">LOG Contents</H2></div>

	<div class="LblockBlank Lclear">
		<div id="logArea" >
			<textarea id="logText" class="ui-corner-all ui-widget-content" style="width=100%;height:100%;padding:5px; margin-top:10px; font-family:Sans-serif; font-size:1.2em;"></textarea>
		</div>
	</div>		
</div>
</div>
</body>
</html>
