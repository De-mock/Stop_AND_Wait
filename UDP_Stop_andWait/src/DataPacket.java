import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


 // DataPacket is the object, which used to identify the packet, store data, sequence numbers and check if the ACKs are received or not. It is also used to convert object to and from bytes.
 
public class DataPacket implements Serializable {
    private byte[] data;
    private int sequenceNumber;
    //Stores the received data from the Client.
    public static ArrayList<DataPacket> serverDataPacketList=new ArrayList<>();
    
    //Creating the object containing the data in bytes and the sequence number in the form of integer.
    public DataPacket(byte[] data, int sequenceNumber) {
        this.data = data;
        this.sequenceNumber = sequenceNumber;
     
    //Method that allows to get back packet data from an object in the form of byte array.    
    }
    public byte[] getData() {
		return data;
		
	//Method that allows the get back the sequence number of a packet object. 	
	}
    public int getSeq() {
		return sequenceNumber;
	}
    // Serializable class is implemented because the objects are need to be converted to a byte stream so that the byte stream can be reverted back into a copy of the object. 
    //Object to byte array converter
    public static byte[] toBytes(Object obj) throws IOException {
		ByteArrayOutputStream bte = new ByteArrayOutputStream();
		ObjectOutputStream objct = new ObjectOutputStream(bte);
		objct.writeObject(obj);
		return bte.toByteArray();
	}
	//Converter function, which converts byte arrays into objects
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bte = new ByteArrayInputStream(bytes);
		ObjectInputStream objct = new ObjectInputStream(bte);
		return objct.readObject();
	}

}
