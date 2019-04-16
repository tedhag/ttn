package se.progatec.ttn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import se.progatec.db.DbConnection;

public class SaveTtnDataCommand {

	private static Logger logger = Logger.getLogger(SaveTtnDataCommand.class.getName());
	
	private final JSONObject json;
	private final String device;
	private final Instant timestamp;
	private final CompletableFuture<String> cf = new CompletableFuture<>();

	public SaveTtnDataCommand(final JSONObject json){
		this.json=json;
		this.device=json.getString("device_id");
		this.timestamp=LocalDateTime.parse(json.getString("time").split("\\.")[0]).atZone(ZoneId.of("Europe/Stockholm")).toInstant();
	}
	
	public void execute() {
		
		//Start to check if the record is stored...
		Optional.ofNullable(checklog())
		.ifPresentOrElse(c->{
			//if not stored we store it
			Optional.ofNullable(insertlog())
			.ifPresentOrElse(pk->{
				//if we get a primary key we save all non null parameters...
				json.keySet().stream()
				.filter(k->!k.equals("device_id") && !k.equals("time"))
				.forEach(k->{
					String v = JSONObject.valueToString(json.opt(k));
					Optional.ofNullable(v.equals("null")?null:v)
					.ifPresent(p->{
						insertparam(pk, k, v);
					});
				});
			}, ()->{
				logger.log(Level.WARNING,device+"@"+timestamp.toString()+" could not be stored correctly");
			});
			
		}, ()->{
			logger.log(Level.INFO,device+"@"+timestamp.toString()+" already stored");
		});
		
		
	}
	
	private final static String checklog = "select count(l.log_id) from log l where l.device=? and l.timestamp=?;";
	private Integer checklog() {
		try(Connection conn = DbConnection.instance().connection();
			PreparedStatement ps = conn.prepareStatement(checklog)){
			ps.setString(1, device);
			ps.setTimestamp(2, new Timestamp(timestamp.toEpochMilli()));
			
			ResultSet rs = ps.executeQuery();
			rs.next();
			return (rs.getInt(1)>0)?null:0;
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Could not check data in log", e);
			return null;
		}
	}
	
	private final static String insertlog = "insert into log (timestamp, device) values(?,?);";
	private Integer insertlog() {
		try(Connection conn = DbConnection.instance().connection();
			PreparedStatement ps = conn.prepareStatement(insertlog, PreparedStatement.RETURN_GENERATED_KEYS);){
			ps.setTimestamp(1, new Timestamp(timestamp.toEpochMilli()));
			ps.setString(2, json.getString("device_id"));
			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			return (rs.next())?rs.getInt(1): null;
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Could not insert data to log", e);
			return null;
		}
	}

	private final static String insertparameter = "insert into parameter (log_id,name,value) values (?,?,?)";
	private void insertparam(Integer pk, String k, String v) {
		try(Connection conn = DbConnection.instance().connection();
			PreparedStatement psparam = conn.prepareStatement(insertparameter)){
			psparam.setInt(1, pk);
			psparam.setString(2, k);
			psparam.setString(3, v);
			psparam.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Could not insert "+k+" to parameter with value "+v, e);
		}
		
	}
	
	public static void main(String[] args) {
		new SaveTtnDataCommand(null).execute();
	}

	
}
