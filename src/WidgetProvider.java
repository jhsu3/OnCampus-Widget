package edu.ncsu.oncampus;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import android.widget.TextView;
import android.widget.Toast;
import edu.ncsu.oncampus.model.StoryObject;
import edu.ncsu.oncampus.model.WeatherObject;
import edu.ncsu.oncampus.util.MobileApi;

public class WidgetProvider extends AppWidgetProvider {

	  RemoteViews remoteViews;

	  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
	  {
	    // Get all ids
	    ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {
			
	    	remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
	    	AsyncTask<Void, Void, WeatherObject[]> downloadWeatherTask = new DownloadWeatherTask(appWidgetManager, widgetId, remoteViews, context).execute();
	    	
	    	//Intent for ListView? Use RemoteAdapter because RemoteViews
	    	Intent storyIntent = new Intent(context, WidgetService.class);
	    	storyIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
	        storyIntent.setData(Uri.parse(storyIntent.toUri(Intent.URI_INTENT_SCHEME)));
	    	remoteViews.setRemoteAdapter(R.id.storiesListView, storyIntent);
	    	appWidgetManager.notifyAppWidgetViewDataChanged(allWidgetIds, R.id.storiesListView);
	    	
	    	// Register an onClickListener
	        Intent update = new Intent(context, WidgetProvider.class);

	        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

	        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, update, PendingIntent.FLAG_UPDATE_CURRENT);
	        remoteViews.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent);
	    	
	    	//Intent for story ListView click
	    	//Link to HomeStoryActivity
	    	Intent startStoryIntent = new Intent(context, HomeStoryActivity.class);
            PendingIntent startStoryPendingIntent = PendingIntent.getActivity(context, 0, startStoryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.storiesListView, startStoryPendingIntent);
            
            //Intent for weather RelativeLayout - icon
            //Link to WeatherActivity
            Intent weatherIntent = new Intent(context, WeatherActivity.class);
            PendingIntent startWeatherPendingIntent = PendingIntent.getActivity(context, 0, weatherIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.weatherIcon, startWeatherPendingIntent);
            
            //Intent for dining ImageButton
            //Link to DiningActivity
            Intent diningIntent = new Intent(context, DiningActivity.class);
            PendingIntent startDiningPendingIntent = PendingIntent.getActivity(context, 0, diningIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetDiningButton, startDiningPendingIntent);
            
            //Intent for maps ImageButton
            //Link to CampusMapActivity
            Intent mapsIntent = new Intent(context, CampusMapActivity.class);
            PendingIntent startMapsPendingIntent = PendingIntent.getActivity(context, 0, mapsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetMapButton, startMapsPendingIntent);
            
            //Intent for wolfline ImageButton
            //Link to WolflineActivity
            Intent wolflineIntent = new Intent(context, WolflineActivity.class);
            PendingIntent startWolflinePendingIntent = PendingIntent.getActivity(context, 0, wolflineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetWolflineButton, startWolflinePendingIntent);
            
            //Intent for directory ImageButton
            //Link to DirectoryActivity
            Intent directoryIntent = new Intent(context, DirectoryActivity.class);
            PendingIntent startDirectoryPendingIntent = PendingIntent.getActivity(context, 0, directoryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetDirectoryButton, startDirectoryPendingIntent);
            
            //Refresh remoteView - Use after updating RemoteView
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
	    	}
	  }
	  
	  public void onReceive(Context context, Intent intent) 
	  {
		    if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) 
		    {
		    	CharSequence text = "Updating widget...";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				super.onReceive(context, intent);
		    }
		    
		    else 
		    {
		        super.onReceive(context, intent);
		    }
	  }

	  //----------------------------------Weather--------------------------------------
	  private class DownloadWeatherTask extends AsyncTask<Void, Void, WeatherObject[]>
	  {
		  private AppWidgetManager appWidgetManager;
		  private RemoteViews views;
		  private int widgetId;
		  private Context context;
		  
		  public DownloadWeatherTask(AppWidgetManager appWidgetManager, int widgetId, RemoteViews views, Context context)
		  {
			  this.appWidgetManager = appWidgetManager;
			  this.views = views;
			  this.widgetId = widgetId;
			  this.context = context;
		  }
		@Override
		protected WeatherObject[] doInBackground(Void... arg0) 
		{
			WeatherObject[] weatherObject;
			try
			{
				weatherObject = MobileApi.getWeather();
			}
			catch(JSONException e)
			{
				Log.d("DownloadWeather doInBackground", e.getMessage());
				weatherObject=null;
			}
			return weatherObject;
		}
		
		protected void onPostExecute(WeatherObject[] result)
		{
			if(isCancelled()) return;
			if(result==null)
			{
				Log.d("DownloadWeatherTask", "Download weather failed");
				CharSequence text = "Download failed. Check your internet connection";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			
			else
			{
				WeatherObject weatherObject = result[0];
				new DownloadWeatherIconTask(appWidgetManager, widgetId, views).execute(weatherObject.icon);
				//Log.d("onPostExecute", weatherObject.toString());
				
				String currentTempDisplay = weatherObject.currentTemp + "\u2109";
				//String apparentTempDisplay = weatherObject.apparentTemp + "\u2109";
				String minTempDisplay = weatherObject.minTemp + "\u2109";
				String maxTempDisplay = weatherObject.maxTemp +"\u2109";
				
				views.setTextViewText(R.id.weatherCurrentTemp, currentTempDisplay);
				//views.setTextViewText(R.id.weatherApparentTemp, apparentTempDisplay);
				views.setTextViewText(R.id.weatherMinTemp, minTempDisplay);
				views.setTextViewText(R.id.weatherMaxTemp, maxTempDisplay);
				
				//Call this to update RemoteView
				appWidgetManager.updateAppWidget(widgetId, views);
			}
		}
	  }
	  
	  public class DownloadWeatherIconTask extends AsyncTask<String, Void, Bitmap> {
		  private AppWidgetManager appWidgetManager;
		  private RemoteViews views;
		  private int widgetId;

		    public DownloadWeatherIconTask(AppWidgetManager appWidgetManager, int widgetId, RemoteViews views){
		    	this.appWidgetManager = appWidgetManager;
				this.views = views;
				this.widgetId = widgetId;
		    }
		    
		    @Override
			protected Bitmap doInBackground(String... params) {
		    	 try {
		    		 //Log.d("DownloadWeatherIconTask", params[0]);
		             InputStream in = new java.net.URL(params[0]).openStream();
		             Bitmap bitmap = BitmapFactory.decodeStream(in);
		             return bitmap;
		             //NOTE:  it is not thread-safe to set the ImageView from inside this method.  It must be done in onPostExecute()
		         } catch (Exception e) {
		             Log.e("ImageDownload", "Download failed: " + e.getMessage());
		         }
		         return null;
			}
		    
		    public void onPostExecute(Bitmap bitmap){
		    	//Log.d("DownloadWeatherIconTask", "onPostExecute");
		        views.setImageViewBitmap(R.id.weatherIcon, bitmap);
		        
		        //Call this to update RemoteView
		        appWidgetManager.updateAppWidget(widgetId, views);
		    }
		}
}