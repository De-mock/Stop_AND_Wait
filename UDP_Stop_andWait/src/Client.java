import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Scanner;

/*
 * Client is the sender, who send information to the Server and get the ACKs
 */
public class Client {
    

    public static void main(String[] args) throws Exception {
        
         //In UDP communications data is encapsulated in a Datagram packet and to transmit packets DatagramSocket is used
         DatagramSocket clientSocket = new DatagramSocket();
        
        //Client socket will be closed after 2000ms in case of timeout. It is set to this particular value because it lets the user to follow the flow in real time.
         clientSocket.setSoTimeout(2000);
 
        
        //To send and receive (from the Server) information, two byte arrays are created to store data. The aim is to use as less memory space as possible. The code is highly customized for the Umbrella use case.          
        byte[] sendData = new byte[1]; //Storage of outbound bytes. It is set to 1 because we send only 1 letter as data at a time which is 1 byte, and it is not necessary to reserve more memory space for this task.
        byte[] receiveData = new byte[90]; //Storage of inbound bytes. It is set to 90 as the DataPacket object has a size of 89 bytes and  1 byte for the return data payload, which is appropriate in this specific scenario. 
        
        // Declaration of a variable for counting the packets sent 
        int lastSentSn = 0;
        
        // Declaration of a variable for the server address and port number
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int portNumber=9091;
        
        //The program was also tested in a LAN for the bellow IP address
        //String strIp = "192.168.1.20";
        //serverAddress = InetAddress.getByName(strIp);
        
        // Declaration of a boolean variable that serves to set the end of the loop at the end of the message.
        boolean timeOut = false;
        
        //Declaring the last packet number variable in order to determine the last expected packet number so the program can exit whenever it receives the ACK for the last packet from the server. It is set to 7 to comply with the "Umbrella" use case.
        int lastPackNum=7;
        
        //Reads the string from a txt file as it was formulated in the requirements:"The data in the file to be sent between the client/server should contain the string ‘umbrella’".
        
        FileInputStream fileInput = new FileInputStream("summative.txt");
        Scanner scansInput = new Scanner(fileInput).useDelimiter("\\A");
        String fileContent = scansInput.hasNext() ? scansInput.next() : "";
        
        //The file content is converted and saved into a byte array, as it is the required form for the DatagramPacket constructor.
        byte[] fileBytes = fileContent.getBytes();
        
        
        System.out.println("**********************Client is loading packets*****************************");


        while (!timeOut) {
            
        	//Copying each letter of a the converted string into to the above specified above byte array (SendData with 1 byte).
        	sendData = Arrays.copyOfRange(fileBytes, lastSentSn, lastSentSn+1);
        	
        	//Creates an instance of a DataPacket class where the data and the last sent packet is given as input parameter, thus satisfying the requirement: "In the packet, the byte payload will include both the original data and a sequence number for the packet (SN=x)." It also satisfies the criteria of: "The sender must read the line of data from the file and combine that text with the sequence number and send the packet to the receiver."      	
        	DataPacket sendDataPacketObject=new DataPacket(sendData,lastSentSn);
			
        	//The above object is fed into the function that converts the object containing the data pay load and the sequence number into a byte array and saves it into the SendDataPacket variable.
        	byte[] sendDataPacket = DataPacket.toBytes(sendDataPacketObject);
       	
            //Increasing the sequence number after each cycle
            lastSentSn++;
            
            try {
            	System.out.println("Sending packet with sn: " + (lastSentSn-1));
                //Setting a communication session according to the requirements, by creating an instance of the DatagramPacket class and sending the packet to the destination IP address and destination port, using arguments.         	
            	DatagramPacket sendPacket = new DatagramPacket(sendDataPacket, sendDataPacket.length, serverAddress, portNumber);
            	clientSocket.send(sendPacket);
            	
            	//Opening a socket in order to reception the acknowledgement. Given the structure of the program, namely that first sending the packet and then opening the same socket for receiving the ACKs satisfies the stop and wait with a window size 1 requirement.
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                
                //The received encapsulated packet is converted from bytes to an object by instantiating and casting the DataPacket class. 
                DataPacket packetObject = (DataPacket) DataPacket.toObject(receivePacket.getData());
                
                //The data payload is converted into a string in order to be able to display the received ACK number
                String sentence = new String(packetObject.getData());
                               
                System.out.println("Server says: " + sentence+" to "+packetObject.getSeq());
                
                // If we receive an ACK with the last sequence number equal to the last expected sequence number, stop the while loop.
                if(packetObject.getSeq() == lastPackNum) {
               	timeOut=true;
                }else {
                	timeOut = false;}
                
              //In case of timeout exception which occurs when the packet is lost, in this case if the ACK packet does not arrive in time, the packet is resent by repeating the loop and deducting one value from the sequence number, which is necessary if we want to resend the same packet.
            } catch (SocketTimeoutException e) {
            	
            	// If the exception occurs at the last packet one more iteration is needed so it displays the packet numbers synchronised and lets the server finish its cycle.
                if(lastSentSn==lastPackNum+1)
    				timeOut=true;
                // If there is no ACKed packet, the packet is resent
                else
                	System.out.println("Timeout (Sequence Number " +(lastSentSn-1) +")"+" Retransmission of packet");
               lastSentSn--;
            }
      }

        
        System.out.println("**********************Communication session closed*******************************");
        //Closing a communication session according to the requirements
		clientSocket.close();
    }

}

