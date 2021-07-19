package fr.cea.nabla.ui.graalvm.instrumentationitf

import fr.cea.nabla.nabla.Affectation
import fr.cea.nabla.nabla.ArgOrVar
import fr.cea.nabla.nabla.Function
import fr.cea.nabla.nabla.Job
import instrumentationInterface.InstrumentationInterfaceFactory
import java.util.HashSet
import org.eclipse.emf.ecore.resource.Resource

class NabLabInstrumentationInterface {

	def getInstrumentationInterface(Resource resource) {

		val InstrumentationInterfaceFactory factory = InstrumentationInterfaceFactory.eINSTANCE

		factory.createInstrumentationInterface => [
			instrumentableElements += resource.allContents.filter[o|o instanceof Job].map[o|o as Job].map [ j |
				factory.createCallableElement => [
					name = j.name
				]
			].toList

			instrumentableElements += resource.allContents.filter[o|o instanceof Function].map[o|o as Function].map [ f |
				factory.createCallableElement => [
					name = f.name
				]
			].toList

			val writableNames = new HashSet<String>

			instrumentableElements +=
				resource.allContents.filter[o|o instanceof Affectation].map[o|o as Affectation].map[a|a.left.target].
					toSet.map [ v |
						writableNames.add(v.name)
						factory.createWriteableElement => [
							name = v.name
						]
					].toList

			readableElements += resource.allContents.filter[o|o instanceof ArgOrVar].map[o|o as ArgOrVar].filter [ v |
				!writableNames.contains(v.name)
			].map [ v |
				factory.createReadableElement => [
					name = v.name
				]
			].toList
		]

	}

}
