function updateClock() {
	var months = [ "January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December" ];
	var days = [ "So.", "Mo.", "Tue.", "Wed.", "Thu.", "Fr.", "Sat." ];
	var now = new Date();

	var day = days[now.getDay()];
	var day_nr = now.getDate();
	var month = months[now.getMonth()];
	var year = now.getFullYear();
	var h = now.getHours();
	var m = now.getMinutes();
	var s = now.getSeconds();
	m = checkTime(m);
	s = checkTime(s);

	var abgabeDate = new Date('2014/12/08');
	var abgabeDays = Math.floor((abgabeDate - now) / (1000 * 60 * 60 * 24));

	document.getElementById("hour").innerHTML = h + ":" + m + ":" + s
			+ "<br />" + day + " " + day_nr + ". " + month + " " + year + "  "
			+ "<br />Abgabe fällig in <span style='color: red'>" + abgabeDays
			+ "</span> Tage";
	t = setTimeout(function() {
		updateClock();
	}, 1000);
}
function checkTime(i) {
	if (i < 10) {
		i = "0" + i;
	}
	return i;
}

function loadSearchEngine(id) {
	document.title = "Suchmachine - bouchnafa.de";
	var searchEngineHTML = '<center><div><input id="searchQuery" type="text" style="margin-top:250px;margin-buttom:300px;height:30px;width:500px;align:center" /></div></center>';
	document.getElementById(id).innerHTML = searchEngineHTML;
}

$(document).ready(function() {
	$("#toggle").click(function() {
		$("#instances").toggle();
	});
});
$(document).ready(function() {
	$("#flip").click(function() {
		$("#panel").toggle();
	});
});
$(document).ready(function() {
	$("#toggleInstances").click(function() {
		$("#instances").toggle();
	});
});
$(document).ready(function() {
	$("select").css("height", "35px", "margin-bottom", "15px");
});

$(document)
		.ready(
				function() {
					if ($("#music").text() == "true") {
						$("#musicFeedback").css("visibility", "visible");
						$("#musicFeedback").css("cursor", "pointer");
						$("#musicFeedback").css("width", "680px");
						$("#musicFeedback").css("height", "220px");
						$("#musicFeedback")
								.css("background",
										"url(../images/musik_ressourcen.png) no-repeat right top; )");
					} 
					if ($("#ortung").text() == "true") {
						$("#ortungFeedback").css("visibility", "visible");
						$("#ortungFeedback").css("cursor", "pointer");
						$("#ortungFeedback").css("width", "680px");
						$("#ortungFeedback").css("height", "220px");
						$("#ortungFeedback")
								.css("background",
										"url(../images/land_ort.png) no-repeat right top; )");
					} 
					if ($("#sport").text() == "true") {
						$("#sportFeedback").css("visibility", "visible");
						$("#sportFeedback").css("cursor", "pointer");
						$("#sportFeedback").css("width", "680px");
						$("#sportFeedback").css("height", "220px");
						$("#sportFeedback")
								.css("background",
										"url(../images/sport_career.png) no-repeat right top; )");
					} 
				});
