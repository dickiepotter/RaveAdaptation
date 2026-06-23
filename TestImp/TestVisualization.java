package TestImp;

import RSSys.*;

public class TestVisualization {

	private TestWidgetA testWidgetA;
	private TestWidgetB testWidgetB1;
	private TestWidgetB testWidgetB2;
	private WidgetManager manager;

	private void Init()
	{
		WidgetManager manager = WidgetManager.getWidgetManager();
		testWidgetA = new TestWidgetA(manager);
		testWidgetB1 = new TestWidgetB(manager);
		testWidgetB2 = new TestWidgetB(manager);
		manager.provideHCIResources();
		System.out.println("Visualization has been initialised");
		/* manager nolonger needed */
		manager = null;
	}
	
	public static void main(String args[])
	{
		TestVisualization testVis = new TestVisualization();
		testVis.Init();
	}
}
