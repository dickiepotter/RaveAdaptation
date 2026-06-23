package RSSys;

import java.util.ArrayList;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

public class Resource {

		public Resource(String id)
		{
			this.id = id;
			retrieveDescription();
		}
		
		/* Replace object with some kind of RDF object */
		//private Object description;
		
		private void retrieveDescription()
		{
			OntModel m = ModelFactory.createOntologyModel();
			OntDocumentManager dm = m.getDocumentManager();
			dm.setMetadataSearchPath("file:res/etc/ont-policy.rdf", false);
			dm.addAltEntry( "http://test.com/owl/ontologies/resource/",
			                "file:./res/Resource.owl"    );
			m.read( "http://test.com/owl/ontologies/resource/" );
			
			/* DEBUG
			System.out.println("Jena policy URL = " + dm.getLoadedPolicyURL());
			System.out.println("Jena metadata serarch path = " + dm.getMetadataSearchPath());
			System.out.println(m.toString());
			*/
			
			/* set description from ontology */
		
		}
		
		private String id;
		
		String getId() {
			return id;
		}
		
		static Resource[] getAvailableResources()
		{
			ArrayList resources = new ArrayList();
			/* todo */
			return (Resource[])resources.toArray();
		}

}
