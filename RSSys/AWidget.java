package RSSys;


public abstract class AWidget {
	
		private IResourceInterface resourceInterface;
		
		public AWidget(WidgetManager registerTo)
		{
			registerTo.registerWidget(this);
		}

		public abstract InteractionReq getInteractionReq();

		void SetResourceInterface(IResourceInterface toSet)
		{
			resourceInterface = toSet;
		}
		
}