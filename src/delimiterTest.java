import java.util.*;
import java.util.regex.Pattern;
public class delimiterTest {

	public static void main(String [] args)
	{
		Scanner scan = new Scanner("2321321312<r>123 432 4324 43243 423423 3242</r><kahu>4324.3432 43 43 4323 23 23 </kahu><r");
		scan.useDelimiter(Pattern.compile("<[a-z]+>"));
		while(scan.hasNext())
		{
			String tag = scan.findInLine(Pattern.compile("<[a-z]+>"));
			String endTag = "</" + tag.substring(1, tag.length());
			System.out.print(tag  + " " + endTag);
			String line = scan.next();
			if(scan.hasNext() == false)
			{
				line = line.split(endTag)[0] + endTag;
			}
			line = tag + line;
			System.out.println("Full Line: " + line);
		}
		
		
	}
	
}
