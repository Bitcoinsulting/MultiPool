package carbon.assassin.multipool;

import java.io.IOException;

import android.os.AsyncTask;
import android.view.View;

public class UpdateTask extends AsyncTask<String, Void, String> {
	View main;
	public UpdateTask(View v)
	{
		super();
		main = v;
	}
	@Override
	protected String doInBackground(String... params) {
		System.out.println("update called");
		try {
			Main.dataPuller.update();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "failed";
		}
		return "updated";
	}
}