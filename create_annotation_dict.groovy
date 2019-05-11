@Grapes([
  @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='5.1.4'),
  @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='5.1.4'),
  @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='5.1.4'),
  @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='5.1.4'),
  @Grab(group='org.apache.commons', module='commons-rdf-api', version='0.5.0'),
  @GrabConfig(systemClassLoader=true)
])

import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.owlapi.apibinding.*
import org.semanticweb.owlapi.reasoner.*

// Usage: groovy create_annotation_dict.groovy hp.owl dict.txt

def oFile = new File(args[0])
def outFile = new File(args[1])

def manager = OWLManager.createOWLOntologyManager()
def factory = OWLManager.getOWLDataFactory()
def config = new OWLOntologyLoaderConfiguration()
config.setFollowRedirects(true)

def hasSynonym = factory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"))

def ont = manager.loadOntologyFromOntologyDocument(new FileDocumentSource(oFile), config)

def res = [:]

ont.getClassesInSignature().each { cl ->
  def outClass = cl.getIRI().tokenize('/').last()
  res[outClass] = []

  ont.getAnnotationAssertionAxioms(cl.getIRI()).each { a ->
    if(a.getProperty().isLabel() || a.getProperty() == hasSynonym) {
	  if(a.getValue() instanceof OWLLiteral) {
		OWLLiteral val = (OWLLiteral) a.getValue();
        def item = val.getLiteral().toLowerCase()
        if(!res[outClass].contains(item)) {
          res[outClass] << item
        }
	  }
    }
  }
}

outFile.text = res.collect { iri, labels -> labels.collect { iri + ' ' + it }.join('\n') }.join('\n')
