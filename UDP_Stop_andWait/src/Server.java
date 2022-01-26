import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

class Server {

    public static void main(String[] args) throws Exception {
    	//In UDP communications data is encapsulated in a Datagram packet and to transmit packets, DatagramSockets are used. /Opening a socket in order to reception the packets on port 9091, satisfying the "The port number is the port number the server must listen on" criteria.
    	DatagramSocket socket = new DatagramSocket(9091);
    	try {
            
    		//To send and receive (from the Client) information, two byte arrays are created to store data. The aim is to use as less memory space as possible. The code is highly customized for the Umbrella use case.
            byte[] receiveData = new byte[90];//Storage of inbound bytes. It is set to 90 as the DataPacket object has a size of 89 bytes and  1 byte for data payload, which is appropriate in this specific scenario. 
            byte[] sendDataPacket = new byte[90];//Storage of outbound bytes (for "ACK"). It is set to 90 as the DataPacket object has a size of 89 bytes and  1 byte for data payload, which is appropriate in this specific scenario.
            
            // Declaration of a boolean variable that serves to set the end of the loop at the end of the message.
            boolean endOfcycle= false;
            
            // Declaration of a variable that will be increased each time the packet is successfully sent. It is to able to compare received and expected sequence numbers. 
            int expectedSN = 0;
            
             //The server waits for the input information until it is disconnected
             
            System.out.println("*********************Server is waiting************************");
            
            
            while (!endOfcycle) {
            	
            	//Opening a socket in order to reception the packet containing the sequence number and the data payload.
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                
                //The received encapsulated packet is converted from bytes to an object by instantiating and casting the DataPacket class.
                DataPacket packetObject = (DataPacket) DataPacket.toObject(receivePacket.getData());
                
                //The data payload is converted into a string in order to be able to display the received data payload. 
                String sentence = new String(packetObject.getData());
                
                //Saving the receives sequence number into a variable in order to be able to compare seq numbers and acknowledge packets later on.
                int receivedsqNum = packetObject.getSeq();
                
                //Declaring a return data payload, which functions as a dummy data as we only need to send back the ACK number. Another solution for this could be to create another class that only takes and stores one input parameter (e.g. sequence number).
                String ack = "Ack";
                
                //Converting a string into bytes
                byte[] fileBytes = ack.getBytes();
                
                // Checking if the received number is the last expected number. This is highly streamlined to the "umbrella" use case.
                if (receivedsqNum==7 ){
                	expectedSN++;
                	
                	//In case the received packet is the last one it saves the data in an ArrayList within the DataPacket object, and ends the cycle.
                	DataPacket.serverDataPacketList.add(packetObject);
                	System.out.println("Received payload data: " + sentence);
                	endOfcycle=true;
                	//If the expected sequence number is equal to the received sequence number, the data is stored in an ArrayList within the DataPacket object and increases the expected sequence number value by one.
                }else if (expectedSN ==receivedsqNum) {
                	expectedSN++;
                	DataPacket.serverDataPacketList.add(packetObject);
                	System.out.println("Received payload data: " + sentence);
                }
               
                //Creates an instance of a DataPacket class where the dummy data and ACKed sequence number is given as input parameter.
                DataPacket sendDataPacketObject=new DataPacket(fileBytes,receivedsqNum);
    			
                //The above object is fed into the function that converts the object containing the data pay load and the sequence number into a byte array and saves it into the SendDataPacket variable.
            	sendDataPacket = DataPacket.toBytes(sendDataPacketObject);
                /*
                 * Generates a random number and simulates dropped packets. Added function, not in the requirements.
                 */            	
                Random random = new Random( );
                int chance = random.nextInt( 100 );
                
                // Declaration of a clientIpAddress and clientPort variable.
                InetAddress clientIpAddress = receivePacket.getAddress(); //strips client IP address from received UPD header
                int clientPort = receivePacket.getPort();//strips client port number from received UPD header
                
                //Setting a communication session according to the requirements, by creating an instance of the DatagramPacket class and sending the packet to the destination IP address and destination port, using arguments. 
                DatagramPacket sendPacket = new DatagramPacket(sendDataPacket, sendDataPacket.length, clientIpAddress, clientPort);
                
                 // The transmission will fail if we got an even number from the random number generator (50% fail or not fail). If it does not fail, the packet will be sent.                 
                if( ((chance % 2) == 0) ) {
                    
                    socket.send(sendPacket);
                   
                // If the last sequence number is lost, it permits to send again the ACKed sequence number and quits the loop. It is to avoid infinite loops.   
                } else {
                	if (receivedsqNum==7 ) {
                		socket.send(sendPacket);
                		endOfcycle=true;
                //In case the ACK packet is lost and the lost ACK is resent by repeating the loop.
                	}else {
                    System.out.println( "Packet is lost with sequence number: "+ (receivedsqNum+1)+" (Retransmission of packet)" );}
                }
            }
            System.out.println("**********************Transmitted data****************************");
            
            //Reading the received data payload in a string from the ArrayList located in the DataPacket class.
            for(DataPacket packet : DataPacket.serverDataPacketList){
    			for(byte bte: packet.getData()){
    				System.out.print((char) bte);
    			}
            }
             //We catch all exception in the catch case
             
        } catch (Exception e) {
            System.out.println(e.toString());

    }
        System.out.println("\n*********************End of transmission**************************");
        //Closing communication session as per requirement.
        socket.close();
    }}
