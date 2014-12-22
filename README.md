KafkaTutorial
=============
Download Kafka froom here https://www.apache.org/dyn/closer.cgi?path=/kafka/0.8.1.1/kafka_2.9.2-0.8.1.1.tgz

No additonal configuration is required

*******kafta/bin/config/zookeeper.properties --> Change the port number to 2180 if it is already in use in zookeper.properties and server.properties

****kafka/bin/config/server.properties --> this has details of the port that will be used by producer for publishing messages. (9092 port number)

if we get below error follow you tube link below or comment

sudo /usr/lib/kafka/bin/kafka-server-start.sh config/server.properties
Unrecognized VM option 'UseCompressedOops'
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.



go to bin/kafa-class.sh and remove -XX:+UseCompressedOops and update as below.
# JVM performance options
if [ -z "$KAFKA_JVM_PERFORMANCE_OPTS" ]; then
  KAFKA_JVM_PERFORMANCE_OPTS="-server -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -Djava.awt.headless=true"
fi

1. Start ZooKeeper  --> Note its shell script so it should be exeuted as ./ if from same folder else use /etc

 bin/zookeeper-server-start.sh config/zookeeper.properties


2. Start Kafka Cluster

bin/kafka-server-start.sh config/server.properties


3. Create Topic -- Optional but can be done at runtime

bin/kafka-topics.sh --create --zookeeper localhost:2180 --replication-factor 1 --partition 1 --topic truckevent

We can now see that topic if we run the list topic command:

> bin/kafka-list-topic.sh --zookeeper localhost:2180


4. Produce message

bin/kafka-console-producer.sh --broker-list localhost:9092 --topic truckevent


5. Consume message

bin/kafka-console-consumer.sh --zookeeper localhost:2180 --topic treuckevent --from-beginning

---------------------------------Instructions to execute Kafka Tutorial-------------------------------------------
n Apache Kafka introduction we discussed some key features of Kafka. In this tutorial we will setup a small Kafka cluster. We will send messages to a topic using a JAVA producer. We will consume the messages using a JAVA consumer.

For this tutorial you will need

(1) Apache Kafka
(2) Apache Zookeeper
(3) JDK 7 or higher. An IDE of your choice is optional
(4) Apache Maven
(5) Source code for this sample kafkasample.zip

Zookeeper is required as the Kafka broker uses Zookeeper to store topic configuration and consumer information.

We will setup a 3 node cluster. I will assume you have 3 machines: host1,host2,host3. We will run a Kafka broker on each host. We will run Zookeeper on host1 and host2. You can do this tutorial on one machine, but you will need to change the port numbers to avoid conflict.

Youtube : https://www.youtube.com/watch?v=ArUHr3Czx-8 

Step 1: Download the latest binaries

Apache Kafka can be downloaded at http://kafka.apache.org/downloads.html
Apache Zookeeper can be downloaded at http://kafka.apache.org/downloads.html

Step 2: Install Zookeeper

We will install zookeeper on host1 and host2. At least 2 is a good idea so that the cluster is still usable if one of the servers goes down.

For instructions on how to setup a zookeeper cluster see http://zookeeper.apache.org/doc/trunk/zookeeperStarted.html. The instructions are simple and we will not repeat them here.

Step 3: Configure the Kafka Brokers

Assume you have unzipped the binaries to /usr/local/kafka.

Edit the config/server.properties on each host as follows:

Each broker needs a unique id. On host1 set
broker.id=1
On host2 set it to 2 and on host3 set it to 3

On each host, set the directory where the messages are stored
log.dirs=/srv/apps/kafka/data

Tell the brokers which zookeepers to connect to
zookeeper.connect=host1:2181,host2:2181

These are the only properties that need changes. All the other default properties in server.properties are good enough and you can change them as required.

Step 4: Start the brokers

On each server , from /usr/local/kafka/bin execute

kafka-server-start.sh server.properties &

Step 5: Create a topic

Let us create a topic mytopic with 3 partitions and a replication factor of 2.

/usr/local/kafka/bin$ kafka-topics.sh --create --zookeeper host1:2181 --replication-factor 2 --partitions 3 --topic mytopic

Step 5: Write a JAVA producer

The complete source code is at kafkasample.zip.

Create the properties that need to be passed to the producer.
       
        Properties props = new Properties();
        props.put("metadata.broker.list", "host2:9092,host3:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");
        ProducerConfig config = new ProducerConfig(props);

metadata.broker.list is the list of brokers that the producer can try to connect to.
We are sending text messages. So we use the String encoder.   Setting request.required.acks to 1 ensures that a publish message request is considered completed only when an acknowledgment is received from the leader.
            
        Producer producer = new Producer(config);
     
The above line creates a producer which will send key and value both of type String.
  
        String topic = "mytopic"     
        for (int i = 1 ; i <= 1000 ; i++) {
           
            String msg = " This is message " + i ;

            KeyedMessage data = new KeyedMessage(topic, String.valueOf(i), msg);
            producer.send(data);

        }
        producer.close();
       
The code above sends 1000 messages to the topic.

Step 6: Write a JAVA consumer

Our topic has 3 partitions. Messages within a partition are delivered to a consumer in order. So one consumer per partition makes sense. In our consumer,we will create 3 threads, one for each partition. We use what is called the High level consumer API

First setup the consumer configuration

        Properties props = new Properties();
        props.put("zookeeper.connect", "host:2181");
        props.put("group.id", "mygroupid2");
        props.put("zookeeper.session.timeout.ms", "413");
        props.put("zookeeper.sync.time.ms", "203");
        props.put("auto.commit.interval.ms", "1000");
        ConsumerConfig cf = new ConsumerConfig(props) ;

zookeeper.connect tells the consumer which zookeeper to connect. Consumer needs to connect to zookeeper to get topic information as well as store consumer offset. group.id is the name of the consumer group. Every message is delivered to each consumer group. auto.commit.interval.ms tells how often the consumer should commit offsets to zookeeper.

       ConsumerConnector consumer = Consumer.createJavaConsumerConnector(cf) ;
       String topic = "mytopic"  ;
       Map topicCountMap = new HashMap();
       topicCountMap.put(topic, new Integer(3));
       Map<String,List<KafkaStream<byte[],byte[]>>> consumerMap =
               consumer.createMessageStreams(topicCountMap);
       List<KafkaStream<byte[],byte[]>> streams = consumerMap.get(topic);

The code above creates a ConsumerConnector and gets a list of KafkaStreams. For this topic, we indicate 3 streams are required ( 1 for each partition) and the connector creates 3 streams.

        ExecutorService executor = Executors.newFixedThreadPool(3); ;
        int threadnum = 0 ;     
        for(KafkaStream stream  : streams) {
            executor.execute(new KafkaPartitionConsumer(threadnum,stream));
            ++threadnum ;
        }

The code above creates a threadpool and submits a runnable KafkaPartitionConsumer that will read the stream. The code for the runnable is shown below.

        public static class KafkaPartitionConsumer implements Runnable {
            private int tnum ;
            private KafkaStream kfs ;
            public KafkaPartitionConsumer(int id, KafkaStream ks) {
                tnum = id ;
                kfs = ks ;
            }   
            public void run() {
                System.out.println("This is thread " + tnum) ;
                ConsumerIterator it = kfs.iterator();
                int i = 1 ;
                while (it.hasNext()) {
                    System.out.println(tnum + " " + i + ": " + new String(it.next().message()));
                    ++i ;
                }
            }
        }

Each thread is reading from a stream from a particular partition. If there are no messages, the call hasNext() will block.

Step 7 : Start the consumer

I built my code using maven. When there are dependencies on jars, it is also easier to use maven to run the program, as it pulls in all the dependencies automatically into the classpath

mvn exec:java -Dexec.mainClass="com.mj.KafkaMultiThreadedConsumer"

Step 8 : Start the producer

mvn exec:java -Dexec.mainClass="com.mj.KafkaProducer"

You should see the consumer print out the messages.

You can start multiple consumers with a different groupid and they will each receive all the messages.

---------------------------------Modifications by Asmath -------

See the consumer and producer file..Changed port and host as we are using only one host
also server and zookeeper.properties file is changed.
Found this tutorial in http://khangaonkar.blogspot.com/2014/05/apache-kafka-java-tutorial.html

