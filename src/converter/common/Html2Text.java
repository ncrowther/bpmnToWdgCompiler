package converter.common;

import java.io.*;  
import javax.swing.text.html.*;  
import javax.swing.text.html.parser.*;  
  
public class Html2Text extends HTMLEditorKit.ParserCallback {  
 StringBuffer s = new StringBuffer();
  
 public Html2Text() {}  
  
 public void parse(Reader in) throws IOException {   
   ParserDelegator delegator = new ParserDelegator();  
   // the third parameter is TRUE to ignore charset directive  
   delegator.parse(in, this, Boolean.TRUE);  
 }  
  
 public void handleText(char[] text, int pos) {  
   s.append(text);  
 }  
  
 public String getText() {  
   return s.toString();  
 }  
  
 public static void main (String[] args) {  
   try {  
     // the HTML to convert  
     // FileReader in = new FileReader("java-new.html");  
	 StringReader in = new StringReader("<html>logMessage --message SendEmail --type &amp;#34;Info&amp;#34;&lt;br /&gt;    &lt;br /&gt;  </html>");
     Html2Text parser = new Html2Text();  
     parser.parse(in);  
     in.close();  
     System.out.println(parser.getText());  
   }  
   catch (Exception e) {  
     e.printStackTrace();  
   }  
 }  
}  