package fr.cea.nabla.ui.graalvm.generator.instrumentationitf

import instrumentationInterface.InstrumentationInterface
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.XMLResource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.resource.SaveOptions

class InstrumentationInterfaceWriter {
	public static val InstrumentationInterfaceExtension = 'instritf'

	def createAndSaveResource(IFileSystemAccess2 fsa, InstrumentationInterface instrumentationInterface) {
		val fileName = instrumentationInterface.name.toLowerCase + '/.' + instrumentationInterface.name + '.' +
			InstrumentationInterfaceExtension
		val uri = fsa.getURI(fileName)
		val rSet = new ResourceSetImpl
		rSet.resourceFactoryRegistry.extensionToFactoryMap.put(InstrumentationInterfaceExtension,
			new XMIResourceFactoryImpl)

		val resource = rSet.createResource(uri)
		resource.contents += instrumentationInterface
		resource.save(xmlSaveOptions)
		return fileName
	}

	private def getXmlSaveOptions() {
		val builder = SaveOptions::newBuilder
		builder.format
		val so = builder.options.toOptionsMap
		so.put(XMLResource::OPTION_LINE_WIDTH, 160)
		return so
	}
}
