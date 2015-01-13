Beschreibung
============

Prototyp zum Aufzeigen des in der Bachelorarbeit realisierten System zur "Klassifizierung von Suchanfragen"

Abstrakt
========

Die Klassifizierung von Web-Suchanfragen nach Benutzerabsichten beschäftigt sich
mit der Fragestellung, welcher Informationsbedarf sich hinter den Suchanfragen der
Nutzer verbirgt. In dieser Arbeit werden einige Merkmale aus den Suchanfragen extrahiert,
ohne irgendwelche externe oder zusätzliche Informationen über die Suchanfragen
zu kennen, um diese Suchanfragen zu klassifizieren. Wir definieren Merkmale,
die auf Ähnlichkeitsfunktionen basieren. Diese berechnen wir automatisch und implementieren
anschließend den Algorithmus aus dem maschinellen Lernen, um die Genauigkeit
der Klassifikation zu validieren und um die Qualität der berechneten Merkmale
zu testen sowie zu dem Zweck, die Ergebnisse der Klassifikation zu evaluieren.
Die Klassifikation unterstützen wir mit einem impliziten Feedback-Ansatz, den wir für
diese Aufgabe erarbeitet haben. Wir analysieren die erhaltenen Ergebnisse und testen
verschiedene Verfahren, die mit dem verwendeten Algorithmus funktionieren. Zum
Schluss präsentieren wir die Erfahrungen, die durch diese Arbeit mit den Daten und
den getesteten Techniken gemacht wurden.

Ansatz 
======  

Der Ansatz besteht aus den folgenden 3 Teilen:

	 ========================
	| Personennamenerkennung |
	 ========================
	            |
	            V
	 ========================
	|    Klassifikation      |
	 ========================
	            ^
	            |
	 ========================
	|       Feedback         |
	 ========================
	
