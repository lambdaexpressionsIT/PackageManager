# PackageManager

L'applicazione offre una serie di servizi REST che permettono di salvare su un server i files di applicazioni android,
di marcarli come invalidi, di cancellarli e di recuperare gli stessi files oltre che tutte le informazioni a essi
relative quali

* nome dell'applicazione
* nome del package dell'applicazione
* elenco delle versioni dell'applicazione presenti sul server
* nome del file dell'applicazione
* URL pubblico del file sul server _Package Manager_
* validità del file

Questi files vengono salvati internamente nel server a partire da una cartella base (da definirsi da parte dell'utente,
vedi documentazione seguente) secondo un pattern definito internamente all'applicazione ovvero
*cartella_base/nome_applicazione/versione/nome_file*

Se la cartella base è resa pubblica dal webserver che ospita l'applicazione, allora i files possono essere raggiungibili
attraverso l'URL pubblico menzionato precedentemente. Anche questo viene composto automaticamente dall'applicazione a
partire da un URL base (da definirsi da parte dell'utente, vedi documentazione seguente) seguendo lo stesso pattern
*URL_base/nome_applicazione/versione/nome_file*

L'applicazione inoltre offre un servizio di limitazione della banda utilizzabile. Questo servizio può essere attivato e
configurato dall'utente.

## Configurazione

### Database set-up

Per il set-up del database dell'applicazione sono necessari due distinti passaggi da effettuarsi esternamente all'
applicazione stessa, quindi esclusivamente sul database server a cui l'applicazione si collegherà.

#### Creazione dell'utente

Il primo passo da effettuare è la creazione di un utente avente diritti di connessione, creazione di tabelle e diritti
di lettura e scrittura su queste. Il file  
__oracle-setup/prodUserCreation.sql__  
contiene le istruzioni per la creazione dell'utente e dei suoi privilegi in un database Oracle e __deve essere eseguito
da un utente con diritti amministrativi__. I dati contenuti in esso sono già coerenti con i valori di default delle
proprietà dell'applicazione

#### Creazione delle tabelle

Successivamente alla creazione dell'utente, è necessario creare la tabella dell'applicazione. Questa operazione deve
essere eseguita dall'utente appena creato per non creare conflitti di diritti e prefissi del nome. Anche per questa
operazione è presente il file  
__oracle-setup/prodTableCreation.sql__  
che contiene l'istruzione per la creazione della tabella dell'applicazione, specificatamente per un database Oracle.
Questo script __deve essere eseguito dall'utente creato per l'applicazione__. Il nome, la struttura e i nomi delle
colonne della tabella devono obbligatoriamente essere rispettati, ogni variazione di questi dati non permetterebbe il
funzionamento dell'applicazione.

### Proprietà dell'applicazione

Il file  
__src/main/resources/application.properties__  
contiene una serie di proprietà che devono essere configurate affinché l'applicazione possa funzionare correttamente.
Per alcune di esse i valori di default permettono già il corretto funzionamento dell'applicazione, mentre altre devono
obbligatoriamente essere definite coerentemente con i dati dell'ambiente di esecuzione.

#### Data source

Qui di seguito sono riportate le proprietà relative alla connessione al database, si tratta di proprietà standard del
framework Spring.

* __spring.datasource.url__: indirizzo del database. __DA DEFINIRSI OBBLIGATORIAMENTE__
* __spring.datasource.username__: nome dell'utente abilitato alla connessione e alle operazioni standard di
  lettura/scrittura sulla tabella dell'applicazione. Il valore di default è coerente con lo script di creazione
  dell'utente oracle-setup/prodUserCreation.sql
* __spring.datasource.password__: password dell'utente abilitato alla connessione e alle operazioni standard di
  lettura/scrittura sulla tabella dell'applicazione. Il valore di default è coerente con lo script di creazione
  dell'utente oracle-setup/prodUserCreation.sql
* __spring.jpa.properties.hibernate.dialect__: permette di generare comandi SQL ottimizzati per uno specifico vendor. Di
  default è impostato per generare SQL ottimizzato per Oracle 12c.

#### Directory base

* __packages.filesystem.base.path__: path assoluto della cartella base in cui verranno salvati i files delle
  applicazioni android secondo il pattern specificato precedentemente. __DA DEFINIRSI OBBLIGATORIAMENTE__

#### Gestione files

Le seguenti proprietà standard del framework Spring permettono di gestire lo scambio di file di dimensioni non
trascurabili via HTTP. I valori di default offrono un certo margine di sicurezza visto l'uso tipico che si prospetta
per _Package Manager_. Oltre a queste proprietà è importante notare che alcuni webserver (ad es. jBoss) possono
necessitare di alcune modifiche alla propria configurazione standard per accettare richieste e files di dimensioni
superiori a una certa soglia (ad es. 10 MB per jBoss).

* __spring.servlet.multipart.max-request-size__: dimensione massima consentita delle richieste HTTP. Valore di default:
  110MB
* __spring.servlet.multipart.max-file-size__: dimensione massima consentita dei file allegati alle richieste HTTP.
  Valore di default: 100MB

#### Limitatore di banda

Questa serie di proprietà, specifiche di *Package Manager*, permette di attivare e configurare il servizio di
limitazione della banda a disposizione dell'applicazione stessa. In questo caso i valori di default permettono il
corretto funzionamento dell'applicazione e non devono necessariamente essere modificati. Di default questo servizio è
disabilitato, il che significa che l'applicazione usa tutta la banda messa a disposizione dall'ambiente di esecuzione.
Utile rimarcare che per quanto i valori di default della banda totale, upload e download siano specificati, questi non
hanno alcun effetto finché il servizio non viene attivato. I valori specificati in questo file vengono caricati
all'avvio dell'esecuzione del programma, e possono essere modificati a run-time attraverso i servizi REST di
configurazione (vedasi documentazione dei servizi REST)

* __bandwidth.limitation.enabled__: true/false, permette di attivare il servizio di limitazione della banda. Valore di
  default: false
* __bandwidth.max.kbitPerSecond__: banda massima allocata per l'applicazione. Se inferiore a uno dei due seguenti, ne
  limita il valore sostituendovi il proprio, se superiore o uguale ai due seguenti, non ne modifica i valori. Valore di
  default: 8000 kbps
* __download.max.kbitPerSecond__: velocità massima consentita di download dall'applicazione. Valore di default: 8000
  kbps
* __upload.max.kbitPerSecond__: velocità massima consentita per upload verso l'applicazione. Valore di default: 4000
  kbps

#### Dati deployment

Definiti come segue i componenti dell'URL di deployment di Package Manager
__{schema}://{indirizzo-base}:{porta}/{contesto}/{indirizzo-specifico}__  
qui un esempio dei componenti di un indirizzo di un servizio di Package Manager  
https://104.196.36.251:8080/package-manager/api/v1/listPackages

* schema: *https*
* indirizzo base: *104.196.36.251*
* porta: *8080*
* contesto: *package-manager*
* indirizzo specifico: *api/v1/listPackages*

Le seguenti proprietà possono essere definite per personalizzare l'ambiente di esecuzione

* __application.public.base.url__: questa proprietà contiene l'indirizzo pubblico di base a cui è raggiungibile Package
  Manager. Viene utilizzata per comporre l'URL pubblico del servizio warehouse (vedasi documentazione servizi REST). Nel
  caso in cui sia specificata, il suo valore deve avere il seguente formato  
  __{schema}://{indirizzo-base}:{porta}__  
  ad esempio  
  https://www.package-manager-instance.com:9090  
  Nel caso in cui il valore della proprietà sia lasciato vuoto, l'URL pubblico del servizio warehouse viene costruito a
  runtime usando l'indirizzo a cui è stato invocato il servizio REST di Package Manager interrogato. Ad esempio se viene
  invocato Package Manager al seguente specifico servizio REST  
  https://104.196.36.251:8080/package-manager/api/v1/listPackages/  
  l'indirizzo base calcolato a runtime sarà  
  https://104.196.36.251:8080  
  Valore di default: (non definito)


* __server.servlet.context-path__: questa proprietà permette di definire il contesto di deployment dell'applicazione.  
  __IL PRIMO CARATTERE DEVE OBBLIGATORIAMENTE ESSERE / E L'ULTIMO CARATTERE NON PUO ESSERE LO STESSO /.__  
  Valore di default: /package-manager

#### Configurazione logging
Le seguenti proprietà sono necessarie per configurare l'attività di logging

* __logging.level.root__: livello minimo dei messaggi di log da salvare nei file di log. Valore di default: info
* __logging.file.path__: percorso della cartella in cui i files di log verranno salvati. Valore di default: /var/log
* __logging.file.name__: nome del file di log globale. Valore di default: PackageManager.log
* __logging.file.max-history__: numero massimo di file di logs passati ammessi nella cartella dei logs. Valore di default: 5
* __logging.file.max-size__: dimensioni massime consentite per un singolo file di log. Quando il file corrente raggiunge questo valore di soglia, viene salvato anche se in anticipo rispetto alla sua naturale scadenza temporanea. Valore di default: 10MB.
* __logging.pattern.rolling-file-name__: nome del file generato per l'intervallo temporaneo scelto. Valore di default: PackageManager.%d{yyyy-MM-dd}.%i.log

## Installazione

Per installare l'applicazione in qualsiasi webserver è prima necessario compilare questi sorgenti con il comando  
__mvn package__  
Questo comando compila i sorgenti, esegue i tests automatici, genera gli snippets della documentazione dei servizi REST
e infine genera il file  
__target/package-manager.jar__  
Questo file prodotto è quindi pronto per essere caricato ed eseguito nell'ambiente di un webserver. Si raccomanda
l'utilizzo di questo comando in quanto permette la generazione automatica della documentazione dei servizi REST.

### Creazione di un jar eseguibile

Per ottenere un file jar eseguibile è necessario compilare questi sorgenti con il comando  
__mvn package spring-boot:repackage__  
Questo comando, in aggiunta a quanto riportato sopra, genera anche il file  
__target/package-manager-microservice.jar__  
direttamente eseguibile da shell tramite il comando  
__java -jar package-manager-microservice.jar__

## Servizi REST

### Documentazione

La documentazione dettagliata e specifica di tutti i servizi REST resi disponibili da _Package Manager_ viene creata
automaticamente all'esecuzione del comando _mvn package_ sopra indicato a partire dal file statico  
__src/main/asciidoc/index.adoc__  
Questo file viene integrato con diversi snippets di HTTP request e response generati automaticamente dall'esecuzione dei
tests automatici e salvati nella cartella  
__target/generated_snippets__  
Il risultato della combinazione del file statico e di questi snippets è il file  
__target/generated_docs/index.html__  
Questi sarà automaticamente disponibile dopo il deployment dell'applicazione all'indirizzo  
__http://{deployment.url}/docs/index.html__

### Esempi di richieste

Il file  
__Package manager.postman_collection.json__  
è una collezione di richieste di esempio verso ognuno dei servizi REST resi disponibili da _Package Manager_. Sarà
sufficiente importare il file in Postman e modificare l'indirizzo base delle requests coerentemente con l'ambiente di
esecuzione corrente per testare il funzionamento dell'applicazione.