package se.progatec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.IntStream;

import org.json.JSONArray;

import se.progatec.ttn.GetDataFromTtnCommand;
import se.progatec.ttn.SaveTtnDataCommand;

public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class.getName());
	private ExecutorService es = Executors.newFixedThreadPool(3);//allocated CPU's on this machine
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	
	public void execute() {
		logger.log(Level.INFO, "*** Start application ***");
		
		ses.scheduleWithFixedDelay(()->{
			
			logger.log(Level.INFO, "---Start new fetch...");
			
			JSONArray data = new GetDataFromTtnCommand().execute();
			
			IntStream.range(0, data.length()).mapToObj(i->Integer.valueOf(i))
			.map(i->data.getJSONObject(i))
//			.limit(1)
//			.peek(jsonobject->System.out.println(jsonobject.toString()))
			.map(jsonobject->{
				return es.submit(()->{new SaveTtnDataCommand(jsonobject).execute();});
			})
			.parallel()
			.forEach(f->{
				try {f.get();} catch (Exception e) {}
			});
			
			
		}, 5, 60, TimeUnit.SECONDS);
		
	}
	
	public static void init() {
		Logger l = Logger.getLogger("");
		
		try {
			FileHandler fh = new FileHandler("/Users/edhag/sandbox/ttn/log/ttn.%g.log", 1024 * 1024, 10, true);
			fh.setFormatter(new SimpleFormatter());
			l.addHandler(fh);
			l.setLevel(Level.CONFIG);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Could not init application",e);
		}
	}

	public static void main(String[] args) {
		Main.init();
		new Main().execute();
	}

}
