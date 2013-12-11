package carbon.assassin.multipool;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class DataPuller {
	
	private LinkedHashMap<String, String> hashRates;
	private LinkedHashMap<String, String> balence;
	private String poolCurrentlyMining;
	private final String currentlyMiningRegex = "mining.*?,";
	private Pattern currentlyMined;
	private final String multiApi = "https://www.multipool.us/api2.php";
	private final String keyConnecter = "?api_key=";
	private String myApiKey = "a38dde781307c3c3f5be0b9c13f2adb2a80ccd8538fc989aafd0071d3963cb6f";
	Map <String,String> cookies;
	Document apiPage;
	public boolean isStarted = false;
	Runnable update, getCookies;
	ConnectivityManager cM;
	boolean isConnected;
	private final String GROUP_REGEX = "\\&quot;(\\w\\w\\w)\\&quot;:\\{.*?(\\},)";
	public DataPuller(ConnectivityManager c) 
	{
		currentlyMined = Pattern.compile(currentlyMiningRegex);
		cM = c;
		hashRates = new LinkedHashMap<String, String>();
		balence = new LinkedHashMap<String, String>();
	}
	public void start() throws IOException
	{
		update = new Runnable()
		{
		    @Override
		    public void run()
		    {
		    	try {
					apiPage = Jsoup.connect(multiApi+keyConnecter+myApiKey).timeout(0)
							.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36").post();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		};
		getCookies = new Runnable()
		{
		    @Override
		    public void run()
		    {
		    	try {
		    		Connection.Response res = Jsoup.connect("https://www.multipool.us").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36")
		                    .execute();
		    		cookies = res.cookies();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	isStarted = true;
		    }
		};
		if(checkNetworkStatus())
		{
		refreshCookies();
		update();
		while(apiPage == null)
		{
			
		}
		}
	}
	
	public void update() throws IOException
	{
		if(isConnected)
		{
			Thread t = new Thread(update);
			t.start();
			while(t.isAlive()){}
		}
	}
	public String getCurrentlyMined()
	{
		Matcher temp = currentlyMined.matcher(apiPage.html());
		if(temp.find())
		{
		String tempMine = temp.group().replace("mining&quot;:&quot;", "");
		tempMine =tempMine.replace("&quot;,", "");
		this.poolCurrentlyMining = tempMine.toUpperCase();
		}
		return this.poolCurrentlyMining;
	}
	public boolean checkNetworkStatus()
	{
		NetworkInfo activeNetwork = cM.getActiveNetworkInfo();
		isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}
	public void refreshCookies()
	{
		if(isConnected)
		{
			Thread t = new Thread(getCookies);
			t.start();
		}
	}
	public LinkedHashMap<String, String> pullListofCurrentHashrates()
	{
		Pattern currency = Pattern.compile("\\{\\&quot;currency\\&quot;.*?(\\}\\})");
		//System.out.println(apiPage.html());
		Matcher temp = currency.matcher(this.apiPage.html());
		temp.find();
		String currencySection = temp.group();
		Pattern hash = Pattern.compile(GROUP_REGEX);
		Matcher tempMatch = hash.matcher(currencySection);
		//Pattern isHashing = Pattern.compile("hashrate.*?((^0\\d*?)(\\&quot;))");
		Pattern isHashing = Pattern.compile("\\&quot;hashrate\\D*?(([1-9]+?)\\&quot;,)");
		Matcher loopMatcher;
		if(hashRates.size() > 0)
		{
			hashRates.clear();	
		}
		while(tempMatch.find())
		{
			loopMatcher = isHashing.matcher(tempMatch.group());
			if(loopMatcher.find())
			{

				String id = tempMatch.group(1);
				String rate = loopMatcher.group(2);
				String[] finalValues = this.processHashRates(id, rate);
				//System.out.println(rate +"  " + id);
				hashRates.put(finalValues[0], finalValues[1]);
				
			}
		}
		return this.hashRates;
	}
	public LinkedHashMap<String, String> pullCurrentBalence()
	{
		Pattern p = Pattern.compile(GROUP_REGEX);
		Matcher groupMatcher = p.matcher(this.apiPage.html());
		Pattern balenceP = Pattern.compile("\\&quot;confirmed_rewards\\D*?(([0-9.]+?)\\&quot;,)");
		Matcher balenceMatcher;
		if(this.balence.size() > 0)
		{
			this.balence.clear();
		}
		while(groupMatcher.find())
		{
			balenceMatcher = balenceP.matcher(groupMatcher.group());
			if(balenceMatcher.find())
			{
				if(balenceMatcher.group(2).equals("0"))
				{
					continue;
				}
				this.balence.put(groupMatcher.group(1), balenceMatcher.group(2));
			}
		}
		return this.balence;
	}
	private String[] processHashRates(String id, String rate)
	{
		id = id.toUpperCase();
		if(rate.length() > 3)
		{
			rate = (Integer.parseInt(rate)/1000) +" " + "MH/s";
		}
		else
		{
			rate = rate + " " +"KH/s";
		}
		return new String[] {id, rate};
	}

}
