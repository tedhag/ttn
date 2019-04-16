package se.progatec.ttn;

import org.json.JSONArray;

import se.progatec.http.HttpRequest;

public class GetDataFromTtnCommand {

	public JSONArray execute() {
		
		String data = HttpRequest
		.get("https://kfa_data.data.thethingsnetwork.org/api/v2/query?last=1h")
		.trustAllHosts()
		.acceptJson()
		.header("Authorization", "key <key>")
		.body();
		
//		System.out.println(new JSONArray(data).toString());
		
		return new JSONArray(data);

	}
	
	public static void main(String[] args) {
		new GetDataFromTtnCommand().execute();
	}
}
