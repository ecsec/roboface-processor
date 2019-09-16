/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.robovm.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class ForwardDecl {

	// list of protocols sorted according to type inheritance
	private final List<ProtocolDefinition> protocols;

	public ForwardDecl(List<ProtocolDefinition> protocols) {
		this.protocols = new LinkedList<>();
		sortProtocols(protocols);
	}

	private void sortProtocols(List<ProtocolDefinition> input) {
		for (ProtocolDefinition next : input) {
			if (next.getExtensions().isEmpty()) {
				// no dependency, insert at the beginning right away
				protocols.add(0, next);
			} else {
				// we want to insert this element after the last element of the extension list
				// or if something is missing, insert it at the foremost position, so that a later insertion will if in
				// doubt precede the missing element
				int idx = 0;
				List<String> extensions = new ArrayList<>(next.getExtensions());
				for (int i = 0; i < protocols.size(); i++) {
					ProtocolDefinition testObj = protocols.get(i);
					// remove and see if an element has been removed actually
					if (extensions.remove(testObj.getObjcName())) {
						// advance insertion index to the position after this element
						idx = i + 1;
					}
					// stop if there is nothing left
					if (extensions.isEmpty()) {
						break;
					}
				}
				// add the element at the correct psoition
				protocols.add(idx, next);
			}
		}
	}

	public List<ProtocolDefinition> getProtocols() {
		return Collections.unmodifiableList(protocols);
	}

}
