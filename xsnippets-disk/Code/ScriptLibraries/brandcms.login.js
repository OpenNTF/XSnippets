dojo.ready(function() {
	try {
	var loginNode = $(".cm-login-attachpoint");
	if (loginNode) {
		lNode = loginNode[0];
		$(lNode).parent().attr('class','dropdown');
		$(lNode).attr('class','dropdown-toggle');
		$(lNode).attr('href','#');
		$(lNode).attr('data-toggle','dropdown');
		$(lNode).prepend('<span class="glyphicon glyphicon-log-in"></span>');
		$(lNode).after('<div class="dropdown-menu" style="padding: 15px; padding-bottom: 10px;">'+
		'<div id="login-alert" class="alert alert-danger" role="alert" style="display:none"></div>'+
		'<div id="login-success" class="alert alert-success" role="alert" style="display:none"></div>'+
		'<form id="login-dialog" class="form-horizontal"  method="post" accept-charset="UTF-8">'+
		'<input id="sp_uname" class="form-control login" type="text" name="sp_uname" placeholder="Username.." style="margin-bottom:5px;" />'+
		'<input id="sp_pass" class="form-control login" type="password" name="sp_pass" placeholder="Password.." style="margin-bottom:5px;"/>'+
		'<a href="https://openntf.org/PasswordResetOpenNTF.nsf/resetPassword.xsp" title="Reset Your Password">Forgotten your login credentials?</a><br/>' +
		'<div style="margin-top:10px"><input class="btn btn-primary" type="submit" name="submit" value="Login" />'+
		'<input class="btn" type="button" name="register" value="Register" onclick="location.href=\'https://openntf.org/main.nsf/registration.xsp\'" style="margin-bottom:5px;float:right" />' +
		'</div></form>'+
		'</div>');
		$('#login-dialog').submit(function(event){
			event.preventDefault();
			var user = $('#sp_uname').val();
			var pw = $('#sp_pass').val();
			if (pw === '' || user === '') {
				$('#login-alert').text("Please provide a username/password").show().fadeOut(2000);
				return;
			}
			var checkLoginURL = window.location.protocol +'//'+ window.location.host +window.location.pathname+"/checklogin"+window.location.search;
			$.post("/names.nsf?login", {username:user,password:pw,redirectto:checkLoginURL}, function(data){
				if(typeof data =='object'){
					$('#login-success').text("Login successful!").show().fadeOut(2000);
					window.location.reload(true);
				} else {
					$('#login-alert').text("Login failed! Please check username / password").show().fadeOut(2000);					
				}		
			});
		});
	};
	} catch (error) {
		console.trace();
		console.log(error);
	}
});