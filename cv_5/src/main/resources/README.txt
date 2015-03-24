Ideal configuration for ThreadPool is 50 Threads.


Úloha 6 - doma

Implementujte echo server protokol tak, že očekává značku <hi> a pak ihned píše text zpět, jinak zavře spojení
<fin> - ukončí klienta
<flip> přepne mód textu a načítá až do </flip> pak pošle text po spátku
(jiná značka zavře spojení)
Otestujte pres JUnit i fail scénáře
Např

# <hi> Hello Bob <flip>Damn</flip> wow <fin>
<# Hello Bob nmaD wow closes

# <hi> Hello <b>Bob<b> <flip>Damn</flip> wow <fin>
<# Hello closes
