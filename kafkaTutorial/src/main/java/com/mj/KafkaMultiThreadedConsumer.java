package com.mj;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class KafkaMultiThreadedConsumer {
	
	public static class KafkaPartitionConsumer implements Runnable {

		private int tnum ;
		private KafkaStream kfs ;
		
		public KafkaPartitionConsumer(int id, KafkaStream ks) {
			tnum = id ;
			kfs = ks ;
			
		}
		
		
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("This is thread " + tnum) ;
			
			ConsumerIterator<byte[], byte[]> it = kfs.iterator();
				int i = 1 ;
	        	while (it.hasNext()) {
	        		System.out.println(tnum + " " + i + ": " + new String(it.next().message()));
	        		++i ;
	        	}
			
		}
		
		
		
		
		
	}
	
	
	public static class MultiKafka {
		
		public void run() {
			
			
			
			
			
		}
		
	}
	
	
	public static void main(String[] args) {
		
		Properties props = new Properties();
        props.put("zookeeper.connect", "host1:2181");
        props.put("group.id", "mygroupid2");
        props.put("zookeeper.session.timeout.ms", "413");
        props.put("zookeeper.sync.time.ms", "203");
        props.put("auto.commit.interval.ms", "1000");
        // props.put("auto.offset.reset", "smallest"); 
        
      
		
        ConsumerConfig cf = new ConsumerConfig(props) ;
        
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(cf) ;
        
        String topic = "mytopic" ;
        
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(3));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
	
        ExecutorService executor = Executors.newFixedThreadPool(3); ;
        
        int threadnum = 0 ;
        
        for(KafkaStream<byte[],byte[]> stream  : streams) {
        	
        	executor.execute(new KafkaPartitionConsumer(threadnum,stream));
        	++threadnum ;
        }
        
        
        
        
        // consumer.shutdown(); 
	}
	
	

}


