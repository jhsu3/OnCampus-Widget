package edu.ncsu.oncampus;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {
	  @Override
	  public RemoteViewsFactory onGetViewFactory(Intent intent) {
		  return new WidgetStoriesViewsFactory(this.getApplicationContext(), intent);
	  }
}

