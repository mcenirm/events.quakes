package ed3.events.quakes;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import ed3.events.cap.Alert;
import ed3.events.cap.CAPConsumer;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
        ResteasyWebTarget target = client.target("http://example.com/base/uri");
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
            String link = alert.getInfo().getWeb();
            int lastIndexOf = link.lastIndexOf('/');
            String substring = link.substring(lastIndexOf + 1);
            File alertFile = new File(substring + ".xml");
            //Status status = consumer.newAlert(alert);
            System.out.println(alertFile);
            javax.xml.bind.JAXB.marshal(alert, alertFile);
        }
    }
}
