<img  align="left" width="150" style="float: left;" src="https://www.upm.es/sfs/Rectorado/Gabinete%20del%20Rector/Logos/UPM/CEI/LOGOTIPO%20leyenda%20color%20JPG%20p.png">
<img  align="right" width="60" style="float: right;" src="https://www.dit.upm.es/images/dit08.gif">


<br/><br/>


# Practica PUB/SUB - Apache Kafka

## 1. Objetivo

- Afianzar los conceptos sobre sistemas de Publicación/Suscripción
- Conocer y operar con los distintos API's para manejo de eventos que provee Apache Kafka

## 2. Dependencias

Para realizar la práctica el alumno deberá tener instalado en su ordenador:
- Herramienta GIT para gestión de repositorios [Github](https://git-scm.com/downloads)
- Apache Kafka + KRaft 4.2.0 [Kafka](https://kafka.apache.org/downloads)



## 3. Descripción de la práctica

La práctica plantea una serie de ejercicios para afianzar los conocimiento sobre el tema de sistemas de Publicación/Subscripción en entornos de Big Data, en este caso la herramienta que se utiliza es Apache Kafka que utiliza Raft como protocolo de consenso.

La práctica está desarrollada en tres bloques en donde se pretende ejemplificar a nivel práctico como usar los diferentes API’s que proporciona Kafka.

El primer bloque del desarrollo de la práctica está compuesto por una única instancia de Kafka y por un productor y un consumidor. Por medio de este entorno se plantea el envío y recepción de multiples mensajes por medio de Kafka  conformando así un sistema de recepción y envío de mensajes en streaming.

Posteriormente, se planeta el uso del API de Kafka connect para capturar y almacenar eventos en streaming en un sistema de almacenamiento.

Finalmente, se proporciona el entorno y código necesario para realizar un contador de palabras de eventos que se generan en streaming por medio del API de Kafka Streams y un programa desarrollado en Java.

## 4. Inicializar el entorno.

Para la realización de la práctica se ha provisionado una máquina virtual con sistema operativo linux y distribución Ubuntu. En los recursos de Moodle de la asignatura se acceder al enlace de descarga.

Descarge el fichero de VM con extensión .ova e importelo en virtualbox. 

Dentro de la máquina virtual abra un terminal y realice los siguientes pasos.

Cambiar el directorio de trabajo para ejecutar los binario de la distribución de Kafka:
```
cd ./Downloads/kafka_2.13-4.2.0
```

Iniciar el servicio de Kafka con KRaft:

Generar el Cluster UUID:
```
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
```

Indicar el formato de logs:

```
bin/kafka-storage.sh format --standalone -t $KAFKA_CLUSTER_ID -c config/server.properties
```

Iniciar el servicio de Kafka:

```
bin/kafka-server-start.sh config/server.properties
```
Al ejecutar dicho comando el servicio de Kafka se habrá arrancado y como salida se verán los logs de inicialización, recuerde no cerrar el teminal a menos que se quiera detener dicho servicio.


## 5. Tareas a realizar.

### Bloque 1: Lectura y escritura de eventos por medio de tópicos, productores y consumidores.

Usando como directorio de trabajo la carpeta donde se encuentran los binarios de Kafka, crear el tópico a usar, ejecutando lo siguiente:

```
bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092

```

Donde `quickstar-events` es el nombre del tópico que vamos a crear y `localhost:9092` es el hostname y el puerto del servidor donde se está ejecutando Kafka.

Verifique que el tópico se ha creado correctamente con la siguiente instrucción:

```
bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092

```

Donde se debería obtener como salida lo siguiente:

```
Topic: quickstart-events        TopicId: NPmZHyhbR9y00wMglMH2sg PartitionCount: 1       ReplicationFactor: 1  Configs:
    Topic: quickstart-events Partition: 0    Leader: 0   Replicas: 0 Isr: 0
```

**Iniciar el productor y escribir algunos eventos en el tópico previamente creado**

```
bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092
```
con el comando anterior el terminal se quedará esperando las instrucciones o mensajes que queramos enviar por lo que allí incluiremos nuestros mensajes:
```
evento 1
nuevo evento 2
evento 3 escrito en el topico
```

Para detener el cliente del productor se puede usar la instrucción `Ctrl-C` en cualquier momento.

**Iniciar el consumidor para leer los eventos que han sido enviados al tópico**

```
bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092
```
Al ejecutar el comando anterior se inicia un consumidor y por lo tanto, todos los mensajes que fueron publicados en dicho tópico se mostrarán en la consola, por lo que la salida esperada sería la siguiente:

```
evento 1
nuevo evento 2
evento 3 escrito en el topico
```


Abrir tres terminales y ejecutar el cliente del consumidor en cada una de ellas para que permitan consumir al  tópico `quickstart-events`. Luego volver al terminal del productor, escribir dos o tres nuevos eventos en donde se incluya su nombre y apellido  y verificar que los nuevos eventos escritos aparecen en todos los consumidores que estan escuchando en ese tópico. Realizar una captura de pantalla (Captura1) de los tres terminales de los consumidores y subirlo a moodle. 

A continuación es necesario descargarse el repositorio donde se encuentran los recursos que se van a utilizar en esta parte de la práctica.

### Bloque 2: Importar y exportar los datos como eventos en streaming usando Kafka Connect.


Kafka connect nos permite por medio un gran conjunto de pluigns, leer y escribir datos de distintas fuentes de almacenamiento (ficheros, bases de datos, etc) por medio de los tópicos de Kafka. 

En esta sección se presenta el escenario en el que por medio de Kafka connect, se leerán cada una de las líneas incluidas en un fichero de texto con el objetivo de convertir cada una de ellas en eventos que serán escritos al tópico `connect-test` y posteriormente se terminará creando un nuevo fichero cuyo contenido incluirá todos los eventos publicados en dicho tópico. De forma simultanea un consumidor suscrito a ese tópico presentará los eventos generados.

Para poder utilizar Kafka connect es necesario realizar un configuración previa indicandole el plugin que vamos a utilizar. En este caso vamos a usar `connect-file-3.4.0.jar` que nos va a permitir leer y escribir datos en un fichero de texto. Por lo tanto, es necesario realizar las siguientes operaciones:

Editar el fichero `config/connect-standalone.properties` para añadir la ruta en donde se encuentra el fichero .jar que vamos a usar:

```
nano config/connect-standalone.properties
```
Identificar la línea en donde se encuentra la propiedad `plugin.path` y modificarla para que quede de la siguiente forma:

```
plugin.path=libs/connect-file-4.2.0.jar
```

Abra otro terminal y clone el proyecto utilizando la siguiente instrucción:

```
cd ~
git clone https://github.com/Big-Data-ETSIT/P4_KAFKA
```

y entrar en el directorio de trabajo

```
cd P4_KAFKA
```

Inspeccione el contenido del fichero test.txt usando el siguiente comando:

```
cat test.txt
```
Como salida obtendrá todo el contenido que se encuentra en dicho fichero.

La siguiente línea muestra cual es el archivo binario de Kafka que debemos ejecutar para poder hacer uso de Kafka connect, además se indican los parámetros que se van a utilizar para poder hacer la lectura y escritura de los ficheros:

```
bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-source.properties config/connect-file-sink.properties

```
`bin/connect-standalone.sh` indica que vamos a arrancar Kafka connect en modo standalone, que quiere decir que se ejecutará en una única instancia. 

`config/connect-standalone.properties` contiene la configuración predeterminada de dicho connector. 

`config/connect-file-source.properties` que es donde se definen las propiedades necesarias para leer el fichero que queremos que se escriba como eventos en Kafka, ese fichero lo tenemos que modificar para incluir la ruta del fichero test.txt. 

`config/connect-file-sink.properties` de forma similar que en el anterior aquí se configuran las propiedades del fichero de salida, el mismo que debemos modificar para incluir la ruta correcta a donde queremos que se cree nuestro nuevo fichero. 

Editar el fichero `config/connect-file-source.properties` y modificar la propiedad `file` para incluir lo siguiente:
```
file=../../P4_KAFKA/test.txt
```
Editar el fichero `config/connect-file-sink.properties` y modificar la propiedad `file` para incluir lo siguiente:
```
file=../../P4_KAFKA/test.sink.txt
```
Ejecutar el comando para leer el fichero test.txt y escribir el contenido en el fichero de salida test.sink.txt:

```
bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-source.properties config/connect-file-sink.properties
```

Iniciar un cliente consumidor en el tópico connect-test y observar los eventos que se han publicado en el mismo:

```
bin/kafka-console-consumer.sh --topic connect-test --from-beginning --bootstrap-server localhost:9092
```

Finalmente, abrir el fichero test.txt añadir un par de líneas adicionales guardarlo y observar el comportamiento tanto en el terminal donde se ha iniciado el consumidor como en el fichero de salida test.sink.txt.


### Bloque 3: Procesar eventos con Kafka Stream.

Kafka stream es una libreria de cliente desarrollada en Java y Scala. Esta librería permite realizar la implementación de aplicaciones críticas que utilizan eventos en tiempo real, además de permitir la comunicación con microservicios. Por medio de esta librería se pueden realizar diferentes operaciones como agregaciones, uniones, etc con los eventos que están almacenados en los diferentes tópicos creados en el Kafka Broker.

En esta parte de la práctica se va a realizar un programa usando Kafka stream para contar la ocurrencia de las distintas palabras almacenadas en el tópico `ibdn-input-events`.

De acuerdo a la documentación disponible, un ejemplo del código para realizar la tarea mencionada anteriormente es el siguiente:

```java
KStream<String, String> textLines = builder.stream("quickstart-events");

KTable<String, Long> wordCounts = textLines
            .flatMapValues(line -> Arrays.asList(line.toLowerCase().split(" ")))
            .groupBy((keyIgnored, word) -> word)
            .count();

wordCounts.toStream().to("output-topic", Produced.with(Serdes.String(), Serdes.Long()));
```
Inspeccionar el código de la aplicación que está disponible en la ruta /src del repositorio clonado en el bloque1.

crear el tópico `ibdn-input-events`:

```
bin/kafka-topics.sh --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic ibdn-input-events
```

Iniciar el programa para empezar a contar las palabras de los eventos publicados en el tópico `ibdn-input-events`

```
cd ./P4_KAFKA/wordCount
java -cp target/uber-kafka-streams-wordcount-1.0-SNAPSHOT.jar es.upm.dit.ibdn.WordCountDemo
```

Iniciar un productor y un consumidor para que publiquen y consuman del tópico mostrado anteriormente y de streams-wordcount-output respectivamente:

Abrir un terminal e iniciar el productor:

```
bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic ibdn-input-events
```

Abrir otro terminal e inicar el consumidor:

```
bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic streams-wordcount-output \
  --from-beginning \
  --formatter org.apache.kafka.tools.consumer.DefaultMessageFormatter \
  --property print.key=true \
  --property print.value=true \
  --property key.separator=" : " \
  --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

Procesar ciertos datos:

```
bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic ibdn-input-events
hola estos son los eventos que van a ser contados por el programa de kafka 
```
En la salida de la consola del consumidor debería obtener la siguiente salida:

```
hola	1
estos	1
son	1
los	1
eventos	1
que	1
van	1
a	1
ser	1
contados	1
por	1
el	1
programa	1
de	1
kafka	1


```

Ahora en la misma ventana del terminal escriba la siguiente frase:

```
El programa utiliza el api de kafka streams
```

En la terminal del consumidor debería verse la siguiente salida:

```
hola	1
estos	1
son	1
los	1
eventos	1
que	1
van	1
a	1
ser	1
contados	1
por	1
el	1
programa	1
de	1
kafka	1
programa	2
utiliza	1
el	3
api	1
de	2
kafka	2
streams	1

```

Finalmente publique  una línea adicional usando el productor iniciado en donde se incluya su nombre y apellido y realice una captura de pantalla  de la salida del consumidor (Captura2).

## 7. Instrucciones para la Entrega y Evaluación.
El alumno debe subir un fichero pdf a moodle en donde se incluyan las dos capturas solicitadas y una descripción de las mismas.


