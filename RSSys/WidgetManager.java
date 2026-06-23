package RSSys;

import java.util.ArrayList;

public class WidgetManager {
	
	//***************SINGLETON PATTERN**************************
	  private WidgetManager()
	  {
	  	widgets = new ArrayList();
	  }

	  public static WidgetManager getWidgetManager()
	  {
	    if (ref == null)
	        ref = new WidgetManager();		
	    return ref;
	  }

	  public Object clone() throws CloneNotSupportedException
	  { throw new CloneNotSupportedException(); }

	  private static WidgetManager ref;
   //***********************************************************
	  
	private ArrayList widgets;
	
	protected void registerWidget(AWidget toAdd) 
	{
		widgets.add(toAdd);
	}

	public void provideHCIResources()
	{
	}

}
