import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import cox5529.generator.SimpleCompositions;
import cox5529.midi.MIDIFile;

/**
 * Test code.
 * 
 * @author Brandon Cox
 * 
 */
public class Main {
	
	/**
	 * Test code.
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) { // Melodic analysis-type stuff should be done
		Scanner s = new Scanner(System.in);
		System.out.println("Value for x? (Integer between 1 and 10)");
		int x = s.nextInt();
		System.out.println("Value for y? (Integer between 1 and 10)");
		int y = s.nextInt();
		System.out.println("Duration of output in Measures?");
		int duration = s.nextInt();
		System.out.println("MIDI source file?");
		s.nextLine();
		try {
			MIDIFile source = MIDIFile.read(new File(s.nextLine()), true);
			SimpleCompositions.fullCompose(duration, x, y, source).write(new File("out.mid"), true);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
