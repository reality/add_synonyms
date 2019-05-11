@Grapes([
  @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='5.1.4'),
  @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='5.1.4'),
  @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='5.1.4'),
  @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='5.1.4'),
  @Grab(group='org.apache.commons', module='commons-rdf-api', version='0.5.0'),
  @Grab(group='org.slf4j', module='slf4j-log4j12', version='1.7.10'),
  @Grab('com.xlson.groovycsv:groovycsv:1.1'),
  @Grab('org.yaml:snakeyaml:1.17'),
  @GrabConfig(systemClassLoader=true)
])

import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.owlapi.apibinding.*
import org.semanticweb.owlapi.reasoner.*
import org.yaml.snakeyaml.Yaml

// Usage: ./addSynonyms.groovy ontologyfile.owl synonymfile.yaml outputontology.owl
def oFile = args[0]
def sFile = args[1]
def outFile = args[2]

// Load Synonyms

def synonyms = new Yaml().load(new File(sFile).text)

// Load ontology

def manager = OWLManager.createOWLOntologyManager()
def factory = OWLManager.getOWLDataFactory()
def config = new OWLOntologyLoaderConfiguration()
config.setFollowRedirects(true)

def hasSynonym = factory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"))

println "Loading ontology ..."
def ont = manager.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(oFile)), config)

// Add synonyms to ontology classes

def totalSynonyms = synonyms.collect { it.getValue().size() }.sum()
println "Adding ${totalSynonyms} synonyms to ${synonyms.size()} classes."

synonyms.each { iri, classSynonyms ->
  classSynonyms.each { newSynonym ->
    def annotation = factory.getOWLAnnotation(hasSynonym, factory.getOWLLiteral(newSynonym));
    def axiom = factory.getOWLAnnotationAssertionAxiom(IRI.create(iri), annotation)
    manager.addAxiom(ont, axiom)
  }
}

println "Saving ontology..."

manager.saveOntology(ont, IRI.create(new File(outFile).toURI()))
