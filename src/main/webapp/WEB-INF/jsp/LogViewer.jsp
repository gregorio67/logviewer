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
  </style>

<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css"></link>
<link rel="stylesheet" href="/resources/demos/style.css" /></link>
<script src="https://code.jquery.com/jquery-1.12.4.js"></script>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  
<%@ include file="/WEB-INF/jsp/common/include/ruiCommon.jspf" %>
<script type="text/javascript">

	$(document).ready(function() {
		
		var width = 1120;
		var height = 600;
		$('#logText').attr('readonly','readonly');
		$('#logText').prop('readonly', true);
		$('#logText').css('width', width);
		$('#logText').css('height', height);
		
		$("#btnSearch").click(function(e) {
			debugger;
			var targetURL = $("#system option:selected").val();
			alert(targetURL);
		    e.preventDefault();
		    $.ajax({
		        type: "POST",
		        url: targetURL,
		        crossDomain : true,
		        dataType : "json",
		        success: function(result) {
		            alert(result.log);
		            $("#logText").val(result.log);
		        },
		        error: function(result) {
		            alert(result.log);
		        }
		    });
		});
	});

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
	<div class="LblockSearch">
        <form action="">
            <table summary="">
                <tbody>
                    <tr>
                    	<th>
                    	</th>
                        <th>
                        	<label for="systemname">SYSTEM</label>
                            <select name="system" id="system">
						        <option value="http://localhost:7070/ibm/log/viewer.do">PORTAL-WAS1</option>
						        <option value="http://localhost:7070/ibm/log/viewer.do">IBM-WAS1</option>
						        <option value="http://localhost:7070/ibm/log/viewer.do">DSC-WAS1</option>
						        <option value="http://localhost:7070/ibm/log/viewer.do">INF-WAS1</option>
						    </select>
                        </th>
                        <th>
                        	<button class="ui-button ui-widget ui-corner-all" id="btnSearch">Log Search</button>
                        </th>                   
                    </tr>
                </tbody>
            </table>
		</form>
	</div>

	<div><H2>LOG</H2></div>

	<div class="LblockBlank Lclear">
		<div id="logArea" >
			<textarea id="logText" class="ui-corner-all ui-widget-content" style="height:100%;padding:5px; margin-top:50px; font-family:Sans-serif; font-size:1.2em;"></textarea>
		</div>
	</div>		
</div>
</div>
</body>
</html>
