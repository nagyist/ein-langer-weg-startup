function updateClock() {
	var months = [ "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" ];
	var days = ["So.", "Mo.", "Tue.", "Wed.", "Thu.", "Fr.", "Sat."];
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
	document.getElementById("hour").innerHTML = day + " " + day_nr + ". "
			+ month + " " + year + "  " + h + ":" + m + ":" + s;
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

function loadMe(id) {
	var visibility = document.getElementById(id).style.visibility;
	if (visibility == 'hidden') {
		var style = "display: inline-block;	font-size: 14px;width: 90%;height: 100%;text-align: center;color: #111;background-color: inherit;font-family: Verdana, Arial, sans-serif;";
		document.getElementById(id).style.visibility = 'visible';
		document.getElementById(id).style.backgroundColor = '#eeee11';
		document.getElementById(id).style.padding = '4px';
		document.getElementById(id).style.marginTop = '-2px';
		document.getElementById(id).style.width = '100%';
		document.getElementById(id).innerHTML = "<li style='"+style+"'>Elem0</li><li style='"+style+"'>Elem1</li><li style='"+style+"'>Elem2</li>";	
	} else {
		document.getElementById(id).style.visibility = 'hidden';
		document.getElementById(id).innerHTML = '';
		document.getElementById(id).style.padding = '0px';
		document.getElementById(id).style.marginTop = '0px';
	}
//	var i = 0;
//	var temp = document.getElementById("elem" + i); 
//	while (temp != null) {
//		if (id != "elem" + i) {
//			document.getElementById("elem" + i).style.visibility = 'hidden';
//			document.getElementById("elem" + i).innerHTML = '';
//			document.getElementById("elem" + i).style.padding = '0px';
//			document.getElementById("elem" + i).style.marginTop = '0px';
//		}
//		i++;
//		temp = document.getElementById("elem" + i);
//	}
}
function loadUeberMich(id) {
	document.title = "About me - bouchnafa.de";
	var uebermich = '<table style="padding:20px;margin-left:30px;"><tbody><tr>'
			+
			'<td style="padding:10px;margin-left:10px;margin-right:10px;"><p>Bouchnafa Thami</p><img src="../ressources/images/bouchnafa_thami.jpg"><p style="text-align: center;">+49 721 911704718</p></td>'
			+
			'<td ><div><div><strong>Allgemein:</strong></div>'
			+
			'<div>Studium: KIT (Karlsruhe Institut of Technology)</div>'
			+
			'<div>Studiengang: Informatik Bachelor of Sciences</div>'
			+
			'<div>Semester: 9</div>'
			+
			'<div>&nbsp;</div>'
			+
			'<div><strong>Studieninteressen und Kompetenzen:</strong></div>'
			+
			'<div>Datenbanken, Datenschutz, Data-mining, Telematik, Sicherheit</div>'
			+
			'<div>Programmiersprachen: Java, C#, C, C++, PHP, JavaScript, HTML, CSS</div>'
			+
			'<div>&nbsp;</div>'
			+
			'<div><strong>Projektteams:</strong></div>'
			+
			'<div>Homepages &amp; Search Produkts Development</div>'
			+
			'<div>Search &amp; Transaction</div>'
			+
			'<div>Moderne Web-Technologien</div>'
			+
			'<div>&nbsp;</div>'
			+
			'<div><strong>Aktuell:</strong></div>'
			+
			'<div>Bachelorarbeit am KIT bei der Firma 1&amp;1 Internet AG</div>'
			+
			'<div>Identifizierung von Benutzerabsichten in Person bezogenen Suchanfragen mit Hilfe von AdaBoost / Feedback Scores</div>'
			+
			'<div>Praktikant bei 1&amp;1 Internet AG (Homepages &amp; Search Produkts Development)</div>'
			+
			'<div>&nbsp;</div></div></td></tr></tbody></table></center></div>';
	document.getElementById(id).innerHTML = uebermich;
}

function loadSearchEngine(id) {
	document.title = "Suchmachine - bouchnafa.de";
	var searchEngineHTML = '<center><div><input id="searchQuery" type="text" style="margin-top:250px;margin-buttom:300px;height:30px;width:500px;align:center" /></div></center>';
	document.getElementById(id).innerHTML = searchEngineHTML;
}
