package kah.json;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * An activity that parses the sample Json data from the Bureau of Meteorology (http://www.bom.gov.au) and displays it in a table on the screen.
 * 
 * @author Kah
 */
public class JsonSampleActivity extends Activity {

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStart() {
		super.onStart();

		// Downloading the RSS feed needs to be done on a separate thread.
		Thread downloadThread = new Thread(new Runnable() {

			public void run() {
				Debug.startMethodTracing("JsonObject");
				try {
					loadData();
				} catch (Exception e) {
					Log.e("Content Retriever", e.getLocalizedMessage(), e);
				} finally {
					Debug.stopMethodTracing();
				}
			}
		}, "Reading Thread");

		downloadThread.start();
	}
	
	/**
	 * Loads the sample JSON data into a table.
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	private void loadData() throws JSONException, IOException {
		populateTable(getContent());
	}

	/**
	 * Reads the data into a {@link JSONObject}.
	 * 
	 * @return the data as a {@link JSONObject}
	 * @throws IOException
	 * @throws JSONException
	 */
	private JSONObject getContent() throws IOException, JSONException {
		BufferedReader bufferedReader = null;
		try {
			InputStream inStream = getResources().openRawResource(R.raw.json);

			BufferedInputStream bufferedStream = new BufferedInputStream(
					inStream);
			InputStreamReader reader = new InputStreamReader(bufferedStream);
			bufferedReader = new BufferedReader(reader);
			StringBuilder builder = new StringBuilder();
			String line = bufferedReader.readLine();
			while (line != null) {
				builder.append(line);
				line = bufferedReader.readLine();
			}

			return new JSONObject(builder.toString());
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
	}

	/**
	 * Populates the table in the main view with data.
	 * 
	 * @param data
	 * 			the read JSON data
	 * @throws JSONException
	 */
	private void populateTable(JSONObject data) throws JSONException {
		JSONArray dataArray = data.getJSONObject("observations").getJSONArray(
				"data");
		final TableLayout table = (TableLayout) findViewById(R.id.table);
		for (int i = 0; i < dataArray.length(); i++) {
			final View row = createRow(dataArray.getJSONObject(i));
			table.post(new Runnable() {

				public void run() {
					table.addView(row);
				}
			});
		}
	}

	/**
	 * Creates a row for the table based on an observation. 
	 * 
	 * @param item
	 * 			the JSON object containing the observation
	 * @return the created row
	 * @throws JSONException
	 */
	private View createRow(JSONObject item) throws JSONException {
		View row = getLayoutInflater().inflate(R.layout.rows, null);
		((TextView) row.findViewById(R.id.localTime)).setText(item
				.getString("local_date_time_full"));
		((TextView) row.findViewById(R.id.apprentTemp)).setText(item
				.getString("apparent_t"));
		((TextView) row.findViewById(R.id.windSpeed)).setText(item
				.getString("wind_spd_kmh"));
		return row;
	}
}