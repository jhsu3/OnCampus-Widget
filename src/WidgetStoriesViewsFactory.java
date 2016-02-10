package edu.ncsu.oncampus;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.json.JSONException;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import edu.ncsu.oncampus.WidgetProvider.DownloadWeatherIconTask;
import edu.ncsu.oncampus.model.StoryObject;
import edu.ncsu.oncampus.util.MobileApi;

//Equivalent to adapter
//For stories ListView
public class WidgetStoriesViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	private StoryObject[] stories;
	private Bitmap[] storiesThumbnails;
	private Context context = null;
	private String errorMessage;
	
	public WidgetStoriesViewsFactory(Context context, Intent intent)
	{
		this.context = context;
		//Log.d("StoriesListProvider", "Constructor");
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(stories!=null)
			return stories.length;
		else return 0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		//StoriesListView gets populated here
		//Similar to adapter
		RemoteViews row=new RemoteViews(context.getPackageName(), R.layout.widget_stories_listrow);

		row.setTextViewText(R.id.widgetStoriesTitle, stories[position].title);
		if(storiesThumbnails!=null)
		{
			//Log.d("getViewAt", "set imageview");
			row.setImageViewBitmap(R.id.widgetStoriesThumbnail, storiesThumbnails[position]);
		}
		
		//Alternate background colors
		if(position%2 == 1)
		{
			int color = R.color.ncsu_gray;
			row.setInt(R.id.widgetListrowRelativeLayout, "setBackgroundResource", color);
			//row.setInt(R.id.widgetStoriesTitle, "setBackgroundColor", R.color.ncsu_red);
		}
		else
		{
			int color = R.color.white;
			row.setInt(R.id.widgetListrowRelativeLayout, "setBackgroundResource", color);
		}
		
		//Set click intent here
		Intent fillInIntent = new Intent();
		fillInIntent.putExtra("title", stories[position].title);
		fillInIntent.putExtra("content", stories[position].content);
		fillInIntent.putExtra("author", stories[position].author);
		fillInIntent.putExtra("pubDate", stories[position].pubDate);
		fillInIntent.putExtra("thumbnail", stories[position].thumbnail);
		fillInIntent.putExtra("contentImage", stories[position].contentImage);
		fillInIntent.putExtra("url", stories[position].url);
        row.setOnClickFillInIntent(R.id.widgetListrowRelativeLayout, fillInIntent);
		
		//Intent i=new Intent();
		//Bundle extras=new Bundle();
		
		//extras.putString(WidgetProvider.EXTRA_WORD, stories[position]);
		//i.putExtras(extras);
		//row.setOnClickFillInIntent(android.R.id.text1, i);
		
		return(row);
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCreate() {
		//Log.d("onCreate", "ViewFactory");
	}

	@Override
	public void onDataSetChanged() {
		//Download featured stories and icons here
		//Don't need async
		//See documentation for how ViewsFactory works
		try {
			stories = MobileApi.getFeatured();
			//new DownloadStoriesThumbnailTask(stories).execute();
			storiesThumbnails = new Bitmap[stories.length];
			for(int i=0; i<stories.length; i++)
			{
				String thumbURL = stories[i].thumbnail;
				InputStream in;
				try {
					in = new java.net.URL(thumbURL).openStream();
					Bitmap bitmap = BitmapFactory.decodeStream(in);
					storiesThumbnails[i] = bitmap;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			errorMessage = e.getMessage();
			//Log.d("ViewsFactory onDataSetChanged", errorMessage);
			//stories = null;	//Display old information if no connection
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
}