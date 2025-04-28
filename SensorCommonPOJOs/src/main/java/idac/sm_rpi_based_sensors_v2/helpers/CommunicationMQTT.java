package idac.sm_rpi_based_sensors_v2.helpers;

import java.util.concurrent.TimeUnit;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient.Mqtt5Publishes;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class CommunicationMQTT extends Communication {

	Mqtt5BlockingClient client;
	Mqtt5Publishes publishes;
	
	public CommunicationMQTT() {
		super();
	}
	
	@Override
	public boolean connect() {
        /*This connection is now made via means of Pub/Sub messaging pattern instead of sockets.
          Read more in here: https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern

          Both the server and the nodes are clients who publish and subscribe to topics in a pub/sub broker (server). 
          It is required to be running in the same local area network as the broker.
        */			
		if(MQTTConfig.trustManagerFactory != null) {
			MqttClientSslConfig clienteConfig = MqttClientSslConfig.builder().trustManagerFactory(MQTTConfig.trustManagerFactory).build();
	        client = Mqtt5Client.builder()
	                .identifier(hostName)
	                .serverHost(MQTTConfig.server)
	                .serverPort(MQTTConfig.port)
	                .sslConfig(clienteConfig)
	                .buildBlocking();
		} else {
	        client = Mqtt5Client.builder()
	                .identifier(hostName)
	                .serverHost(MQTTConfig.server)
	                .serverPort(MQTTConfig.port)
	                .buildBlocking();
		}
        
		if (!MQTTConfig.user.equals("")) {
	        client.connectWith()
			        .simpleAuth()
		            .username(MQTTConfig.user)
		            .password(MQTTConfig.password.getBytes())
		            .applySimpleAuth()
		            .cleanStart(false)
		            .send();
		} else {
			client.connect();
		}
        
        return client.getState().isConnected();
	}

	@Override
	public void register() {
		publishes = client.publishes(MqttGlobalPublishFilter.ALL); //This lines indicates that the client will be listening to all new publications
	}

	@Override
	public void publish(String topic, String content) {
		if(!client.getState().isConnected())
			connect();
		client.publishWith() // publishes the message to the subscribed topic 
	    	.topic(topic) // publishes to the specified topic
	    	.qos(MqttQos.AT_LEAST_ONCE) // Sets the QoS to 2 (At least once)
	        .payload(content.getBytes())  // the contents of the message (has to be send in bytes) 
	    	.send();
	}

//	@Override
//	public void subscribe(String topic) {
//		if(!client.getState().isConnected())
//			connect();
//		
//		client.subscribeWith() // creates a subscription
//	    	.topicFilter(topic) // filters to receive messages only on this topic (# = Multilevel wild card, + = single level wild card)
//	    	.qos(MqttQos.AT_LEAST_ONCE). // Sets the QoS to 2 (At least once)
//	    	send();
//	}

	@Override
	public String receiveMessage(String topic, long timeout) throws InterruptedException {
		if(!client.getState().isConnected())
			connect();
		
		client.subscribeWith() // creates a subscription
	    	.topicFilter(topic) // filters to receive messages only on this topic (# = Multilevel wild card, + = single level wild card)
	    	.qos(MqttQos.AT_LEAST_ONCE). // Sets the QoS to 2 (At least once)
	    	send();
		
		String message = "";
		if(!client.getState().isConnected())
			connect();
	
		Mqtt5Publish receivedMessage = publishes.receive(timeout, TimeUnit.SECONDS).get();
		message = new String(receivedMessage.getPayloadAsBytes()); //The message is received in bytes so it has to be converted to String
		receivedMessage = null; //Sets null to avoid memory leaks

		return message;
	}

	@Override
	public void disconnect() {
		if(client.getState().isConnected())
			client.disconnect();
	}

	@Override
    /* Overriding finalize method to release memory of data structures */
    protected void finalize() throws Throwable  
    {
        client = null;
        publishes = null;
    }
}
