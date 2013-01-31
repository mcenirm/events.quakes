package ed3.events.quakes;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import ed3.events.cap.Alert;
import ed3.events.cap.CAPConsumer;
import static ed3.events.cap.SAMECodes.EARTHQUAKE_WARNING;
import static ed3.events.cap.SAMECodes.SAME;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.rometools.fetcher.FeedFetcher;
import org.rometools.fetcher.FetcherException;
import org.rometools.fetcher.impl.DiskFeedInfoCache;
import org.rometools.fetcher.impl.HttpURLFeedFetcher;

public class App {

    public static void main(String[] args) throws MalformedURLException, IllegalArgumentException, IOException, FetcherException, FeedException {
        File cacheDir = new File(".", "feedcache");
        if (!cacheDir.isDirectory() && !cacheDir.mkdir()) {
            throw new IOException("could not create cache directory");
        }
        String cachePath = cacheDir.getPath();
        DiskFeedInfoCache feedInfoCache = new DiskFeedInfoCache(cachePath);
        FeedFetcher fetcher = new HttpURLFeedFetcher(feedInfoCache);
        SyndFeed feed = fetcher.retrieveFeed(new URL("http://earthquake.usgs.gov/earthquakes/feed/atom/1.0/hour"));
        System.out.println(feed);
        List<SyndEntryImpl> entries = feed.getEntries();
        ResteasyClient client = new ResteasyClient();
        ResteasyWebTarget target = client.target("http://ed3test.itsc.uah.edu/ed3/events/new.php");
        CAPConsumer consumer = target.proxy(CAPConsumer.class);
        AlertBuilder builder = new AlertBuilder();
        for (SyndEntryImpl entry : entries) {
            Alert alert = builder.build(entry);
            alert.setSender("USGS");
            alert.setStatus("Actual");
            alert.setMsgType("Alert");
            alert.setScope("Public");
            alert.setInfoCategory("Geo");
            alert.setInfoEvent("Earthquake");
            alert.setInfoUrgency("Past");
            alert.setInfoSeverity("Minor");
            alert.setInfoCertainty("Observed");
            alert.setInfoEventCode(SAME, EARTHQUAKE_WARNING);
            String link = alert.getInfo().getWeb();
            int lastIndexOf = link.lastIndexOf('/');
            String substring = link.substring(lastIndexOf + 1);
            File alertFile = new File(substring + ".xml");
            System.out.println(alertFile);
            javax.xml.bind.JAXB.marshal(alert, alertFile);
            Response response = consumer.newAlert(alert);
            System.out.println(response.getStatusInfo());
            System.out.println(response.getEntity());
            response.close();
        }
    }
}
